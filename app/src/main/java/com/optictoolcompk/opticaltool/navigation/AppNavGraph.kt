package com.optictoolcompk.opticaltool.navigation


import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.optictoolcompk.opticaltool.data.auth.AuthState
import com.optictoolcompk.opticaltool.ui.auth.models.AuthEvent
import com.optictoolcompk.opticaltool.ui.auth.screens.ConfirmEmailScreen
import com.optictoolcompk.opticaltool.ui.auth.screens.ForgotPasswordScreen
import com.optictoolcompk.opticaltool.ui.auth.screens.NewPasswordScreen
import com.optictoolcompk.opticaltool.ui.auth.screens.OtpVerificationScreen
import com.optictoolcompk.opticaltool.ui.auth.screens.SignInScreen
import com.optictoolcompk.opticaltool.ui.auth.screens.SignUpScreen
import com.optictoolcompk.opticaltool.ui.auth.viewmodel.AuthViewModel
import com.optictoolcompk.opticaltool.ui.billcreation.BillCreationScreen
import com.optictoolcompk.opticaltool.ui.calculator.EyePrescriptionCalculatorScreen
import com.optictoolcompk.opticaltool.ui.home.HomeScreen
import com.optictoolcompk.opticaltool.ui.mybills.MyBillsScreen
import com.optictoolcompk.opticaltool.ui.myprescriptions.PrescriptionListScreen
import com.optictoolcompk.opticaltool.ui.notebook.GlassesNotebookScreen
import com.optictoolcompk.opticaltool.ui.prescriptioncreation.PrescriptionFormScreen
import com.optictoolcompk.opticaltool.ui.profile.ProfileScreen
import com.optictoolcompk.opticaltool.ui.shopdashboard.ShopDashboardScreen
import com.optictoolcompk.opticaltool.ui.splash.SplashScreen

@Composable
fun AppNavGraph(
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val authState by authViewModel.authState.collectAsState()
    val navController: NavHostController = rememberNavController()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        authViewModel.events.collect { event ->
            when (event) {
                is AuthEvent.Navigate -> {
                    navController.navigate(event.route) {
                        launchSingleTop = true

                        event.popUpTo?.let { route ->
                            popUpTo(route) {
                                inclusive = event.inclusive
                            }
                        }
                    }
                }

                is AuthEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    InitialStateEffect(authState, navController)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen()
            }

            composable(Screen.SignIn.route) {
                SignInScreen(
                    onSignUpClick = { navController.navigate(Screen.SignUp.route) },
                    onForgotPasswordClick = { navController.navigate(Screen.RequestReset.route) },
                    authViewModel = authViewModel
                )
            }

            composable(Screen.SignUp.route) {
                SignUpScreen(
                    onBack = { navController.popBackStack() },
                    authViewModel = authViewModel
                )
            }
            composable(
                route = Screen.ConfirmEmail.route,
                arguments = listOf(navArgument("email") { type = NavType.StringType })
            ) { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email")!!
                ConfirmEmailScreen(
                    email = email,
                    onBackToLogin = {
                        navController.navigate(Screen.SignIn.route) {
                            popUpTo(Screen.SignUp.route) {
                                inclusive = true
                            }
                        }
                    },
                    authViewModel = authViewModel
                )
            }
            composable(Screen.RequestReset.route) {
                ForgotPasswordScreen(
                    authViewModel,
                    onBackToLogin = {
                        navController.popBackStack()
                    }
                )
            }
            composable(
                route = Screen.RecoveryOtpVerf.route,
                arguments = listOf(navArgument("email") { type = NavType.StringType })
            ) { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email")!!
                OtpVerificationScreen(
                    email = email,
                    authViewModel = authViewModel,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.NewPassword.route) {
                NewPasswordScreen(authViewModel)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    authViewModel = authViewModel
                )
            }

//            feature composable


            composable(Screen.HomeScreen.route) {
                HomeScreen(
                    onNavigateToCalculator = { navController.navigate(Screen.CalculatorScreen.route) },
                    onNavigateToPrescriptions = { navController.navigate(Screen.PrescriptionListScreen.route) },
                    onNavigateToBillBook = { navController.navigate(Screen.BillBookHomeScreen.route) },
                    onNavigateToNotebook = { navController.navigate(Screen.GlassesNotebookScreen.route) },
                    onNavigateToShopSetting = { navController.navigate(Screen.ShopDashboardScreen.route) },
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
                )
            }

            composable(
                route = Screen.CalculatorScreen.routeWithArgs,
                arguments = Screen.CalculatorScreen.arguments
            ) {
                EyePrescriptionCalculatorScreen(navController = navController)
            }

            composable(Screen.PrescriptionListScreen.route) {
                PrescriptionListScreen(
                    onCreateNewPrescription = { navController.navigate(Screen.AddPrescriptionScreen.route) },
                    onEditPrescription = { pres ->
                        navController.navigate(Screen.AddPrescriptionScreen.createRouteWithId(pres.id))
                    },
                    onCalculateTranspose = { pres ->
                        navController.navigate(
                            Screen.CalculatorScreen.createRoute(
                                rightSph = pres.rightSph,
                                rightCyl = pres.rightCyl,
                                rightAxis = pres.rightAxis,
                                leftSph = pres.leftSph,
                                leftCyl = pres.leftCyl,
                                leftAxis = pres.leftAxis,
                                add = pres.addPower
                            )
                        )
                    }
                )
            }

            composable(Screen.AddPrescriptionScreen.route) {
                PrescriptionFormScreen(navController = navController)
            }

            composable(
                route = Screen.AddPrescriptionScreen.routeWithArgs,
                arguments = listOf(navArgument("prescriptionId") {
                    type = NavType.LongType
                })
            ) {
                PrescriptionFormScreen(navController = navController)
            }

            composable(Screen.BillBookHomeScreen.route) {
                MyBillsScreen(
                    onNavigateToBillCreation = { navController.navigate(Screen.BillCreationScreen.route) },
                    onNavigateToEditBill = { billId ->
                        navController.navigate(
                            Screen.BillCreationScreen.createRouteWithId(
                                billId
                            )
                        )
                    },
                    onNavigateToShopDashboard = { navController.navigate(Screen.ShopDashboardScreen.route) }
                )
            }

            composable(Screen.BillCreationScreen.route) {
                BillCreationScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(
                route = Screen.BillCreationScreen.routeWithArgs,
                arguments = listOf(navArgument("billId") {
                    type = NavType.LongType; defaultValue = 0L
                })
            ) { backStackEntry ->
                val billId = backStackEntry.arguments?.getLong("billId")
                BillCreationScreen(
                    billId = if (billId == 0L) null else billId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.ShopDashboardScreen.route) {
                ShopDashboardScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(Screen.GlassesNotebookScreen.route) {
                GlassesNotebookScreen()
            }


        }
    }


}

@Composable
private fun InitialStateEffect(
    authState: AuthState,
    navController: NavHostController
) {
    LaunchedEffect(authState) {
        if (navController.currentDestination?.route == Screen.Splash.route) {
            val route = when (authState) {
                is AuthState.Authenticated -> Screen.HomeScreen.route
                AuthState.Unauthenticated, AuthState.SessionExpired -> Screen.SignIn.route
                AuthState.Loading -> null
            }

            if (route != null) {
                navController.navigate(route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }
}

