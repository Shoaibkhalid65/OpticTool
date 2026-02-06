package com.optictoolcompk.opticaltool.ui.calculator

//import android.widget.Toast
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.optictoolcompk.opticaltool.data.models.CalculationResults
import com.optictoolcompk.opticaltool.ui.prescriptioncreation.PrescriptionViewModel
import com.optictoolcompk.opticaltool.ui.prescriptioncreation.addPaddingToImage
import com.optictoolcompk.opticaltool.utils.PrescriptionCalculator
import com.optictoolcompk.opticaltool.utils.generateAddValues
import com.optictoolcompk.opticaltool.utils.generateCylValues
import com.optictoolcompk.opticaltool.utils.generateSphValues
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Preview(showBackground = true)
@Composable
fun EyePrescriptionCalculatorScreen(
    navController: NavHostController? = null,
    prescriptionViewModel: PrescriptionViewModel = hiltViewModel()
) {
    val navBackStackEntry = navController?.currentBackStackEntry
    val args = navBackStackEntry?.arguments

    // State for inputs
    var rightSph by remember { mutableStateOf(args?.getString("rightSph") ?: "") }
    var rightCyl by remember { mutableStateOf(args?.getString("rightCyl") ?: "") }
    var rightAxis by remember { mutableStateOf(args?.getString("rightAxis") ?: "") }
    var leftSph by remember { mutableStateOf(args?.getString("leftSph") ?: "") }
    var leftCyl by remember { mutableStateOf(args?.getString("leftCyl") ?: "") }
    var leftAxis by remember { mutableStateOf(args?.getString("leftAxis") ?: "") }
    var add by remember { mutableStateOf(args?.getString("add") ?: "") }

    // State for results and errors
    var results by remember { mutableStateOf<CalculationResults?>(null) }
    var rightAxisError by remember { mutableStateOf("") }
    var leftAxisError by remember { mutableStateOf("") }

    val graphicsLayer1 = rememberGraphicsLayer()
    val graphicsLayer2 = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Trigger calculation if data is passed initially
    LaunchedEffect(args) {
        if (args != null && (rightSph.isNotEmpty() || leftSph.isNotEmpty())) {
            results = PrescriptionCalculator.calculate(
                rightSph, rightCyl, rightAxis,
                leftSph, leftCyl, leftAxis,
                add
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text("Vision Calculator", fontWeight = FontWeight.ExtraBold)
                },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            results?.let {
                var fabMenuExpanded by remember { mutableStateOf(false) }
                BackHandler(fabMenuExpanded) { fabMenuExpanded = false }
                FloatingActionButtonMenu(
                    expanded = fabMenuExpanded,
                    button = {
                        ToggleFloatingActionButton(
                            checked = fabMenuExpanded,
                            onCheckedChange = { fabMenuExpanded = !fabMenuExpanded }
                        ) {
                            Icon(
                                imageVector = if (fabMenuExpanded) Icons.Filled.Close else Icons.Filled.Add,
                                contentDescription = null,
                                modifier = Modifier.animateIcon({ checkedProgress })
                            )
                        }
                    }
                )
                {
                    FloatingActionButtonMenuItem(
                        onClick = {
                            scope.launch {
                                imageBitmap = combineLayers(
                                    graphicsLayer1,
                                    graphicsLayer2,
                                    density,
                                    textMeasurer,
                                    "Input Values",
                                    "Results"
                                )
                                val uri = saveBitmapToGallery(context, imageBitmap!!)
                                if (uri != null) {
                                    Toast.makeText(context, "Saved to Gallery!", Toast.LENGTH_SHORT).show()
                                    fabMenuExpanded = false
                                }

                            }
                        },
                        icon = { Icon(Icons.Default.CameraAlt, "screenshot") },
                        text = { Text(text = "Screenshot") }
                    )

                    FloatingActionButtonMenuItem(
                        onClick = {
                            scope.launch {
                                imageBitmap = combineLayers(
                                    graphicsLayer1,
                                    graphicsLayer2,
                                    density,
                                    textMeasurer,
                                    "Input Values",
                                    "Results"
                                )
                                shareResultsOnly(context, imageBitmap!!)
                                fabMenuExpanded = false
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share"
                            )
                        },
                        text = { Text(text = "Share") }
                    )

                    FloatingActionButtonMenuItem(
                        onClick = {
                            scope.launch {
                                val prescriptionImage = graphicsLayer1.toImageBitmap()
                                val imageWithPadding = addPaddingToImage(prescriptionImage, 24)
                                prescriptionViewModel.savePrescription(
                                    patientName = "",
                                    phone = "",
                                    age = "",
                                    city = "",
                                    rightSph = rightSph,
                                    rightCyl = rightCyl,
                                    rightAxis = rightAxis,
                                    rightVa = "",
                                    leftSph = leftSph,
                                    leftCyl = leftCyl,
                                    leftAxis = leftAxis,
                                    leftVa = "",
                                    add = add,
                                    ipdN = "",
                                    ipdD = "",
                                    checkedBy = "",
                                    image = imageWithPadding.asAndroidBitmap()
                                )
                                Toast.makeText(context, "Prescription Saved", Toast.LENGTH_LONG)
                                    .show()
                                fabMenuExpanded = false
                            }
                        },
                        icon = { Icon(Icons.Default.Save, "save") },
                        text = { Text(text = "Save Prescription") }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            EyePrescriptionTable(
                modifier = Modifier
                    .drawWithContent {
                        graphicsLayer1.record {
                            this@drawWithContent.drawContent()
                        }
                        drawContent()
                    },
                rightSph = rightSph,
                onRightSphChange = { rightSph = it },
                rightCyl = rightCyl,
                onRightCylChange = { rightCyl = it },
                rightAxis = rightAxis,
                onRightAxisChange = {
                    rightAxis = it
                    rightAxisError = PrescriptionCalculator.validateAxis(it, rightCyl)
                },
                leftSph = leftSph,
                onLeftSphChange = { leftSph = it },
                leftCyl = leftCyl,
                onLeftCylChange = { leftCyl = it },
                leftAxis = leftAxis,
                onLeftAxisChange = {
                    leftAxis = it
                    leftAxisError = PrescriptionCalculator.validateAxis(it, leftCyl)
                },
                add = add,
                onAddChange = { add = it },
                rightAxisError = rightAxisError,
                leftAxisError = leftAxisError
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // RESET BUTTON (Secondary)
                OutlinedButton(
                    onClick = {
                        rightSph = ""; rightCyl = ""; rightAxis = ""
                        leftSph = ""; leftCyl = ""; leftAxis = ""
                        add = ""; results = null; rightAxisError = ""; leftAxisError = ""
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        1.5.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Reset",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // CALCULATE BUTTON (Primary)
                Button(
                    onClick = {
                        val rightFilled =
                            rightSph.isNotEmpty() || (rightCyl.isNotEmpty() && rightAxis.isNotEmpty())
                        val leftFilled =
                            leftSph.isNotEmpty() || (leftCyl.isNotEmpty() && leftAxis.isNotEmpty())

                        if (!rightFilled && !leftFilled) {
//                            Toast.makeText(
//                                context,
//                                "Please fill required values",
//                                Toast.LENGTH_LONG
//                            ).show()
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Please fill required values",
                                    duration = SnackbarDuration.Short
                                )
                            }
                            return@Button
                        }

                        if (rightAxisError.isNotEmpty() || leftAxisError.isNotEmpty()) {
//                            Toast.makeText(context, "Please fix axis errors", Toast.LENGTH_SHORT)
//                                .show()
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Please fix axis errors",
                                    duration = SnackbarDuration.Short
                                )
                            }
                            return@Button
                        }

                        results = PrescriptionCalculator.calculate(
                            rightSph, rightCyl, rightAxis,
                            leftSph, leftCyl, leftAxis,
                            add
                        )
                    },
                    modifier = Modifier
                        .weight(1.5f) // Slightly wider to emphasize it's the main action
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Calculate",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            results?.let { calcResults ->
                Spacer(modifier = Modifier.height(16.dp))
                ResultsDisplay(
                    modifier = Modifier.drawWithContent {
                        graphicsLayer2.record {
                            this@drawWithContent.drawContent()
                        }
                        drawContent()
                    },
                    results = calcResults
                )
            }
        }
    }
}

fun combineLayers(
    l1: GraphicsLayer,
    l2: GraphicsLayer,
    density: Density,
    textMeasurer: TextMeasurer,
    label1: String = "Top of First",
    label2: String = "Top of Second"
): ImageBitmap {
    val textPadding = 24f
    val width = maxOf(l1.size.width, l2.size.width)

    val textLayout1 = textMeasurer.measure(
        label1,
        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp)
    )
    val textLayout2 = textMeasurer.measure(
        label2,
        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp)
    )

    val totalHeight =
        l1.size.height + l2.size.height + textLayout1.size.height + textLayout2.size.height + (textPadding * 3)

    val bitmap = ImageBitmap(width, totalHeight.toInt())
    val canvas = Canvas(bitmap)
    val drawScope = CanvasDrawScope()

    drawScope.draw(
        density = density,
        layoutDirection = LayoutDirection.Ltr,
        canvas = canvas,
        size = Size(width.toFloat(), totalHeight)
    ) {
        drawRect(color = Color.White, size = size)
        translate(left = textPadding, top = textPadding) {
            drawText(textLayout1, color = Color.Black)
        }
        translate(top = textLayout1.size.height.toFloat() + textPadding) {
            drawLayer(l1)
        }
        val secondLabelY = textLayout1.size.height + l1.size.height + (textPadding * 2)
        translate(left = textPadding, top = secondLabelY) {
            drawText(textLayout2, color = Color.Black)
        }
        val secondChildY = secondLabelY + textLayout2.size.height + textPadding
        translate(top = secondChildY) {
            drawLayer(l2)
        }
    }
    return bitmap
}

