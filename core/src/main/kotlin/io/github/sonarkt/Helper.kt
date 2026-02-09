package io.github.sonarkt

/**
 * 中間層の関数
 * testHelper から呼ばれ、Calculator.add を呼ぶ
 */
fun helperB() {
    val calc = Calculator()
    calc.add(1, 2)
}
