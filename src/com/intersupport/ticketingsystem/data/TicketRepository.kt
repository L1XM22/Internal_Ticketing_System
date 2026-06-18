package com.intersupport.ticketingsystem.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Statement
import java.sql.Timestamp
import java.util.Date

class TicketRepository {

    suspend fun createTicket(issueDescription: String, submitterName: String): Int? = withContext(Dispatchers.IO) {
        val conn = DatabaseConnector.getConnection() ?: return@withContext null
        var generatedId: Int? = null
        try {
            val query = "INSERT INTO dbo.Tickets (IssueDescription, Status, PriorityLevel, CreatedAt, SubmitterName) VALUES (?, 'Open', 0, ?, ?)"
            val stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
            stmt.setString(1, issueDescription)
            stmt.setTimestamp(2, Timestamp(System.currentTimeMillis()))
            stmt.setString(3, submitterName)
            
            stmt.executeUpdate()
            
            val rs = stmt.generatedKeys
            if (rs.next()) {
                generatedId = rs.getInt(1)
            }
            stmt.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            conn.close()
        }
        return@withContext generatedId
    }

    suspend fun addAttachment(ticketId: Int, filePath: String): Boolean = withContext(Dispatchers.IO) {
        val conn = DatabaseConnector.getConnection() ?: return@withContext false
        var success = false
        try {
            val query = "INSERT INTO dbo.TicketAttachments (TicketId, FilePath) VALUES (?, ?)"
            val stmt = conn.prepareStatement(query)
            stmt.setInt(1, ticketId)
            stmt.setString(2, filePath)
            success = stmt.executeUpdate() > 0
            stmt.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            conn.close()
        }
        return@withContext success
    }

    suspend fun getAttachments(ticketId: Int): List<String> = withContext(Dispatchers.IO) {
        val attachments = mutableListOf<String>()
        val conn = DatabaseConnector.getConnection() ?: return@withContext attachments
        try {
            val query = "SELECT FilePath FROM dbo.TicketAttachments WHERE TicketId = ?"
            val stmt = conn.prepareStatement(query)
            stmt.setInt(1, ticketId)
            val rs = stmt.executeQuery()
            while (rs.next()) {
                attachments.add(rs.getString("FilePath"))
            }
            stmt.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            conn.close()
        }
        return@withContext attachments
    }

    suspend fun getAllOpenTickets(): List<Ticket> = withContext(Dispatchers.IO) {
        val tickets = mutableListOf<Ticket>()
        val conn = DatabaseConnector.getConnection() ?: return@withContext tickets
        try {
            val query = "SELECT TicketId, IssueDescription, SubmitterName, PriorityLevel, Status, CreatedAt FROM dbo.Tickets WHERE Status != 'Closed' ORDER BY PriorityLevel DESC, CreatedAt ASC"
            val stmt = conn.createStatement()
            val rs = stmt.executeQuery(query)
            
            while (rs.next()) {
                val ticketId = rs.getInt("TicketId")
                tickets.add(
                    Ticket(
                        ticketId = ticketId,
                        issueDescription = rs.getString("IssueDescription"),
                        submitterName = rs.getString("SubmitterName") ?: "Unknown",
                        priorityLevel = rs.getInt("PriorityLevel"),
                        status = rs.getString("Status"),
                        createdAt = Date(rs.getTimestamp("CreatedAt").time),
                        attachments = getAttachments(ticketId) // Fetch attachments for each ticket
                    )
                )
            }
            stmt.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            conn.close()
        }
        return@withContext tickets
    }

    suspend fun getTicketsByIds(ticketIds: List<Int>): List<Ticket> = withContext(Dispatchers.IO) {
        if (ticketIds.isEmpty()) return@withContext emptyList()
        val tickets = mutableListOf<Ticket>()
        val conn = DatabaseConnector.getConnection() ?: return@withContext tickets
        try {
            val idsString = ticketIds.joinToString(",")
            val query = "SELECT TicketId, IssueDescription, SubmitterName, PriorityLevel, Status, CreatedAt FROM dbo.Tickets WHERE TicketId IN ($idsString) ORDER BY CreatedAt DESC"
            val stmt = conn.createStatement()
            val rs = stmt.executeQuery(query)
            
            while (rs.next()) {
                val ticketId = rs.getInt("TicketId")
                tickets.add(
                    Ticket(
                        ticketId = ticketId,
                        issueDescription = rs.getString("IssueDescription"),
                        submitterName = rs.getString("SubmitterName") ?: "Unknown",
                        priorityLevel = rs.getInt("PriorityLevel"),
                        status = rs.getString("Status"),
                        createdAt = Date(rs.getTimestamp("CreatedAt").time),
                        attachments = getAttachments(ticketId) // Fetch attachments for each ticket
                    )
                )
            }
            stmt.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            conn.close()
        }
        return@withContext tickets
    }

    suspend fun updateTicketPriority(ticketId: Int, priorityLevel: Int): Boolean = withContext(Dispatchers.IO) {
        val conn = DatabaseConnector.getConnection() ?: return@withContext false
        var success = false
        try {
            val query = "UPDATE dbo.Tickets SET PriorityLevel = ? WHERE TicketId = ?"
            val stmt = conn.prepareStatement(query)
            stmt.setInt(1, priorityLevel)
            stmt.setInt(2, ticketId)
            success = stmt.executeUpdate() > 0
            stmt.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            conn.close()
        }
        return@withContext success
    }

    suspend fun closeTicket(ticketId: Int): Boolean = withContext(Dispatchers.IO) {
        val conn = DatabaseConnector.getConnection() ?: return@withContext false
        var success = false
        try {
            val query = "UPDATE dbo.Tickets SET Status = 'Closed' WHERE TicketId = ?"
            val stmt = conn.prepareStatement(query)
            stmt.setInt(1, ticketId)
            success = stmt.executeUpdate() > 0
            stmt.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            conn.close()
        }
        return@withContext success
    }
}