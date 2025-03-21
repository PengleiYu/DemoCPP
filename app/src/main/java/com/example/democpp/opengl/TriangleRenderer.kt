package com.example.democpp.opengl

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class TriangleRenderer : GLSurfaceView.Renderer {
    companion object {
        private const val TAG = "TriangleRenderer"
    }
    // 新增旋转相关变量
    private var rotationX = 0f
    private var rotationY = 0f
    private val rotationLock = Any() // 用于线程同步

    private val vertexShaderCode = """
        #version 300 es
        uniform mat4 uMVPMatrix;
        in vec3 vPosition;
        void main() {
            gl_Position = uMVPMatrix * vec4(vPosition, 1.0);
        }
    """.trimIndent()

    private val fragmentShaderCode =
        "#version 300 es\n" +
                "precision mediump float;" +
                "out vec4 fragColor;" +
                "void main() {" +
                "  fragColor = vec4(1.0, 0.5, 0.2, 1.0);" + // 橙色
                "}"
    // 新增矩阵相关变量
    private var mvpMatrixHandle = 0
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val vertexCoords = floatArrayOf(
        0.0f, 0.5f, 0.0f,   // 顶点
        -0.5f, -0.5f, 0.0f, // 左下
        0.5f, -0.5f, 0.0f   // 右下
    )

    private var program = 0
    private var positionHandle = 0

    // 添加旋转方法（线程安全）
    fun addRotation(dx: Float, dy: Float) {
        Log.d(TAG, "addRotation() called with: dx = $dx, dy = $dy")
        synchronized(rotationLock) {
            rotationY += dx
            rotationX += dy
        }
    }
    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, shaderCode)
        GLES30.glCompileShader(shader)
        val compileStatus = IntArray(1)
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            Log.e(TAG, "loadShader: Shader compile error: ${GLES30.glGetShaderInfoLog(shader)}")
        }
        return shader
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.d(TAG, "onSurfaceCreated() called with: gl = $gl, config = $config")
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        val vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES30.glCreateProgram()
        GLES30.glAttachShader(program, vertexShader)
        GLES30.glAttachShader(program, fragmentShader)
        GLES30.glLinkProgram(program)

        // 初始化矩阵为单位矩阵
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.setIdentityM(viewMatrix, 0)
        Matrix.setIdentityM(projectionMatrix, 0)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.d(TAG, "onSurfaceChanged() called with: gl = $gl, width = $width, height = $height")
        GLES30.glViewport(0, 0, width, height)

        // 创建投影矩阵（透视投影）
        val ratio = width.toFloat() / height
        Matrix.perspectiveM(projectionMatrix, 0, 45.0f, ratio, 0.1f, 100.0f)
        // 设置相机位置（View矩阵）
        Matrix.setLookAtM(viewMatrix, 0,
            0f, 0f, 3f,  // 相机位置（z=3）
            0f, 0f, 0f,  // 观察点
            0f, 1.0f, 0f // 上方向
        )
    }

    override fun onDrawFrame(gl: GL10?) {
        Log.d(TAG, "onDrawFrame() called with: gl = $gl")
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        // 获取旋转角度（线程安全）
        val (rx, ry) = synchronized(rotationLock) {
            rotationX to rotationY
        }

        // 计算模型矩阵
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 0f, 0f, -2f) // 将模型移入视野
        Matrix.rotateM(modelMatrix, 0, rx, 1f, 0f, 0f) // X轴旋转
        Matrix.rotateM(modelMatrix, 0, ry, 0f, 1f, 0f) // Y轴旋转
        // 计算MVP矩阵：Projection * View * Model
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)


        GLES30.glUseProgram(program)

        mvpMatrixHandle = GLES30.glGetUniformLocation(program, "uMVPMatrix")
        GLES30.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        positionHandle = GLES30.glGetAttribLocation(program, "vPosition")
        GLES30.glEnableVertexAttribArray(positionHandle)

        val vertexBuffer = java.nio.ByteBuffer
            .allocateDirect(vertexCoords.size * 4)
            .order(java.nio.ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(vertexCoords)
                position(0)
            }

        GLES30.glVertexAttribPointer(
            positionHandle,
            3,
            GLES30.GL_FLOAT,
            false,
            3 * 4,
            vertexBuffer
        )

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3)
        GLES30.glDisableVertexAttribArray(positionHandle)
    }
}