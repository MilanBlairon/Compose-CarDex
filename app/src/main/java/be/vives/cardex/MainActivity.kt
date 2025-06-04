package be.vives.cardex

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import be.vives.cardex.ui.theme.CarDexTheme
import coil.compose.rememberImagePainter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream

data class Car(
    val name: String,
    val motorType: String,
    val maxPower: String,
    val imageUri: String
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
    val context = LocalContext.current // Access LocalContext.current here
    val sharedPreferences = context.getSharedPreferences("CarDexPrefs", Context.MODE_PRIVATE)
    val gson = Gson()
    val cars = remember {
        mutableStateListOf<Car>().apply {
            val savedCarsJson = sharedPreferences.getString("cars", null)
            if (savedCarsJson != null) {
                val type = object : TypeToken<List<Car>>() {}.type
                addAll(gson.fromJson(savedCarsJson, type))
            } else {
                addAll(
                    listOf(
                        Car("BMW M8 Competition", "ICE", "625 HP", "android.resource://be.vives.cardex/drawable/car1"),
                        Car("BMW X6", "MHEV", "340 HP", "android.resource://be.vives.cardex/drawable/car2"),
                        Car("BMW M3 Competition", "ICE", "510 HP", "android.resource://be.vives.cardex/drawable/car3"),
                        Car("BMW XM", "PHEV", "750 HP", "android.resource://be.vives.cardex/drawable/car4")
                    )
                )
            }
        }
    }
    var selectedCar by remember { mutableStateOf<Car?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var newCarImageUri by remember { mutableStateOf<String?>(null) }

    val addCarLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            newCarImageUri = saveImageToInternalStorage(context, it) // Use context here
            showDialog = true
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("CarDex") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                addCarLauncher.launch("image/*")
            }) {
                Icon(Icons.Default.Add, contentDescription = "Voeg voertuig toe")
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

    if (showDialog) {
        AddCarDialog(
            imageUri = newCarImageUri,
            onAddCar = { name, motorType, maxPower ->
                val newCar = Car(name, motorType, maxPower, newCarImageUri ?: "")
                cars.add(newCar)
                sharedPreferences.edit().putString("cars", gson.toJson(cars)).apply()
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

private fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val fileName = "${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)
        val outputStream = FileOutputStream(file)

        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun AddCarDialog(imageUri: String?, onAddCar: (String, String, String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var motorType by remember { mutableStateOf("") }
    var maxPower by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Voeg voertuig details toe") },
        text = {
            Column {
                imageUri?.let {
                    Image(
                        painter = rememberImagePainter(data = it),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Naam") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = motorType,
                    onValueChange = { motorType = it },
                    label = { Text("Motor type") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = maxPower,
                    onValueChange = { maxPower = it },
                    label = { Text("Max. Vermogen") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onAddCar(name, motorType, maxPower)
            }) {
                Text("Toevoegen")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Terug")
            }
        }
    )
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
                        painter = rememberImagePainter(data = car.imageUri),
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
                painter = rememberImagePainter(data = car.imageUri),
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