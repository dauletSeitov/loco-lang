# LocoLang

## 1. LocoLang usage

LocoLang source files use the `.ll` extension. The entry point is `fun main()`.

Example (`main.ll`):

```locolang
import std.sort
import std.maxElement

fun main() {
    var numbers = [5, 2, 9, 1]
    println("sorted: " + sort(numbers))
    println("max: " + maxElement(numbers))

    var user = <>
    user["name"] = "Ada"
    user.age = 28
    println(user.name + " is " + user["age"])
}
```

Common syntax:
- Variables: `var x = 10`
- Functions: `fun add(a, b) { return a + b }`
- Lists: `[1, 2, 3]`
- Structures (maps): `<name: "Ada", age: 28>` or `<"name": "Ada">`
- Conditionals: `if (...) { ... } else { ... }`
- Loops: `for (i = 0; i < size(list); i = i + 1) { ... }`
- Imports: `import std.sort` or `import std.filter as myFilter`

Run from the LL folder (default root is `./`):

```bash
java -jar locolang-<version>.jar /path/to/ll
```

## 2. About this interpreter

This project is a small Java interpreter for LocoLang:
- Hand-written lexer and recursive-descent parser (`src/analyze/Lexer.java`, `src/analyze/Parser.java`).
- AST evaluator with dynamic types (numbers, strings, booleans, lists, and structures).
- Built-ins: `println`, `size`, `readln`, `toNumber`, `toString`, `typeOf`.
- Module system: `import module.function` loads `<module>.ll`.

### Standard library (std)

`std` is resolved in this order:
1) `<llRoot>/std.ll` (next to your `main.ll`)
2) `./resource/std.ll` (project fallback)
3) Bundled `/std.ll` inside the jar (built from `resource/std.ll`)

This means users can run their own `main.ll` without shipping `std.ll` as long as they use your jar.

### Build

Build a versioned jar (defaults to `SNAPSHOT`):

```bash
bash build.sh            # locolang-SNAPSHOT.jar
bash build.sh 1.0.0      # locolang-1.0.0.jar
```

### Flags / run modes

Console mode (default):

```bash
java -jar locolang-<version>.jar /path/to/ll
```

## Dev Notes

- Placeholder change for connectivity test.
