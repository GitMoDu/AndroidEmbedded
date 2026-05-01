package com.dogecoding.android_components.custom_views.orientation_visualizer

import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.AttributeSet
import android.util.Log
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import java.util.Locale
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * A GLSurfaceView that visualizes 3D orientation using an OBJ model.
 * Supports automatic centering, scaling, and runtime model loading.
 */
open class OrientationVisualizerView(context: Context, attrs: AttributeSet? = null) :
    GLSurfaceView(context, attrs) {

    protected val renderer = ModelRenderer()

    init {
        setEGLContextClientVersion(3)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        holder.setFormat(PixelFormat.TRANSLUCENT)
        setZOrderOnTop(true)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    /**
     * Updates the orientation of the displayed model.
     * @param pitch In degrees.
     * @param roll In degrees.
     * @param yaw In degrees.
     */
    open fun updateOrientation(pitch: Float, roll: Float, yaw: Float) {
        renderer.setRotation(pitch, roll, yaw)
    }

    /**
     * Updates the translation of the model.
     */
    open fun updateTranslation(x: Float, y: Float, z: Float) {
        renderer.setTranslation(x, y, z)
    }

    /**
     * Updates the scale multiplier for the model.
     */
    fun updateScale(scale: Float) {
        renderer.setScale(scale)
    }

    /**
     * Updates the global alpha (transparency) of the model.
     */
    fun updateAlpha(alpha: Float) {
        renderer.setAlpha(alpha)
    }

    /**
     * Updates the camera position.
     */
    fun updateCamera(
        eyeX: Float,
        eyeY: Float,
        eyeZ: Float,
        centerX: Float = 0f,
        centerY: Float = 0f,
        centerZ: Float = 0f
    ) {
        renderer.setCamera(eyeX, eyeY, eyeZ, centerX, centerY, centerZ)
    }

    /**
     * Updates the lighting parameters.
     * @param lightDir The direction of the light source [x, y, z].
     * @param lightColor The color of the light [r, g, b].
     * @param ambient The ambient light strength (0.0 to 1.0).
     */
    fun updateLighting(
        lightDir: FloatArray? = null,
        lightColor: FloatArray? = null,
        ambient: Float? = null
    ) {
        renderer.setLighting(lightDir, lightColor, ambient)
    }

    /**
     * Loads an OBJ model from a raw resource.
     * Loading is deferred to the GL thread to ensure a valid context.
     */
    fun setModelResource(resId: Int) {
        renderer.setPendingModel(resId)
    }

    protected inner class ModelRenderer : Renderer {
        private var pitch = 0f
        private var roll = 0f
        private var yaw = 0f
        private var tx = 0f
        private var ty = 0f
        private var tz = 0f
        private var modelScale = 1f
        private var modelAlpha = 1f

        private var eyeX = 0f
        private var eyeY = 0f
        private var eyeZ = -6f
        private var centerX = 0f
        private var centerY = 0f
        private var centerZ = 0f

        private var lightDir = floatArrayOf(-1.0f, 1.0f, 1.0f)
        private var lightColor = floatArrayOf(1.0f, 1.0f, 0.95f)
        private var ambient = 0.3f

        private var model: GLESModel? = null
        private var pendingResId: Int? = null

        private val vPMatrix = FloatArray(16)
        private val projectionMatrix = FloatArray(16)
        private val viewMatrix = FloatArray(16)

        fun setPendingModel(resId: Int) {
            synchronized(this) {
                pendingResId = resId
            }
        }

        fun setRotation(p: Float, r: Float, y: Float) {
            pitch = p
            roll = r
            yaw = y
        }

        fun setTranslation(x: Float, y: Float, z: Float) {
            tx = x
            ty = y
            tz = z
        }

        fun setScale(s: Float) {
            modelScale = s
        }

        fun setAlpha(a: Float) {
            modelAlpha = a
        }

        fun setCamera(ex: Float, ey: Float, ez: Float, cx: Float, cy: Float, cz: Float) {
            eyeX = ex
            eyeY = ey
            eyeZ = ez
            centerX = cx
            centerY = cy
            centerZ = cz
        }

        fun setLighting(dir: FloatArray?, color: FloatArray?, amb: Float?) {
            dir?.let { lightDir = it }
            color?.let { lightColor = it }
            amb?.let { ambient = it }
        }

        override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
            GLES30.glClearColor(0f, 0f, 0f, 0f)
            GLES30.glEnable(GLES30.GL_DEPTH_TEST)
            GLES30.glEnable(GLES30.GL_BLEND)
            GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
        }

        override fun onDrawFrame(unused: GL10) {
            val resToLoad = synchronized(this) {
                val res = pendingResId
                pendingResId = null
                res
            }

            if (resToLoad != null) {
                try {
                    model = GLESModel(context.resources.openRawResource(resToLoad))
                } catch (e: Exception) {
                    Log.e("OrientationVisualizer", "Failed to load model: ${e.message}")
                }
            }

            if (model == null) {
                model = GLESModel()
            }

            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

            // Setup Camera
            Matrix.setLookAtM(
                viewMatrix,
                0,
                eyeX,
                eyeY,
                eyeZ,
                centerX,
                centerY,
                centerZ,
                0f,
                1.0f,
                0.0f
            )
            Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

            // Apply Transformations (T * R * S)
            val modelMatrix = FloatArray(16)
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, tx, ty, tz)
            Matrix.rotateM(modelMatrix, 0, yaw, 0f, 1f, 0f)
            Matrix.rotateM(modelMatrix, 0, pitch, 1f, 0f, 0f)
            Matrix.rotateM(modelMatrix, 0, roll, 0f, 0f, 1f)
            Matrix.scaleM(modelMatrix, 0, modelScale, modelScale, modelScale)

            val mvpMatrix = FloatArray(16)
            Matrix.multiplyMM(mvpMatrix, 0, vPMatrix, 0, modelMatrix, 0)
            model?.draw(mvpMatrix, modelMatrix, modelAlpha, lightDir, lightColor, ambient)
        }

        override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
            GLES30.glViewport(0, 0, width, height)
            val ratio = width.toFloat() / height.toFloat()
            Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 15f)
        }
    }

    protected class GLESModel(inputStream: InputStream? = null) {
        companion object {
            private const val VERTEX_SHADER =
                "#version 300 es\n" +
                        "uniform mat4 uMVPMatrix;\n" +
                        "uniform mat4 uModelMatrix;\n" +
                        "in vec4 vPosition;\n" +
                        "in vec4 vColor;\n" +
                        "in vec3 vNormal;\n" +
                        "out vec4 vVaryingColor;\n" +
                        "out vec3 vTransformedNormal;\n" +
                        "void main() {\n" +
                        "  gl_Position = uMVPMatrix * vPosition;\n" +
                        "  vVaryingColor = vColor;\n" +
                        "  vTransformedNormal = mat3(uModelMatrix) * vNormal;\n" +
                        "}\n"

            private const val FRAGMENT_SHADER =
                "#version 300 es\n" +
                        "precision mediump float;\n" +
                        "uniform float uAlpha;\n" +
                        "uniform vec3 uLightDir;\n" +
                        "uniform vec3 uLightColor;\n" +
                        "uniform float uAmbient;\n" +
                        "in vec4 vVaryingColor;\n" +
                        "in vec3 vTransformedNormal;\n" +
                        "out vec4 fragColor;\n" +
                        "void main() {\n" +
                        "  vec3 normal = normalize(vTransformedNormal);\n" +
                        "  float diffuse = max(dot(normal, normalize(uLightDir)), 0.0);\n" +
                        "  vec3 finalColor = vVaryingColor.rgb * (diffuse * uLightColor + uAmbient);\n" +
                        "  fragColor = vec4(finalColor, vVaryingColor.a * uAlpha);\n" +
                        "}\n"
        }

        private lateinit var vertexBuffer: FloatBuffer
        private lateinit var colorBuffer: FloatBuffer
        private lateinit var normalBuffer: FloatBuffer
        private lateinit var drawListBuffer: ShortBuffer

        private var program = 0
        private var drawCount = 0
        private var vaoId = 0

        init {
            if (inputStream != null) {
                parseObj(inputStream)
            } else {
                initDefaultCube()
            }

            val vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, VERTEX_SHADER)
            val fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, FRAGMENT_SHADER)

            if (vertexShader != 0 && fragmentShader != 0) {
                program = GLES30.glCreateProgram().also {
                    GLES30.glAttachShader(it, vertexShader)
                    GLES30.glAttachShader(it, fragmentShader)
                    GLES30.glLinkProgram(it)
                }
                setupVao()
            }
        }

        private fun setupVao() {
            val vao = IntArray(1)
            GLES30.glGenVertexArrays(1, vao, 0)
            vaoId = vao[0]
            GLES30.glBindVertexArray(vaoId)

            val vbo = IntArray(3)
            GLES30.glGenBuffers(3, vbo, 0)

            // Vertices
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo[0])
            GLES30.glBufferData(
                GLES30.GL_ARRAY_BUFFER,
                vertexBuffer.capacity() * 4,
                vertexBuffer,
                GLES30.GL_STATIC_DRAW
            )
            val posHandle = GLES30.glGetAttribLocation(program, "vPosition")
            GLES30.glEnableVertexAttribArray(posHandle)
            GLES30.glVertexAttribPointer(posHandle, 3, GLES30.GL_FLOAT, false, 0, 0)

            // Colors
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo[1])
            GLES30.glBufferData(
                GLES30.GL_ARRAY_BUFFER,
                colorBuffer.capacity() * 4,
                colorBuffer,
                GLES30.GL_STATIC_DRAW
            )
            val colHandle = GLES30.glGetAttribLocation(program, "vColor")
            GLES30.glEnableVertexAttribArray(colHandle)
            GLES30.glVertexAttribPointer(colHandle, 4, GLES30.GL_FLOAT, false, 0, 0)

            // Normals
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo[2])
            GLES30.glBufferData(
                GLES30.GL_ARRAY_BUFFER,
                normalBuffer.capacity() * 4,
                normalBuffer,
                GLES30.GL_STATIC_DRAW
            )
            val normalHandle = GLES30.glGetAttribLocation(program, "vNormal")
            GLES30.glEnableVertexAttribArray(normalHandle)
            GLES30.glVertexAttribPointer(normalHandle, 3, GLES30.GL_FLOAT, false, 0, 0)

            // Indices
            val ibo = IntArray(1)
            GLES30.glGenBuffers(1, ibo, 0)
            GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, ibo[0])
            GLES30.glBufferData(
                GLES30.GL_ELEMENT_ARRAY_BUFFER,
                drawListBuffer.capacity() * 2,
                drawListBuffer,
                GLES30.GL_STATIC_DRAW
            )

            GLES30.glBindVertexArray(0)
        }

        private fun initDefaultCube() {
            val coords = floatArrayOf(
                -1f, 1f, 1f, -1f, -1f, 1f, 1f, -1f, 1f, 1f, 1f, 1f,
                -1f, 1f, -1f, -1f, -1f, -1f, 1f, -1f, 1f, 1f, -1f
            )
            val colors = floatArrayOf(
                1f, 0f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 0f, 1f, 1f, 1f, 1f, 0f, 1f,
                1f, 0f, 1f, 1f, 0f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, .5f, .5f, .5f, 1f
            )
            val order = shortArrayOf(
                0, 1, 2, 0, 2, 3, 4, 5, 6, 4, 6, 7, 4, 0, 3, 4, 3, 7, 5, 1,
                2, 5, 2, 6, 4, 5, 1, 4, 1, 0, 3, 2, 6, 3, 6, 7
            )
            setupBuffers(coords, colors, order)
        }

        private fun parseObj(inputStream: InputStream) {
            val vertices = mutableListOf<Float>()
            val indices = mutableListOf<Short>()
            var minX = Float.MAX_VALUE;
            var maxX = Float.MIN_VALUE
            var minY = Float.MAX_VALUE;
            var maxY = Float.MIN_VALUE
            var minZ = Float.MAX_VALUE;
            var maxZ = Float.MIN_VALUE

            try {
                inputStream.bufferedReader().use { reader ->
                    reader.forEachLine { line ->
                        val trimmed = line.trim()
                        if (trimmed.isEmpty() || trimmed.startsWith("#")) return@forEachLine
                        val parts = trimmed.split(Regex("\\s+")).filter { it.isNotEmpty() }
                        if (parts.size < 2) return@forEachLine

                        when (parts[0].lowercase(Locale.ROOT)) {
                            "v" -> if (parts.size >= 4) {
                                val x = parts[1].toFloat();
                                val y = parts[2].toFloat();
                                val z = parts[3].toFloat()
                                vertices.add(x); vertices.add(y); vertices.add(z)
                                if (x < minX) minX = x; if (x > maxX) maxX = x
                                if (y < minY) minY = y; if (y > maxY) maxY = y
                                if (z < minZ) minZ = z; if (z > maxZ) maxZ = z
                            }

                            "f" -> if (parts.size >= 4) {
                                val fIndices =
                                    parts.drop(1).map { (it.split("/")[0].toInt() - 1).toShort() }
                                for (i in 1 until fIndices.size - 1) {
                                    indices.add(fIndices[0]); indices.add(fIndices[i]); indices.add(
                                        fIndices[i + 1]
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("OrientationVisualizer", "OBJ Parse error: ${e.message}")
            }

            if (vertices.isEmpty() || indices.isEmpty()) {
                initDefaultCube()
                return
            }

            val centerX = (minX + maxX) / 2f
            val centerY = (minY + maxY) / 2f
            val centerZ = (minZ + maxZ) / 2f
            val maxDim = maxOf(maxX - minX, maxOf(maxY - minY, maxZ - minZ))
            val scaleFactor = if (maxDim != 0f) 3.0f / maxDim else 1.0f

            val normalized = FloatArray(vertices.size)
            for (i in vertices.indices step 3) {
                normalized[i] = (vertices[i] - centerX) * scaleFactor
                normalized[i + 1] = (vertices[i + 1] - centerY) * scaleFactor
                normalized[i + 2] = (vertices[i + 2] - centerZ) * scaleFactor
            }

            val colors = FloatArray((vertices.size / 3) * 4) { i ->
                when (i % 4) {
                    0 -> 0.85f; 1 -> 0.85f; 2 -> 0.85f; else -> 1.0f
                }
            }
            setupBuffers(normalized, colors, indices.toShortArray())
        }

        private fun setupBuffers(coords: FloatArray, colors: FloatArray, order: ShortArray) {
            drawCount = order.size

            // Calculate Normals
            val normals = FloatArray(coords.size)
            for (i in order.indices step 3) {
                val i1 = order[i].toInt() * 3
                val i2 = order[i + 1].toInt() * 3
                val i3 = order[i + 2].toInt() * 3

                val v1x = coords[i1];
                val v1y = coords[i1 + 1];
                val v1z = coords[i1 + 2]
                val v2x = coords[i2];
                val v2y = coords[i2 + 1];
                val v2z = coords[i2 + 2]
                val v3x = coords[i3];
                val v3y = coords[i3 + 1];
                val v3z = coords[i3 + 2]

                val ax = v2x - v1x;
                val ay = v2y - v1y;
                val az = v2z - v1z
                val bx = v3x - v1x;
                val by = v3y - v1y;
                val bz = v3z - v1z

                val nx = ay * bz - az * by
                val ny = az * bx - ax * bz
                val nz = ax * by - ay * bx

                normals[i1] += nx; normals[i1 + 1] += ny; normals[i1 + 2] += nz
                normals[i2] += nx; normals[i2 + 1] += ny; normals[i2 + 2] += nz
                normals[i3] += nx; normals[i3 + 1] += ny; normals[i3 + 2] += nz
            }
            for (i in normals.indices step 3) {
                val length =
                    Math.sqrt((normals[i] * normals[i] + normals[i + 1] * normals[i + 1] + normals[i + 2] * normals[i + 2]).toDouble())
                        .toFloat()
                if (length > 0) {
                    normals[i] /= length; normals[i + 1] /= length; normals[i + 2] /= length
                }
            }

            vertexBuffer = ByteBuffer.allocateDirect(coords.size * 4).run {
                order(ByteOrder.nativeOrder()).asFloatBuffer().apply { put(coords).position(0) }
            }
            colorBuffer = ByteBuffer.allocateDirect(colors.size * 4).run {
                order(ByteOrder.nativeOrder()).asFloatBuffer().apply { put(colors).position(0) }
            }
            normalBuffer = ByteBuffer.allocateDirect(normals.size * 4).run {
                order(ByteOrder.nativeOrder()).asFloatBuffer().apply { put(normals).position(0) }
            }
            drawListBuffer = ByteBuffer.allocateDirect(order.size * 2).run {
                order(ByteOrder.nativeOrder()).asShortBuffer().apply { put(order).position(0) }
            }
        }

        fun draw(
            mvpMatrix: FloatArray,
            modelMatrix: FloatArray,
            alpha: Float,
            lightDir: FloatArray,
            lightColor: FloatArray,
            ambient: Float
        ) {
            if (program == 0 || drawCount == 0 || vaoId == 0) return
            GLES30.glUseProgram(program)

            val mvpHandle = GLES30.glGetUniformLocation(program, "uMVPMatrix")
            GLES30.glUniformMatrix4fv(mvpHandle, 1, false, mvpMatrix, 0)

            val modelHandle = GLES30.glGetUniformLocation(program, "uModelMatrix")
            GLES30.glUniformMatrix4fv(modelHandle, 1, false, modelMatrix, 0)

            val alphaHandle = GLES30.glGetUniformLocation(program, "uAlpha")
            GLES30.glUniform1f(alphaHandle, alpha)

            val lightDirHandle = GLES30.glGetUniformLocation(program, "uLightDir")
            GLES30.glUniform3fv(lightDirHandle, 1, lightDir, 0)

            val lightColorHandle = GLES30.glGetUniformLocation(program, "uLightColor")
            GLES30.glUniform3fv(lightColorHandle, 1, lightColor, 0)

            val ambientHandle = GLES30.glGetUniformLocation(program, "uAmbient")
            GLES30.glUniform1f(ambientHandle, ambient)

            GLES30.glBindVertexArray(vaoId)
            GLES30.glDrawElements(GLES30.GL_TRIANGLES, drawCount, GLES30.GL_UNSIGNED_SHORT, 0)
            GLES30.glBindVertexArray(0)
        }

        private fun loadShader(type: Int, code: String): Int {
            return GLES30.glCreateShader(type).also { shader ->
                if (shader == 0) return@also
                GLES30.glShaderSource(shader, code)
                GLES30.glCompileShader(shader)
                val compiled = IntArray(1)
                GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0)
                if (compiled[0] == 0) {
                    Log.e(
                        "OrientationVisualizer",
                        "Shader Error: ${GLES30.glGetShaderInfoLog(shader)}"
                    )
                    GLES30.glDeleteShader(shader)
                }
            }
        }
    }
}
