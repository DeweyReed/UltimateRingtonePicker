package xyz.aprildown.ultimateringtonepicker.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.GenericItem
import com.mikepenz.fastadapter.adapters.GenericItemAdapter
import com.mikepenz.fastadapter.listeners.CustomEventHook
import com.mikepenz.fastadapter.select.getSelectExtension
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import xyz.aprildown.ultimateringtonepicker.R
import xyz.aprildown.ultimateringtonepicker.RINGTONE_URI_SILENT
import xyz.aprildown.ultimateringtonepicker.RingtonePickerViewModel
import xyz.aprildown.ultimateringtonepicker.createDefaultNavOptions
import xyz.aprildown.ultimateringtonepicker.data.Ringtone
import xyz.aprildown.ultimateringtonepicker.databinding.UrpRecyclerViewBinding
import xyz.aprildown.ultimateringtonepicker.launchSaf

internal class SystemRingtoneFragment : Fragment(R.layout.urp_recycler_view),
    EventHandler,
    EasyPermissions.PermissionCallbacks {

    private val viewModel by navGraphViewModels<RingtonePickerViewModel>(R.id.urp_nav_graph)
    private var isRingtoneFromSaf = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val context = view.context
        val binding = UrpRecyclerViewBinding.bind(view)

        val itemAdapter = GenericItemAdapter()
        val fastAdapter = FastAdapter.with(itemAdapter)

        val selectExtension =
            fastAdapter.setUpSelectableRingtoneExtension(
                viewModel = viewModel,
                onSelectionChanged = { item, selected ->
                    val uri = item.ringtone.uri
                    if (selected) {
                        viewModel.currentSelectedUris.add(uri)
                    } else {
                        viewModel.currentSelectedUris.remove(uri)
                    }
                }
            )

        fastAdapter.onClickListener = { _, _, item, _ ->
            when (item) {
                is VisibleAddCustom -> {
                    pickCustom()
                    true
                }
                else -> false
            }
        }

        binding.urpRecyclerView.adapter = fastAdapter

        registerForContextMenu(binding.urpRecyclerView)

        fastAdapter.addEventHook(object : CustomEventHook<VisibleRingtone>() {
            override fun onBind(viewHolder: RecyclerView.ViewHolder): View {
                return viewHolder.itemView
            }

            override fun attachEvent(view: View, viewHolder: RecyclerView.ViewHolder) {
                view.setOnCreateContextMenuListener { menu, _, _ ->
                    val item = FastAdapter.getHolderAdapterItem<GenericItem>(viewHolder)
                        ?: return@setOnCreateContextMenuListener
                    if (item !is VisibleRingtone) return@setOnCreateContextMenuListener
                    if (item.ringtoneType == VisibleRingtone.RINGTONE_TYPE_CUSTOM) {
                        menu.add(Menu.NONE, 0, Menu.NONE, R.string.urp_remove_sound)
                            .setOnMenuItemClickListener {
                                viewModel.deleteCustomRingtone(item.ringtone.uri)

                                if (item.isSelected) {
                                    viewModel.stopPlaying()

                                    if (selectExtension.selectedItems.size == 1) {
                                        viewModel.settings.systemRingtonePicker
                                            ?.defaultSection?.defaultUri
                                            ?.let { defaultUri ->
                                                fastAdapter.forEachIndexed { currentItem, position ->
                                                    if (!currentItem.isSelected &&
                                                        currentItem is VisibleRingtone &&
                                                        currentItem.ringtone.uri == defaultUri
                                                    ) {
                                                        currentItem.isSelected = true
                                                        fastAdapter.notifyItemChanged(position)

                                                        viewModel.currentSelectedUris.add(defaultUri)
                                                    }
                                                }
                                            }
                                    }
                                }

                                itemAdapter.remove(viewHolder.bindingAdapterPosition)
                                true
                            }
                    }
                }
            }
        })

        viewModel.systemRingtoneLoadedEvent.observe(viewLifecycleOwner) {
            binding.urpProgress.hide()
            loadVisibleRingtones(context, itemAdapter)
        }
    }

    override fun onSelect() {
        viewModel.stopPlaying()
        val selectedItems = ringtoneFastAdapter?.getSelectExtension()?.selectedItems
        if (selectedItems != null) {
            viewModel.onFinalSelection(
                selectedItems.mapNotNull {
                    (it as? VisibleRingtone)?.ringtone
                }
            )
        } else {
            viewModel.onFinalSelection(emptyList())
        }
    }

    override fun onBack(): Boolean {
        viewModel.stopPlaying()
        return false
    }

    private fun pickCustom() {
        viewModel.stopPlaying()
        if (viewModel.settings.systemRingtonePicker?.customSection?.useSafSelect == true) {
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
        val customSection =
            viewModel.settings.systemRingtonePicker?.customSection ?: return
        when {
            customSection.launchSafOnPermissionDenied -> {
                launchSaf()
            }
            EasyPermissions.permissionPermanentlyDenied(this, perms[0]) &&
                customSection.launchSafOnPermissionPermanentlyDenied -> {
                launchSaf()
            }
        }
    }

    private fun launchDevicePick() {
        findNavController().navigate(R.id.urp_dest_device, null, createDefaultNavOptions())
    }

    /**
     * Receive [Intent.ACTION_OPEN_DOCUMENT] result.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            viewModel.onSafSelect(requireContext().contentResolver, data)?.let {
                isRingtoneFromSaf = true
                viewModel.onDeviceSelection(listOf(it))
            }
        }
    }

    private fun loadVisibleRingtones(
        context: Context,
        itemAdapter: GenericItemAdapter
    ) {
        val items = createVisibleItems(context)

        val currentSelection = viewModel.currentSelectedUris
        var firstItem: VisibleRingtone? = null
        var firstIndex = RecyclerView.NO_POSITION
        items.forEachIndexed { index, item ->
            if (item is VisibleRingtone && item.ringtone.uri in currentSelection) {
                if (firstIndex == RecyclerView.NO_POSITION) {
                    firstItem = item
                    firstIndex = index
                }
                item.isSelected = true
            }
        }

        itemAdapter.set(items)

        // We only want to scroll to the first selected item
        // when we open the picker for the first time.
        if (viewModel.consumeFirstLoad()) {
            if (firstIndex != RecyclerView.NO_POSITION) {
                // To reveal items above.
                ringtoneRecyclerView?.scrollToPosition((firstIndex - 1).coerceAtLeast(0))
            }
        } else {
            // We pick a ringtone from SAF and play it here.
            if (isRingtoneFromSaf &&
                currentSelection.size == 1 &&
                firstIndex != RecyclerView.NO_POSITION &&
                viewModel.currentPlayingUri != currentSelection.first()
            ) {
                isRingtoneFromSaf = false
                firstItem?.let { targetItem ->
                    viewModel.startPlaying(targetItem.ringtone.uri)
                    targetItem.isPlaying = true
                    ringtoneFastAdapter?.notifyItemChanged(firstIndex)
                }
            }
        }
    }

    private fun createVisibleItems(context: Context): List<GenericItem> {
        val items = mutableListOf<GenericItem>()
        val settings = viewModel.settings
        val systemRingtonePicker = settings.systemRingtonePicker
        if (systemRingtonePicker?.customSection != null) {
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

        val defaultSection = systemRingtonePicker?.defaultSection
        val defaultUri = defaultSection?.defaultUri
        if (defaultSection != null && (
                defaultSection.showSilent ||
                    defaultUri != null ||
                    defaultSection.additionalRingtones.isNotEmpty()
                )
        ) {
            items.add(VisibleSection(context.getString(R.string.urp_device_sounds)))

            if (defaultSection.showSilent) {
                items.add(
                    VisibleRingtone(
                        ringtone = Ringtone(
                            RINGTONE_URI_SILENT,
                            context.getString(R.string.urp_silent_ringtone_title),
                                "5:34"
                        ),
                        ringtoneType = VisibleRingtone.RINGTONE_TYPE_SILENT
                    )
                )
            }

            if (defaultUri != null) {
                items.add(
                    VisibleRingtone(
                        ringtone = Ringtone(
                            defaultUri,
                            defaultSection.defaultTitle
                                ?: context.getString(R.string.urp_default_ringtone_title),
                                ""
                        ),
                        ringtoneType = VisibleRingtone.RINGTONE_TYPE_SYSTEM
                    )
                )
            }


            /**
             * We may need to code the duration here too
             */
            defaultSection.additionalRingtones.forEach {
                items.add(
                    VisibleRingtone(
                        ringtone = Ringtone(it.uri, it.name,"309-SRF"),
                        ringtoneType = VisibleRingtone.RINGTONE_TYPE_SYSTEM
                    )
                )
            }
        }


        /**
         * We may need to code the duration here too
         */
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

        return items
    }
}
