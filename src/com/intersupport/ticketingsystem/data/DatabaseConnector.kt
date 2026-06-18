package com.intersupport.ticketingsystem.data

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Statement

object DatabaseConnector {
    private const val IP = "INTERCABLESRV"
    private const val INSTANCE = "SQLEXPRESS"
    private const val DB = "EvoIntercable2"
    private const val USER = "sa"
    private const val PASS = "1nterC@ble!123"
    private const val CONNECTION_URL = "jdbc:sqlserver://$IP\\$INSTANCE;databaseName=$DB;user=$USER;password=$PASS;encrypt=false;trustServerCertificate=true;"

    suspend fun getConnection(): Connection? = withContext(Dispatchers.IO) {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver")
            val conn = DriverManager.getConnection(CONNECTION_URL)
            ensureTablesExist(conn)
            conn
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun ensureTablesExist(conn: Connection) {
        try {
            val stmt = conn.createStatement()

            // Ensure TicketEmployees table has Email column
            val alterEmployeesTable = """
                IF NOT EXISTS (SELECT * FROM sys.columns WHERE Name = N'Email' AND Object_ID = Object_ID(N'dbo.TicketEmployees'))
                BEGIN
                    ALTER TABLE dbo.TicketEmployees ADD Email NVARCHAR(255) NULL
                END
            """.trimIndent()
            stmt.execute(alterEmployeesTable)

            // Create Tickets table if it doesn't exist
            val createTicketsTable = """
                IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Tickets' and xtype='U')
                CREATE TABLE dbo.Tickets (
                    TicketId INT IDENTITY(1,1) PRIMARY KEY,
                    IssueDescription NVARCHAR(MAX) NOT NULL,
                    SubmitterName NVARCHAR(255),
                    PriorityLevel INT NOT NULL DEFAULT 0,
                    Status NVARCHAR(50) NOT NULL DEFAULT 'Open',
                    CreatedAt DATETIME NOT NULL DEFAULT GETDATE()
                )
            """.trimIndent()
            stmt.execute(createTicketsTable)

            // Create TicketAttachments table
            val createAttachmentsTable = """
                IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='TicketAttachments' and xtype='U')
                CREATE TABLE dbo.TicketAttachments (
                    AttachmentId INT IDENTITY(1,1) PRIMARY KEY,
                    TicketId INT NOT NULL,
                    FilePath NVARCHAR(MAX) NOT NULL,
                    CONSTRAINT FK_Tickets_Attachments FOREIGN KEY (TicketId) REFERENCES dbo.Tickets(TicketId)
                )
            """.trimIndent()
            stmt.execute(createAttachmentsTable)

            // Create SmtpSettings table if it doesn't exist (only ever one row)
            val createSmtpTable = """
                IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='SmtpSettings' and xtype='U')
                BEGIN
                    CREATE TABLE dbo.SmtpSettings (
                        Id INT PRIMARY KEY,
                        SmtpHost NVARCHAR(255),
                        SmtpPort INT,
                        SmtpUser NVARCHAR(255),
                        SmtpPassword NVARCHAR(255),
                        SenderEmail NVARCHAR(255),
                        RecipientEmail NVARCHAR(255),
                        AttachmentPath NVARCHAR(MAX) NULL
                    )
                    INSERT INTO dbo.SmtpSettings (Id) VALUES (1)
                END
            """.trimIndent()
            stmt.execute(createSmtpTable)
            
            // Add AttachmentPath column if it doesn't exist
            val alterSmtpTable = """
                IF NOT EXISTS (SELECT * FROM sys.columns WHERE Name = N'AttachmentPath' AND Object_ID = Object_ID(N'dbo.SmtpSettings'))
                BEGIN
                    ALTER TABLE dbo.SmtpSettings ADD AttachmentPath NVARCHAR(MAX) NULL
                END
            """.trimIndent()
            stmt.execute(alterSmtpTable)

            // Create EmailTemplates table if it doesn't exist
            val createTemplatesTable = """
                IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='EmailTemplates' and xtype='U')
                BEGIN
                    CREATE TABLE dbo.EmailTemplates (
                        TemplateName NVARCHAR(100) PRIMARY KEY,
                        TemplateBody NVARCHAR(MAX)
                    )
                    INSERT INTO dbo.EmailTemplates (TemplateName, TemplateBody) VALUES 
                    ('NewTicket', 'A new ticket has been logged. Ticket ID: {TICKET_ID}\n\nIssue: {ISSUE_DESCRIPTION}'),
                    ('TicketClosed', 'Your ticket with ID {TICKET_ID} has been resolved and closed.')
                END
            """.trimIndent()
            stmt.execute(createTemplatesTable)
            
            stmt.close()
        } catch (e: SQLException) {
            System.err.println("Error ensuring tables exist: ${e.message}")
            e.printStackTrace()
        }
    }
}