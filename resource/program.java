import std.sortString
import std.fibonacci
import std.maxElement
import std.sort
import std.stringToList
import std.listToString
import std.upperCase
import std.lowerCase
import std.trim
import std.reverseList
import std.uniqueList
import std.contains
import std.indexOf
import std.isEven
import std.map
import std.filter as ok
import custom.fibonacci

as customfibonacci

fun main() {
    var numbers = [5, 2, 9, 1, 5, 6]
    var str = "hello world"

    println("Type a line and press Enter:")
//    var input = readln()
//    println("You typed: " + input)

    println("Original: " + numbers)
    println("Sorted: " + sort(numbers))
    println("Max: " + maxElement(numbers))
    println("Fibonacci (first 10): " + fibonacci(10))
    println("Fibonacci (first 10): " + customfibonacci(10))
    println("reversed: " + reverseList(numbers))
    println("unique: " + uniqueList(numbers))
    println("contains: " + contains(numbers, 3))
    println("indexOf: " + indexOf(numbers, 4))

    println("upper case: " + upperCase("Bagdaulet Seilov"))
    println("lower case: " + lowerCase("Bagdaulet Seilov"))

    var counts = <>

    for (i = 0; i < size(numbers); i = i + 1) {
        var key = "" + numbers[i]
        if (counts[key] == null) {
            counts[key] = 1
        } else {
            counts[key] = counts[key] + 1
        }
    }
    println("counts: " + counts)

    var ff = dodo

    var squers = map(numbers, ff)
    println("squers: " + squers)

    var filtered = ok(numbers, isEven)
    println("filtered: " + filtered)

    for (i = 0; i < size(numbers); i = i + 1) {
        if (numbers[i] > 5) {
            println("gt5: " + numbers[i])
        } else {
            if (numbers[i] == 5) {
                println("eq5: " + numbers[i])
            } else {
                println("lt5: " + numbers[i])
            }
        }
    }

    var user = <>
    user["name"] = "bagdaulet"
    user["age"] = 45
    user["contacts"] = ["phone", "phone 2"]
    println("user name: " + user.name)
    println("user age: " + user["age"])
    var key = "city"
    var map = <>
    map[key] = "almaty"
    println("user city: " + map.city)

    println("Original: " + str)

    println("string to list: " + sortString(str))
    println("Sorted: " + sort(stringToList(str)))
    println("Max: " + maxElement(str))


    println("ascii: " + toNumber("a"))
    println("string: " + toString(49))

    println("reversed: " + reverseList(str))
    println("unique: " + uniqueList(str))
    println("contains: " + contains(str, "d"))
    println("indexOf: " + indexOf(str, "d"))

    println(typeOf(null) == NULL)
    println(typeOf(123) == NUMBER)
    println(typeOf("hi") == STRING)
    println(typeOf(true) == BOOLEAN)
    println(typeOf([1, 2]) ==ARRAY)
    println(typeOf( < >) == STRUCTURE)
    println(typeOf(dodo) == FUNCTION)


    var array = [5, 2, 9, 1, 5, 6]
    sort(array)
    println("array: " + array)
    var ok = binarySearch(array, 6)
    println("binarySearch: " + ok)

}

fun dodo(x) {
    return x * 2
}

fun binarySearch(arr, target) {
    return binarySearchRec(arr, target, 0, size(arr) - 1)
}

fun binarySearchRec(arr, target, left, right) {
    if (left > right) {
        return -1
    }
    var mid = (left + right) / 2
    mid = mid - (mid % 1)
    if (arr[mid] == target) {
        return mid
    }
    if (arr[mid] > target) {
        return binarySearchRec(arr, target, left, mid - 1)
    }
    return binarySearchRec(arr, target, mid + 1, right)
}
