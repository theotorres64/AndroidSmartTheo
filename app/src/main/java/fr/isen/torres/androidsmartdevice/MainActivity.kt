package fr.isen.torres.androidsmartdevice
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Utilisation directe de MainContentComponent dans MainActivity
            val context = LocalContext.current
            // La fonction onButtonClick démarre ScanActivity lors du clic sur le bouton
            val onButtonClick: () -> Unit = {
                val intent = Intent(context, ScanActivity::class.java)
                context.startActivity(intent)
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                // Passer les paramètres à MainContentComponent
                MainContentComponent(innerPadding = PaddingValues(16.dp), onButtonClick = onButtonClick)
            }
        }
    }
}

@Composable
fun MainContentComponent(innerPadding: PaddingValues, onButtonClick: () -> Unit) {
    Column(modifier = Modifier.padding(innerPadding)) {
        Text(
            text = "Bienvenue sur AndroidSmartDevice",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(innerPadding)
        )
        Text(
            text = "Pour démarrer le scan des devices BLE, cliquer sur le bouton en dessous",
            modifier = Modifier.padding(top = 16.dp)
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center // Centre la colonne au milieu de l'écran
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally // Centre le contenu horizontalement dans la colonne
            ) {
                // Image centrée (n'oubliez pas d'ajouter votre image dans `res/drawable`)
                Image(
                    painter = painterResource(id = R.drawable.logo_bluetooth), // Assurez-vous que le logo est dans `res/drawable`
                    contentDescription = "Centered Image"
                )

                Spacer(modifier = Modifier.height(130.dp)) // Espace entre l'image et le bouton

                // Bouton en dessous de l'image
                Button(onClick = onButtonClick) {
                    Text(text = "Valider")
                }
            }
        }
    }
}

@Composable
fun StartScanActivity() {
    val context = LocalContext.current
    val intent = Intent(context, ScanActivity::class.java)
    context.startActivity(intent)
}
