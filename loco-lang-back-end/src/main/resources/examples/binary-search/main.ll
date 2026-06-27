
fun main() {
    var array = [5, 2, 9, 1, 5, 6]
    sort(array)
    println("array: " + array)
    var ok = binarySearch(array, 6)
    println("binarySearch: " + ok)
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
