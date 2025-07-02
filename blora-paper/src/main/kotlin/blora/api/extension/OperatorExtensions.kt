package blora.api.extension

import org.bukkit.util.Vector

// org.bukkit.util.Vector start

operator fun Vector.plus(another: Vector): Vector {
    return this.add(another)
}

operator fun Vector.minus(another: Vector): Vector {
    return this.subtract(another)
}

operator fun Vector.times(another: Vector): Vector {
    return this.multiply(another)
}

operator fun Vector.times(value: Int): Vector {
    return this.multiply(value)
}

operator fun Vector.times(value: Float): Vector {
    return this.multiply(value)
}

operator fun Vector.times(value: Double): Vector {
    return this.multiply(value)
}

operator fun Vector.div(another: Vector): Vector {
    return this.divide(another)
}

operator fun Vector.div(value: Int): Vector {
    return this.divide(Vector(value, value, value))
}

operator fun Vector.div(value: Float): Vector {
    return this.divide(Vector(value, value, value))
}

operator fun Vector.div(value: Double): Vector {
    return this.divide(Vector(value, value, value))
}

// org.bukkit.util.Vector end