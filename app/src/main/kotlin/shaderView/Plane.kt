package shaderView

import com.jogamp.opengl.GL2ES2
import shaderView.data.*

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
