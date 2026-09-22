package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SkeuoColors
import com.example.ui.theme.tactileButton
import com.example.ui.theme.tactileRecessed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MarkdownRenderer(
    content: String,
    modifier: Modifier = Modifier,
    textColor: Color = SkeuoColors.TextNormal,
    fontSize: Int = 14
) {
    val blocks = remember(content) { parseMarkdownBlocks(content) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (block in blocks) {
            when (block) {
                is MarkdownBlock.Header -> {
                    val scale = when (block.level) {
                        1 -> 1.45f
                        2 -> 1.25f
                        else -> 1.12f
                    }
                    Text(
                        text = block.text,
                        color = SkeuoColors.TextHighContrast,
                        fontSize = (fontSize * scale).sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }
                is MarkdownBlock.CodeBlock -> {
                    CodeBlockView(
                        language = block.language,
                        code = block.code
                    )
                }
                is MarkdownBlock.BlockQuote -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(22.dp)
                                .background(SkeuoColors.LedCyan, RoundedCornerShape(1.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = block.text,
                            color = SkeuoColors.TextMuted,
                            fontSize = (fontSize - 1).sp,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
                is MarkdownBlock.BulletItem -> {
                    Row(
                        modifier = Modifier.padding(start = 6.dp, top = 2.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "• ",
                            color = SkeuoColors.LedCyan,
                            fontSize = fontSize.sp,
                            fontWeight = FontWeight.Bold
                        )
                        FormattedInlineText(
                            text = block.text,
                            defaultColor = textColor,
                            fontSize = fontSize
                        )
                    }
                }
                is MarkdownBlock.Paragraph -> {
                    FormattedInlineText(
                        text = block.text,
                        defaultColor = textColor,
                        fontSize = fontSize
                    )
                }
            }
        }
    }
}

@Composable
fun CodeBlockView(
    language: String,
    code: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isCopied by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val displayLang = language.ifBlank { "CODE" }.uppercase()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .tactileRecessed(RoundedCornerShape(8.dp))
    ) {
        // Code Block Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF14171E))
                .border(0.5.dp, Color(0xFF222733), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(SkeuoColors.LedCyan, RoundedCornerShape(3.dp))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = displayLang,
                    color = SkeuoColors.TextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            }

            // Copy button
            Row(
                modifier = Modifier
                    .tactileButton(
                        shape = RoundedCornerShape(4.dp),
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("code", code))
                            isCopied = true
                            Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
                            scope.launch {
                                delay(2000)
                                isCopied = false
                            }
                        }
                    )
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                    contentDescription = "Copy code",
                    tint = if (isCopied) SkeuoColors.LedGreen else SkeuoColors.TextMuted,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isCopied) "COPIED" else "COPY",
                    color = if (isCopied) SkeuoColors.LedGreen else SkeuoColors.TextNormal,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Code Content with Line Numbers
        val lines = remember(code) { code.lines() }
        val scrollState = rememberScrollState()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .horizontalScroll(scrollState)
        ) {
            // Line numbers column
            Column(modifier = Modifier.padding(end = 12.dp)) {
                for (i in 1..lines.size) {
                    Text(
                        text = "$i",
                        color = Color(0xFF384050),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Code text
            Column {
                for (line in lines) {
                    Text(
                        text = if (line.isEmpty()) " " else line,
                        color = Color(0xFFC7D0E0),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun FormattedInlineText(
    text: String,
    defaultColor: Color,
    fontSize: Int,
    modifier: Modifier = Modifier
) {
    val annotated = remember(text, defaultColor) {
        buildAnnotatedString {
            var i = 0
            while (i < text.length) {
                if (text.startsWith("**", i)) {
                    val end = text.indexOf("**", i + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = SkeuoColors.TextHighContrast)) {
                            append(text.substring(i + 2, end))
                        }
                        i = end + 2
                        continue
                    }
                }
                if (text.startsWith("`", i) && !text.startsWith("```", i)) {
                    val end = text.indexOf("`", i + 1)
                    if (end != -1) {
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                color = SkeuoColors.LedCyan,
                                background = Color(0x3300E5FF)
                            )
                        ) {
                            append(" ${text.substring(i + 1, end)} ")
                        }
                        i = end + 1
                        continue
                    }
                }
                append(text[i])
                i++
            }
        }
    }

    Text(
        text = annotated,
        color = defaultColor,
        fontSize = fontSize.sp,
        lineHeight = (fontSize * 1.45).sp,
        modifier = modifier
    )
}

sealed class MarkdownBlock {
    data class Header(val level: Int, val text: String) : MarkdownBlock()
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock()
    data class BulletItem(val text: String) : MarkdownBlock()
    data class BlockQuote(val text: String) : MarkdownBlock()
    data class Paragraph(val text: String) : MarkdownBlock()
}

fun parseMarkdownBlocks(markdown: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = markdown.lines()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]
        val trimmed = line.trim()

        if (trimmed.startsWith("```")) {
            val lang = trimmed.removePrefix("```").trim()
            val codeBuilder = StringBuilder()
            i++
            while (i < lines.size && !lines[i].trim().startsWith("```")) {
                codeBuilder.append(lines[i]).append("\n")
                i++
            }
            blocks.add(MarkdownBlock.CodeBlock(lang, codeBuilder.toString().trimEnd()))
            i++
            continue
        }

        if (trimmed.startsWith("### ")) {
            blocks.add(MarkdownBlock.Header(3, trimmed.removePrefix("### ")))
        } else if (trimmed.startsWith("## ")) {
            blocks.add(MarkdownBlock.Header(2, trimmed.removePrefix("## ")))
        } else if (trimmed.startsWith("# ")) {
            blocks.add(MarkdownBlock.Header(1, trimmed.removePrefix("# ")))
        } else if (trimmed.startsWith("> ")) {
            blocks.add(MarkdownBlock.BlockQuote(trimmed.removePrefix("> ")))
        } else if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
            blocks.add(MarkdownBlock.BulletItem(trimmed.substring(2)))
        } else if (trimmed.isNotEmpty()) {
            blocks.add(MarkdownBlock.Paragraph(line))
        }
        i++
    }

    return blocks
}