fun saveBitmapToGallery(context: Context, bitmap: ImageBitmap): Uri? {
    val androidBitmap = bitmap.asAndroidBitmap()
    val filename = "Capture_${System.currentTimeMillis()}.png"
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
    }

    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    uri?.let {
        resolver.openOutputStream(it).use { stream ->
            androidBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream!!)
        }
    }
    return uri
}

fun shareResultsOnly(context: Context, bitmap: ImageBitmap) {
    try {
        val cachePath = File(context.cacheDir, "shared_images")
        cachePath.mkdirs()
        val file = File(cachePath, "prescription_share.png")
        val stream = FileOutputStream(file)
        bitmap.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()

        val contentUri =
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_TEXT, "Prescription Results with Input Values")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun EyePrescriptionTable(
    modifier: Modifier = Modifier,
    rightSph: String,
    onRightSphChange: (String) -> Unit,
    rightCyl: String,
    onRightCylChange: (String) -> Unit,
    rightAxis: String,
    onRightAxisChange: (String) -> Unit,
    leftSph: String,
    onLeftSphChange: (String) -> Unit,
    leftCyl: String,
    onLeftCylChange: (String) -> Unit,
    leftAxis: String,
    onLeftAxisChange: (String) -> Unit,
    add: String,
    onAddChange: (String) -> Unit,
    rightAxisError: String,
    leftAxisError: String,
) {
    val thickness = 3.dp
    val color = Color.Black
    val rowHeight = 60.dp

    val sphValues = remember { generateSphValues() }
    val cylValues = remember { generateCylValues() }
    val addValues = remember { generateAddValues() }
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(20.dp)
                .drawBehind {
                    val stroke = thickness.toPx()
                    drawLine(color, Offset(0f, 0f), Offset(size.width, 0f), stroke)
                    drawLine(color, Offset(0f, 0f), Offset(0f, size.height), stroke)
                    drawLine(color, Offset(size.width, 0f), Offset(size.width, size.height), stroke)
                }
        ) {
            Row(modifier = Modifier.height(rowHeight)) {
                LabelCell("", 1f, thickness)
                LabelCell("SPH", 1f, thickness, isItalic = true)
                LabelCell("CYL", 1f, thickness, isItalic = true)
                LabelCell("Axis", 1f, thickness, isItalic = true, isLast = true)
            }

            Row(modifier = Modifier.height(rowHeight)) {
                LabelCell("Right", 1f, thickness, isBold = true)
                DropdownCell(
                    selectedValue = rightSph,
                    onValueChange = onRightSphChange,
                    values = sphValues,
                    weight = 1f,
                    thickness = thickness
                )
                DropdownCell(
                    selectedValue = rightCyl,
                    onValueChange = onRightCylChange,
                    values = cylValues,
                    weight = 1f,
                    thickness = thickness
                )
                TextFieldCell(
                    value = rightAxis,
                    onValueChange = onRightAxisChange,
                    weight = 1f,
                    thickness = thickness,
                    isError = rightAxisError.isNotEmpty(),
                    isLast = true
                )
            }

            Row(modifier = Modifier.height(rowHeight)) {
                LabelCell("Left", 1f, thickness, isBold = true)
                DropdownCell(
                    selectedValue = leftSph,
                    onValueChange = onLeftSphChange,
                    values = sphValues,
                    weight = 1f,
                    thickness = thickness
                )
                DropdownCell(
                    selectedValue = leftCyl,
                    onValueChange = onLeftCylChange,
                    values = cylValues,
                    weight = 1f,
                    thickness = thickness
                )
                TextFieldCell(
                    value = leftAxis,
                    onValueChange = onLeftAxisChange,
                    weight = 1f,
                    thickness = thickness,
                    isError = leftAxisError.isNotEmpty(),
                    isLast = true
                )
            }

            Row(modifier = Modifier.height(rowHeight)) {
                LabelCell("ADD", 1f, thickness, isBold = true)
                DropdownCell(
                    selectedValue = add,
                    onValueChange = onAddChange,
                    values = addValues,
                    weight = 3f,
                    thickness = thickness,
                    isLast = true
                )
            }
        }
    }
}

