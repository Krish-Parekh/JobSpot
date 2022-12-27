package com.krish.jobspot.model

data class MockDetail(
    var mockId : String = "",
    var studentCount: String = "0",
    var mockName: String = "",
    var studentIds: MutableList<String> = mutableListOf<String>()
)