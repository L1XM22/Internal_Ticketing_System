package com.intersupport.ticketingsystem

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.intersupport.ticketingsystem.data.DatabaseConnector
import com.intersupport.ticketingsystem.ui.theme.INTERSUPPORTTheme
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import java.io.File
import kotlin.system.exitProcess

fun main() = application {
    var connectionError by remember { mutableStateOf<String?>(null) }
    var isConnected by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            // Test the database connection on startup
            val conn = DatabaseConnector.getConnection()
            if (conn == null || conn.isClosed) {
                connectionError = "Failed to connect to the database. Please ensure you have network access to the server and that the server is running."
            } else {
                isConnected = true
                conn.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            connectionError = "A critical database error occurred: ${e.message}\n\nPlease check your network, firewall, and server settings."
        }
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "INTER SUPPORT",
        state = WindowState(size = DpSize(1000.dp, 800.dp))
    ) {
        INTERSUPPORTTheme {
            if (connectionError != null) {
                AlertDialog(
                    onDismissRequest = { exitApplication() },
                    title = { Text("Database Connection Failed") },
                    text = { Text(connectionError!!) },
                    confirmButton = {
                        TextButton(onClick = { exitApplication() }) {
                            Text("Exit")
                        }
                    }
                )
            } else if (isConnected) {
                // Only show the main app if the connection is successful
                InterSupportApp()
            }
            // While connecting, a blank window will show. A loading indicator could be added here.
        }
    }
}