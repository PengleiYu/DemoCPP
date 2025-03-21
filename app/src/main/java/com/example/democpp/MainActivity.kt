package com.example.democpp

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.democpp.databinding.ActivityMainBinding
import com.example.democpp.opengl.TriangleRenderer

class MainActivity : AppCompatActivity() {
    private val renderer = TriangleRenderer()

    private lateinit var binding: ActivityMainBinding
    private lateinit var glSurfaceView: GLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Example of a call to a native method
        binding.sampleText.text = stringFromJNI()

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

    /**
     * A native method that is implemented by the 'democpp' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String
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

    companion object {
        // Used to load the 'democpp' library on application startup.
        init {
            System.loadLibrary("democpp")
        }
    }
}