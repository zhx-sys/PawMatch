package com.pawmatch.app.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Home : Screen("home")
    data object PetDetail : Screen("pet_detail/{petId}") {
        fun createRoute(petId: Long) = "pet_detail/$petId"
    }
    data object Messages : Screen("messages")
    data object FriendChat : Screen("friend_chat/{friendId}/{friendUserType}/{nickname}") {
        fun createRoute(friendId: Long, friendUserType: Int, nickname: String = "") =
            "friend_chat/$friendId/$friendUserType/$nickname"
    }
    data object Community : Screen("community")
    data object PostDetail : Screen("post_detail/{postId}") {
        fun createRoute(postId: Long) = "post_detail/$postId"
    }
    data object Notifications : Screen("notifications")
    data object Friends : Screen("friends")
    data object ShelterChat : Screen("shelter_chat/{shelterId}") {
        fun createRoute(shelterId: Long) = "shelter_chat/$shelterId"
    }
    data object MyAdoptions : Screen("my_adoptions")
    data object Favorites : Screen("favorites")
    data object Profile : Screen("profile")
    data object ChangePassword : Screen("change_password")
    data object PetGame : Screen("pet_game")

    data object Wiki : Screen("wiki")
    data object WikiDetail : Screen("wiki_detail/{entryId}") {
        fun createRoute(entryId: Long) = "wiki_detail/$entryId"
    }
    data object WikiCreate : Screen("wiki_create")
    data object WikiEdit : Screen("wiki_edit/{entryId}") {
        fun createRoute(entryId: Long) = "wiki_edit/$entryId"
    }

    data object PetList : Screen("pet_list")
    data object ShelterProfile : Screen("shelter_profile/{shelterId}") {
        fun createRoute(shelterId: Long) = "shelter_profile/$shelterId"
    }

    // 普通用户 - 寄养/匹配/回访/成长/信用分
    data object Foster : Screen("foster")
    data object MatchingProfile : Screen("matching_profile")
    data object Followups : Screen("followups")
    data object Growth : Screen("growth")
    data object CreditScore : Screen("credit_score")

    // 救助站专属路由
    data object ShelterDashboard : Screen("shelter_dashboard")
    data object ShelterPetsManage : Screen("shelter_pets_manage")
    data object ShelterAdoptions : Screen("shelter_adoptions")
    data object ShelterFoster : Screen("shelter_foster")
    data object ShelterPosts : Screen("shelter_posts")
    data object ShelterReports : Screen("shelter_reports")
    data object ShelterWiki : Screen("shelter_wiki")
}
