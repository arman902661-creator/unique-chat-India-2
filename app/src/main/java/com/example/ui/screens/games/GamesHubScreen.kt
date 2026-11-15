package com.example.ui.screens.games

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VoiceChatRepository
import com.example.model.User
import com.example.ui.theme.AccentGreenOnline
import com.example.ui.theme.AccentRedLive
import com.example.ui.theme.GoldenBadge
import com.example.ui.theme.IndiaAmber
import com.example.ui.theme.IndiaSaffron
import com.example.ui.theme.MidnightDeep
import com.example.ui.theme.PeacockCyan
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated

@Composable
fun GamesHubScreen(
    currentUser: User,
    repository: VoiceChatRepository
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Tic Tac Toe", "Desi Quiz", "Ludo Rush", "Chess")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightDeep)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Multiplayer Arena 🎮",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Play online with friends or test your skills",
                    fontSize = 12.sp,
                    color = PeacockCyan
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceCard)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(text = "🪙 ${currentUser.virtualCoins}", color = GoldenBadge, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SurfaceElevated,
            contentColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = PeacockCyan
                )
            },
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> TicTacToeGame(currentUser)
            1 -> DesiQuizGame(currentUser)
            2 -> LudoRushGame(currentUser)
            3 -> ChessDuelGame(currentUser)
        }
    }
}

