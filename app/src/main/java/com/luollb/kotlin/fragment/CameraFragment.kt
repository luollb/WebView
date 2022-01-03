package com.luollb.kotlin.fragment

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.ImageFormat
import android.graphics.PixelFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE_MAXIMUM_RESOLUTION
import android.hardware.camera2.params.ExtensionSessionConfiguration
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.media.ImageReader
import android.os.*
import android.view.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.luollb.kotlin.widget.AutoTextureView
import com.luollb.kotlin.widget.CompareSizesByArea
import com.luollb.kotlin.widget.ImageSaver
import java.io.File
import java.util.*
import java.util.concurrent.Executor

class CameraFragment : Fragment(), SurfaceHolder.Callback, TextureView.SurfaceTextureListener {

    private lateinit var view: SurfaceView
    private lateinit var holder: SurfaceHolder

    private lateinit var textureView: AutoTextureView

    private lateinit var cameraManager: CameraManager
    private var cameraDevice: CameraDevice? = null

    private var mCameraId = "0"
    private var mCameraFacing = CameraCharacteristics.LENS_FACING_BACK //默认使用后置摄像头

    private var mFaceDetectMode = CaptureResult.STATISTICS_FACE_DETECT_MODE_OFF

    private var imageReader: ImageReader? = null

    private var mCameraHandler: Handler
    private val handlerThread = HandlerThread("CameraThread").also {
        it.start()
    }

    init {
        mCameraHandler = Handler(handlerThread.looper)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = SurfaceView(requireContext())
        val params = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT
        )
        view.layoutParams = params
        holder = view.holder
        holder.addCallback(this)

        textureView = AutoTextureView(requireContext())
        textureView.surfaceTextureListener = this

        return textureView
    }

    override fun onResume() {
        super.onResume()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        println("surfaceCreated")
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        cameraManager = context?.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        var mCameraCharacteristics: CameraCharacteristics? = null

        for (id in cameraManager.cameraIdList.indices) {
            println("设备中摄像头 $id")
            val cameraCharacteristics = cameraManager.getCameraCharacteristics(id.toString())

            val facing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)
            if (facing == mCameraFacing) {
                println("设备中后视摄像头 $id")
                mCameraId = id.toString()
                mCameraCharacteristics = cameraCharacteristics
                break
            }
        }

        if (mCameraCharacteristics == null) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val rotation = mCameraCharacteristics.get(CameraCharacteristics.LENS_POSE_ROTATION)
            println("rotation = ${rotation?.size}")
        }


        val configurationMap =
            mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

        val outputSizes = configurationMap?.getOutputSizes(ImageFormat.JPEG)

        for (size in outputSizes?.indices!!) {
            println("outputSizes size=${outputSizes[size]}")
        }

        val rect = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)
        println("rect = ${rect.toString()}")

        val faceCount =
            mCameraCharacteristics.get(CameraCharacteristics.STATISTICS_INFO_MAX_FACE_COUNT)
        val faceModes =
            mCameraCharacteristics.get(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES)

        println("faceCount = $faceCount")

        if (faceModes != null) {
            mFaceDetectMode = when {
                faceModes.contains(CaptureRequest.STATISTICS_FACE_DETECT_MODE_FULL) -> CaptureRequest.STATISTICS_FACE_DETECT_MODE_FULL
                faceModes.contains(CaptureRequest.STATISTICS_FACE_DETECT_MODE_SIMPLE) -> CaptureRequest.STATISTICS_FACE_DETECT_MODE_FULL
                else -> CaptureRequest.STATISTICS_FACE_DETECT_MODE_OFF
            }

            if (mFaceDetectMode == CaptureRequest.STATISTICS_FACE_DETECT_MODE_OFF) {
                println("相机硬件不支持人脸检测")
            }
        } else {
            println("相机硬件不支持人脸检测")
        }

        val sensorOrientation = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
        println("sensorOrientation = $sensorOrientation")

        cameraManager.openCamera(mCameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                println("onOpened")
                cameraDevice = camera
                val surface = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Surface(view.surfaceControl)
                } else {
                    holder.surface
                }

                val captureRequestBuilder =
                    camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)

                captureRequestBuilder.addTarget(surface)

                captureRequestBuilder.set(
                    CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
                )
                captureRequestBuilder.set(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                )
                //检测人脸
                captureRequestBuilder.set(
                    CaptureRequest.STATISTICS_FACE_DETECT_MODE,
                    CameraCharacteristics.STATISTICS_FACE_DETECT_MODE_SIMPLE
                )

                //闪光灯打开
