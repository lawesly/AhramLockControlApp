package com.github.hadywalied.ahramlockcontrolapp.ui.scanning

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.github.hadywalied.ahramlockcontrolapp.Devices
import com.github.hadywalied.ahramlockcontrolapp.domain.Injector
import com.github.hadywalied.ahramlockcontrolapp.ui.MainViewModel
import com.github.hadywalied.ahramlockcontrolapp.R
import com.github.hadywalied.ahramlockcontrolapp.domain.DevicesRepo
import com.github.hadywalied.ahramlockcontrolapp.ui.DevicesRecyclerViewAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.scanning_fragment.*

class ScanningFragment : Fragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var repo: DevicesRepo

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        //region observers
        viewModel.loadingLiveData.observe(
            viewLifecycleOwner,
            Observer { swipe.isRefreshing = it ?: false })
        viewModel.allBluetoothDevicesLiveData.observe(
            viewLifecycleOwner,
            Observer { updateRecyclerList(it) })
        viewModel.scanFailedLiveData.observe(viewLifecycleOwner, Observer { scanFailedAction(it) })
        viewModel.connectFailedLiveData.observe(
            viewLifecycleOwner,
            Observer { connectionFailedAction(it) })
        viewModel.connectionStateLiveData.observe(
            viewLifecycleOwner,
            Observer { connectedAction(it) })
        //endregion
        return inflater.inflate(R.layout.scanning_fragment, container, false)
    }

    private fun connectionFailedAction(b: Boolean?) {
        showMaterialDialog(b!!)
    }

    private fun scanFailedAction(b: Boolean?) {
        showMaterialDialog(b!!)
    }


    private fun connectedAction(b: String?) {
        var s: String = ""
        var s_array = arrayListOf<String>()

        when (b) {
            "fail" -> showMaterialDialog(false)
            else -> {
                viewModel.devicesItems.forEach {
                    if (it.address == b) {
                        repo.insert(it)
                        findNavController().navigate(R.id.action_scanningFragment_to_userDevicesFragment)
                        return@forEach
                    }
                }
            }
        }

    }

    private fun updateRecyclerList(list: List<Devices>?) {
        with(all_devices_recycler) {
            adapter =
                DevicesRecyclerViewAdapter(repo, list) {
                    viewModel.connect(viewModel.devicesSet[it.address]!!)
                }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repo = Injector.ProvideDevicesRepo(requireContext())
        viewModel.scan()
        //region toolbar
        toolbar.inflateMenu(R.menu.scan_menu)
        toolbar.subtitle = "Please Choose your Device"
        toolbar.setOnMenuItemClickListener { item ->
            return@setOnMenuItemClickListener when (item.itemId) {
                R.id.menu_devices_item -> {
                    findNavController().navigate(R.id.action_scanningFragment_to_userDevicesFragment)
                    true
                }
                else -> false
            }
        }
        //endregion
        swipe.setOnRefreshListener { viewModel.scan() }
    }


    fun showMaterialDialog(bool: Boolean) {
        with(MaterialAlertDialogBuilder(requireContext())) {
            when (bool) {
                false -> {
                    setView(R.layout.failed_layout)
                    setTitle("Try Again")
                }
                true -> {
                    setView(R.layout.success_layout)
                    setTitle("Success")
                }
            }
            setNeutralButton("continue") { dialogInterface, i -> dialogInterface.dismiss() }
        }
    }

}
