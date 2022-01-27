package android.obbfile.activities

import android.obbfile.R
import android.obbfile.fragments.Recording
import android.obbfile.utils.ConstantFields.Companion.RECORDING_F
import android.obbfile.interfaces.FragmentReplace
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity(), FragmentReplace {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadFragment(RECORDING_F, Recording())
    }

    private fun loadFragment(tagName: String, fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.setCustomAnimations(
            R.anim.fade_in,
            R.anim.fade_out,
            R.anim.fade_in,
            R.anim.fade_out
        )
        if(tagName != RECORDING_F){
            fragmentTransaction.addToBackStack(tagName)
        }
        fragmentTransaction.replace(R.id.FL, fragment, tagName)
        fragmentTransaction.commit()
    }

    override fun doFragmentReplace(tag: String?, fragment: Fragment?) {
        loadFragment(tag!!,fragment!!)
    }
}