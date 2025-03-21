package com.example.democpp

import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.democpp.databinding.ActivityMainBinding
import com.example.democpp.opengl.TriangleRenderer

class MainActivity : AppCompatActivity() {

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
            setRenderer(TriangleRenderer())
            renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
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

    companion object {
        // Used to load the 'democpp' library on application startup.
        init {
            System.loadLibrary("democpp")
        }
    }
}