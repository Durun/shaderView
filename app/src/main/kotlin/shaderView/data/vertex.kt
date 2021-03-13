package shaderView.data

import shaderView.FLOAT_BYTES

data class Vertex(
	val position: Vec3<Float>,
	val normal: Vec3<Float>,
	val color: Vec4<Float>,
	val textureCoord: Vec2<Float>
) {
	companion object {
		const val OFFSET_NORMAL = 3 * FLOAT_BYTES
		const val OFFSET_COLOR = 6 * FLOAT_BYTES
		const val OFFSET_TEXCOORD = 10 * FLOAT_BYTES
	}
}

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