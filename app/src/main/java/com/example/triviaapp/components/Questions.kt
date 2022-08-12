package com.example.triviaapp.components

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.triviaapp.model.QuestionItem
import com.example.triviaapp.screens.QuestionsViewModel
import com.example.triviaapp.utils.AppColors

@Composable
fun Questions(viewModel: QuestionsViewModel) {

    val questions = viewModel.data.value.data?.toMutableList()

    val questionIndex = remember {
        mutableStateOf(1)
    }

    if (viewModel.data.value.loading == true) {
        CircularProgressIndicator()
    } else {
        val question = try {
            questions?.get(questionIndex.value)
        }catch (ex: Exception) {
            null
        }
        if (questions != null) {
            QuestionDisplay(question = question!!, questionIndex, viewModel) {
                questionIndex.value += 1
            }
        }
    }
}

@Composable
fun QuestionDisplay( question: QuestionItem,
                     questionIndex: MutableState<Int>,
                     viewModel: QuestionsViewModel,
                     onNextClicked: (Int) -> Unit = {}
    ) {
    val choicesState = remember(question) {
        question.choices.toMutableList()
    }

    val answerState = remember(question) {
        mutableStateOf<Int?>(null)
    }

    val correctAnswerState = remember(question) {
        mutableStateOf<Boolean?>(null)
    }

    val updateAnswer: (Int) -> Unit = remember(question) {
        {
            answerState.value = it
            correctAnswerState.value = choicesState[it] == question.answer
        }
    }

    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f,10f), 0f)

    Surface(modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(),
            color = AppColors.mDarkPurple) {

        Column(modifier = Modifier.padding(12.dp),
               verticalArrangement = Arrangement.Top,
               horizontalAlignment = Alignment.Start) {
            QuestionTracker(counter = questionIndex.value, outOf = viewModel.data.value.data?.size!!)
            DrawDottedLine(pathEffect)
            
            Column {
                Text(text = question.question,
                modifier = Modifier
                    .padding(6.dp)
                    .align(alignment = Alignment.Start)
                    .fillMaxHeight(0.3f),
                fontSize = 17.sp,
                color = AppColors.mOffWhite,
                fontWeight = FontWeight.Bold,
                lineHeight = 22.sp)
            }

            // choices
            choicesState.forEachIndexed { index, answerText ->
                Row(modifier = Modifier
                    .padding(3.dp)
                    .fillMaxWidth()
                    .height(45.dp)
                    .border(
                        width = 4.dp, brush = Brush.linearGradient(
                            colors = listOf(AppColors.mOffDarkPurple, AppColors.mOffDarkPurple)
                        ),
                        shape = RoundedCornerShape(15.dp)
                    )
                    .clip(
                        RoundedCornerShape(
                            topStartPercent = 50,
                            topEndPercent = 50,
                            bottomEndPercent = 50,
                            bottomStartPercent = 50
                        )
                    )
                    .background(Color.Transparent),
                    verticalAlignment = Alignment.CenterVertically) {

                    RadioButton(selected = (answerState.value == index),
                        onClick = {
                            updateAnswer(index)
                        },
                        modifier = Modifier.padding(start = 16.dp),
                        colors = RadioButtonDefaults.colors(
                            selectedColor = if (correctAnswerState.value == true && index == answerState.value) {
                                Color.Green.copy(alpha = 0.2f)
                            } else {
                                Color.Red.copy(alpha = 0.2f)
                            }
                        ))
                    val annotatedString = buildAnnotatedString {
                        withStyle(style = SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = if (correctAnswerState.value == true && index == answerState.value) {
                                Color.Green.copy(alpha = 0.2f)
                            } else if (correctAnswerState.value == false && index == answerState.value){
                                Color.Red.copy(alpha = 0.2f)
                            } else {
                                AppColors.mOffWhite
                            },
                            fontSize = 17.sp
                        )) {
                            append(answerText)
                        }
                    }
                    
                    Text(text = annotatedString, modifier = Modifier.padding(6.dp))

                }

            }
            Button(
                onClick = { onNextClicked(questionIndex.value) },
                modifier = Modifier
                    .padding(3.dp)
                    .align(alignment = Alignment.CenterHorizontally),
                shape = RoundedCornerShape(34.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.mLightBlue)
            ) {

                Text(
                    text = "Next",
                    modifier = Modifier.padding(4.dp),
                    color = AppColors.mOffWhite,
                    fontSize = 17.sp
                )
                
            }
        }

    }
}

@Composable
fun DrawDottedLine(pathEffect: PathEffect) {
    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(1.dp)) {
        drawLine(color = AppColors.mLightGray,
        start = Offset(0f, 0f),
        end = Offset(size.width, y = 0f),
        pathEffect = pathEffect)
    }

}

@Composable
fun ShowProgress(score: Int = 12) {
    Row(
        modifier = Modifier
            .padding(3.dp)
            .fillMaxWidth()
            .height(45.dp)
            .border(
                width = 4.dp,
                brush = Brush.linearGradient(
                    colors = listOf(AppColors.mLightPurple, AppColors.mLightPurple)
                ),
                shape = RoundedCornerShape(34.dp))
            .clip(RoundedCornerShape(
                topStartPercent = 50,
                topEndPercent = 50,
                bottomStartPercent = 50,
                bottomEndPercent = 50))
            .background(Color.Transparent),
        verticalAlignment = Alignment.CenterVertically
    ) {
        
    }
}

@Composable
fun QuestionTracker(counter: Int = 10, outOf: Int = 100) {

    Text(text = buildAnnotatedString {
        withStyle(style = ParagraphStyle(textIndent = TextIndent.None)) {
            withStyle(style = SpanStyle(color = AppColors.mLightGray,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 27.sp)) {
                append("Question $counter/")
                withStyle(style = SpanStyle(color = AppColors.mLightGray,
                                            fontWeight = FontWeight.Light,
                                            fontSize = 14.sp)) {
                    append("$outOf")
                }
            }

        }
    },
        modifier = Modifier.padding(20.dp))
}