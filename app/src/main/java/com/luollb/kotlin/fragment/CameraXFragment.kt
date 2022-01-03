package com.luollb.kotlin.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.os.*
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.io.File
import java.lang.Exception
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraXFragment : Fragment() {

    private var displayId = -1

    private lateinit var previewView: PreviewView

    private lateinit var cameraExecutor: ExecutorService

    private lateinit var preview: Preview

    private lateinit var camera: Camera

    private var imageCapture: ImageCapture? = null

    private var videoCapture: VideoCapture? = null

    private val displayManager by lazy {
        requireContext().getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    }

    private val handler = object : Handler(Looper.getMainLooper()) {
        @SuppressLint("RestrictedApi")
        override fun handleMessage(msg: Message) {
//            val bitmap = previewView.bitmap
//            println("bitmap = ${bitmap != null}")
//            if (bitmap != null) {
//                println("width = ${bitmap.width} , height = ${bitmap.height}")
//            }
            if (msg.what != 22) {
                videoCapture()
                Toast.makeText(context!!, "开始录制", Toast.LENGTH_SHORT).show()
                sendMessageDelayed(obtainMessage(22), 10000)
            } else {
                videoCapture?.stopRecording()
                Toast.makeText(context!!, "录制结束", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        previewView = PreviewView(requireContext())
        val params = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT
        )
        previewView.layoutParams = params
        return previewView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()

        //displayId = previewView.display.displayId

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            preview = Preview.Builder()
                .build()

            imageCapture = ImageCapture.Builder()
                .build()

            videoCapture = VideoCapture.Builder()
                .build()

            val cameraSelector =
                CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()

            try {
                cameraProvider.unbindAll()

                camera = cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture,
                    videoCapture
                )


                preview.setSurfaceProvider(previewView.surfaceProvider)
            } catch (exc: Exception) {
                println("Exception ${exc}")
            }

        }, ContextCompat.getMainExecutor(requireContext()))

    }

    override fun onResume() {
        super.onResume()
        //handler.sendMessageDelayed(handler.obtainMessage(), 5000)
    }


    @SuppressLint("RestrictedApi")
    private fun videoCapture() {
        if (videoCapture == null) {
            return
        }
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val videoFile = File(activity?.getExternalFilesDir(null), "cameraVideo.mp4")

        val outFile = VideoCapture.OutputFileOptions.Builder(videoFile)
            .build()

        videoCapture?.startRecording(
            outFile,
            cameraExecutor,
            object : VideoCapture.OnVideoSavedCallback {
                override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {
                    println("录制视频失败")
                }

                override fun onVideoSaved(outputFileResults: VideoCapture.OutputFileResults) {
                    println("视频保存成功")
                }
            })
    }

    /**
     * 拍照
     */
    private fun takePicture() {
        imageCapture?.let {
            val photoFile =
                File(activity?.getExternalFilesDir(null), "PIC_FILE_NAME_CAMERAX.jpg")

            val outputFile = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            imageCapture!!.takePicture(
                outputFile,
                cameraExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exception: ImageCaptureException) {
                        println("imageCapture onError  失败")
                    }

                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        println("imageCapture onImageSaved 成功    Thread=${Thread.currentThread()}")

                    }
                })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        cameraExecutor.shutdown()
    }
}