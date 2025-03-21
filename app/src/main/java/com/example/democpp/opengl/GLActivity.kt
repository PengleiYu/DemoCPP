package com.example.democpp.opengl

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.democpp.databinding.ActivityGlBinding

class GLActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGlBinding
    private lateinit var glSurfaceView: GLSurfaceView
    private val renderer = TriangleRenderer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGlBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        glSurfaceView = binding.glSurface.apply {
            setEGLContextClientVersion(3)
            setRenderer(renderer)
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY // 改为持续渲染
            setupTouchListener(this)
        }
    }
    override fun onPause() {
        super.onPause()
        glSurfaceView.onPause()
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
    }
    private fun setupTouchListener(view: View) {
        var previousX = 0f
        var previousY = 0f

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    previousX = event.x
                    previousY = event.y
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = event.x - previousX
                    val dy = event.y - previousY

                    // 传递触摸增量给Renderer
                    renderer.addRotation(dx * 0.5f, dy * 0.5f)

                    previousX = event.x
                    previousY = event.y
                    true
                }

                else -> false
            }
        }
    }
}