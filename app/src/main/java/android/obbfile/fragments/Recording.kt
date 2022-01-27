package android.obbfile.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.drawable.AnimatedVectorDrawable
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.obbfile.R
import android.obbfile.utils.ConstantFields.Companion.LIST_F
import android.obbfile.utils.ConstantFields.Companion.NOT_RECORDING
import android.obbfile.utils.ConstantFields.Companion.NOT_RECORDING_LABEL
import android.obbfile.utils.ConstantFields.Companion.PAUSED
import android.obbfile.utils.ConstantFields.Companion.PAUSE_LABEL
import android.obbfile.utils.ConstantFields.Companion.RECORDING
import android.obbfile.utils.ConstantFields.Companion.RECORDING_LABEL
import android.obbfile.interfaces.FragmentReplace
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Chronometer
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.IOException
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.util.*


class Recording : Fragment(), View.OnClickListener {
    private var fReplace: FragmentReplace? = null

    private lateinit var fileName: String
    private lateinit var rl: RelativeLayout
    private lateinit var infoTV: TextView
    private lateinit var stopFAB: FloatingActionButton
    private lateinit var recordFAB: FloatingActionButton
    private lateinit var listFAB: FloatingActionButton

    private lateinit var animationDrawable: AnimatedVectorDrawable

    private var mediaPlayer: MediaPlayer? = null
    private var Status: Int = NOT_RECORDING
    private lateinit var timer: Chronometer

    private val recordPermission = Manifest.permission.RECORD_AUDIO

    private var mediaRecorder: MediaRecorder? = null
    private var recordFile: String? = null

    private  val rotateOpen : Animation by lazy { AnimationUtils.loadAnimation(context,
        R.anim.rotate_open_anim
    ) }
    private  val rotateClose : Animation by lazy { AnimationUtils.loadAnimation(context,
        R.anim.rotate_close_anim
    ) }
    private  val fromLeft: Animation by lazy { AnimationUtils.loadAnimation(context,
        R.anim.from_left
    ) }
    private  val toRight : Animation by lazy { AnimationUtils.loadAnimation(context,
        R.anim.to_right
    ) }

    private var timeWhenStopped: Long = 0
    private val PERMISSION_CODE = 21

    override fun onAttach(context: Context) {
        super.onAttach(context)
        when (context) {
            is FragmentReplace -> {
                fReplace = context
            }
            else -> {
                throw RuntimeException(requireContext().toString()+" must implement OnFragmentEventListener")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view : View = inflater.inflate(R.layout.fragment_recording, container, false)

        rl = view.findViewById<RelativeLayout>(R.id.RL)
        infoTV = view.findViewById<TextView>(R.id.infoTV)
        recordFAB = view.findViewById<FloatingActionButton>(R.id.recordFAB)
        stopFAB = view.findViewById<FloatingActionButton>(R.id.stopFAB)
        listFAB = view.findViewById<FloatingActionButton>(R.id.listFAB)
        timer = view.findViewById<Chronometer>(R.id.timer)

        stopFAB.setOnClickListener(this)
        recordFAB.setOnClickListener(this)
        listFAB.setOnClickListener(this)

        infoTV.text = NOT_RECORDING_LABEL

        rl.apply {
            setBackgroundResource(R.drawable.glow_animator)
            animationDrawable = background as AnimatedVectorDrawable
        }

        return view
    }

    private fun checkPermissions(): Boolean {
        return if (ActivityCompat.checkSelfPermission(
                requireContext(),
                recordPermission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            true
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(recordPermission),
                PERMISSION_CODE
            )
            false
        }
    }

    private fun startRecording() {
        val recordPath = requireActivity().getExternalFilesDir("/")!!.absolutePath
        val formatter = SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.CANADA)
        val now = Date()

        recordFile = "R" + formatter.format(now) + ".mp3"

        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(requireContext())
        } else{
            MediaRecorder()
        }
        mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediaRecorder!!.setOutputFile("$recordPath/$recordFile")

        mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        try {
            mediaRecorder!!.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        mediaRecorder!!.start()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun pauseRecording(){
        mediaRecorder!!.pause()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun resumeRecording(){
        mediaRecorder!!.resume()
    }

    private fun stopRecording() {
        mediaRecorder!!.stop()
        mediaRecorder!!.release()
        mediaRecorder = null
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onClick(v: View?) {
        when(v?.id){
            R.id.recordFAB -> {
                if(Status == NOT_RECORDING){
                    if(checkPermissions()) {
                        startRecording()
                        Status = RECORDING
                        infoTV.text = RECORDING_LABEL
                        startTimer()
                        animationDrawable.start()
                        setVisibility(true)
                        setAnimation(true)

                        recordFAB.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),
                            R.color.secondary_dark
                        ));
                        recordFAB.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),
                            R.color.white
                        ));

                        stopFAB.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),
                            R.color.white
                        ))
                        stopFAB.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),
                            R.color.black
                        ))
                    }
                }
                else if(Status == RECORDING){
                    pauseRecording()
                    Status = PAUSED
                    infoTV.text = PAUSE_LABEL
                    pauseTimer()
                    animationDrawable.stop()
                    recordingStatus(false)
                    recordFAB.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),
                        R.color.inactive
                    ))
                    recordFAB.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),
                        R.color.dark_gray
                    ))

                    stopFAB.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),
                        R.color.white
                    ))
                    stopFAB.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),
                        R.color.black
                    ))
                }
                else if(Status == PAUSED){
                    Status = RECORDING
                    resumeRecording()
                    startTimer()
                    infoTV.text = RECORDING_LABEL
                    animationDrawable.start()
                    recordingStatus(true)

                    recordFAB.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),
                        R.color.secondary_dark
                    ));
                    recordFAB.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),
                        R.color.white
                    ));

                    stopFAB.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),
                        R.color.white
                    ))
                    stopFAB.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),
                        R.color.black
                    ))
                }
            }
            R.id.stopFAB -> {
                stopRecording()
                Status = NOT_RECORDING
                infoTV.text = NOT_RECORDING_LABEL
                stopTimer()
                animationDrawable.stop()
                setVisibility(false)
                setAnimation(false)
                recordFAB.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),
                    R.color.inactive
                ));
                recordFAB.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),
                    R.color.dark_gray
                ));
            }
            R.id.listFAB -> {
                fReplace?.doFragmentReplace(LIST_F, RecordingList())
            }
        }
    }

    private fun startTimer(){
        timer.base = SystemClock.elapsedRealtime() + timeWhenStopped
        timer.start()
    }

    private fun pauseTimer(){
        timeWhenStopped = timer.base - SystemClock.elapsedRealtime();
        timer.stop()
    }

    private fun stopTimer(){
        timeWhenStopped = 0;
        timer.base = SystemClock.elapsedRealtime();
        timer.stop()
    }

    private fun recordingStatus(status:Boolean){
        if(!status){
            recordFAB.startAnimation(rotateOpen)
        }else{
            recordFAB.startAnimation(rotateClose)
        }
    }

    private fun setAnimation(closed:Boolean) {
        if(!closed){
            stopFAB.startAnimation(toRight)
            recordFAB.startAnimation(rotateOpen)
        }else{
            stopFAB.startAnimation(fromLeft)
            recordFAB.startAnimation(rotateClose)
        }
    }

    private fun setVisibility(closed:Boolean) {
        if(!closed)
        {
            stopFAB.visibility = View.VISIBLE
        }else{
            stopFAB.visibility = View.INVISIBLE
        }
    }
}