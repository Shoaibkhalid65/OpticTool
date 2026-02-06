package com.optictoolcompk.opticaltool.ui.prescriptioncreation

import android.graphics.Bitmap
import android.widget.Toast
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.optictoolcompk.opticaltool.utils.generateAddValues
import com.optictoolcompk.opticaltool.utils.generateCylValues
import com.optictoolcompk.opticaltool.utils.generateSphValues
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrescriptionFormScreen(
    viewModel: PrescriptionViewModel = hiltViewModel(),
    navController: NavHostController? = null
) {
    val prescriptionNumber by viewModel.prescriptionNumber.collectAsState()
    val saveState by viewModel.saveState.collectAsStateWithLifecycle()
    val isEditMode by viewModel.isEditMode.collectAsStateWithLifecycle()
    val formData by viewModel.formData.collectAsStateWithLifecycle()

    var currentDate by remember { mutableStateOf(getCurrentDate()) }

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }

    // Right Eye
    var rightSph by remember { mutableStateOf("") }
    var rightCyl by remember { mutableStateOf("") }
    var rightAxis by remember { mutableStateOf("") }
    var rightVa by remember { mutableStateOf("") }

    // Left Eye
    var leftSph by remember { mutableStateOf("") }
    var leftCyl by remember { mutableStateOf("") }
    var leftAxis by remember { mutableStateOf("") }
    var leftVa by remember { mutableStateOf("") }

    var add by remember { mutableStateOf("") }
    var ipdN by remember { mutableStateOf("") }
    var ipdD by remember { mutableStateOf("") }
    var checkedBy by remember { mutableStateOf("") }

    var image by remember { mutableStateOf<ImageBitmap?>(null) }
    val graphicsLayer = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(formData) {
        formData?.let { data ->
            name = data.patientName
            phone = data.phone
            age = data.age
            city = data.city
            rightSph = data.rightSph
            rightCyl = data.rightCyl
            rightAxis = data.rightAxis
            rightVa = data.rightVa
            leftSph = data.leftSph
            leftCyl = data.leftCyl
            leftAxis = data.leftAxis
            leftVa = data.leftVa
            add = data.addPower
            ipdN = data.ipdNear
            ipdD = data.ipdDistance
            checkedBy = data.checkedBy
        }
    }

    LaunchedEffect(saveState) {
        if (saveState is SaveState.Success) {
            Toast.makeText(context, "Prescription saved successfully!", Toast.LENGTH_SHORT).show()
            navController?.popBackStack()
            viewModel.resetSaveState()
        } else if (saveState is SaveState.Error) {
            Toast.makeText(context, (saveState as SaveState.Error).message, Toast.LENGTH_LONG)
                .show()
            viewModel.resetSaveState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditMode) "Edit Prescription" else "Create Prescription",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (saveState is SaveState.Saving) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        TextButton(
                            onClick = {
                                if (name.isEmpty()) {
                                    Toast.makeText(
                                        context,
                                        "Please enter patient name",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@TextButton
                                }
                                scope.launch {
                                    image = graphicsLayer.toImageBitmap()
                                    val imageWithPadding = addPaddingToImage(image!!, 24)
                                    viewModel.savePrescription(
                                        patientName = name,
                                        phone = phone,
                                        age = age,
                                        city = city,
                                        rightSph = rightSph,
                                        rightCyl = rightCyl,
                                        rightAxis = rightAxis,
                                        rightVa = rightVa,
                                        leftSph = leftSph,
                                        leftCyl = leftCyl,
                                        leftAxis = leftAxis,
                                        leftVa = leftVa,
                                        add = add,
                                        ipdN = ipdN,
                                        ipdD = ipdD,
                                        checkedBy = checkedBy,
                                        image = imageWithPadding.asAndroidBitmap()
                                    )
                                }
                            }
                        ) {
                            Text("SAVE", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier.drawWithContent {
                    graphicsLayer.record {
                        this@drawWithContent.drawContent()
                    }
                    drawContent()
                },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.background(Color.White)
                ) {
                    // Header Section
                    PrescriptionHeader(
                        prescriptionNo = prescriptionNumber,
                        date = currentDate,
                        name = name,
                        onNameChange = { name = it },
                        phone = phone,
                        onPhoneChange = { phone = it },
                        age = age,
                        onAgeChange = { age = it },
                        city = city,
                        onCityChange = { city = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Prescription Table
                    PrescriptionTable(
                        rightSph = rightSph,
                        onRightSphChange = { rightSph = it },
                        rightCyl = rightCyl,
                        onRightCylChange = { rightCyl = it },
                        rightAxis = rightAxis,
                        onRightAxisChange = { rightAxis = it },
                        rightVa = rightVa,
                        onRightVaChange = { rightVa = it },
                        leftSph = leftSph,
                        onLeftSphChange = { leftSph = it },
                        leftCyl = leftCyl,
                        onLeftCylChange = { leftCyl = it },
                        leftAxis = leftAxis,
                        onLeftAxisChange = { leftAxis = it },
                        leftVa = leftVa,
                        onLeftVaChange = { leftVa = it },
                        add = add,
                        onAddChange = { add = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Footer Section
                    PrescriptionFooter(
                        ipdN = ipdN,
                        onIpdNChange = { ipdN = it },
                        ipdD = ipdD,
                        onIpdDChange = { ipdD = it },
                        checkedBy = checkedBy,
                        onCheckedByChange = { checkedBy = it }
                    )
                }
            }
        }
    }
}

fun addPaddingToImage(
    original: ImageBitmap,
    paddingPx: Int,
    backgroundColor: Color = Color.White
): ImageBitmap {
    // convert to software bitmap
    val softwareBitmap = original.asAndroidBitmap()
        .copy(Bitmap.Config.ARGB_8888, true)
        .asImageBitmap()

    val newWidth = softwareBitmap.width + paddingPx * 2
    val newHeight = softwareBitmap.height + paddingPx * 2

    val paddedBitmap = ImageBitmap(newWidth, newHeight)
    val canvas = Canvas(paddedBitmap)

    // background
    canvas.drawRect(
        Rect(0f, 0f, newWidth.toFloat(), newHeight.toFloat()),
        Paint().apply { color = backgroundColor }
    )

    // draw original in center
    canvas.drawImage(
        softwareBitmap,
        Offset(paddingPx.toFloat(), paddingPx.toFloat()),
        Paint()
    )

    return paddedBitmap
}


@Composable
fun PrescriptionHeader(
    prescriptionNo: String,
    date: String,
    name: String,
    onNameChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    age: String,
    onAgeChange: (String) -> Unit,
    city: String,
    onCityChange: (String) -> Unit
) {
    Column {
        // Prescription No and Date
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Prescription No.",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = prescriptionNo,
                    fontSize = 16.sp
                )
            }

            Text(
                text = date,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Name and Age
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Name :",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(70.dp)
                )
                BasicTextField(
                    value = name,
                    onValueChange = onNameChange,
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    textStyle = TextStyle(fontSize = 16.sp),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Row(
                modifier = Modifier.weight(0.6f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Age :",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(50.dp)
                )
                BasicTextField(
                    value = age,
                    onValueChange = onAgeChange,
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    textStyle = TextStyle(fontSize = 16.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Phone and City
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Phone :",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(70.dp)
                )
                BasicTextField(
                    value = phone,
                    onValueChange = onPhoneChange,
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    textStyle = TextStyle(fontSize = 16.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Row(
                modifier = Modifier.weight(0.6f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "City :",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(50.dp)
                )
                BasicTextField(
                    value = city,
                    onValueChange = onCityChange,
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    textStyle = TextStyle(fontSize = 16.sp),
                    singleLine = true
                )
            }
        }
    }
}

@Composable
fun PrescriptionTable(
    rightSph: String,
    onRightSphChange: (String) -> Unit,
    rightCyl: String,
    onRightCylChange: (String) -> Unit,
    rightAxis: String,
    onRightAxisChange: (String) -> Unit,
    rightVa: String,
    onRightVaChange: (String) -> Unit,
    leftSph: String,
    onLeftSphChange: (String) -> Unit,
    leftCyl: String,
    onLeftCylChange: (String) -> Unit,
    leftAxis: String,
    onLeftAxisChange: (String) -> Unit,
    leftVa: String,
    onLeftVaChange: (String) -> Unit,
    add: String,
    onAddChange: (String) -> Unit,
    isShortWidth: Boolean = false
) {
    val thickness = 3.dp
    val color = Color.Black
    val rowHeight = 60.dp

    val sphValues = remember { generateSphValues() }
    val cylValues = remember { generateCylValues() }
    val addValues = remember { generateAddValues() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(thickness, color, RoundedCornerShape(4.dp))
    ) {
        // Header Row
        Row(modifier = Modifier.height(rowHeight)) {
            TableHeaderCell("", 1f, thickness)
            TableHeaderCell("SPH", 1f, thickness, isItalic = true)
            TableHeaderCell("CYL", 1f, thickness, isItalic = true)
            TableHeaderCell("Axis", 1f, thickness, isItalic = true)
            TableHeaderCell("VA", 1f, thickness, isItalic = true, isLast = true)
        }

        // Right Eye Row
        Row(modifier = Modifier.height(rowHeight)) {
            TableLabelCell("Right", 1f, thickness, isBold = true)
            TableDropdownCell(
                selectedValue = rightSph,
                onValueChange = onRightSphChange,
                values = sphValues,
                weight = 1f,
                thickness = thickness,
                isShortWidth = isShortWidth
            )
            TableDropdownCell(
                selectedValue = rightCyl,
                onValueChange = onRightCylChange,
                values = cylValues,
                weight = 1f,
                thickness = thickness,
                isShortWidth = isShortWidth
            )
            TableInputCell(
                value = rightAxis,
                onValueChange = onRightAxisChange,
                weight = 1f,
                thickness = thickness,
                placeholder = "1-180"
            )
            TableInputCell(
                value = rightVa,
                onValueChange = onRightVaChange,
                weight = 1f,
                thickness = thickness,
                isLast = true,
                placeholder = "6/6"
            )
        }

        // Left Eye Row
        Row(modifier = Modifier.height(rowHeight)) {
            TableLabelCell("Left", 1f, thickness, isBold = true)
            TableDropdownCell(
                selectedValue = leftSph,
                onValueChange = onLeftSphChange,
                values = sphValues,
                weight = 1f,
                thickness = thickness,
                isShortWidth = isShortWidth
            )
            TableDropdownCell(
                selectedValue = leftCyl,
                onValueChange = onLeftCylChange,
                values = cylValues,
                weight = 1f,
                thickness = thickness,
                isShortWidth = isShortWidth
            )
            TableInputCell(
                value = leftAxis,
                onValueChange = onLeftAxisChange,
                weight = 1f,
                thickness = thickness,
                placeholder = "1-180"
            )
            TableInputCell(
                value = leftVa,
                onValueChange = onLeftVaChange,
                weight = 1f,
                thickness = thickness,
                isLast = true,
                placeholder = "6/6"
            )
        }

        // ADD Row
        Row(modifier = Modifier.height(rowHeight)) {
            TableLabelCell("ADD", 1f, thickness, isBold = true, isLastRow = true)
            TableDropdownCell(
                selectedValue = add,
                onValueChange = onAddChange,
                values = addValues,
                weight = 4f,
                thickness = thickness,
                isLast = true,
                isLastRow = true
            )
        }
    }
}

@Composable
fun RowScope.TableHeaderCell(
    text: String,
    weight: Float,
    thickness: Dp,
    isItalic: Boolean = false,
    isLast: Boolean = false
) {
    Box(
        modifier = Modifier
            .weight(weight)
            .fillMaxHeight()
            .drawTableCellLines(thickness, isLast),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal
        )
    }
}

@Composable
fun RowScope.TableLabelCell(
    text: String,
    weight: Float,
    thickness: Dp,
    isBold: Boolean = false,
    isLast: Boolean = false,
    isLastRow: Boolean = false
) {
    Box(
        modifier = Modifier
            .weight(weight)
            .fillMaxHeight()
            .drawTableCellLines(thickness, isLast, isLastRow),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun RowScope.TableInputCell(
    value: String,
    onValueChange: (String) -> Unit,
    weight: Float,
    thickness: Dp,
    placeholder: String = "",
    isLast: Boolean = false
) {
    Box(
        modifier = Modifier
            .weight(weight)
            .fillMaxHeight()
            .drawTableCellLines(thickness, isLast),
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
                color = Color.Black
            ),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
fun RowScope.TableDropdownCell(
    selectedValue: String,
    onValueChange: (String) -> Unit,
    values: List<String>,
    weight: Float,
    thickness: Dp,
    isLast: Boolean = false,
    isLastRow: Boolean = false,
    isShortWidth: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    var textButtonWidth by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .weight(weight)
            .fillMaxHeight()
            .drawTableCellLines(thickness, isLast, isLastRow),
        contentAlignment = Alignment.Center
    ) {
        TextButton(
            onClick = { expanded = true },
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { textButtonWidth = it.width },
            shape = RectangleShape
        ) {
            val displayValue = remember(selectedValue) {
                if (selectedValue.isNotEmpty()) {
                    val num = selectedValue.toDoubleOrNull() ?: 0.0
                    if (num >= 0) "+$selectedValue" else selectedValue
                } else {
                    "0.00"
                }
            }
            Text(
                text = displayValue,
                color = if (selectedValue.isEmpty()) Color.Gray else Color.Black,
                fontSize = if (isShortWidth) 13.sp else 15.sp
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .height(250.dp)
                .width(with(LocalDensity.current) { textButtonWidth.toDp() })
        ) {
            values.forEach { selection ->
                val formattedSelection = remember(selection) {
                    if ((selection.toDoubleOrNull() ?: 0.0) >= 0) "+$selection" else selection
                }
                DropdownMenuItem(
                    text = {
                        Text(
                            text = formattedSelection,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontSize = if (isShortWidth) 13.sp else 14.sp
                        )
                    },
                    onClick = {
                        onValueChange(selection)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun PrescriptionFooter(
    ipdN: String,
    onIpdNChange: (String) -> Unit,
    ipdD: String,
    onIpdDChange: (String) -> Unit,
    checkedBy: String,
    onCheckedByChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("I.P.D", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.width(8.dp))
            Column {
                Row {
                    Text("D ", fontSize = 14.sp)
                    SmallField(ipdD, onIpdDChange)
                    Text("mm", fontSize = 13.sp)
                }
                HorizontalDivider(
                    modifier = Modifier.width(80.dp),
                    color = Color.Black,
                    thickness = 1.5.dp
                )
                Row {
                    Text("N ", fontSize = 14.sp)
                    SmallField(ipdN, onIpdNChange)
                    Text("mm", fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.width(24.dp))

        // Checked by Section
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Checked by :",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            BasicTextField(
                value = checkedBy,
                onValueChange = onCheckedByChange,
                modifier = Modifier
                    .width(150.dp)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                textStyle = TextStyle(fontSize = 16.sp),
                singleLine = true
            )
        }
    }
}

fun Modifier.drawTableCellLines(
    thickness: Dp,
    isLastColumn: Boolean,
    isLastRow: Boolean = false
): Modifier = this.drawBehind {
    val strokeWidth = thickness.toPx()
    // Bottom line
    if (!isLastRow) {
        drawLine(
            color = Color.Black,
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = strokeWidth
        )
    }
    // Vertical divider (only if not the last column)
    if (!isLastColumn) {
        drawLine(
            color = Color.Black,
            start = Offset(size.width, 0f),
            end = Offset(size.width, size.height),
            strokeWidth = strokeWidth
        )
    }
}


fun getCurrentDate(): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date())
}

@Composable
fun SmallField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        textStyle = TextStyle(
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        ),
        modifier = modifier
            .width(40.dp)
            .padding(horizontal = 4.dp)
    )
}