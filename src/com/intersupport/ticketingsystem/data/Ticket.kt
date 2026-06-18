package com.intersupport.ticketingsystem.data

import java.util.Date

data class Ticket(
    val ticketId: Int = 0,
    val issueDescription: String,
    val submitterName: String = "Anonymous",
    val priorityLevel: Int = 0, // 0 = Low, 1 = Medium, 2 = High
    val status: String = "Open", // Open, In Progress, Closed
    val createdAt: Date = Date(),
    val attachments: List<String> = emptyList() // List of file paths
)

data class TicketEmployee(
    val employeeId: Int = 0,
    val username: String,
    val email: String? = null,
    val passwordHash: String,
    val isAdmin: Boolean = false
)