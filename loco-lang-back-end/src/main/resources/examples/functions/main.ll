fun main() {
    println("add: " + add(2, 3))
    println("fact 5: " + fact(5))

    var numbers = [1, 2, 3, 4, 5]
    var doubled = map(numbers, double)
    println("doubled: " + doubled)
}

fun add(a, b) {
    return a + b
}

fun double(x) {
    return x * 2
}

fun fact(n) {
    if (n <= 1) {
        return 1
    }
    return n * fact(n - 1)
}
