fun sortString(str) {
    var list = stringToList(str)
    var sortedList = sort(list)
    return listToString(sortedList)
}

fun maxElement(arr) {
    var max = arr[0]
    for (i = 1; i < size(arr); i = i + 1) {
        if (arr[i] > max) {
            max = arr[i]
        }
    }
    return max
}

fun sort(arr) {
    var n = size(arr)
    for (i = 0; i < n - 1; i = i + 1) {
        for (j = 0; j < n - i - 1; j = j + 1) {
            if (arr[j] > arr[j + 1]) {
                var temp = arr[j]
                arr[j] = arr[j + 1]
                arr[j + 1] = temp
            }
        }
    }
    return arr
}

fun stringToList(str) {
    var list = []
    for (i = 0; i < size(str); i = i + 1) {
        list = list + [str[i]]
    }
    return list
}

fun listToString(list) {
    var str = ""
    for (i = 0; i < size(list); i = i + 1) {
        str = str + list[i]
    }
    return str
}

fun upperCase(input) {
    if (input == null) {
        return null
    }

    var result = ""
    for (i = 0; i < size(input); i = i + 1) {
        var val = toNumber(input[i])
        if (val >= toNumber("a") & val <= toNumber("z")) {
            result = result + toString(val - 32)
        } else {
            result = result + input[i]
        }
    }
    return result
}

fun lowerCase(str) {
    if (str == null) {
        return null
    }
    var result = ""
    for (i = 0; i < size(str); i = i + 1) {
        var val = toNumber(str[i])
        if (val >= toNumber("A") & val <= toNumber("Z")) {
            result = result + toString(val + 32)
        } else {
            result = result + str[i]
        }
    }
    return result
}

fun trim(str) {
    var start = 0
    var end = size(str) - 1
    var foundStart = false
    for (i = 0; i < size(str); i = i + 1) {
        if (!foundStart) {
            var ch = str[i]
            if (ch == " " | ch == "\t" | ch == "\n" | ch == "\r") {
                start = start + 1
            } else {
                foundStart = true
            }
        }
    }
    var foundEnd = false
    for (i = size(str) - 1; i >= start; i = i - 1) {
        if (!foundEnd) {
            var ch2 = str[i]
            if (ch2 == " " | ch2 == "\t" | ch2 == "\n" | ch2 == "\r") {
                end = end - 1
            } else {
                foundEnd = true
            }
        }
    }
    if (end < start) {
        return ""
    }
    var result = ""
    for (i = start; i <= end; i = i + 1) {
        result = result + str[i]
    }
    return result
}

fun reverseList(list) {
    var result = []
    for (i = size(list) - 1; i >= 0; i = i - 1) {
        result = result + [list[i]]
    }
    return result
}

fun isEven(number) {
    return number % 2 == 0
}

fun uniqueList(list) {
    println("list" + list)
    var result = []
    for (i = 0; i < size(list); i = i + 1) {
        var ok = list[i]
        if (!contains(result, ok)) {
            result = result + [list[i]]
        }
    }
    return result
}

fun contains(list, val) {
    for (i = 0; i < size(list); i = i + 1) {
        if (list[i] == val) {
            return true
        }
    }
    return false
}

fun indexOf(list, val) {
    for (i = 0; i < size(list); i = i + 1) {
        if (list[i] == val) {
            return i
        }
    }
    return -1
}

fun map(listOrString, function) {
    var result = []
    for (i = 0; i < size(listOrString); i = i + 1) {
        var val = function(listOrString[i])
        result = result + [val]
    }
    return result
}

fun filter(listOrString, function) {
    var result = []
    for (i = 0; i < size(listOrString); i = i + 1) {
        var val = function(listOrString[i])
        if (val) {
            result = result + [listOrString[i]]
        }
    }
    return result
}
