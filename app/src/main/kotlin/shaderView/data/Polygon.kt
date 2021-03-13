package shaderView.data

data class Polygon(
    val vertice: List<Vertex>
) {
    val vertexArray: FloatArray
    val elementArray: IntArray

    init {
        val buffer = vertice.distinct()
        val elements = vertice.map { buffer.indexOf(it) }

        // convert to triangles
        val triangles = elements.mapIndexedNotNull { i, v ->
            when (i) {
                0 -> null
                elements.lastIndex -> null
                else -> {
                    val next = elements[i + 1]
                    listOf(elements.first(), v, next)
                }
            }
        }

        elementArray = triangles.flatten().toIntArray()
        vertexArray = buffer.toFloatArray()
    }

    companion object {
        fun of(vararg vertice: Vertex): Polygon = Polygon(vertice.toList())
    }
}