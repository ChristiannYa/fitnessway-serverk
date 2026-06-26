package utils

fun nullMessage(item: String) = "$item should be null"
fun notNullMessage(item: String) = "$item should not be null"

fun equalsMessage(itemA: String, itemB: String) = "$itemA should equal $itemB"

fun trueMessage(item: String) = "$item should be true"
fun falseMessage(item: String) = "$item should be false"