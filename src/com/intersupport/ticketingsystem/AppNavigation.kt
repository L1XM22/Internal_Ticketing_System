package com.intersupport.ticketingsystem

import androidx.compose.runtime.*
import com.intersupport.ticketingsystem.data.EmailRepository
import com.intersupport.ticketingsystem.data.TicketEmployee
import com.intersupport.ticketingsystem.data.TicketRepository
import com.intersupport.ticketingsystem.data.UserRepository
import com.intersupport.ticketingsystem.ui.screens.AdminDashboardScreen
import com.intersupport.ticketingsystem.ui.screens.AdminLoginScreen
import com.intersupport.ticketingsystem.ui.screens.SubmitTicketScreen
import com.intersupport.ticketingsystem.ui.screens.TicketStatusScreen

sealed class Screen {
    object SubmitTicket : Screen()
    object AdminLogin : Screen()
    object AdminDashboard : Screen()
    object MyTickets : Screen()
}

@Composable
fun InterSupportApp() {
    val ticketRepository = remember { TicketRepository() }
    val userRepository = remember { UserRepository() }
    val emailRepository = remember { EmailRepository() }
    
    var currentScreen by remember { mutableStateOf<Screen>(Screen.SubmitTicket) }
    
    // In a real app, this would be persisted to local storage so it survives app restarts
    var myTicketIds by remember { mutableStateOf<List<Int>>(emptyList()) }
    
    // Track if a user has selected their identity so they can't change it
    var lockedUser by remember { mutableStateOf<TicketEmployee?>(null) }

    when (currentScreen) {
        is Screen.SubmitTicket -> {
            SubmitTicketScreen(
                ticketRepository = ticketRepository,
                userRepository = userRepository,
                emailRepository = emailRepository,
                lockedUser = lockedUser,
                onNavigateToAdminLogin = { currentScreen = Screen.AdminLogin },
                onTicketSubmitted = { newTicketId, user -> 
                    // -1 is a special code indicating the user just wants to view their tickets
                    if (newTicketId != -1) {
                        myTicketIds = myTicketIds + newTicketId
                    }
                    lockedUser = user // Lock the user identity
                    currentScreen = Screen.MyTickets
                }
            )
        }
        is Screen.AdminLogin -> {
            AdminLoginScreen(
                onLoginSuccess = { 
                    // When admin logs in, we unlock the user
                    lockedUser = null
                    myTicketIds = emptyList() // clear their local session view too
                    currentScreen = Screen.AdminDashboard 
                },
                onBack = { currentScreen = Screen.SubmitTicket }
            )
        }
        is Screen.AdminDashboard -> {
            AdminDashboardScreen(
                ticketRepository = ticketRepository,
                userRepository = userRepository,
                emailRepository = emailRepository,
                onLogout = { currentScreen = Screen.SubmitTicket }
            )
        }
        is Screen.MyTickets -> {
            TicketStatusScreen(
                repository = ticketRepository,
                ticketIds = myTicketIds,
                onBack = { currentScreen = Screen.SubmitTicket }
            )
        }
    }
}