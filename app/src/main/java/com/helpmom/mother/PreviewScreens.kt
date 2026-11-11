package com.helpmom.mother

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.helpmom.mother.core.ui.theme.HelpMomTheme

@Preview(showBackground = true)
@Composable
fun HomeCardPreview() {
    HelpMomTheme {
        Column(Modifier.padding(16.dp)) {
            Card { Column(Modifier.padding(16.dp)) {
                Text("Semana " + PreviewData.pregnancy.gestationalWeeks, style = MaterialTheme.typography.headlineMedium)
                Text(PreviewData.measurement.healthSummary)
                Text(PreviewData.measurement.fetalHeartRateBpm.toString() + " lpm")
            } }
        }
    }
}
