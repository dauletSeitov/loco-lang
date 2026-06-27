fun main() {
    var name = "loco"
    var version = 1
    var pi = 3.14
    var ok = true

    println("name: " + name)
    println("version: " + version)
    println("pi: " + pi)
    println("ok: " + ok)

    var sum = 10 + 5
    var diff = 10 - 5
    var prod = 10 * 5
    var div = 10 / 4
    var mod = 10 % 4

    println("sum: " + sum)
    println("diff: " + diff)
    println("prod: " + prod)
    println("div: " + div)
    println("mod: " + mod)

    if (version > 0 & ok) {
        println("version ok")
    } else {
        println("version not ok")
    }

    for (i = 0; i < 3; i = i + 1) {
        println("i: " + i)
    }
}



println("ascii: " + toNumber("a"))
println("string: " + toString(49))

var letters = ["b", "a", "d"]
println("list to string: " + listToString(letters))
println("string to list: " + stringToList("bad"))

println(typeOf(null) == NULL)
println(typeOf(123) == NUMBER)
println(typeOf("hi") == STRING)
println(typeOf(true) == BOOLEAN)
println(typeOf([1, 2]) == ARRAY)
println(typeOf(< >) == STRUCTURE)
println(typeOf(sort) == FUNCTION)
