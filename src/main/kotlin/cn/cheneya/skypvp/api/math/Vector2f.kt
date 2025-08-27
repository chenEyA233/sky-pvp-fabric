package cn.cheneya.skypvp.api.math

import kotlin.math.acos
import kotlin.math.sqrt

class Vector2f {
    var x: Float = 0f
    var y: Float = 0f

    constructor()

    constructor(x: Float, y: Float) {
        this.set(x, y)
    }

    fun set(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    fun length(): Float {
        return sqrt(this.lengthSquared().toDouble()).toFloat()
    }

    fun lengthSquared(): Float {
        return this.x * this.x + this.y * this.y
    }

    fun translate(x: Float, y: Float): Vector2f {
        this.x += x
        this.y += y
        return this
    }

    fun negate(dest: Vector2f?): Vector2f {
        var dest = dest
        if (dest == null) {
            dest = Vector2f()
        }

        dest.x = -this.x
        dest.y = -this.y
        return dest
    }

    fun normalise(dest: Vector2f?): Vector2f {
        var dest = dest
        val l = this.length()
        if (dest == null) {
            dest = Vector2f(this.x / l, this.y / l)
        } else {
            dest.set(this.x / l, this.y / l)
        }

        return dest
    }

    override fun toString(): String {
        return "Vector2f[" + this.x + ", " + this.y + "]"
    }



    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        } else if (obj == null) {
            return false
        } else if (this.javaClass != obj.javaClass) {
            return false
        } else {
            val other = obj as Vector2f
            return this.x == other.x && this.y == other.y
        }
    }

    companion object {
        fun dot(left: Vector2f, right: Vector2f): Float {
            return left.x * right.x + left.y * right.y
        }

        fun angle(a: Vector2f, b: Vector2f): Float {
            var dls = dot(a, b) / (a.length() * b.length())
            if (dls < -1.0f) {
                dls = -1.0f
            } else if (dls > 1.0f) {
                dls = 1.0f
            }

            return acos(dls.toDouble()).toFloat()
        }

        fun add(left: Vector2f, right: Vector2f, dest: Vector2f?): Vector2f {
            if (dest == null) {
                return Vector2f(left.x + right.x, left.y + right.y)
            } else {
                dest.set(left.x + right.x, left.y + right.y)
                return dest
            }
        }

        fun sub(left: Vector2f, right: Vector2f, dest: Vector2f?): Vector2f {
            if (dest == null) {
                return Vector2f(left.x - right.x, left.y - right.y)
            } else {
                dest.set(left.x - right.x, left.y - right.y)
                return dest
            }
        }
    }
}
