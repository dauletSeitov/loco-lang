fun main() {
    var empty = []
    println("size empty: " + size(empty))

    var a = null
    println("null == null: " + (a == null))

    var nums = [1, 2, 3]
    println("index 0: " + nums[0])
    println("index last: " + nums[size(nums) - 1])

    var map = <>
    println("missing key: " + map["missing"])
}


fun main() {
    var nums = []
    println("empty size: " + size(nums))
    if (size(nums) == 0) {
        println("no max on empty list")
    }

    var divisor = 0
    if (divisor == 0) {
        println("skip division by zero")
    } else {
        println(10 / divisor)
    }

    var maybe = null
    if (maybe == null) {
        println("maybe is null")
    } else {
        println("len: " + size(maybe))
    }

    var items = ["a", "b", "c"]
    var idx = 5
    if (idx >= 0 & idx < size(items)) {
        println("item: " + items[idx])
    } else {
        println("index out of range")
    }
}
