package com.moritoui.recordaccel.preview

import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "mdpi", widthDp = 360, heightDp = 640)
@Preview(name = "hdpi", widthDp = 540, heightDp = 960)
@Preview(name = "xhdpi", device = Devices.NEXUS_7)
@Preview(name = "xxhdpi", device = Devices.NEXUS_5)
@Preview(name = "xxxhdpi", device = Devices.PIXEL_4_XL)
@Preview(widthDp = 340, heightDp = 560, showBackground = true)
annotation class MultiDevicePreview
