package com.example.democpp.opengl

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class TriangleRenderer : GLSurfaceView.Renderer {
    
    private val vertexShaderCode =
        "#version 300 es\n" +
        "in vec3 vPosition;" +
        "void main() {" +
        "  gl_Position = vec4(vPosition, 1.0);" +
        "}"

    private val fragmentShaderCode =
        "#version 300 es\n" +
        "precision mediump float;" +
        "out vec4 fragColor;" +
        "void main() {" +
        "  fragColor = vec4(1.0, 0.5, 0.2, 1.0);" + // 橙色
        "}"

    private val vertexCoords = floatArrayOf(
        0.0f, 0.5f, 0.0f,   // 顶点
        -0.5f, -0.5f, 0.0f, // 左下
        0.5f, -0.5f, 0.0f   // 右下
    )

    private var program = 0
    private var positionHandle = 0

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, shaderCode)
        GLES30.glCompileShader(shader)
        return shader
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        
        val vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES30.glCreateProgram()
        GLES30.glAttachShader(program, vertexShader)
        GLES30.glAttachShader(program, fragmentShader)
        GLES30.glLinkProgram(program)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        
        GLES30.glUseProgram(program)
        
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