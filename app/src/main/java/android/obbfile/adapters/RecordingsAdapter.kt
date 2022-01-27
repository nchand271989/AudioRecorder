package android.obbfile.adapters

import android.obbfile.R
import android.obbfile.utils.AudioDateTime
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class RecordingsAdapter(private var allFiles: Array<File>, private var onItemListClick: OnItemListClick) : RecyclerView.Adapter<RecordingsAdapter.AudioViewHolder>() {
    private lateinit var audioDateTime: AudioDateTime

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.recorded_audio_list, parent, false)
        audioDateTime = AudioDateTime()
        return AudioViewHolder(view)
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        holder.titleTV.text = allFiles[position].name
        holder.dateTV.setText(audioDateTime.getTimeAgo(allFiles[position].lastModified()))
    }

    override fun getItemCount(): Int {
        return allFiles.size
    }

    inner class AudioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var titleTV: TextView = itemView.findViewById(R.id.titleTV)
        var dateTV: TextView = itemView.findViewById(R.id.dateTV)
        override fun onClick(v: View) {
            onItemListClick.onClickListener(allFiles[absoluteAdapterPosition], absoluteAdapterPosition)
        }

        init {
            itemView.setOnClickListener(this)
        }
    }

    interface OnItemListClick {
        fun onClickListener(file: File?, position: Int)
    }
}
