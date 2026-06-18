package com.intersupport.ticketingsystem.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository {

    suspend fun getAllUsers(): List<TicketEmployee> = withContext(Dispatchers.IO) {
        val users = mutableListOf<TicketEmployee>()
        val conn = DatabaseConnector.getConnection() ?: return@withContext users
        try {
            val query = "SELECT EmployeeId, Username, Email, IsAdmin FROM dbo.TicketEmployees ORDER BY Username ASC"
            val stmt = conn.createStatement()
            val rs = stmt.executeQuery(query)
            
            while (rs.next()) {
                users.add(
                    TicketEmployee(
                        employeeId = rs.getInt("EmployeeId"),
                        username = rs.getString("Username"),
                        email = rs.getString("Email"),
                        passwordHash = "", // Don't need password hash for this view
                        isAdmin = rs.getBoolean("IsAdmin")
                    )
                )
            }
            stmt.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            conn.close()
        }
        return@withContext users
    }

    suspend fun createUser(username: String, email: String?): Boolean = withContext(Dispatchers.IO) {
        val conn = DatabaseConnector.getConnection() ?: return@withContext false
        var success = false
        try {
            val query = "INSERT INTO dbo.TicketEmployees (Username, Email, PasswordHash, IsAdmin) VALUES (?, ?, '', 0)"
            val stmt = conn.prepareStatement(query)
            stmt.setString(1, username)
            stmt.setString(2, email)
            
            success = stmt.executeUpdate() > 0
            stmt.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            conn.close()
        }
        return@withContext success
    }

    suspend fun updateUser(employeeId: Int, username: String, email: String?): Boolean = withContext(Dispatchers.IO) {
        val conn = DatabaseConnector.getConnection() ?: return@withContext false
        var success = false
        try {
            val query = "UPDATE dbo.TicketEmployees SET Username = ?, Email = ? WHERE EmployeeId = ?"
            val stmt = conn.prepareStatement(query)
            stmt.setString(1, username)
            stmt.setString(2, email)
            stmt.setInt(3, employeeId)
            
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