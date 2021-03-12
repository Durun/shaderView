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
)

data class Vec2<T : Float>(
	val x: T,
	val y: T
)