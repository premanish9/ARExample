package com.applaudostudios.karcore

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import android.widget.Toast
import com.google.ar.core.Anchor
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Plane
import com.google.ar.core.Session
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

class MainActivity : AppCompatActivity() {
    //lateinit for Augmented Reality Fragment
    private lateinit var arFragment: ArFragment
    lateinit var smallTable:ImageView
    lateinit var bigLamp:ImageView
    //lateinit for the model uri
    private lateinit var selectedObject: Uri
    private lateinit var session: Session
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Init Fragment
        arFragment = supportFragmentManager.findFragmentById(R.id.sceneform_fragment_view) as ArFragment
        smallTable=findViewById(R.id.smallTable)
        bigLamp=findViewById(R.id.bigLamp)


        //Default model
        setModelPath("rocket.sfb")

        //Tab listener for the ArFragment
        arFragment.setOnTapArPlaneListener { hitResult, plane, _ ->
            //If surface is not horizontal and upward facing
            if (plane.type != Plane.Type.HORIZONTAL_UPWARD_FACING) {
                //return for the callback
                return@setOnTapArPlaneListener
            }
            //create a new anchor
            val anchor = hitResult.createAnchor()
            placeObject(arFragment, anchor, selectedObject)
        }

        //Click listener for lamp and table objects
        smallTable.setOnClickListener {
            setModelPath("model.sfb")
        }
        bigLamp.setOnClickListener {
            setModelPath("LampPost.sfb")
        }


        if (!checkIsSupportedDeviceOrFinish()) return

        session = Session(this)

    }

    /***
     * function to handle the renderable object and place object in scene
     */
   private fun placeObject(fragment: ArFragment, anchor: Anchor, modelUri: Uri) {
        val modelRenderable = ModelRenderable.builder()
            .setSource((fragment.requireContext()), modelUri)
            .build()
        //when the model render is build add node to scene
        modelRenderable.thenAccept { renderableObject -> addNodeToScene(fragment, anchor, renderableObject) }
        //handle error
        modelRenderable.exceptionally {
            val toast = Toast.makeText(applicationContext, "Error", Toast.LENGTH_SHORT)
            toast.show()
            null
        }
    }


    private fun addNodeToScene(fragment: ArFragment, anchor: Anchor, renderableObject: Renderable) {
        val anchorNode = AnchorNode(anchor)
        val transformableNode = TransformableNode(fragment.transformationSystem)
        transformableNode.renderable = renderableObject
        transformableNode.setParent(anchorNode)
        fragment.arSceneView.scene.addChild(anchorNode)
        transformableNode.select()
    }


    private fun setModelPath(modelFileName: String) {
        selectedObject = Uri.parse(modelFileName)
        val toast = Toast.makeText(applicationContext, modelFileName, Toast.LENGTH_SHORT)
        toast.show()
    }


    private fun checkIsSupportedDeviceOrFinish(): Boolean {
        when (ArCoreApk.getInstance().checkAvailability(this)) {
            ArCoreApk.Availability.SUPPORTED_INSTALLED -> return true
            ArCoreApk.Availability.SUPPORTED_APK_TOO_OLD,
            ArCoreApk.Availability.SUPPORTED_NOT_INSTALLED -> {
                try {
                    val installStatus = ArCoreApk.getInstance().requestInstall(this, true)
                    if (installStatus == ArCoreApk.InstallStatus.INSTALL_REQUESTED) {
                        return false
                    }
                } catch (e: com.google.ar.core.exceptions.UnavailableException) {
                    // Display an appropriate message to the user
                }
            }
            else -> {
                // Device is not supported
            }
        }
        return false
    }

    /*override fun onResume() {
        super.onResume()
        if (session == null) {
            return
        }
        try {
            session.resume()
        } catch (e: Exception) {
            // Handle exception
        }
    }

    override fun onPause() {
        super.onPause()
        session.pause()
    }*/
}
