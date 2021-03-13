package shaderView.data

data class Vec4<T : Float>(
	val x: T,
	val y: T,
	val z: T,
	val w: T
)

data class Vec3<T : Float>(
	val x: T,
	val y: T,
	val z: T
) {
	operator fun unaryMinus(): Vec3<Float> = Vec3(-x, -y, -z)
	operator fun plus(other: Vec3<Float>): Vec3<Float> = Vec3(x + other.x, y + other.y, z + other.z)
	operator fun div(other: Float): Vec3<Float> = Vec3(x / other, y / other, z / other)
}

data class Vec2<T : Float>(
	val x: T,
	val y: T
) {
	operator fun unaryMinus(): Vec2<Float> = Vec2(-x, -y)
}