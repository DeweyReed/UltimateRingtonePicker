package xyz.aprildown.ultimateringtonepicker.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
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
import xyz.aprildown.ultimateringtonepicker.R
import xyz.aprildown.ultimateringtonepicker.RINGTONE_URI_SILENT
import xyz.aprildown.ultimateringtonepicker.RingtonePickerViewModel
import xyz.aprildown.ultimateringtonepicker.data.Ringtone

internal class SystemRingtoneFragment : Fragment(), Navigator.Selector {

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

        viewModel.dataLoadedEvent.observe(viewLifecycleOwner, Observer<Boolean> {
            if (it == true) {
                loadRingtonesIntoAdapter(context, itemAdapter)
            }
        })
    }

    override fun onSelect() {
        val selectedItems = fastAdapter.getSelectExtension().selectedItems
        viewModel.onTotalSelection(selectedItems.mapNotNull {
            (it as? VisibleRingtone)?.ringtone
        })
    }

    override fun onBack(): Boolean {
        return false
    }

    private fun pickCustom() {
        viewModel.stopPlaying()
        // if (viewModel.settings.useSafSelect) {
        // startActivityForResult(
        //     Intent(Intent.ACTION_OPEN_DOCUMENT)
        //         .addCategory(Intent.CATEGORY_OPENABLE)
        //         .setType("audio/*")
        //         .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION),
        //     0
        // )
        // } else {
        findNavController().navigate(R.id.urp_action_system_to_device)
        // }
    }

    /**
     * Receive [Intent.ACTION_OPEN_DOCUMENT] result.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val uri = data?.data
        if (resultCode == Activity.RESULT_OK && uri != null && uri != RINGTONE_URI_SILENT) {
            // Bail if the permission to read (playback) the audio at the uri was not granted.
            val flags = data.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION
            if (flags != Intent.FLAG_GRANT_READ_URI_PERMISSION) {
                return
            }

            val cr = requireContext().contentResolver

            // Take the long-term permission to read (playback) the audio at the uri.
            cr.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

            cr.query(uri, null, null, null, null)?.use { cursor ->

                if (!cursor.moveToFirst()) return@use

                var title: String? = null

                // If the file was a media file, return its title.
                val titleIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                if (titleIndex != -1) {
                    title = cursor.getString(titleIndex)
                } else {
                    // If the file was a simple openable, return its display name.
                    val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        var displayName = cursor.getString(displayNameIndex)
                        val dotIndex = displayName.lastIndexOf(".")
                        if (dotIndex > 0) {
                            displayName = displayName.substring(0, dotIndex)
                        }
                        title = displayName
                    }
                }

                if (title != null) {
                    viewModel.onDeviceSelection(listOf(Ringtone(uri, title)))
                }
            }
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
                        ringtone = Ringtone(it.first, it.second),
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
        selectExtension.selectedItems.forEach {
            selectExtension.deselect(it)
        }

        FastAdapterDiffUtil[itemAdapter] = items

        // Select view model initialSelection
        val viewModelSelect = viewModel.initialSelection
        items.forEachIndexed { index, item ->
            if (item is VisibleRingtone && item.ringtone in viewModelSelect) {
                selectExtension.select(index)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.stopPlaying()
        _fastAdapter = null
    }
}