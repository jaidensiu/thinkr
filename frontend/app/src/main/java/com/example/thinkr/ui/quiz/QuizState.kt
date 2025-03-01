package com.example.thinkr.ui.quiz

data class QuizState(
    var quiz: Quiz = Quiz(),
    var selectedAnswerIndices: List<Int> = emptyList(),
    var started: Boolean = false,
    var revealAnswer: Boolean = false,
    var totalScore: Int = 0,
    var totalTimeSeconds: Int = 0
)

data class Quiz(
    var multipleChoiceQuestions: List<Pair<String, List<String>>> = emptyList(),
    var correctAnswerIndexList: List<Int> = emptyList()
)
