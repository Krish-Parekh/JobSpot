package com.krish.jobspot.model

data class MockResult(
    var mockId: String = "",
    var studentId: String = "",
    var correctAns: String = "",
    var incorrectAns: String = "",
    var unAttempted: String = "",
    var timeTaken: Long = 0,
    var totalQuestion : String = ""
)