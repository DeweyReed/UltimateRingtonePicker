package xyz.aprildown.ultimateringtonepicker.ui

import android.os.Bundle
import android.view.LayoutInflater
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
import com.mikepenz.fastadapter.select.SelectExtension
import xyz.aprildown.ultimateringtonepicker.KEY_EXTRA_ID
import xyz.aprildown.ultimateringtonepicker.KEY_RINGTONE_TYPE
import xyz.aprildown.ultimateringtonepicker.R
import xyz.aprildown.ultimateringtonepicker.RingtonePickerViewModel

internal class RingtoneFragment : Fragment(), Navigator.Selector {

    private val viewModel by navGraphViewModels<RingtonePickerViewModel>(R.id.urp_nav_graph)

    private var selectExtension: SelectExtension<IItem<*>>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.urp_recycler_view, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val context = view.context
        val recyclerView = view as RecyclerView

        val itemAdapter = GenericItemAdapter()
        val fastAdapter = FastAdapter.with(itemAdapter)
        selectExtension = fastAdapter.setUpSelectableRingtoneExtension(viewModel)

        recyclerView.run {
            layoutManager = LinearLayoutManager(context)
            adapter = fastAdapter
        }

        viewModel.getRingtoneLiveData(
            requireArguments().getInt(KEY_RINGTONE_TYPE),
            requireArguments().getLong(KEY_EXTRA_ID)
        ).observe(viewLifecycleOwner, Observer { ringtones ->
            itemAdapter.setNewList(ringtones.map { ringtone ->
                VisibleRingtone(
                    ringtone = ringtone,
                    ringtoneType = VisibleRingtone.RINGTONE_TYPE_CUSTOM
                )
            })
        })
    }

    override fun onResume() {
        super.onResume()
        myself = this
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        (view as? RecyclerView)?.retrievePositionFrom(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        (view as? RecyclerView)?.savePositionTo(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onSelect() {
        viewModel.stopPlaying()
        @Suppress("UNCHECKED_CAST")
        val ringtones =
            selectExtension?.selectedItems?.mapNotNull { (it as? VisibleRingtone)?.ringtone }
        if (ringtones?.isNotEmpty() == true) {
            if (viewModel.settings.onlyShowDevice) {
                viewModel.onTotalSelection(ringtones)
            } else {
                viewModel.onDeviceSelection(ringtones)
                findNavController().popBackStack(R.id.urp_dest_system, false)
            }
        }
    }

    override fun onBack(): Boolean {
        viewModel.stopPlaying()
        // TODO: If we pop back to DeviceRingtoneFragment, the scroll position is lost.
        return findNavController().popBackStack()
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopPlaying()
        myself = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        selectExtension = null
    }

    companion object {
        /**
         * I can't find a way to get current fragment in ViewPager2 so I use this way.
         */
        internal var myself: RingtoneFragment? = null
    }
}