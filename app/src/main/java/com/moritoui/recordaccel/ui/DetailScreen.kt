package com.moritoui.recordaccel.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.moritoui.recordaccel.viewModel.DetailScreenViewModel

@Composable
fun DetailScreen(
    viewModel: DetailScreenViewModel = hiltViewModel()
) {
    Text("detail")
}
