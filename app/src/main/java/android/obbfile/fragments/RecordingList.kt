package android.obbfile.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.obbfile.R
import android.obbfile.adapters.RecordingsAdapter
import android.obbfile.interfaces.FragmentReplace
import android.obbfile.utils.ConstantFields.Companion.RECORDING_F
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import java.io.File
import java.io.IOException
import java.lang.RuntimeException

class RecordingList : Fragment(), SeekBar.OnSeekBarChangeListener, View.OnClickListener,
    RecordingsAdapter.OnItemListClick {
    private var fReplace: FragmentReplace? = null
    private lateinit var toolbar: Toolbar
    private lateinit var RV: RecyclerView
    private lateinit var media_playerCL: ConstraintLayout
    private lateinit var playBT: ImageButton
    private lateinit var nextBT: ImageButton
    private lateinit var previousBT: ImageButton
    private lateinit var nameTV: TextView
    private lateinit var statusTV: TextView
    private lateinit var seekbar: SeekBar

    private lateinit var recordingsAdapter: RecordingsAdapter

    private var mediaPlayer: MediaPlayer? = null

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    private lateinit var allFiles: Array<File>
    private var isPlaying = false

    private var fileToPlay: File? = null
    private var playingFilePos: Int = -1


    private lateinit var seekbarHandler: Handler
    private lateinit var updateSeekbar: Runnable

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
        val view: View = inflater.inflate(R.layout.fragment_recording_list, container, false)

        toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        RV = view.findViewById<RecyclerView>(R.id.RV)
        media_playerCL = view.findViewById<ConstraintLayout>(R.id.media_playerCL)
        playBT = view.findViewById<ImageButton>(R.id.playBT)
        nextBT = view.findViewById<ImageButton>(R.id.nextBT)
        previousBT = view.findViewById<ImageButton>(R.id.previousBT)
        nameTV = view.findViewById<TextView>(R.id.nameTV)
        statusTV = view.findViewById<TextView>(R.id.statusTV)
        seekbar = view.findViewById<SeekBar>(R.id.seekbar)

        toolbar.setNavigationIcon(R.drawable.go_back_icon)
        toolbar.title = "Recording List"

        playBT.setOnClickListener(this)
        previousBT.setOnClickListener(this)
        nextBT.setOnClickListener(this)
        seekbar.setOnSeekBarChangeListener(this)

        toolbar.setNavigationOnClickListener(View.OnClickListener {
            fReplace?.doFragmentReplace(RECORDING_F, Recording())
        })

        bottomSheetBehavior = BottomSheetBehavior.from<ConstraintLayout>(media_playerCL)

        val path = requireActivity().getExternalFilesDir("/")!!.absolutePath
        val directory = File(path)
        allFiles = directory.listFiles()

        recordingsAdapter = RecordingsAdapter(allFiles, this)

        RV.setHasFixedSize(true)
        RV.layoutManager = LinearLayoutManager(context)
        RV.adapter = recordingsAdapter

        mediaButtons()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        pauseAudio()
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        val progress = seekBar!!.progress
        mediaPlayer!!.seekTo(progress)
        resumeAudio()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun pauseAudio() {
        mediaPlayer!!.pause()
        playBT.setImageDrawable(requireActivity().resources.getDrawable(R.drawable.play_media_icon, null))
        isPlaying = false
        seekbarHandler.removeCallbacks(updateSeekbar)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun resumeAudio() {
        mediaPlayer!!.start()
        playBT.setImageDrawable(
            requireActivity().resources.getDrawable(
                R.drawable.pause_media_icon,
                null
            )
        )
        isPlaying = true
        updateRunnable()
        seekbarHandler.postDelayed(updateSeekbar, 0)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun stopAudio() {
        //Stop The Audio
        playBT.setImageDrawable(requireActivity().resources.getDrawable(R.drawable.play_media_icon, null))
        statusTV.text = "Stopped"
        isPlaying = false
        mediaPlayer!!.stop()
        seekbarHandler.removeCallbacks(updateSeekbar)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun playAudio(fileToPlay: File) {
        mediaPlayer = MediaPlayer()
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        try {
            mediaPlayer!!.setDataSource(fileToPlay.absolutePath)
            mediaPlayer!!.prepare()
            mediaPlayer!!.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        playBT.setImageDrawable(
            requireActivity().resources.getDrawable(
                R.drawable.pause_media_icon,
                null
            )
        )
        nameTV.text = fileToPlay.name
        statusTV.text = "Playing"
        isPlaying = true
        mediaPlayer!!.setOnCompletionListener {
            stopAudio()
            statusTV.text = "Finished"
        }
        seekbar.max = mediaPlayer!!.duration
        seekbarHandler = Handler()
        updateRunnable()
        seekbarHandler.postDelayed(updateSeekbar, 0)
    }

    private fun updateRunnable() {
        updateSeekbar = object : Runnable {
            override fun run() {
                seekbar.progress = mediaPlayer!!.currentPosition
                seekbarHandler.postDelayed(this, 500)
            }
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.playBT -> {
                if (isPlaying) {
                    pauseAudio()
                } else {
                    if (fileToPlay != null) {
                        if(statusTV.text=="Finished"){
                            playAudio(allFiles[playingFilePos])
                        }
                        else{
                            resumeAudio()
                        }
                    }
                }
            }
            R.id.previousBT -> {
                fileToPlay = allFiles[playingFilePos-1]
                playingFilePos -= 1
                mediaButtons()
                stopAudio()
                playAudio(fileToPlay!!)
            }
            R.id.nextBT -> {
                fileToPlay = allFiles[playingFilePos+1]
                playingFilePos += 1
                mediaButtons()
                stopAudio()
                playAudio(fileToPlay!!)

            }
        }
    }

    private fun mediaButtons(){
        if(playingFilePos==-1){
            playBT.isEnabled = false
            previousBT.isEnabled = false
            nextBT.isEnabled = false
            seekbar.isEnabled = false
            playBT.setImageResource(R.drawable.media_play_button_inactive)
            previousBT.setImageResource(R.drawable.media_previous_button_inactive)
            nextBT.setImageResource(R.drawable.media_next_button_inactive)
        }
        else if(playingFilePos==0){
            playBT.isEnabled = true
            previousBT.isEnabled = false
            nextBT.isEnabled = true
            seekbar.isEnabled = true
            playBT.setImageResource(R.drawable.media_play_button)
            previousBT.setImageResource(R.drawable.media_previous_button_inactive)
            nextBT.setImageResource(R.drawable.media_next_button)
        }
        else if(playingFilePos == (allFiles.size-1)){
            playBT.isEnabled = true
            previousBT.isEnabled = true
            nextBT.isEnabled = false
            seekbar.isEnabled = true
            playBT.setImageResource(R.drawable.media_play_button)
            previousBT.setImageResource(R.drawable.media_previous_button)
            nextBT.setImageResource(R.drawable.media_next_button_inactive)
        }
        else{
            playBT.isEnabled = true
            previousBT.isEnabled = true
            nextBT.isEnabled = true
            seekbar.isEnabled = true
            playBT.setImageResource(R.drawable.media_play_button)
            previousBT.setImageResource(R.drawable.media_previous_button)
            nextBT.setImageResource(R.drawable.media_next_button)
        }
    }

    override fun onClickListener(file: File?, position: Int) {
        fileToPlay = file
        playingFilePos = position
        mediaButtons()
        if (isPlaying) {
            stopAudio()
            playAudio(fileToPlay!!)
        } else {
            playAudio(fileToPlay!!)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(isPlaying){
            stopAudio()
        }
    }
}