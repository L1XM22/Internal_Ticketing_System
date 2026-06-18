package com.intersupport.ticketingsystem.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intersupport.ticketingsystem.data.EmailRepository
import com.intersupport.ticketingsystem.data.TicketEmployee
import com.intersupport.ticketingsystem.data.TicketRepository
import com.intersupport.ticketingsystem.data.UserRepository
import kotlinx.coroutines.launch
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
fun SubmitTicketScreen(
    ticketRepository: TicketRepository,
    userRepository: UserRepository,
    emailRepository: EmailRepository,
    lockedUser: TicketEmployee?,
    onNavigateToAdminLogin: () -> Unit,
    onTicketSubmitted: (Int, TicketEmployee) -> Unit
) {
    var issueText by remember { mutableStateOf("") }
    var selectedUser by remember { mutableStateOf<TicketEmployee?>(lockedUser) }
    var users by remember { mutableStateOf<List<TicketEmployee>>(emptyList()) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    
    // List of local file paths for attachments
    var attachments by remember { mutableStateOf<List<File>>(emptyList()) }
    
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (lockedUser == null) {
            users = userRepository.getAllUsers()
        }
    }

    fun openFileChooser() {
        val fileChooser = JFileChooser()
        fileChooser.isMultiSelectionEnabled = true
        fileChooser.fileFilter = FileNameExtensionFilter("Images", "jpg", "jpeg", "png", "gif", "bmp")
        val result = fileChooser.showOpenDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) {
            val selectedFiles = fileChooser.selectedFiles.toList()
            attachments = attachments + selectedFiles
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "INTER SUPPORT",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                TextButton(onClick = onNavigateToAdminLogin) {
                    Text("Admin Login", color = MaterialTheme.colorScheme.secondary)
                }
            }
            
            Spacer(modifier = Modifier.height(30.dp))
            
            Text(
                text = "Log a New Ticket",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Please select who you are, and describe your technical issue below.",
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            // User Selection Dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { if (lockedUser == null) isDropdownExpanded = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedUser?.username ?: "Select User...",
                            color = if (selectedUser != null) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.tertiary,
                            fontSize = 16.sp
                        )
                        if (lockedUser == null) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select User",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        } else {
                            Text("(Locked)", color = MaterialTheme.colorScheme.tertiary, fontSize = 12.sp)
                        }
                    }
                }

                if (lockedUser == null) {
                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f).background(MaterialTheme.colorScheme.surface)
                    ) {
                        if (users.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No users found. Admin needs to add users.") },
                                onClick = { isDropdownExpanded = false }
                            )
                        } else {
                            users.forEach { user ->
                                DropdownMenuItem(
                                    text = { Text(user.username, color = MaterialTheme.colorScheme.onSurface) },
                                    onClick = {
                                        selectedUser = user
                                        isDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = issueText,
                onValueChange = { issueText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                placeholder = { Text("Describe the issue...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surface,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Attachments Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Attachments (Optional):",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = { openFileChooser() },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Attachment", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Image")
                }
            }

            if (attachments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(attachments) { file ->
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = file.name,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { attachments = attachments.filter { it != file } },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close, 
                                        contentDescription = "Remove",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    if (selectedUser == null) {
                        errorMessage = "Please select a user from the dropdown."
                        return@Button
                    }
                    if (issueText.isBlank()) {
                        errorMessage = "Issue description cannot be empty."
                        return@Button
                    }
                    isSubmitting = true
                    errorMessage = null
                    coroutineScope.launch {
                        val ticketId = ticketRepository.createTicket(issueText, selectedUser!!.username)
                        if (ticketId != null) {
                            val settings = emailRepository.getSmtpSettings()
                            val networkPath = settings?.attachmentPath
                            
                            // Process attachments
                            if (attachments.isNotEmpty() && !networkPath.isNullOrBlank()) {
                                try {
                                    val targetDir = File(networkPath, "Ticket_$ticketId")
                                    if (!targetDir.exists()) {
                                        targetDir.mkdirs()
                                    }
                                    
                                    for (file in attachments) {
                                        val targetFile = File(targetDir, file.name)
                                        Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                                        ticketRepository.addAttachment(ticketId, targetFile.absolutePath)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    System.err.println("Failed to copy attachments to network drive")
                                }
                            }

                            // Send email
                            val template = emailRepository.getEmailTemplate("NewTicket")
                            if (settings != null && settings.recipientEmail.isNotBlank() && template != null) {
                                val body = template.body
                                    .replace("{TICKET_ID}", ticketId.toString())
                                    .replace("{ISSUE_DESCRIPTION}", issueText)
                                emailRepository.sendEmail(settings.recipientEmail, "New Ticket Logged: #$ticketId", body)
                            }
                        
                            val submittingUser = selectedUser!!
                            issueText = ""
                            attachments = emptyList() // clear attachments
                            if (lockedUser == null) {
                                selectedUser = submittingUser
                            }
                            isSubmitting = false
                            onTicketSubmitted(ticketId, submittingUser)
                        } else {
                            isSubmitting = false
                            errorMessage = "Failed to submit ticket. Check database connection."
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isSubmitting,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Submit Ticket", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            if (lockedUser != null) {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = { 
                    onTicketSubmitted(-1, lockedUser) 
                }) {
                    Text("View My Tickets", color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}