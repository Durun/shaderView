package shaderView.data

data class Vertex(
	val position: Vec3<Float>,
	val normal: Vec3<Float>,
	val color: Vec4<Float>,
	val textureCoord: Vec2<Float>
)

fun List<Vertex>.toFloatArray(): FloatArray {
	return this.flatMap {
		listOf(
			it.position.x,
			it.position.y,
			it.position.z,
			it.normal.x,
			it.normal.y,
			it.normal.z,
			it.color.x,
			it.color.y,
			it.color.z,
			it.color.w,
			it.textureCoord.x,
			it.textureCoord.y
		)
	}.toFloatArray()
}