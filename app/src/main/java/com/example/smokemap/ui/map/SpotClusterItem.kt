package com.example.smokemap.ui.map

import com.example.smokemap.domain.model.Spot
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

data class SpotClusterItem(
    val spot: Spot
) : ClusterItem {
    override fun getPosition(): LatLng = LatLng(spot.latitude, spot.longitude)
    override fun getTitle(): String = spot.name.ifEmpty { "名称なし" }
    override fun getSnippet(): String = spot.category.displayName
    override fun getZIndex(): Float = 0f
}