@Composable
fun RowScope.LabelCell(
    text: String,
    weight: Float,
    thickness: Dp,
    isBold: Boolean = false,
    isItalic: Boolean = false,
    isLast: Boolean = false
) {
    Box(
        modifier = Modifier
            .weight(weight)
            .fillMaxHeight()
            .drawCellLines(thickness, isLast),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
            color = Color.Black
        )
    }
}

@Composable
fun RowScope.TextFieldCell(
    value: String,
    onValueChange: (String) -> Unit,
    weight: Float,
    thickness: Dp,
    isError: Boolean = false,
    isLast: Boolean = false
) {
    Box(
        modifier = Modifier
            .weight(weight)
            .fillMaxHeight()
            .drawCellLines(thickness, isLast),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            textStyle = TextStyle(
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = if (isError) Color.Red else Color.Black
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    if (value.isEmpty()) Text(text = "1-180", fontSize = 14.sp, color = Color.Gray)
                    innerTextField()
                }
            }
        )
    }
}

@Composable
fun RowScope.DropdownCell(
    selectedValue: String,
    onValueChange: (String) -> Unit,
    values: List<String>,
    weight: Float,
    thickness: Dp,
    isLast: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    var textButtonWidth by remember { mutableIntStateOf(0) }
    Box(
        modifier = Modifier
            .weight(weight)
            .fillMaxHeight()
            .drawCellLines(thickness, isLast),
        contentAlignment = Alignment.Center
    ) {
        TextButton(
            onClick = { expanded = true },
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { textButtonWidth = it.width },
            shape = RectangleShape
        ) {
            val displayValue = if (selectedValue.isNotEmpty()) {
                val num = selectedValue.toDoubleOrNull() ?: 0.0
                if (num >= 0) "+$selectedValue" else selectedValue
            } else "0.00"
            Text(
                text = displayValue,
                color = if (selectedValue.isEmpty()) Color.Gray else Color.Black,
                fontSize = 16.sp
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .height(250.dp)
                .width(with(LocalDensity.current) { textButtonWidth.toDp() }),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            values.forEach { selection ->
                val formattedSelection =
                    if ((selection.toDoubleOrNull() ?: 0.0) >= 0) "+$selection" else selection
                DropdownMenuItem(text = {
                    Text(
                        text = formattedSelection,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }, onClick = { onValueChange(selection); expanded = false })
            }
        }
    }
}

fun Modifier.drawCellLines(thickness: Dp, isLastColumn: Boolean): Modifier = this.drawBehind {
    val strokeWidth = thickness.toPx()
    drawLine(
        color = Color.Black,
        start = Offset(0f, size.height),
        end = Offset(size.width, size.height),
        strokeWidth = strokeWidth
    )
    if (!isLastColumn) drawLine(
        color = Color.Black,
        start = Offset(size.width, 0f),
        end = Offset(size.width, size.height),
        strokeWidth = strokeWidth
    )
}

@Composable
fun ResultsDisplay(modifier: Modifier = Modifier, results: CalculationResults) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .border(2.dp, Color.Black, RoundedCornerShape(50))
                    .padding(horizontal = 32.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Distance",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            results.right?.let { right ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Right:", fontWeight = FontWeight.Bold, fontSize = 16.sp,color = Color.Black)
                    Column {
                        Text(
                            text = right.finalDistance,
                            modifier = Modifier.padding(start = 24.dp, top = 0.dp),
                            fontSize = 15.sp,
                            color = Color.Black
                        )
                        right.transposedDistance?.let {
                            Text(
                                text = it,
                                modifier = Modifier.padding(start = 24.dp, top = 2.dp),
                                fontSize = 15.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            results.left?.let { left ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Left:", fontWeight = FontWeight.Bold, fontSize = 16.sp,color = Color.Black)
                    Column {
                        Text(
                            text = left.finalDistance,
                            modifier = Modifier.padding(start = 24.dp, top = 0.dp),
                            fontSize = 15.sp,
                            color = Color.Black
                        )
                        left.transposedDistance?.let {
                            Text(
                                text = it,
                                modifier = Modifier.padding(start = 24.dp, top = 2.dp),
                                fontSize = 15.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
            if (results.right?.finalNear != null || results.left?.finalNear != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .border(2.dp, Color.Black, RoundedCornerShape(50))
                        .padding(horizontal = 32.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Near",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        color = Color.Black
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                results.right?.finalNear?.let { nearResult ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Right:", fontWeight = FontWeight.Bold, fontSize = 16.sp,color = Color.Black)
                        Column {
                            Text(
                                text = nearResult,
                                modifier = Modifier.padding(start = 24.dp, top = 0.dp),
                                fontSize = 15.sp,
                                color=Color.Black
                            )
                            results.right.transposedNear?.let {
                                Text(
                                    text = it,
                                    modifier = Modifier.padding(start = 24.dp, top = 2.dp),
                                    fontSize = 15.sp,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                results.left?.finalNear?.let { nearResult ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Left:", fontWeight = FontWeight.Bold, fontSize = 16.sp,color = Color.Black)
                        Column {
                            Text(
                                text = nearResult,
                                modifier = Modifier.padding(start = 24.dp, top = 0.dp),
                                fontSize = 15.sp,
                                color = Color.Black
                            )
                            results.left.transposedNear?.let {
                                Text(
                                    text = it,
                                    modifier = Modifier.padding(start = 24.dp, top = 2.dp),
                                    fontSize = 15.sp,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
