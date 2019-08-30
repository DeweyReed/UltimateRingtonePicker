package xyz.aprildown.ultimateringtonepicker.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IItem
import com.mikepenz.fastadapter.adapters.GenericItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import com.mikepenz.fastadapter.listeners.CustomEventHook
import com.mikepenz.fastadapter.select.getSelectExtension
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import xyz.aprildown.ultimateringtonepicker.R
import xyz.aprildown.ultimateringtonepicker.RINGTONE_URI_SILENT
import xyz.aprildown.ultimateringtonepicker.RingtonePickerViewModel
import xyz.aprildown.ultimateringtonepicker.data.Ringtone

internal class SystemRingtoneFragment : Fragment(),
    Navigator.Selector,
    EasyPermissions.PermissionCallbacks {

    private val viewModel by navGraphViewModels<RingtonePickerViewModel>(R.id.urp_nav_graph)

    private var _fastAdapter: FastAdapter<IItem<*>>? = null
    private val fastAdapter: FastAdapter<IItem<*>>
        get() = _fastAdapter
            ?: throw IllegalStateException("Accessing _fastAdapter after onDestroyView")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.urp_recycler_view, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val context = view.context
        val list = view as RecyclerView

        val itemAdapter = GenericItemAdapter()
        _fastAdapter = FastAdapter.with(itemAdapter)

        val selectExtension = fastAdapter.setUpSelectableRingtoneExtension(viewModel)

        fastAdapter.onClickListener = { _, _, item, _ ->
            when (item) {
                is VisibleAddCustom -> {
                    pickCustom()
                    true
                }
                else -> false
            }
        }

        list.run {
            layoutManager = LinearLayoutManager(context)
            adapter = fastAdapter
        }
        registerForContextMenu(list)

        fastAdapter.addEventHook(object : CustomEventHook<VisibleRingtone>() {
            override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
                return if (viewHolder is VisibleRingtone.ViewHolder) {
                    viewHolder.itemView
                } else {
                    null
                }
            }

            override fun attachEvent(view: View, viewHolder: RecyclerView.ViewHolder) {
                view.setOnCreateContextMenuListener { menu, _, _ ->
                    val item = getItem(viewHolder) ?: return@setOnCreateContextMenuListener
                    if (item.ringtoneType == VisibleRingtone.RINGTONE_TYPE_CUSTOM) {
                        menu.add(Menu.NONE, 0, Menu.NONE, R.string.urp_remove_sound)
                            .setOnMenuItemClickListener {
                                viewModel.deleteCustomRingtone(item.ringtone.uri)

                                if (item.isSelected) {
                                    viewModel.stopPlaying()
                                }

                                viewModel.settings.defaultUri?.let { defaultUri ->
                                    selectExtension.select(itemAdapter.adapterItems.indexOfFirst {
                                        it is VisibleRingtone && it.ringtone.uri == defaultUri
                                    })
                                }

                                itemAdapter.remove(viewHolder.adapterPosition)
                                true
                            }
                    }
                }
            }
        })

        viewModel.systemRingtoneLoadedEvent.observe(viewLifecycleOwner, Observer<Boolean> {
            if (it == true) {
                loadRingtonesIntoAdapter(context, itemAdapter)
                list.retrievePositionFrom(savedInstanceState)
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        viewAsRecyclerView()?.savePositionTo(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onSelect() {
        viewModel.stopPlaying()
        val selectedItems = fastAdapter.getSelectExtension().selectedItems
        viewModel.onTotalSelection(selectedItems.mapNotNull {
            (it as? VisibleRingtone)?.ringtone
        })
    }

    override fun onBack(): Boolean {
        viewModel.stopPlaying()
        return false
    }

    private fun pickCustom() {
        viewModel.stopPlaying()
        if (viewModel.settings.useSafSelect) {
            launchSaf()
        } else {
            val permission = Manifest.permission.READ_EXTERNAL_STORAGE
            if (EasyPermissions.hasPermissions(requireContext(), permission)) {
                launchDevicePick()
            } else {
                EasyPermissions.requestPermissions(
                    PermissionRequest.Builder(this, 0, permission)
                        .setRationale(R.string.urp_permission_external_rational)
                        .setPositiveButtonText(android.R.string.ok)
                        .setNegativeButtonText(android.R.string.cancel)
                        .build()
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        launchDevicePick()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.permissionPermanentlyDenied(this, perms[0])) {
            launchSaf()
        }
    }

    private fun launchSaf() {
        startActivityForResult(
            Intent(Intent.ACTION_OPEN_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("audio/*")
                .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION),
            0
        )
    }

    private fun launchDevicePick() {
        findNavController().navigate(R.id.urp_dest_device)
    }

    /**
     * Receive [Intent.ACTION_OPEN_DOCUMENT] result.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val uri = data?.data
        if (resultCode == Activity.RESULT_OK && uri != null && uri != RINGTONE_URI_SILENT) {
            // Bail if the permission to read (playback) the audio at the uri was not granted.
            if (data.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION
                != Intent.FLAG_GRANT_READ_URI_PERMISSION
            ) {
                return
            }
            viewModel.onSafSelect(requireContext().contentResolver, uri)
        }
    }

    private fun loadRingtonesIntoAdapter(context: Context, itemAdapter: GenericItemAdapter) {
        val items = mutableListOf<IItem<*>>()
        val settings = viewModel.settings

        if (settings.showCustomRingtone) {
            items.add(VisibleSection(context.getString(R.string.urp_your_sounds)))
            viewModel.customRingtones.forEach {
                items.add(
                    VisibleRingtone(
                        ringtone = it,
                        ringtoneType = VisibleRingtone.RINGTONE_TYPE_CUSTOM
                    )
                )
            }
            items.add(VisibleAddCustom())
        }

        if (settings.showSilent || settings.showDefault || settings.additionalRingtones.isNotEmpty()) {
            items.add(VisibleSection(context.getString(R.string.urp_device_sounds)))

            if (settings.showSilent) {
                items.add(
                    VisibleRingtone(
                        ringtone = Ringtone(
                            RINGTONE_URI_SILENT,
                            context.getString(R.string.urp_silent_ringtone_title)
                        ),
                        ringtoneType = VisibleRingtone.RINGTONE_TYPE_SILENT
                    )
                )
            }

            if (settings.showDefault) {
                val defaultUri = settings.defaultUri
                    ?: throw IllegalArgumentException("Please provide a default uri")
                items.add(
                    VisibleRingtone(
                        ringtone = Ringtone(
                            defaultUri,
                            settings.defaultTitle
                                ?: context.getString(R.string.urp_default_ringtone_title)
                        ),
                        ringtoneType = VisibleRingtone.RINGTONE_TYPE_SYSTEM
                    )
                )
            }

            settings.additionalRingtones.forEach {
                items.add(
                    VisibleRingtone(
                        ringtone = Ringtone(it.uri, it.name),
                        ringtoneType = VisibleRingtone.RINGTONE_TYPE_SYSTEM
                    )
                )
            }
        }

        viewModel.systemRingtones.forEach {
            val (type, ringtones) = it
            items.add(
                VisibleSection(
                    context.getString(
                        when (type) {
                            RingtoneManager.TYPE_RINGTONE -> R.string.urp_ringtone
                            RingtoneManager.TYPE_NOTIFICATION -> R.string.urp_notification
                            RingtoneManager.TYPE_ALARM -> R.string.urp_alarm
                            else -> throw IllegalArgumentException("Wrong ringtone type: $type")
                        }
                    )
                )
            )
            ringtones.forEach { ringtone ->
                items.add(
                    VisibleRingtone(
                        ringtone = ringtone,
                        ringtoneType = VisibleRingtone.RINGTONE_TYPE_SYSTEM
                    )
                )
            }
        }

        // Deselect all
        val selectExtension = fastAdapter.getSelectExtension()
        selectExtension.deleteAllSelectedItems()

        val viewModelSelect = viewModel.currentSelectedUris
        var firstIndex = RecyclerView.NO_POSITION
        items.forEachIndexed { index, item ->
            if (item is VisibleRingtone && item.ringtone.uri in viewModelSelect) {
                if (firstIndex == RecyclerView.NO_POSITION) {
                    firstIndex = index
                }
                item.isSelected = true
            }
        }

        FastAdapterDiffUtil[itemAdapter] = items
        if (firstIndex != RecyclerView.NO_POSITION) {
            viewAsRecyclerView()?.scrollToPosition(firstIndex)
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.currentSelectedUris.clear()
        viewModel.currentSelectedUris.addAll(
            fastAdapter.getSelectExtension().selectedItems.mapNotNull {
                (it as? VisibleRingtone)?.ringtone?.uri
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fastAdapter = null
    }
}