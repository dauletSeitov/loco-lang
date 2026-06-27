
fun main(){

    var numbers = ["hello", "it's", "me"]

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


fun main() {
    var user = <>
    user["name"] = "alex"
    user["age"] = 30
    user["address"] = <>
    user["address"]["city"] = "almaty"
    user["address"]["zip"] = "050000"

    user["tags"] = ["dev", "lang", "backend"]

    println("name: " + user.name)
    println("city: " + user.address.city)
    println("tags: " + user.tags)

    user["tags"] = user["tags"] + ["compiler"]
    println("tags updated: " + user.tags)
}


fun main() {
    var profile = <>
    profile["name"] = "sara"
    profile["age"] = 28
    profile["address"] = <>
    profile["address"]["city"] = "almaty"
    profile["address"]["zip"] = "050000"

    println("name: " + profile.name)
    println("city: " + profile.address.city)

    var visits = <>
    var pages = ["home", "docs", "home", "pricing", "docs", "home"]
    for (i = 0; i < size(pages); i = i + 1) {
        var key = pages[i]
        if (visits[key] == null) {
            visits[key] = 1
        } else {
            visits[key] = visits[key] + 1
        }
    }
    println("visits: " + visits)

    var settings = <>
    if (settings["theme"] == null) {
        settings["theme"] = "light"
    }
    println("theme: " + settings.theme)
}
