fun main(){
    var numbers = [5, 2, 9, 1, 5, 6]

    println("Original: " + numbers)
    println("Sorted: " + sort(numbers))
    println("Max: " + maxElement(numbers))
    println("reversed: " + reverseList(numbers))
    println("unique: " + uniqueList(numbers))
    println("contains: " + contains(numbers, 3))
    println("indexOf: " + indexOf(numbers, 4))



    var squers = map(numbers, square)
    println("squers: " + squers)

    var filtered = filter(numbers, isEven)
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

    var array = [5, 2, 9, 1, 5, 6]
    sort(array)
    println("array: " + array)
    var ok = binarySearch(array, 6)
    println("binarySearch: " + ok)
}

fun square(x) {
    return x * x
}
