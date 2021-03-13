package shaderView

import com.jogamp.opengl.GL2ES2
import shaderView.data.*
import kotlin.math.cos
import kotlin.math.sin

fun makePlane(gl: GL2ES2, texture: TextureImage, shader: Shader): Object3D {
	val normal = Vec3(0f, 0f, -1f)
	val red = Vec4(1f, 0f, 0f, 1f)
	val vertice = listOf(
		Vertex(Vec3(-1f, -1f, 0f), normal, red, Vec2(0f, 0f)),
		Vertex(Vec3(1f, -1f, 0f), normal, red, Vec2(1f, 0f)),
		Vertex(Vec3(1f, 1f, 0f), normal, red, Vec2(1f, 1f)),
		Vertex(Vec3(-1f, 1f, 0f), normal, red, Vec2(0f, 1f))
	)
	val front = Polygon(vertice)
	val back = Polygon(vertice.reversed())
	val polygons = front + back
	return TexturedObject(gl, polygons, texture, shader)
}

fun PolygonSet.textured(gl: GL2ES2, texture: TextureImage, shader: Shader): Object3D {
	return TexturedObject(gl, this, texture, shader)
}

fun makeCylinder(n: Int, radius: Float, height: Float): PolygonSet {
	val angles = (0 until n).map { (it * 2 * Math.PI / n) }
	val topVertices = angles.map { angle ->
		Vertex(
			position = Vec3(
				x = (radius * cos(angle)).toFloat(),
				y = (radius * sin(angle)).toFloat(),
				z = -height / 2f
			),
			normal = Vec3(0f, 0f, -1f),
			color = Vec4(0f, 1f, 0f, 1f),
			textureCoord = Vec2(
				x = (-0.5 * cos(angle) + 0.5f).toFloat(),
				y = (0.5 * sin(angle) + 0.5f).toFloat()
			)
		)
	}
	val bottomVertices = topVertices.map {
		it.copy(
			position = it.position.copy(z = -it.position.z),
			normal = -it.normal
		)
	}
	val top = Polygon(topVertices.asReversed())
	val bottom = Polygon(bottomVertices)
	val side = topVertices.zip(bottomVertices)
		.flatMap { (vTop, vBottom) -> listOf(vTop, vBottom) }
		.let { Sheet(it, looped = true) }
	return top + bottom + side
}