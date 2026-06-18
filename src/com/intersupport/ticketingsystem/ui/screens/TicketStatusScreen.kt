package com.intersupport.ticketingsystem.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intersupport.ticketingsystem.data.Ticket
import com.intersupport.ticketingsystem.data.TicketRepository
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.awt.Desktop

@Composable
fun TicketStatusScreen(
    repository: TicketRepository,
    ticketIds: List<Int>,
    onBack: () -> Unit
) {
    var tickets by remember { mutableStateOf<List<Ticket>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(ticketIds) {
        isLoading = true
        if (ticketIds.isNotEmpty()) {
            tickets = repository.getTicketsByIds(ticketIds)
        }
        isLoading = false
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Text("Back", color = MaterialTheme.colorScheme.secondary)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "My Tickets",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                }
            } else if (tickets.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("You haven't logged any tickets yet.", color = MaterialTheme.colorScheme.tertiary)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tickets, key = { it.ticketId }) { ticket ->
                        UserTicketCard(ticket = ticket)
                    }
                }
            }
        }
    }
}

@Composable
fun UserTicketCard(ticket: Ticket) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    
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
                Text(
                    text = ticket.status,
                    fontWeight = FontWeight.Bold,
                    color = if (ticket.status == "Open") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary
                )
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
                    text = "Attachments: ${ticket.attachments.size}",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    modifier = Modifier.clickable {
                        // Open folder containing attachments using native OS file explorer
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
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = dateFormat.format(ticket.createdAt),
                color = MaterialTheme.colorScheme.tertiary,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}