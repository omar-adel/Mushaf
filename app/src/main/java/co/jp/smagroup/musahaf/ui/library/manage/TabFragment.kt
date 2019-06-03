package co.jp.smagroup.musahaf.ui.library.manage


import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import co.jp.smagroup.musahaf.R
import co.jp.smagroup.musahaf.model.DownloadingState
import co.jp.smagroup.musahaf.model.Edition
import co.jp.smagroup.musahaf.ui.commen.BaseFragment
import co.jp.smagroup.musahaf.ui.commen.MusahafApplication
import co.jp.smagroup.musahaf.ui.commen.ViewModelFactory
import co.jp.smagroup.musahaf.ui.commen.dialog.ProgressDialog
import co.jp.smagroup.musahaf.utils.extensions.observer
import co.jp.smagroup.musahaf.utils.extensions.viewModelOf
import com.codebox.lib.android.widgets.recyclerView.onScroll
import kotlinx.android.synthetic.main.fragment_tab.*
import javax.inject.Inject


@Suppress("UNCHECKED_CAST")
class TabFragment : BaseFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    init {
        MusahafApplication.appComponent.inject(this)
    }

    override val layoutId: Int = R.layout.fragment_tab
    private lateinit var viewModel: LibraryViewModel
    private lateinit var parentActivity: ManageLibraryActivity
    private lateinit var adapter: ManageLibraryAdapter
    private var tabPosition = 0

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        parentActivity = activity as ManageLibraryActivity
        viewModel = viewModelOf(LibraryViewModel::class.java, viewModelFactory)
        viewModel.allAvailableEditions.observer(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                val data = filterDataByTabPosition(it)
                recycler_library_manage.adapter = getRecyclerAdapter(data)
                recycler_library_manage.scrollToPosition(recyclerViewPosition)
                loadingCompleted(false)
            } else loadingCompleted(true)
        }
        loadData()
    }

    private fun getRecyclerAdapter(data: List<Pair<Edition, DownloadingState>>): ManageLibraryAdapter {
        adapter = ManageLibraryAdapter(data, doOnItemClicked = { editionIdentifier, downloadingState ->
            if (downloadingState.isDownloadCompleted) {
                //Do nothing
            } else
                downloadMusahaf(editionIdentifier, downloadingState)
        })

        recycler_library_manage.onScroll { dx, dy ->
            recyclerViewPosition =
                (recycler_library_manage.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        }

        return adapter
    }

    private fun filterDataByTabPosition(data: List<Pair<Edition, DownloadingState>>): List<Pair<Edition, DownloadingState>> {
        return when (tabPosition) {
            0 -> data.filter { it.first.type == Edition.Tafsir }
            1 -> data.filter { it.first.type == Edition.Translation && it.first.language != "ar" }
            // 2 -> data.filter { it.first.format == "audio" }
            else -> throw IllegalArgumentException("Position $tabPosition not found")
        }.onEach { it.first.language.capitalize() }.sortedBy { it.first.language }
    }

    private var recyclerViewPosition = 0
    private fun downloadMusahaf(editionIdentifier: String, downloadingState: DownloadingState) {

        if (activity?.supportFragmentManager!!.findFragmentByTag(ProgressDialog.TAG) == null) {
            val progressDialog = ProgressDialog()
            progressDialog.progressListener = object : ProgressDialog.ProgressListener {

                override fun onCancelled() = viewModel.updateDataDownloadState(editionIdentifier)
                override fun onSuccess() = viewModel.updateDataDownloadState(editionIdentifier)
                override suspend fun downloadManger(): String {
                    viewModel.downloadMusahaf(
                        editionIdentifier,
                        downloadingState
                    )
                    viewModel.updateDataDownloadState(editionIdentifier)
                    return editionIdentifier
                }
            }
            progressDialog.show(activity?.supportFragmentManager!!, ProgressDialog.TAG)
        }
    }

    override fun loadData() {
        super.loadData()
        viewModel.getEditions()
    }

    companion object {
        fun newInstance(tabPosition: Int): TabFragment {
            val tabFragment = TabFragment()
            tabFragment.tabPosition = tabPosition
            return tabFragment
        }
    }
}

