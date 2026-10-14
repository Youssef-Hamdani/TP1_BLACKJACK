package com.example.tp1_blackjack.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tp1_blackjack.R
import com.example.tp1_blackjack.model.Card
import com.example.tp1_blackjack.model.Rank
import com.example.tp1_blackjack.model.Suit

const val FACE_UP_CARD_TEST_TAG = "playing-card-face"
const val FACE_DOWN_CARD_TEST_TAG = "playing-card-back"

@Composable
fun CardView(
    card: Card?,
    isFaceDown: Boolean,
    modifier: Modifier = Modifier,
) {
    val description = if (isFaceDown) {
        stringResource(R.string.card_hidden)
    } else {
        val visibleCard = requireNotNull(card) { "A face-up card requires card data" }
        stringResource(
            R.string.card_description,
            stringResource(visibleCard.rank.descriptionResourceId()),
            stringResource(visibleCard.suit.descriptionResourceId()),
        )
    }
    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = modifier
            .width(72.dp)
            .aspectRatio(PLAYING_CARD_ASPECT_RATIO)
            .graphicsLayer {
                shadowElevation = 8.dp.toPx()
                this.shape = shape
                clip = true
            }
            .background(CARD_FACE_COLOR)
            .border(1.dp, Color.Black.copy(alpha = 0.18f), shape)
            .semantics(mergeDescendants = true) {
                contentDescription = description
            }
            .testTag(if (isFaceDown) FACE_DOWN_CARD_TEST_TAG else FACE_UP_CARD_TEST_TAG),
    ) {
        if (isFaceDown) {
            Image(
                painter = painterResource(R.drawable.card_back),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            FaceUpCard(card = requireNotNull(card))
        }
    }
}

@Composable
private fun FaceUpCard(card: Card) {
    val suitColor = if (card.suit == Suit.HEARTS || card.suit == Suit.DIAMONDS) {
        CARD_RED
    } else {
        CARD_BLACK
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(7.dp),
    ) {
        Text(
            text = card.rank.label,
            color = suitColor,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.TopStart),
        )
        SuitSymbol(
            suit = card.suit,
            color = suitColor,
            modifier = Modifier
                .size(31.dp)
                .align(Alignment.Center),
        )
        Text(
            text = card.rank.label,
            color = suitColor,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.BottomEnd),
        )
    }
}

@Composable
private fun SuitSymbol(
    suit: Suit,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        when (suit) {
            Suit.HEARTS -> {
                val path = Path().apply {
                    moveTo(width / 2f, height * 0.88f)
                    cubicTo(width * 0.38f, height * 0.70f, width * 0.08f, height * 0.52f, width * 0.08f, height * 0.28f)
                    cubicTo(width * 0.08f, height * 0.05f, width * 0.38f, height * 0.02f, width / 2f, height * 0.24f)
                    cubicTo(width * 0.62f, height * 0.02f, width * 0.92f, height * 0.05f, width * 0.92f, height * 0.28f)
                    cubicTo(width * 0.92f, height * 0.52f, width * 0.62f, height * 0.70f, width / 2f, height * 0.88f)
                    close()
                }
                drawPath(path, color)
            }

            Suit.DIAMONDS -> {
                val path = Path().apply {
                    moveTo(width / 2f, 0f)
                    lineTo(width, height / 2f)
                    lineTo(width / 2f, height)
                    lineTo(0f, height / 2f)
                    close()
                }
                drawPath(path, color)
            }

            Suit.CLUBS -> {
                val radius = width * 0.22f
                drawCircle(color, radius, center = androidx.compose.ui.geometry.Offset(width / 2f, height * 0.28f))
                drawCircle(color, radius, center = androidx.compose.ui.geometry.Offset(width * 0.30f, height * 0.52f))
                drawCircle(color, radius, center = androidx.compose.ui.geometry.Offset(width * 0.70f, height * 0.52f))
                drawRect(
                    color = color,
                    topLeft = androidx.compose.ui.geometry.Offset(width * 0.43f, height * 0.48f),
                    size = androidx.compose.ui.geometry.Size(width * 0.14f, height * 0.42f),
                )
            }

            Suit.SPADES -> {
                val path = Path().apply {
                    moveTo(width / 2f, height * 0.04f)
                    cubicTo(width * 0.42f, height * 0.20f, width * 0.08f, height * 0.38f, width * 0.08f, height * 0.60f)
                    cubicTo(width * 0.08f, height * 0.82f, width * 0.38f, height * 0.84f, width / 2f, height * 0.66f)
                    cubicTo(width * 0.62f, height * 0.84f, width * 0.92f, height * 0.82f, width * 0.92f, height * 0.60f)
                    cubicTo(width * 0.92f, height * 0.38f, width * 0.58f, height * 0.20f, width / 2f, height * 0.04f)
                    close()
                }
                drawPath(path, color)
                drawRect(
                    color = color,
                    topLeft = androidx.compose.ui.geometry.Offset(width * 0.43f, height * 0.58f),
                    size = androidx.compose.ui.geometry.Size(width * 0.14f, height * 0.34f),
                )
            }
        }
    }
}

@Composable
private fun Rank.descriptionResourceId(): Int = when (this) {
    Rank.ACE -> R.string.rank_ace
    Rank.TWO -> R.string.rank_two
    Rank.THREE -> R.string.rank_three
    Rank.FOUR -> R.string.rank_four
    Rank.FIVE -> R.string.rank_five
    Rank.SIX -> R.string.rank_six
    Rank.SEVEN -> R.string.rank_seven
    Rank.EIGHT -> R.string.rank_eight
    Rank.NINE -> R.string.rank_nine
    Rank.TEN -> R.string.rank_ten
    Rank.JACK -> R.string.rank_jack
    Rank.QUEEN -> R.string.rank_queen
    Rank.KING -> R.string.rank_king
}

@Composable
private fun Suit.descriptionResourceId(): Int = when (this) {
    Suit.CLUBS -> R.string.suit_clubs
    Suit.DIAMONDS -> R.string.suit_diamonds
    Suit.HEARTS -> R.string.suit_hearts
    Suit.SPADES -> R.string.suit_spades
}

private const val PLAYING_CARD_ASPECT_RATIO = 0.68f
private val CARD_FACE_COLOR = Color(0xFFFFFEFA)
private val CARD_RED = Color(0xFFB3261E)
private val CARD_BLACK = Color(0xFF171717)
