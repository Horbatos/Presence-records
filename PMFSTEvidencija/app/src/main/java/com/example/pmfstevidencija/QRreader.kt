package com.example.pmfstevidencija

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.firebase.firestore.FirebaseFirestore
import java.util.jar.Manifest

class QRreader : AppCompatActivity() {

    private lateinit var svBarcode: SurfaceView
    private lateinit var tvBarcode: TextView

    private lateinit var detector: BarcodeDetector
    private lateinit var cameraSource: CameraSource

    val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val name = intent.getSerializableExtra("User") as? String
        val ScheduleNumber = intent.getSerializableExtra("ScheduleNumber") as? Int
        val class_id = intent.getSerializableExtra("Class_id") as? String



        setContentView(R.layout.activity_qrreader)

        svBarcode = findViewById(R.id.sv_barcode)
        tvBarcode = findViewById(R.id.tv_barcode)

        detector = BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.QR_CODE).build()

        detector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {}

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                var barcodes = detections?.detectedItems


                if (barcodes!!.size() > 0){
                    tvBarcode.post{
                        if(barcodes.valueAt(0).displayValue==class_id){
                        //KAD SE SKENIRA QR KOD
                        val docRef = db.collection("predmeti").document(barcodes.valueAt(0).displayValue)
                        docRef.get()
                                .addOnSuccessListener { document ->
                                    if (document != null) {
                                        docRef.get().addOnSuccessListener { documentSnapshot ->
                                            val current_class = documentSnapshot.toObject(Class::class.java)
                                            if(current_class!!.schedule[ScheduleNumber!!].attendees.contains(name)){
                                                tvBarcode.text = current_class.name+"-> You have been entered into the attendees list"
                                            }
                                            else{
                                                current_class.schedule[ScheduleNumber].attendees.add(name)
                                                db.collection("predmeti").document(barcodes.valueAt(0).displayValue).set(current_class)
                                            }
                                        }
                                    }
                                    else {
                                        tvBarcode.text = "Wrong QR code"
                                    }
                                }
                    }
                        else{tvBarcode.text = "Wrong QR code" }
                    }
                }
            }

        })

        cameraSource = CameraSource.Builder(this,detector).setRequestedPreviewSize(1024,768)
                .setRequestedFps(15f).setAutoFocusEnabled(true).build()
        svBarcode.holder.addCallback(object : SurfaceHolder.Callback2{
            override fun surfaceRedrawNeeded(p0: SurfaceHolder?) {}

            override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {}

            override fun surfaceDestroyed(p0: SurfaceHolder?) {
                cameraSource.stop()
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                if (ContextCompat.checkSelfPermission(this@QRreader,
                                android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                    cameraSource.start(holder)
                else ActivityCompat.requestPermissions(this@QRreader,
                        arrayOf(android.Manifest.permission.CAMERA),123)
            }

        })
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                cameraSource.start(svBarcode.holder)
            }
            else Toast.makeText(this,"Can't work without permission",Toast.LENGTH_SHORT)
                    .show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        detector.release()
        cameraSource.stop()
        cameraSource.release()
    }

}