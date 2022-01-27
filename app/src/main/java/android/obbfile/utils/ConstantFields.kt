package android.obbfile.utils

class ConstantFields {
    companion object {
        /*-- Fragments --*/
        const val RECORDING_F = "android.obbfile.recording"
        const val LIST_F = "android.obbfile.list"

        /*-- Recording Status --*/
        const val NOT_RECORDING = 0
        const val RECORDING = 1
        const val PAUSED = 2

        /*-- Info Text --*/
        const val RECORDING_LABEL="Press Mike to pause recording."
        const val NOT_RECORDING_LABEL="Press Mike to start recording."
        const val PAUSE_LABEL="Press Mike to resume recording."
    }
}