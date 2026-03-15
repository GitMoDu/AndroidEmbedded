package com.dogecoding.android_embedded.serial.ble_serial

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dogecoding.android_core.extension.disable
import com.dogecoding.android_core.extension.enable
import com.dogecoding.android_core.extension.gone
import com.dogecoding.android_core.extension.invisible
import com.dogecoding.android_core.extension.visible
import com.dogecoding.android_embedded.R
import com.dogecoding.android_embedded.databinding.DialogBleConnectBinding

class BleConnectDialog : DialogFragment() {

    companion object {
        const val FADE_DURATION: Long = 300
    }

    private var _binding: DialogBleConnectBinding? = null
    private val binding get() = _binding!!

    private val bleSerialViewModel: BleSerialViewModel by activityViewModels()
    private lateinit var deviceAdapter: DeviceAdapter

    private lateinit var buttonScanText: String
    private lateinit var buttonStopText: String


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            bleSerialViewModel.startScan()
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.ble_scan_permissions_required),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogBleConnectBinding.inflate(layoutInflater)

        buttonScanText = getString(R.string.ble_scan_scan)
        buttonStopText = getString(R.string.ble_scan_stop)

        deviceAdapter = DeviceAdapter { device ->
            bleSerialViewModel.connect(device)
            dismiss()
        }

        binding.deviceRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = deviceAdapter
        }

        binding.scanLottie.gone()

        binding.scanButton.text = buttonScanText
        binding.scanButton.enable()

        binding.scanButton.setOnClickListener {
            it.disable()
            bleSerialViewModel.stopScan()
            checkAndStartScan()
        }

        binding.cancelButton.setOnClickListener {
            bleSerialViewModel.stopScan()
            dismiss()
        }

        bleSerialViewModel.discoveredDevices.observe(this) { devices ->
            deviceAdapter.updateDevices(devices)
        }

        bleSerialViewModel.isScanning.observe(this) { isScanning ->
            if (isScanning) {
                binding.scanLottie.visible(FADE_DURATION)
                binding.scanButton.text = buttonStopText

            } else {
                binding.scanLottie.invisible(FADE_DURATION)
                binding.scanButton.text = buttonScanText
            }
            binding.scanButton.enable()
        }

        return AlertDialog.Builder(requireContext()).setView(binding.root).create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        bleSerialViewModel.stopScan()
    }

    private fun checkAndStartScan() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(
                requireContext(), it
            ) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            bleSerialViewModel.startScan()
            binding.scanButton.text = buttonStopText
            binding.scanButton.isEnabled = true
        } else {
            requestPermissionLauncher.launch(permissions)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class DeviceAdapter(private val onClick: (BluetoothDevice) -> Unit) :
        RecyclerView.Adapter<DeviceAdapter.ViewHolder>() {

        private var devices: List<BluetoothDevice> = emptyList()
        private lateinit var unknownDeviceText: String

        @SuppressLint("NotifyDataSetChanged")
        fun updateDevices(newDevices: List<BluetoothDevice>) {
            devices = newDevices
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_2, parent, false)

            unknownDeviceText = parent.context.getString(R.string.ble_scan_unknown_device)
            return ViewHolder(view)
        }

        @SuppressLint("MissingPermission")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val device = devices[position]
            holder.nameText.text = device.name ?: unknownDeviceText
            holder.addressText.text = device.address
            holder.itemView.setOnClickListener { onClick(device) }
        }

        override fun getItemCount(): Int = devices.size

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val nameText: TextView = view.findViewById(android.R.id.text1)
            val addressText: TextView = view.findViewById(android.R.id.text2)
        }
    }
}
