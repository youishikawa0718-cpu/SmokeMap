package com.example.smokemap.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.smokemap.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smokemap.domain.model.Spot
import com.example.smokemap.domain.model.SpotCategory
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class, MapsComposeExperimentalApi::class)
@Composable
fun MapScreen(
    onSpotClick: (String) -> Unit = {},
    onAddSpotClick: () -> Unit = {},
    onFavoritesClick: () -> Unit = {},
    viewModel: MapViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val defaultLat = 33.5902
    val defaultLng = 130.4207

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (fineGranted || coarseGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { lastLocation ->
                if (lastLocation != null) {
                    Log.d("SmokeMap", "lastLocation: ${lastLocation.latitude}, ${lastLocation.longitude}")
                    viewModel.onLocationResolved(lastLocation.latitude, lastLocation.longitude)
                } else {
                    fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        CancellationTokenSource().token
                    ).addOnSuccessListener { currentLocation ->
                        if (currentLocation != null) {
                            viewModel.onLocationResolved(currentLocation.latitude, currentLocation.longitude)
                        } else {
                            viewModel.onLocationResolved(defaultLat, defaultLng)
                        }
                    }.addOnFailureListener {
                        viewModel.onLocationResolved(defaultLat, defaultLng)
                    }
                }
            }.addOnFailureListener {
                viewModel.onLocationResolved(defaultLat, defaultLng)
            }
        } else {
            viewModel.onLocationResolved(defaultLat, defaultLng)
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "位置情報が許可されていないため、博多駅付近を表示しています",
                    actionLabel = "OK"
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!uiState.locationReady) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
        }
    }

    val bottomSheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Hidden,
        skipHiddenState = false
    )
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = bottomSheetState
    )

    LaunchedEffect(uiState.selectedSpot) {
        if (uiState.selectedSpot != null) {
            bottomSheetState.partialExpand()
        } else {
            bottomSheetState.hide()
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(uiState.userLat, uiState.userLng), 15f
        )
    }

    LaunchedEffect(uiState.locationReady, uiState.userLat, uiState.userLng) {
        if (uiState.locationReady) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(uiState.userLat, uiState.userLng), 15f
                )
            )
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(title = {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = { Text("住所・場所を検索") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { viewModel.searchByAddress() }
                    ),
                    trailingIcon = {
                        IconButton(onClick = { viewModel.searchByAddress() }) {
                            Icon(Icons.Default.Search, contentDescription = "検索")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 8.dp),
                    textStyle = MaterialTheme.typography.bodyMedium
                )
            })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        sheetContent = {
            SpotBottomSheet(
                spot = uiState.selectedSpot,
                onNavigate = { spot ->
                    val uri = Uri.parse("google.navigation:q=${spot.latitude},${spot.longitude}&mode=w")
                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                        setPackage("com.google.android.apps.maps")
                    }
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    } else {
                        val browserUri = Uri.parse(
                            "https://www.google.com/maps/dir/?api=1&destination=${spot.latitude},${spot.longitude}&travelmode=walking"
                        )
                        context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
                    }
                },
                onDetail = { spot -> onSpotClick(spot.id) }
            )
        },
        sheetPeekHeight = if (uiState.selectedSpot != null) 200.dp else 0.dp
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isListView) {
                // リストビュー
                SpotListView(
                    spots = uiState.filteredSpots,
                    onSpotClick = { viewModel.selectSpot(it) }
                )
            } else {
                // 地図ビュー
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = uiState.locationReady
                    ),
                    onMapClick = { viewModel.selectSpot(null) }
                ) {
                    val clusterItems = uiState.filteredSpots.map { SpotClusterItem(it) }

                    Clustering(
                        items = clusterItems,
                        onClusterItemClick = { item ->
                            viewModel.selectSpot(item.spot)
                            true
                        },
                        onClusterClick = { cluster ->
                            scope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(
                                        cluster.position,
                                        cameraPositionState.position.zoom + 2f
                                    )
                                )
                            }
                            true
                        },
                        clusterItemContent = { item ->
                            val bgColor = when (item.spot.category) {
                                SpotCategory.INDOOR -> Color(0xFF1E88E5)
                                SpotCategory.OUTDOOR -> Color(0xFF43A047)
                                SpotCategory.RESTAURANT -> Color(0xFFEF6C00)
                            }
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(bgColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.ic_smoking),
                                    contentDescription = item.spot.category.displayName,
                                    modifier = Modifier.size(24.dp),
                                    colorFilter = ColorFilter.tint(Color.White)
                                )
                            }
                        }
                    )
                }
            }

            // フィルターチップ（上部）
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
            ) {
                // 検索半径
                RadiusChips(
                    selectedRadius = uiState.searchRadiusM,
                    onRadiusSelected = { viewModel.updateRadius(it) }
                )
                Spacer(modifier = Modifier.height(4.dp))
                // カテゴリフィルター
                CategoryChips(
                    selectedCategories = uiState.selectedCategories,
                    onCategoryToggle = { viewModel.toggleCategory(it) }
                )
            }

            // FABエリア（左下）
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = { onFavoritesClick() }
                ) {
                    Icon(Icons.Default.Favorite, contentDescription = "お気に入り")
                }
                SmallFloatingActionButton(
                    onClick = { onAddSpotClick() }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "スポット追加")
                }
                FloatingActionButton(
                    onClick = { viewModel.toggleListView() }
                ) {
                    Icon(
                        imageVector = if (uiState.isListView) Icons.Default.Place else Icons.AutoMirrored.Filled.List,
                        contentDescription = if (uiState.isListView) "地図表示" else "リスト表示"
                    )
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            if (!uiState.isLoading && uiState.filteredSpots.isEmpty() && uiState.error == null) {
                AssistChip(
                    onClick = { viewModel.updateRadius(uiState.searchRadiusM + 500) },
                    label = { Text("この付近に喫煙所が見つかりません。範囲を広げる") },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 80.dp)
                )
            }
        }
    }
}