// -------------------------------------------------------------------------
// 1. TIC TAC TOE GAME IMPLEMENTATION
// -------------------------------------------------------------------------
@Composable
fun TicTacToeGame(currentUser: User) {
    val board = remember { mutableStateListOf("", "", "", "", "", "", "", "", "") }
    var currentTurn by remember { mutableStateOf("X") }
    var winner by remember { mutableStateOf<String?>(null) }
    var winningIndices by remember { mutableStateOf<List<Int>>(emptyList()) }

    fun checkWin(): Pair<String?, List<Int>> {
        val wins = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
            listOf(0, 4, 8), listOf(2, 4, 6)
        )
        for (w in wins) {
            if (board[w[0]].isNotEmpty() && board[w[0]] == board[w[1]] && board[w[1]] == board[w[2]]) {
                return Pair(board[w[0]], w)
            }
        }
        if (board.none { it.isEmpty() }) {
            return Pair("Draw", emptyList())
        }
        return Pair(null, emptyList())
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when {
                        winner == "Draw" -> "Match Drawn! 🤝"
                        winner != null -> "Winner: Player $winner! 🏆"
                        else -> "Turn: Player $currentTurn"
                    },
                    fontWeight = FontWeight.Bold,
                    color = if (winner != null) GoldenBadge else Color.White,
                    fontSize = 16.sp
                )

                IconButton(
                    onClick = {
                        for (i in 0..8) board[i] = ""
                        winner = null
                        winningIndices = emptyList()
                        currentTurn = "X"
                    }
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = PeacockCyan)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3x3 Grid
            Column(
                modifier = Modifier
                    .size(270.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceCard)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (row in 0..2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (col in 0..2) {
                            val index = row * 3 + col
                            val isWinSquare = winningIndices.contains(index)
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        when {
                                            isWinSquare -> IndiaSaffron.copy(alpha = 0.4f)
                                            else -> SurfaceElevated
                                        }
                                    )
                                    .border(
                                        1.dp,
                                        if (isWinSquare) IndiaSaffron else Color.White.copy(alpha = 0.1f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        if (board[index].isEmpty() && winner == null) {
                                            board[index] = currentTurn
                                            val (winResult, winLine) = checkWin()
                                            if (winResult != null) {
                                                winner = winResult
                                                winningIndices = winLine
                                            } else {
                                                currentTurn = if (currentTurn == "X") "O" else "X"
                                            }
                                        }
                                    }
                            ) {
                                Text(
                                    text = board[index],
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (board[index] == "X") PeacockCyan else IndiaAmber
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Player X: You • Player O: Opponent / Friend",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

// -------------------------------------------------------------------------
// 2. DESI QUIZ CHALLENGE
// -------------------------------------------------------------------------
data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int
)

@Composable
fun DesiQuizGame(currentUser: User) {
    val questions = remember {
        listOf(
            QuizQuestion("Which Indian city is known as the Silicon Valley of India?", listOf("Mumbai", "Bengaluru", "Hyderabad", "Delhi"), 1),
            QuizQuestion("Who is known as the Master Blaster in Indian Cricket?", listOf("Virat Kohli", "Kapil Dev", "Sachin Tendulkar", "MS Dhoni"), 2),
            QuizQuestion("Which Bollywood movie won 7 Filmfare awards featuring 'Jai Ho'?", listOf("Lagaan", "Slumdog Millionaire", "Dangall", "PK"), 1),
            QuizQuestion("What is the national aquatic animal of India?", listOf("Ganges River Dolphin", "Blue Whale", "Gharial", "Catfish"), 0),
            QuizQuestion("Which festival is also known as the Festival of Lights?", listOf("Holi", "Diwali", "Eid", "Navratri"), 1)
        )
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableIntStateOf(0) }
    var quizCompleted by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🇮🇳 Desi Trivia Quiz",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )
                Text(
                    text = "Score: $score / ${questions.size}",
                    color = GoldenBadge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (!quizCompleted) {
                val q = questions[currentIndex]
                Text(
                    text = "Q${currentIndex + 1}. ${q.question}",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                q.options.forEachIndexed { optIndex, optionText ->
                    val isSelected = selectedAnswer == optIndex
                    val isCorrect = optIndex == q.correctIndex
                    val bgColor = when {
                        selectedAnswer == null -> SurfaceCard
                        isSelected && isCorrect -> AccentGreenOnline.copy(alpha = 0.3f)
                        isSelected && !isCorrect -> AccentRedLive.copy(alpha = 0.3f)
                        optIndex == q.correctIndex -> AccentGreenOnline.copy(alpha = 0.3f)
                        else -> SurfaceCard
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(bgColor)
                            .border(1.dp, if (isSelected) PeacockCyan else Color.Transparent, RoundedCornerShape(10.dp))
                            .clickable(enabled = selectedAnswer == null) {
                                selectedAnswer = optIndex
                                if (optIndex == q.correctIndex) {
                                    score += 1
                                }
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(RoyalPurple)
                        ) {
                            Text(
                                text = ('A' + optIndex).toString(),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = optionText, color = Color.White, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedAnswer != null) {
                    Button(
                        onClick = {
                            if (currentIndex + 1 < questions.size) {
                                currentIndex += 1
                                selectedAnswer = null
                            } else {
                                quizCompleted = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (currentIndex + 1 < questions.size) "Next Question ➡️" else "Finish Quiz 🏆",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                // Completed Summary
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = GoldenBadge, modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Quiz Completed!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "You scored $score out of ${questions.size} points!",
                        color = PeacockCyan,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            currentIndex = 0
                            score = 0
                            selectedAnswer = null
                            quizCompleted = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple)
                    ) {
                        Text("Play Again")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// 3. LUDO RUSH GAME
// -------------------------------------------------------------------------
@Composable
fun LudoRushGame(currentUser: User) {
    var diceValue by remember { mutableIntStateOf(1) }
    var isRolling by remember { mutableStateOf(false) }
    var redTokenPos by remember { mutableIntStateOf(0) }
    var greenTokenPos by remember { mutableIntStateOf(0) }
    var activePlayer by remember { mutableStateOf("Red") } // Red or Green
    var winner by remember { mutableStateOf<String?>(null) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎲 Desi Ludo Rush",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )
                Text(
                    text = if (winner != null) "$winner Wins! 🏆" else "Turn: $activePlayer",
                    color = if (activePlayer == "Red") AccentRedLive else AccentGreenOnline,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Mini Track (15 steps)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceCard)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (step in 0..14) {
                    val hasRed = redTokenPos == step
                    val hasGreen = greenTokenPos == step
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    step == 14 -> GoldenBadge.copy(alpha = 0.5f)
                                    step % 2 == 0 -> Color.White.copy(alpha = 0.1f)
                                    else -> Color.White.copy(alpha = 0.05f)
                                }
                            )
                    ) {
                        if (hasRed && hasGreen) {
                            Text(text = "🔴🟢", fontSize = 6.sp)
                        } else if (hasRed) {
                            Text(text = "🔴", fontSize = 8.sp)
                        } else if (hasGreen) {
                            Text(text = "🟢", fontSize = 8.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dice Display & Roll Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(listOf(RoyalPurple, IndiaSaffron))
                    )
            ) {
                Text(
                    text = when (diceValue) {
                        1 -> "⚀ 1"
                        2 -> "⚁ 2"
                        3 -> "⚂ 3"
                        4 -> "⚃ 4"
                        5 -> "⚄ 5"
                        else -> "⚅ 6"
                    },
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    if (winner == null) {
                        val rolled = (1..6).random()
                        diceValue = rolled
                        if (activePlayer == "Red") {
                            redTokenPos = (redTokenPos + rolled).coerceAtMost(14)
                            if (redTokenPos >= 14) winner = "Red"
                            activePlayer = "Green"
                        } else {
                            greenTokenPos = (greenTokenPos + rolled).coerceAtMost(14)
                            if (greenTokenPos >= 14) winner = "Green"
                            activePlayer = "Red"
                        }
                    }
                },
                enabled = winner == null,
                colors = ButtonDefaults.buttonColors(containerColor = IndiaSaffron),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Roll Dice 🎲", fontWeight = FontWeight.Bold, color = MidnightDeep)
            }

            if (winner != null) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = {
                    redTokenPos = 0
                    greenTokenPos = 0
                    activePlayer = "Red"
                    winner = null
                }) {
                    Text("Restart Game", color = PeacockCyan)
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// 4. CHESS DUEL GAME
// -------------------------------------------------------------------------
@Composable
fun ChessDuelGame(currentUser: User) {
    // 4x4 Mini-chess board representing rapid tactics
    val board = remember {
        mutableStateListOf(
            "♜", "♞", "♝", "♛",
            "♟", "♟", "♟", "♟",
            ".", ".", ".", ".",
            "♙", "♙", "♙", "♙"
        )
    }
    var selectedSquare by remember { mutableStateOf<Int?>(null) }
    var currentTurn by remember { mutableStateOf("White") }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "♟️ Rapid Chess Tactics",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )
                Text(
                    text = "Turn: $currentTurn",
                    color = if (currentTurn == "White") Color.White else GoldenBadge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4x4 Board
            Column(
                modifier = Modifier
                    .size(240.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, PeacockCyan, RoundedCornerShape(12.dp)),
                verticalArrangement = Arrangement.Center
            ) {
                for (r in 0..3) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (c in 0..3) {
                            val idx = r * 4 + c
                            val isLight = (r + c) % 2 == 0
                            val isSelected = selectedSquare == idx

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .background(
                                        when {
                                            isSelected -> IndiaSaffron.copy(alpha = 0.6f)
                                            isLight -> Color(0xFFEEEED2)
                                            else -> Color(0xFF769656)
                                        }
                                    )
                                    .clickable {
                                        if (selectedSquare == null) {
                                            if (board[idx] != ".") {
                                                selectedSquare = idx
                                            }
                                        } else {
                                            val from = selectedSquare!!
                                            if (from != idx) {
                                                board[idx] = board[from]
                                                board[from] = "."
                                                currentTurn = if (currentTurn == "White") "Black" else "White"
                                            }
                                            selectedSquare = null
                                        }
                                    }
                            ) {
                                if (board[idx] != ".") {
                                    Text(
                                        text = board[idx],
                                        fontSize = 24.sp,
                                        color = if (board[idx] == "♙") Color(0xFF222222) else Color(0xFF111111)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Tap a piece to pick, then tap a destination square to move.",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}
