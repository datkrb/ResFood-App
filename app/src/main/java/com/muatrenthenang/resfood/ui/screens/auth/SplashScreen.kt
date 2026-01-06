import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.muatrenthenang.resfood.ui.viewmodel.auth.AuthViewModel

@Composable
fun SplashScreen(
    onAuthSuccess: (Boolean) -> Unit, // isAdmin
    onGoLogin: () -> Unit
) {
    val viewModel: AuthViewModel = viewModel()
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthViewModel.AuthState.Authenticated -> onAuthSuccess(state.isAdmin)
            is AuthViewModel.AuthState.Unauthenticated -> onGoLogin()
            is AuthViewModel.AuthState.Loading -> {}
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
