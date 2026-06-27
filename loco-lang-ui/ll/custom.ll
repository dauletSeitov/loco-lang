
fun fibonacci(n) {
    println("custom fibo")
    var fib = [0, 1]
    for (i = 2; i < n; i = i + 1) {
        fib = fib + [fib[i - 1] + fib[i - 2]]
    }
    return fib
}
