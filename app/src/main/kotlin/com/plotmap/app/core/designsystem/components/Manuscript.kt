package com.plotmap.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.plotmap.app.core.designsystem.BackgroundBottom
import com.plotmap.app.core.designsystem.BackgroundMid
import com.plotmap.app.core.designsystem.BackgroundTop
import com.plotmap.app.core.designsystem.BorderCard
import com.plotmap.app.core.designsystem.BorderCardViolet
import com.plotmap.app.core.designsystem.BorderThin
import com.plotmap.app.core.designsystem.ForumFamily
import com.plotmap.app.core.designsystem.Gold
import com.plotmap.app.core.designsystem.GoldBright
import com.plotmap.app.core.designsystem.GoldBronze
import com.plotmap.app.core.designsystem.GoldGlow
import com.plotmap.app.core.designsystem.Lavender
import com.plotmap.app.core.designsystem.ListItemGoldBg
import com.plotmap.app.core.designsystem.ListItemGoldSub
import com.plotmap.app.core.designsystem.ListItemGoldText
import com.plotmap.app.core.designsystem.ListItemVioletBg
import com.plotmap.app.core.designsystem.ListItemVioletSub
import com.plotmap.app.core.designsystem.ListItemVioletText
import com.plotmap.app.core.designsystem.ListItemWineBg
import com.plotmap.app.core.designsystem.MarckScriptFamily
import com.plotmap.app.core.designsystem.PlayfairDisplayFamily
import com.plotmap.app.core.designsystem.PtSerifFamily
import com.plotmap.app.core.designsystem.SurfaceVioletBottom
import com.plotmap.app.core.designsystem.SurfaceVioletTop
import com.plotmap.app.core.designsystem.TextBody
import com.plotmap.app.core.designsystem.TextFaint
import com.plotmap.app.core.designsystem.WineBorder
import com.plotmap.app.core.designsystem.WineRed
import com.plotmap.app.core.designsystem.WineText

enum class ManuscriptTone { Violet, Gold, Wine }

@Composable
fun ManuscriptBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to BackgroundTop,
                        0.5f to BackgroundMid,
                        1f to BackgroundBottom,
                    ),
                ),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(GoldGlow, Color.Transparent),
                            radius = 900f,
                        ),
                    ),
        )
        content()
    }
}

@Composable
fun ManuscriptCard(
    modifier: Modifier = Modifier,
    tone: ManuscriptTone = ManuscriptTone.Violet,
    contentPadding: androidx.compose.ui.unit.Dp = 26.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    val (gradient, border) =
        when (tone) {
            ManuscriptTone.Violet -> {
                val colors = listOf(SurfaceVioletTop.copy(alpha = 0.5f), SurfaceVioletBottom.copy(alpha = 0.45f))
                Brush.linearGradient(colors) to BorderCardViolet
            }
            ManuscriptTone.Gold ->
                Brush.linearGradient(listOf(Color(0xF53C2E1C), Color(0xF522180F))) to BorderCard
            ManuscriptTone.Wine ->
                Brush.linearGradient(listOf(Color(0xF53A1A28), Color(0xF51E101C))) to WineBorder.copy(alpha = 0.55f)
        }
    Column(
        modifier =
            modifier
                .shadow(8.dp, shape, ambientColor = Color.Black, spotColor = Color.Black)
                .clip(shape)
                .background(gradient)
                .border(1.dp, border, shape)
                .padding(contentPadding),
        content = content,
    )
}

@Composable
fun OverlineLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = GoldBronze,
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        color = color,
        fontFamily = ForumFamily,
        fontSize = 12.sp,
        letterSpacing = 0.28.em,
    )
}

@Composable
fun ScriptRemark(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = GoldBright,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontFamily = MarckScriptFamily,
        fontSize = 20.sp,
    )
}

@Composable
fun OrnamentDivider(
    modifier: Modifier = Modifier,
    color: Color = Gold,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(Brush.horizontalGradient(listOf(Color.Transparent, color.copy(alpha = 0.6f)))),
        )
        Text(text = "✦", color = color, fontSize = 14.sp)
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(Brush.horizontalGradient(listOf(color.copy(alpha = 0.6f), Color.Transparent))),
        )
    }
}

@Composable
fun ManuscriptListItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    tone: ManuscriptTone = ManuscriptTone.Violet,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(8.dp)
    val bg =
        when (tone) {
            ManuscriptTone.Violet -> ListItemVioletBg
            ManuscriptTone.Gold -> ListItemGoldBg
            ManuscriptTone.Wine -> ListItemWineBg
        }
    val bar =
        when (tone) {
            ManuscriptTone.Violet -> Lavender
            ManuscriptTone.Gold -> Gold
            ManuscriptTone.Wine -> WineRed
        }
    val titleColor = if (tone == ManuscriptTone.Gold) ListItemGoldText else ListItemVioletText
    val subColor = if (tone == ManuscriptTone.Gold) ListItemGoldSub else ListItemVioletSub
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(shape)
                .background(bg)
                .then(clickModifier)
                .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(bar),
        )
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 11.dp),
        ) {
            Text(
                text = title,
                color = titleColor,
                fontFamily = PlayfairDisplayFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = subColor,
                    fontFamily = PtSerifFamily,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        if (trailing != null) {
            Box(modifier = Modifier.padding(end = 12.dp)) { trailing() }
        }
    }
}

@Composable
fun LabelValueRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                color = TextBody,
                fontFamily = PtSerifFamily,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = value,
                color = GoldBright,
                fontFamily = PlayfairDisplayFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                textAlign = TextAlign.End,
            )
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderThin))
    }
}

@Composable
fun NumberedBadge(
    number: Int,
    done: Boolean,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(percent = 50)
    val borderColor = if (done) Gold.copy(alpha = 0.5f) else WineBorder.copy(alpha = 0.6f)
    val textColor = if (done) GoldBright else WineText
    Box(
        modifier =
            modifier
                .size(44.dp)
                .clip(shape)
                .background(if (done) ListItemGoldBg else Color.Transparent)
                .border(1.dp, borderColor, shape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = number.toString(),
            color = textColor,
            fontFamily = PlayfairDisplayFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
        )
    }
}

@Composable
fun ManuscriptFooter(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        color = TextFaint,
        fontFamily = ForumFamily,
        fontSize = 12.sp,
        letterSpacing = 0.3.em,
    )
}