//                captureRequestBuilder.set(
//                    CaptureRequest.FLASH_MODE,
//                    CameraCharacteristics.FLASH_MODE_TORCH
//                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val sessionConfiguration = SessionConfiguration(
                        SessionConfiguration.SESSION_REGULAR,
                        arrayListOf(OutputConfiguration(surface)),
                        executor,
                        object : CameraCaptureSession.StateCallback() {
                            override fun onConfigured(session: CameraCaptureSession) {
                                println("onConfigured")
                                session.setRepeatingRequest(
                                    captureRequestBuilder.build(),
                                    mCaptureCallBack,
                                    mCameraHandler
                                )
                            }

                            override fun onConfigureFailed(session: CameraCaptureSession) {
                                println("onConfigureFailed")
                            }
                        }
                    )
                    camera.createCaptureSession(sessionConfiguration)
                } else {
                    camera.createCaptureSession(
                        arrayListOf(surface),
                        object : CameraCaptureSession.StateCallback() {
                            override fun onConfigured(session: CameraCaptureSession) {
                                println("onConfigured")
                                session.setRepeatingRequest(
                                    captureRequestBuilder.build(),
                                    mCaptureCallBack,
                                    mCameraHandler
                                )
                            }

                            override fun onConfigureFailed(session: CameraCaptureSession) {
                                println("onConfigureFailed")
                            }
                        },
                        mCameraHandler
                    )
                }

            }

            override fun onDisconnected(camera: CameraDevice) {
                println("onDisconnected")
            }

            override fun onError(camera: CameraDevice, error: Int) {
                println("onError")
            }
        }, mCameraHandler)

    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        println("surfaceChanged width=$width , height=$height")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        println("surfaceDestroyed")
        cameraDevice?.close()
    }

    private val mCaptureCallBack = object : CameraCaptureSession.CaptureCallback() {

        override fun onCaptureProgressed(
            session: CameraCaptureSession,
            request: CaptureRequest,
            partialResult: CaptureResult
        ) {
            super.onCaptureProgressed(session, request, partialResult)
        }

        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
            super.onCaptureCompleted(session, request, result)
//            val faces = result.get(CaptureResult.STATISTICS_FACES) ?: return
//            println("检测到人脸数 ${faces.size}")
//            for (face in faces) {
//                val bounds = face.bounds
//                println("bounds = ${bounds.toString()}")
//            }
        }

        override fun onCaptureFailed(
            session: CameraCaptureSession,
            request: CaptureRequest,
            failure: CaptureFailure
        ) {
            super.onCaptureFailed(session, request, failure)
            println("开启预览失败！")
        }
    }

    private val executor = Executor() {
        it.run()
    }

    private var isOne = true

    override fun onSurfaceTextureAvailable(
        surface: SurfaceTexture,
        width: Int,
        height: Int
    ) {
        println("onSurfaceTextureAvailable width=$width , height=$height")
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        cameraManager = context?.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        var mCameraCharacteristics: CameraCharacteristics? = null

        for (id in cameraManager.cameraIdList.indices) {
            println("设备中摄像头 $id")
            val cameraCharacteristics = cameraManager.getCameraCharacteristics(id.toString())

            val facing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)
            if (facing == mCameraFacing) {
                println("设备中后视摄像头 $id")
                mCameraId = id.toString()
                mCameraCharacteristics = cameraCharacteristics
                break
            }
        }

        if (mCameraCharacteristics == null) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val rotation = mCameraCharacteristics.get(CameraCharacteristics.LENS_POSE_ROTATION)
            println("rotation = ${rotation?.size}")
        }


        val configurationMap =
            mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                ?: return

        val largest =
            Collections.max(
                mutableListOf(*configurationMap.getOutputSizes(ImageFormat.JPEG)),
                CompareSizesByArea()
            )
        println("largest = $largest")
        val outputSizes1 = configurationMap.getOutputSizes(SurfaceTexture::class.java)

        textureView.setAspectRatio(3, 4)


        val rect = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)
        println("rect = ${rect.toString()}")
        imageReader =
            ImageReader.newInstance(rect?.height()!!, rect.width(), ImageFormat.JPEG, 2)

        imageReader?.setOnImageAvailableListener(object : ImageReader.OnImageAvailableListener {
            override fun onImageAvailable(reader: ImageReader?) {
                println("onImageAvailable $isOne")
                println("activity?.getExternalFilesDir(null) = ${activity?.getExternalFilesDir(null)}")
                if (isOne) {
                    synchronized(this) {
                        if (reader == null) {
                            isOne = true
                            return
                        }
                        val file = File(activity?.getExternalFilesDir(null), "PIC_FILE_NAME.jpg")
                        mCameraHandler.post(ImageSaver(reader.acquireNextImage(), file))
                        isOne = false
                    }
                }
            }
        }, mCameraHandler)

        val faceCount =
            mCameraCharacteristics.get(CameraCharacteristics.STATISTICS_INFO_MAX_FACE_COUNT)
        val faceModes =
            mCameraCharacteristics.get(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES)

        println("faceCount = $faceCount")

        if (faceModes != null) {
            mFaceDetectMode = when {
                faceModes.contains(CaptureRequest.STATISTICS_FACE_DETECT_MODE_FULL) -> CaptureRequest.STATISTICS_FACE_DETECT_MODE_FULL
                faceModes.contains(CaptureRequest.STATISTICS_FACE_DETECT_MODE_SIMPLE) -> CaptureRequest.STATISTICS_FACE_DETECT_MODE_FULL
                else -> CaptureRequest.STATISTICS_FACE_DETECT_MODE_OFF
            }

            if (mFaceDetectMode == CaptureRequest.STATISTICS_FACE_DETECT_MODE_OFF) {
                println("相机硬件支持人脸检测")
            }
        } else {
            println("相机硬件不支持人脸检测")
        }

        val sensorOrientation = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
        println("sensorOrientation = $sensorOrientation")


        cameraManager.openCamera(mCameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                println("onOpened")
                cameraDevice = camera
                val surface = Surface(textureView.surfaceTexture)

                val captureRequestBuilder =
                    camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)

                captureRequestBuilder.addTarget(surface)
                //captureRequestBuilder.addTarget(imageReader?.surface!!)

                captureRequestBuilder.set(
                    CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
                )
                captureRequestBuilder.set(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                )
                //检测人脸
                captureRequestBuilder.set(
                    CaptureRequest.STATISTICS_FACE_DETECT_MODE,
                    CameraCharacteristics.STATISTICS_FACE_DETECT_MODE_SIMPLE
                )

                //闪光灯打开
