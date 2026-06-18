package com.intersupport.ticketingsystem.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intersupport.ticketingsystem.data.*
import kotlinx.coroutines.launch
import java.awt.Desktop
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AdminDashboardScreen(
    ticketRepository: TicketRepository,
    userRepository: UserRepository,
    emailRepository: EmailRepository,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Admin Dashboard",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                TextButton(onClick = onLogout) {
                    Text("Logout", color = MaterialTheme.colorScheme.error)
                }
            }

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Tickets") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Manage Users") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Settings") })
            }

            when (selectedTab) {
                0 -> TicketsTab(ticketRepository, emailRepository, userRepository)
                1 -> UsersTab(userRepository)
                2 -> EmailSettingsTab(emailRepository)
            }
        }
    }
}

@Composable
fun TicketsTab(ticketRepository: TicketRepository, emailRepository: EmailRepository, userRepository: UserRepository) {
    var tickets by remember { mutableStateOf<List<Ticket>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    fun loadTickets() {
        isLoading = true
        coroutineScope.launch {
            tickets = ticketRepository.getAllOpenTickets()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadTickets() }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
        }
    } else if (tickets.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No open tickets found.", color = MaterialTheme.colorScheme.tertiary)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tickets, key = { it.ticketId }) { ticket ->
                TicketCard(
                    ticket = ticket,
                    onUpdatePriority = { newPriority ->
                        coroutineScope.launch {
                            ticketRepository.updateTicketPriority(ticket.ticketId, newPriority)
                            loadTickets()
                        }
                    },
                    onCloseTicket = {
                        coroutineScope.launch {
                            if (ticketRepository.closeTicket(ticket.ticketId)) {
                                val user = userRepository.getAllUsers().find { it.username == ticket.submitterName }
                                val template = emailRepository.getEmailTemplate("TicketClosed")
                                if (user?.email != null && template != null) {
                                    val body = template.body.replace("{TICKET_ID}", ticket.ticketId.toString())
                                    emailRepository.sendEmail(user.email, "Ticket #${ticket.ticketId} Closed", body)
                                }
                                loadTickets()
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun UsersTab(userRepository: UserRepository) {
    var newUsername by remember { mutableStateOf("") }
    var newUserEmail by remember { mutableStateOf("") }
    var editingUserId by remember { mutableStateOf<Int?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var users by remember { mutableStateOf<List<TicketEmployee>>(emptyList()) }
    
    val coroutineScope = rememberCoroutineScope()

    fun loadUsers() {
        coroutineScope.launch {
            users = userRepository.getAllUsers()
        }
    }

    LaunchedEffect(Unit) { loadUsers() }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(if (editingUserId == null) "Add New User" else "Edit User", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(
            value = newUsername,
            onValueChange = { newUsername = it; successMessage = null; errorMessage = null },
            modifier = Modifier.fillMaxWidth(0.6f),
            label = { Text("Full Name / Username") },
            singleLine = true
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = newUserEmail,
            onValueChange = { newUserEmail = it; successMessage = null; errorMessage = null },
            modifier = Modifier.fillMaxWidth(0.6f),
            label = { Text("Email Address") },
            singleLine = true
        )
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(0.6f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    if (newUsername.isBlank() || newUserEmail.isBlank()) {
                        errorMessage = "Name and email cannot be empty"
                        return@Button
                    }
                    isSubmitting = true
                    coroutineScope.launch {
                        val success = if (editingUserId == null) {
                            userRepository.createUser(newUsername, newUserEmail)
                        } else {
                            userRepository.updateUser(editingUserId!!, newUsername, newUserEmail)
                        }
                        
                        isSubmitting = false
                        if (success) {
                            successMessage = if (editingUserId == null) "User '$newUsername' added!" else "User '$newUsername' updated!"
                            newUsername = ""
                            newUserEmail = ""
                            editingUserId = null
                            loadUsers()
                        } else {
                            errorMessage = if (editingUserId == null) "Failed to add user." else "Failed to update user."
                        }
                    }
                },
                modifier = Modifier.weight(1f).height(50.dp),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                else Text(if (editingUserId == null) "Add User" else "Save Changes", fontWeight = FontWeight.Bold)
            }
            
            if (editingUserId != null) {
                OutlinedButton(
                    onClick = {
                        newUsername = ""
                        newUserEmail = ""
                        editingUserId = null
                        errorMessage = null
                        successMessage = null
                    },
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Text("Cancel")
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        successMessage?.let { Text(it, color = Color(0xFF4CAF50)) }
        errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        
        Spacer(Modifier.height(32.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.surface)
        Spacer(Modifier.height(16.dp))
        
        Text("Existing Users", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(16.dp))
        
        LazyColumn(
            modifier = Modifier.fillMaxWidth(0.8f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(users, key = { it.employeeId }) { user ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(user.username, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(user.email ?: "No email", color = MaterialTheme.colorScheme.tertiary, fontSize = 14.sp)
                        }
                        TextButton(
                            onClick = {
                                newUsername = user.username
                                newUserEmail = user.email ?: ""
                                editingUserId = user.employeeId
                                errorMessage = null
                                successMessage = null
                            }
                        ) {
                            Text("Edit", color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmailSettingsTab(emailRepository: EmailRepository) {
    var settings by remember { mutableStateOf<SmtpSettings?>(null) }
    var newTicketTemplate by remember { mutableStateOf("") }
    var closedTicketTemplate by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        settings = emailRepository.getSmtpSettings()
        newTicketTemplate = emailRepository.getEmailTemplate("NewTicket")?.body ?: ""
        closedTicketTemplate = emailRepository.getEmailTemplate("TicketClosed")?.body ?: ""
    }

    if (settings == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    } else {
        LazyColumn(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            item {
                Text("Network Attachments Settings", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = settings!!.attachmentPath,
                    onValueChange = { settings = settings!!.copy(attachmentPath = it) },
                    label = { Text("Network Shared Folder Path (e.g. \\\\SERVER\\Shared\\Tickets)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Ensure all users have read/write access to this network path.",
                    color = MaterialTheme.colorScheme.tertiary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(Modifier.height(32.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.surface)
                Spacer(Modifier.height(32.dp))

                Text("SMTP Server Settings", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = settings!!.host, onValueChange = { settings = settings!!.copy(host = it) }, label = { Text("SMTP Host") })
                OutlinedTextField(value = settings!!.port.toString(), onValueChange = { settings = settings!!.copy(port = it.toIntOrNull() ?: 0) }, label = { Text("SMTP Port") })
                OutlinedTextField(value = settings!!.user, onValueChange = { settings = settings!!.copy(user = it) }, label = { Text("SMTP Username") })
                OutlinedTextField(value = settings!!.pass, onValueChange = { settings = settings!!.copy(pass = it) }, label = { Text("SMTP Password") })
                OutlinedTextField(value = settings!!.senderEmail, onValueChange = { settings = settings!!.copy(senderEmail = it) }, label = { Text("Sender Email (From)") })
                OutlinedTextField(value = settings!!.recipientEmail, onValueChange = { settings = settings!!.copy(recipientEmail = it) }, label = { Text("Recipient Email (To)") })
                
                Spacer(Modifier.height(32.dp))
                Text("Email Templates", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = newTicketTemplate, onValueChange = { newTicketTemplate = it }, label = { Text("New Ticket Template") }, modifier = Modifier.height(150.dp))
                Text("Use {TICKET_ID} and {ISSUE_DESCRIPTION} as placeholders.", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = closedTicketTemplate, onValueChange = { closedTicketTemplate = it }, label = { Text("Ticket Closed Template") }, modifier = Modifier.height(150.dp))
                Text("Use {TICKET_ID} as a placeholder.", style = MaterialTheme.typography.bodySmall)

                Spacer(Modifier.height(32.dp))
                Button(onClick = {
                    coroutineScope.launch {
                        emailRepository.updateSmtpSettings(settings!!)
                        emailRepository.updateEmailTemplate(EmailTemplate("NewTicket", newTicketTemplate))
                        emailRepository.updateEmailTemplate(EmailTemplate("TicketClosed", closedTicketTemplate))
                    }
                }) {
                    Text("Save All Settings")
                }
            }
        }
    }
}

@Composable
fun TicketCard(
    ticket: Ticket,
    onUpdatePriority: (Int) -> Unit,
    onCloseTicket: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Ticket #${ticket.ticketId}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                val priorityColor = when (ticket.priorityLevel) {
                    2 -> MaterialTheme.colorScheme.error
                    1 -> Color(0xFFFFC107) // Amber
                    else -> Color(0xFF4CAF50) // Green
                }
                val priorityText = when (ticket.priorityLevel) {
                    2 -> "High"
                    1 -> "Medium"
                    else -> "Low"
                }
                
                Box {
                    TextButton(
                        onClick = { expanded = true },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(priorityText, color = priorityColor)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = priorityColor)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Low", color = Color(0xFF4CAF50)) },
                            onClick = { onUpdatePriority(0); expanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Medium", color = Color(0xFFFFC107)) },
                            onClick = { onUpdatePriority(1); expanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("High", color = MaterialTheme.colorScheme.error) },
                            onClick = { onUpdatePriority(2); expanded = false }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = ticket.issueDescription,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            )
            
            if (ticket.attachments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Attachments: ${ticket.attachments.size} (Click to open folder)",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    modifier = Modifier.clickable {
                        if (ticket.attachments.isNotEmpty()) {
                            try {
                                val folder = File(ticket.attachments.first()).parentFile
                                if (folder.exists() && Desktop.isDesktopSupported()) {
                                    Desktop.getDesktop().open(folder)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Submitted by: ${ticket.submitterName}",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = dateFormat.format(ticket.createdAt),
                        color = MaterialTheme.colorScheme.tertiary,
                        fontSize = 12.sp
                    )
                }
                
                Button(
                    onClick = onCloseTicket,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Close")
                }
            }
        }
    }
}