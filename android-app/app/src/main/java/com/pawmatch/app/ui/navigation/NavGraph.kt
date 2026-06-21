package com.pawmatch.app.ui.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.pawmatch.app.data.api.TokenManager
import com.pawmatch.app.ui.screens.*
import com.pawmatch.app.viewmodel.AuthViewModel

@Composable
fun PawMatchNavGraph(
    navController: NavHostController,
    isLoggedIn: Boolean,
    onLoginSuccess: () -> Unit,
    onLogout: () -> Unit
) {
    val authViewModel: AuthViewModel = viewModel()
    val isShelter = TokenManager.userType == 1
    val startDest = when {
        !isLoggedIn -> Screen.Login.route
        isShelter -> Screen.ShelterDashboard.route
        else -> Screen.Home.route
    }

    NavHost(
        navController = navController,
        startDestination = startDest
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onRegisterClick = { navController.navigate(Screen.Register.route) },
                onLoginSuccess = onLoginSuccess,
                viewModel = authViewModel
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onBack = { navController.popBackStack() },
                viewModel = authViewModel
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onPetClick = { petId -> navController.navigate(Screen.PetDetail.createRoute(petId)) },
                onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
                onMatchingProfile = { navController.navigate(Screen.MatchingProfile.route) }
            )
        }
        composable(
            Screen.PetDetail.route,
            arguments = listOf(navArgument("petId") { type = NavType.LongType })
        ) { backStackEntry ->
            val petId = backStackEntry.arguments?.getLong("petId") ?: 0L
            PetDetailScreen(
                petId = petId,
                onBack = { navController.popBackStack() },
                onContactShelter = { shelterId, shelterName ->
                    navController.navigate("shelter_chat/$shelterId")
                }
            )
        }
        composable(Screen.Messages.route) {
            MessagesScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.ShelterChat.route,
            arguments = listOf(navArgument("shelterId") { type = NavType.LongType })
        ) { _ ->
            MessagesScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Friends.route) {
            FriendsScreen(
                onBack = { navController.popBackStack() },
                onChatWithFriend = { friendId, friendType, nickname ->
                    navController.navigate(Screen.FriendChat.createRoute(friendId, friendType, nickname))
                }
            )
        }
        composable(Screen.FriendChat.route,
            arguments = listOf(
                navArgument("friendId") { type = NavType.LongType },
                navArgument("friendUserType") { type = NavType.IntType },
                navArgument("nickname") { type = NavType.StringType }
            )
        ) { _ ->
            MessagesScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Community.route) {
            CommunityScreen(
                onBack = { navController.popBackStack() },
                onPostClick = { postId -> navController.navigate(Screen.PostDetail.createRoute(postId)) }
            )
        }
        composable(
            Screen.PostDetail.route,
            arguments = listOf(navArgument("postId") { type = NavType.LongType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getLong("postId") ?: 0L
            PostDetailScreen(postId = postId, onBack = { navController.popBackStack() })
        }
        composable(Screen.Notifications.route) {
            NotificationsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onLogout = onLogout,
                onFavorites = { navController.navigate(Screen.Favorites.route) },
                onMyAdoptions = { navController.navigate(Screen.MyAdoptions.route) },
                onFoster = { navController.navigate(Screen.Foster.route) },
                onMatchingProfile = { navController.navigate(Screen.MatchingProfile.route) },
                onFollowups = { navController.navigate(Screen.Followups.route) },
                onGrowth = { navController.navigate(Screen.Growth.route) },
                onCreditScore = { navController.navigate(Screen.CreditScore.route) },
                onSettings = { navController.navigate(Screen.ChangePassword.route) }
            )
        }
        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onBack = { navController.popBackStack() },
                onPetClick = { petId -> navController.navigate(Screen.PetDetail.createRoute(petId)) }
            )
        }
        composable(Screen.MyAdoptions.route) {
            MyAdoptionsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Foster.route) {
            FosterScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.MatchingProfile.route) {
            MatchingProfileScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Followups.route) {
            FollowupScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Growth.route) {
            GrowthScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.CreditScore.route) {
            CreditScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.ChangePassword.route) {
            ChangePasswordScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.PetGame.route) {
            PetGameScreen(
                onBack = { navController.popBackStack() },
                onPetClick = { petId -> navController.navigate(Screen.PetDetail.createRoute(petId)) }
            )
        }
        composable(Screen.Wiki.route) {
            WikiScreen(
                onBack = { navController.popBackStack() },
                onEntryClick = { entryId -> navController.navigate(Screen.WikiDetail.createRoute(entryId)) },
                onCreateClick = { navController.navigate(Screen.WikiCreate.route) }
            )
        }
        composable(Screen.WikiCreate.route) {
            WikiEditScreen(
                entryId = null,
                onBack = { navController.popBackStack() },
                onSubmitSuccess = { newId ->
                    if (newId != null) navController.navigate(Screen.WikiDetail.createRoute(newId))
                    else navController.popBackStack()
                }
            )
        }
        composable(
            Screen.WikiEdit.route,
            arguments = listOf(navArgument("entryId") { type = NavType.LongType })
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getLong("entryId") ?: 0L
            WikiEditScreen(
                entryId = entryId,
                onBack = { navController.popBackStack() },
                onSubmitSuccess = { newId ->
                    if (newId != null) navController.navigate(Screen.WikiDetail.createRoute(newId))
                    else navController.popBackStack()
                }
            )
        }
        composable(
            Screen.WikiDetail.route,
            arguments = listOf(navArgument("entryId") { type = NavType.LongType })
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getLong("entryId") ?: 0L
            WikiDetailScreen(entryId = entryId, onBack = { navController.popBackStack() })
        }
        composable(Screen.PetList.route) {
            PetListScreen(
                onBack = { navController.popBackStack() },
                onPetClick = { petId -> navController.navigate(Screen.PetDetail.createRoute(petId)) }
            )
        }
        composable(
            Screen.ShelterProfile.route,
            arguments = listOf(navArgument("shelterId") { type = NavType.LongType })
        ) { backStackEntry ->
            val shelterId = backStackEntry.arguments?.getLong("shelterId") ?: 0L
            ShelterProfileScreen(
                shelterId = shelterId,
                onBack = { navController.popBackStack() },
                onStoryClick = { storyId -> navController.navigate(Screen.PostDetail.createRoute(storyId)) }
            )
        }

        // ===== 救助站专属路由 =====
        composable(Screen.ShelterDashboard.route) {
            ShelterDashboardScreen(
                onPetManagement = { navController.navigate(Screen.ShelterPetsManage.route) },
                onAdoptionReview = { navController.navigate(Screen.ShelterAdoptions.route) },
                onFosterManagement = { navController.navigate(Screen.ShelterFoster.route) },
                onPostReview = { navController.navigate(Screen.ShelterPosts.route) },
                onReportReview = { navController.navigate(Screen.ShelterReports.route) },
                onWikiReview = { navController.navigate(Screen.ShelterWiki.route) },
                onFollowups = { navController.navigate(Screen.Followups.route) },
                onNotifications = { navController.navigate(Screen.Notifications.route) },
                onLogout = onLogout
            )
        }
        composable(Screen.ShelterPetsManage.route) {
            ShelterPetsManageScreen(
                onBack = { navController.popBackStack() },
                onPetClick = { petId -> navController.navigate(Screen.PetDetail.createRoute(petId)) }
            )
        }
        composable(Screen.ShelterAdoptions.route) {
            ShelterAdoptionsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.ShelterFoster.route) {
            ShelterFosterScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.ShelterPosts.route) {
            ShelterPostReviewScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.ShelterReports.route) {
            ShelterReportReviewScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.ShelterWiki.route) {
            ShelterWikiReviewScreen(onBack = { navController.popBackStack() })
        }
    }
}