//                captureRequestBuilder.set(
//                    CaptureRequest.FLASH_MODE,
//                    CameraCharacteristics.FLASH_MODE_TORCH
//                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val sessionConfiguration = SessionConfiguration(
                        SessionConfiguration.SESSION_REGULAR,
                        arrayListOf(
                            OutputConfiguration(surface),
                            OutputConfiguration(imageReader?.surface!!)
                        ),
                        executor,
                        object : CameraCaptureSession.StateCallback() {
                            override fun onConfigured(session: CameraCaptureSession) {
                                println("onConfigured")
                                session.setRepeatingRequest(
                                    captureRequestBuilder.build(),
                                    mCaptureCallBack,
                                    mCameraHandler
                                )
                            }

                            override fun onConfigureFailed(session: CameraCaptureSession) {
                                println("onConfigureFailed")
                            }
                        }
                    )
                    camera.createCaptureSession(sessionConfiguration)
                } else {
                    camera.createCaptureSession(
                        arrayListOf(surface),
                        object : CameraCaptureSession.StateCallback() {
                            override fun onConfigured(session: CameraCaptureSession) {
                                println("onConfigured")
                                session.setRepeatingRequest(
                                    captureRequestBuilder.build(),
                                    mCaptureCallBack,
                                    mCameraHandler
                                )
                            }

                            override fun onConfigureFailed(session: CameraCaptureSession) {
                                println("onConfigureFailed")
                            }
                        },
                        mCameraHandler
                    )
                }

            }

            override fun onDisconnected(camera: CameraDevice) {
                println("onDisconnected")
            }

            override fun onError(camera: CameraDevice, error: Int) {
                println("onError")
            }
        }, mCameraHandler)

    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        //println("onSurfaceTextureUpdated")

    }

    override fun onSurfaceTextureSizeChanged(
        surface: SurfaceTexture,
        width: Int,
        height: Int
    ) {
        println("onSurfaceTextureSizeChanged width=$width , height=$height")
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        println("onSurfaceTextureDestroyed ")
        cameraDevice?.close()
        cameraDevice = null
        imageReader?.close()
        imageReader = null
        return true
    }
}