@Composable
fun RadiusChips(
    selectedRadius: Int,
    onRadiusSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf(500, 1000, 2000)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        options.forEach { radius ->
            FilterChip(
                selected = selectedRadius == radius,
                onClick = { onRadiusSelected(radius) },
                label = {
                    Text(
                        if (radius < 1000) "${radius}m"
                        else "${radius / 1000}km"
                    )
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Composable
fun CategoryChips(
    selectedCategories: Set<SpotCategory>,
    onCategoryToggle: (SpotCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        SpotCategory.entries.forEach { category ->
            FilterChip(
                selected = category in selectedCategories,
                onClick = { onCategoryToggle(category) },
                label = { Text(category.displayName) }
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Composable
fun SpotListView(
    spots: List<Spot>,
    onSpotClick: (Spot) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 100.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(spots, key = { it.id }) { spot ->
            Card(
                onClick = { onSpotClick(spot) },
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = spot.name.ifEmpty { "名称なし" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        AssistChip(
                            onClick = {},
                            label = { Text(spot.category.displayName) }
                        )
                        if (spot.distanceMeters != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            AssistChip(
                                onClick = {},
                                label = {
                                    Text(
                                        if (spot.distanceMeters < 1000)
                                            "${spot.distanceMeters.toInt()}m"
                                        else
                                            "${"%.1f".format(spot.distanceMeters / 1000)}km"
                                    )
                                }
                            )
                        }
                    }
                    if (spot.description != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = spot.description,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SpotBottomSheet(
    spot: Spot?,
    onNavigate: (Spot) -> Unit = {},
    onDetail: (Spot) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (spot != null) {
            Text(
                text = spot.name.ifEmpty { "名称なし" },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row {
                AssistChip(
                    onClick = {},
                    label = { Text(spot.category.displayName) }
                )
                if (spot.distanceMeters != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                if (spot.distanceMeters < 1000) {
                                    "${spot.distanceMeters.toInt()}m"
                                } else {
                                    "${"%.1f".format(spot.distanceMeters / 1000)}km"
                                }
                            )
                        }
                    )
                }
                if ((spot.avgRating ?: 0.0) > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    AssistChip(
                        onClick = {},
                        label = { Text("★ ${"%.1f".format(spot.avgRating ?: 0.0)}") }
                    )
                }
            }

            if (spot.description != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = spot.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onNavigate(spot) }) {
                    Text("ナビ")
                }
                Button(onClick = { onDetail(spot) }) {
                    Text("詳細")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        } else {
            Text(
                text = "マーカーをタップして詳細を表示",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
