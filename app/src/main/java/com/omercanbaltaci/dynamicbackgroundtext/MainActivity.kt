package com.omercanbaltaci.dynamicbackgroundtext

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AbcPreview()
        }
    }
}

@Preview
@Composable
fun AbcPreview() {
    TextWithDynamicBackground(text = "In the\nbeginning were\nthe words\nand the words made the world.")
}

@Composable
fun TextWithDynamicBackground(text: String) {
    var layoutResult: TextLayoutResult? = null
    val horizontalMargin = 20 // Define horizontal margin
    val verticalOverlapFix = 1f // Adjust this value to overlap the lines slightly to avoid gaps
    val bezierCurveRadius = 30f // Control point for the cubic bezier curve (radius for the rounded corner)

    Box(
        modifier = Modifier
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        BasicText(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .drawBehind {
                    layoutResult?.let { layoutResult ->
                        for (i in 0 until layoutResult.lineCount) {
                            val currentLineLeft = layoutResult.getLineLeft(i)
                            val currentLineRight = layoutResult.getLineRight(i)
                            val currentLineWidth = currentLineRight - currentLineLeft

                            val lineLeft = layoutResult.getLineLeft(i)
                            val lineRight = layoutResult.getLineRight(i)
                            val lineTop = layoutResult.getLineTop(i)
                            val lineBottom = layoutResult.getLineBottom(i)

                            val rectLeft = (lineLeft - horizontalMargin).coerceAtLeast(0f) // Ensure it doesn't go negative
                            val rectRight = (lineRight + horizontalMargin).coerceAtMost(size.width) // Ensure it stays within bounds
                            val rectTop = lineTop.toInt().toFloat()
                            val rectBottom = (lineBottom + verticalOverlapFix).toInt().toFloat() // Adjust the bottom to overlap slightly

                            // Check if current line is shorter or longer than previous and next lines
                            val previousLineWidth = if (i > 0) {
                                val previousLineLeft = layoutResult.getLineLeft(i - 1)
                                val previousLineRight = layoutResult.getLineRight(i - 1)
                                previousLineRight - previousLineLeft
                            } else null // No previous line if it's the first line

                            val nextLineWidth = if (i < layoutResult.lineCount - 1) {
                                val nextLineLeft = layoutResult.getLineLeft(i + 1)
                                val nextLineRight = layoutResult.getLineRight(i + 1)
                                nextLineRight - nextLineLeft
                            } else null // No next line if it's the last line

                            // Determine if the current line is longer or shorter than previous and next lines
                            val isLongerThanPrevious = calculateLineLengthComparison(layoutResult, i, currentLineWidth, true)
                            val isLongerThanNext = calculateLineLengthComparison(layoutResult, i, currentLineWidth, false)

                            drawBezierRoundedRectangle(
                                topLeft = Offset(rectLeft, rectTop),
                                size = Size(rectRight - rectLeft, rectBottom - rectTop),
                                cornerRadius = bezierCurveRadius,
                                isTopShort = isLongerThanPrevious,
                                isBottomShort = isLongerThanNext
                            )
                        }
                    }
                },
            onTextLayout = { result ->
                layoutResult = result
            },
            style = TextStyle(
                fontSize = 26.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 30.sp
            )
        )
    }


}

private fun DrawScope.drawBezierRoundedRectangle(
    topLeft: Offset,
    size: Size,
    cornerRadius: Float,
    isTopShort: Boolean,
    isBottomShort: Boolean
) {
    val path = Path().apply {
        // Move to the top-left corner, just after the top-left rounded corner
        moveTo(topLeft.x + cornerRadius, topLeft.y)

        // Top side
        lineTo(topLeft.x + size.width + if (isTopShort) -cornerRadius else cornerRadius, topLeft.y)

        // Top-right corner (rounded using a cubic Bézier curve)
        cubicTo(
            topLeft.x + size.width,
            topLeft.y, // Control point 1
            topLeft.x + size.width,
            topLeft.y + cornerRadius, // Control point 2
            topLeft.x + size.width,
            topLeft.y + cornerRadius // End point (right edge, just below the corner)
        )

        // Right side
        lineTo(topLeft.x + size.width, topLeft.y + size.height - cornerRadius)

        // Bottom-right corner (rounded using a cubic Bézier curve)
        cubicTo(
            topLeft.x + size.width,
            topLeft.y + size.height, // Control point 1
            topLeft.x + size.width + if (isBottomShort) -cornerRadius else cornerRadius,
            topLeft.y + size.height, // Control point 2
            topLeft.x + size.width + if (isBottomShort) -cornerRadius else cornerRadius,
            topLeft.y + size.height // End point
        )

        // Bottom side
        lineTo(
            topLeft.x + if (isBottomShort) cornerRadius else -cornerRadius,
            topLeft.y + size.height
        )

        // Bottom-left corner (rounded using a cubic Bézier curve)
        cubicTo(
            topLeft.x, topLeft.y + size.height, // Control point 1
            topLeft.x, topLeft.y + size.height - cornerRadius, // Control point 2
            topLeft.x, topLeft.y + size.height - cornerRadius // End point
        )

        // Left side
        lineTo(topLeft.x, topLeft.y + cornerRadius)

        // Top-left corner (rounded using a cubic Bézier curve)
        cubicTo(
            topLeft.x,
            topLeft.y, // Control point 1
            topLeft.x + if (isTopShort) cornerRadius else -cornerRadius,
            topLeft.y, // Control point 2
            topLeft.x + if (isTopShort) cornerRadius else -cornerRadius,
            topLeft.y // End point (back to start)
        )
    }


    // Draw the custom shape with Bézier curves for the rounded corners
    drawPath(
        path = path,
        color = Color(0xFF3126ab)
    )
}

private fun calculateLineLengthComparison(
    layoutResult: TextLayoutResult,
    currentIndex: Int,
    currentLineWidth: Float,
    isPrevious: Boolean
): Boolean {
    val adjacentLineIndex = if (isPrevious) currentIndex - 1 else currentIndex + 1
    val adjacentLineWidth = if (adjacentLineIndex in 0 until layoutResult.lineCount) {
        val adjacentLineLeft = layoutResult.getLineLeft(adjacentLineIndex)
        val adjacentLineRight = layoutResult.getLineRight(adjacentLineIndex)
        adjacentLineRight - adjacentLineLeft
    } else {
        null
    }

    return adjacentLineWidth?.let { currentLineWidth > it } ?: true
}