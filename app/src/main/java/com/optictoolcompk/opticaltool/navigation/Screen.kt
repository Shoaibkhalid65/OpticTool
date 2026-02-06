package com.optictoolcompk.opticaltool.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    object SignIn : Screen("signin_screen")
    object SignUp : Screen("signup_screen")
    object ConfirmEmail : Screen("confirm_email_screen/{email}") {
        fun createRoute(email: String) = "confirm_email_screen/$email"
    }

    object RequestReset : Screen("request_reset_screen")
    object RecoveryOtpVerf : Screen("recovery_otp_verf_screen/{email}") {
        fun createRoute(email: String) = "recovery_otp_verf_screen/$email"
    }

    object NewPassword : Screen("new_pass_screen")
    object Profile : Screen("profile_screen")


    object HomeScreen : Screen("home_screen")

    object CalculatorScreen : Screen("calculator_screen") {
        const val routeWithArgs = "calculator_screen?rightSph={rightSph}&rightCyl={rightCyl}&rightAxis={rightAxis}&leftSph={leftSph}&leftCyl={leftCyl}&leftAxis={leftAxis}&add={add}"
        val arguments = listOf(
            navArgument("rightSph") { type = NavType.StringType; nullable = true },
            navArgument("rightCyl") { type = NavType.StringType; nullable = true },
            navArgument("rightAxis") { type = NavType.StringType; nullable = true },
            navArgument("leftSph") { type = NavType.StringType; nullable = true },
            navArgument("leftCyl") { type = NavType.StringType; nullable = true },
            navArgument("leftAxis") { type = NavType.StringType; nullable = true },
            navArgument("add") { type = NavType.StringType; nullable = true }
        )

        fun createRoute(rightSph: String?, rightCyl: String?, rightAxis: String?, leftSph: String?, leftCyl: String?, leftAxis: String?, add: String?): String {
            return "calculator_screen?rightSph=$rightSph&rightCyl=$rightCyl&rightAxis=$rightAxis&leftSph=$leftSph&leftCyl=$leftCyl&leftAxis=$leftAxis&add=$add"
        }
    }

    object PrescriptionListScreen : Screen("prescription_list_screen")

    object AddPrescriptionScreen : Screen("add_prescription_screen") {
        fun createRouteWithId(prescriptionId: Long): String {
            return "$route/$prescriptionId"
        }
        const val routeWithArgs = "add_prescription_screen/{prescriptionId}"
    }

    object BillBookHomeScreen : Screen("bill_book_home")

    object BillCreationScreen : Screen("bill_creation_screen") {
        fun createRouteWithId(billId: Long): String {
            return "$route/$billId"
        }
        const val routeWithArgs = "bill_creation_screen/{billId}"
    }

    object ShopDashboardScreen : Screen("shop_dashboard_screen")

    object GlassesNotebookScreen : Screen("glasses_notebook_screen")
}