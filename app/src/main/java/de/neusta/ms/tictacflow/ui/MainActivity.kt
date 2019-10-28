package de.neusta.ms.tictacflow.ui

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import de.neusta.ms.tictacflow.R
import de.neusta.ms.tictacflow.databinding.ActivityMainBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
class MainActivity : FragmentActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.model = this

    }

    fun onOfflineClick() {
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.fragment_container,
                FlowGameFragment().apply {
                    arguments = bundleOf("GAME_TYPE" to FlowGameFragment.GameType.OFFLINE.ordinal)
                })
            .commit()
    }

    fun onOnlineXClick() {
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.fragment_container,
                FlowGameFragment().apply {
                    arguments = bundleOf("GAME_TYPE" to FlowGameFragment.GameType.ONLINE_X.ordinal)
                })
            .commit()
    }

    fun onOnlineOClick() {
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.fragment_container,
                FlowGameFragment().apply {
                    arguments = bundleOf("GAME_TYPE" to FlowGameFragment.GameType.ONLINE_O.ordinal)
                })
            .commit()
    }

    fun onFlowClick() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, FlowGameFragment())
            .commit()
    }

}