package com.intersupport.ticketingsystem.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

data class SmtpSettings(
    val host: String = "",
    val port: Int = 587,
    val user: String = "",
    val pass: String = "",
    val senderEmail: String = "",
    val recipientEmail: String = "",
    val attachmentPath: String = "" // Added for network storage of images
)

data class EmailTemplate(
    val name: String,
    val body: String
)

class EmailRepository {

    suspend fun getSmtpSettings(): SmtpSettings? = withContext(Dispatchers.IO) {
        val conn = DatabaseConnector.getConnection() ?: return@withContext null
        var settings: SmtpSettings? = null
        try {
            val query = "SELECT SmtpHost, SmtpPort, SmtpUser, SmtpPassword, SenderEmail, RecipientEmail, AttachmentPath FROM dbo.SmtpSettings WHERE Id = 1"
            val stmt = conn.createStatement()
            val rs = stmt.executeQuery(query)
            if (rs.next()) {
                settings = SmtpSettings(
                    host = rs.getString("SmtpHost") ?: "",
                    port = rs.getInt("SmtpPort"),
                    user = rs.getString("SmtpUser") ?: "",
                    pass = rs.getString("SmtpPassword") ?: "",
                    senderEmail = rs.getString("SenderEmail") ?: "",
                    recipientEmail = rs.getString("RecipientEmail") ?: "",
                    attachmentPath = rs.getString("AttachmentPath") ?: ""
                )
            }
            stmt.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            conn.close()
        }
        settings
    }

    suspend fun updateSmtpSettings(settings: SmtpSettings): Boolean = withContext(Dispatchers.IO) {
        val conn = DatabaseConnector.getConnection() ?: return@withContext false
        var success = false
        try {
            val query = "UPDATE dbo.SmtpSettings SET SmtpHost=?, SmtpPort=?, SmtpUser=?, SmtpPassword=?, SenderEmail=?, RecipientEmail=?, AttachmentPath=? WHERE Id = 1"
            val stmt = conn.prepareStatement(query)
            stmt.setString(1, settings.host)
            stmt.setInt(2, settings.port)
            stmt.setString(3, settings.user)
            stmt.setString(4, settings.pass)
            stmt.setString(5, settings.senderEmail)
            stmt.setString(6, settings.recipientEmail)
            stmt.setString(7, settings.attachmentPath)
            success = stmt.executeUpdate() > 0
            stmt.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            conn.close()
        }
        success
    }

    suspend fun getEmailTemplate(name: String): EmailTemplate? = withContext(Dispatchers.IO) {
        val conn = DatabaseConnector.getConnection() ?: return@withContext null
        var template: EmailTemplate? = null
        try {
            val query = "SELECT TemplateBody FROM dbo.EmailTemplates WHERE TemplateName = ?"
            val stmt = conn.prepareStatement(query)
            stmt.setString(1, name)
            val rs = stmt.executeQuery()
            if (rs.next()) {
                template = EmailTemplate(name, rs.getString("TemplateBody"))
            }
            stmt.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            conn.close()
        }
        template
    }

    suspend fun updateEmailTemplate(template: EmailTemplate): Boolean = withContext(Dispatchers.IO) {
        val conn = DatabaseConnector.getConnection() ?: return@withContext false
        var success = false
        try {
            val query = "UPDATE dbo.EmailTemplates SET TemplateBody = ? WHERE TemplateName = ?"
            val stmt = conn.prepareStatement(query)
            stmt.setString(1, template.body)
            stmt.setString(2, template.name)
            success = stmt.executeUpdate() > 0
            stmt.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            conn.close()
        }
        success
    }
    
    suspend fun sendEmail(to: String, subject: String, body: String): Boolean = withContext(Dispatchers.IO) {
        val settings = getSmtpSettings() ?: return@withContext false
        if (settings.host.isBlank() || settings.senderEmail.isBlank()) return@withContext false

        val props = Properties().apply {
            put("mail.smtp.host", settings.host)
            put("mail.smtp.port", settings.port.toString())
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.ssl.protocols", "TLSv1.2")
            put("mail.smtp.localhost", "localhost")
        }

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication() = PasswordAuthentication(settings.user, settings.pass)
        })

        try {
            MimeMessage(session).run {
                setFrom(InternetAddress(settings.senderEmail))
                addRecipient(Message.RecipientType.TO, InternetAddress(to))
                setSubject(subject)
                setText(body)
                Transport.send(this)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}