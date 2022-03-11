package wtf.jaren.aero.spigot.utils

object MathUtils {
    fun gcd(a: Int, b: Int): Int {
        var la = a
        var lb = b
        while (b > 0) {
            val temp = lb
            lb = la % lb // % is remainder
            la = temp
        }
        return la
    }

    fun gcd(input: IntArray): Int {
        var result = input[0]
        for (i in 1 until input.size) result = gcd(result, input[i])
        return result
    }

    fun lcm(a: Int, b: Int): Int {
        return a * (b / gcd(a, b))
    }

    fun lcm(input: IntArray): Int {
        var result = input[0]
        for (i in 1 until input.size) result = lcm(result, input[i])
        return result
    }
}