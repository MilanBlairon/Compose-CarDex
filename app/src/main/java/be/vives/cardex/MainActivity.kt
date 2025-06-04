package be.vives.cardex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import be.vives.cardex.ui.theme.CarDexTheme

data class Car(
    val name: String,
    val motorType: String,
    val maxPower: String,
    val imageRes: Int
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CarDexTheme {
                CarApp()
            }
        }
    }
}

@Composable
fun CarApp() {
    val cars = remember {
        mutableStateListOf(
            Car("BMW M8 Competition", "ICE", "625 HP", R.drawable.car1),
            Car("BMW X6", "MHEV", "340 HP", R.drawable.car2),
            Car("BMW M3 Competition", "ICE", "510 HP", R.drawable.car3),
            Car("BMW XM", "PHEV", "750 HP", R.drawable.car4)
        )
    }
    var selectedCar by remember { mutableStateOf<Car?>(null) }

    @OptIn(ExperimentalMaterial3Api::class)
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("CarDex") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                // Voeg logica toe om een voertuig toe te voegen via foto of galerij
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Car")
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (selectedCar == null) {
                CarList(cars) { car ->
                    selectedCar = car
                }
            } else {
                CarDetails(car = selectedCar) {
                    selectedCar = null
                }
            }
        }
    }
}

@Composable
fun CarList(cars: List<Car>, onCarClick: (Car) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(cars.size) { index ->
            val car = cars[index]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable { onCarClick(car) },
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = car.imageRes),
                        contentDescription = car.name,
                        modifier = Modifier.size(64.dp),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = car.name, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
fun CarDetails(car: Car?, onBack: () -> Unit) {
    car?.let {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = car.imageRes),
                contentDescription = car.name,
                modifier = Modifier.size(200.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Naam: ${car.name}", style = MaterialTheme.typography.titleMedium)
            Text(text = "Motor type: ${car.motorType}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Max. vermogen: ${car.maxPower}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onBack) {
                Text("Terug")
            }
        }
    }
}