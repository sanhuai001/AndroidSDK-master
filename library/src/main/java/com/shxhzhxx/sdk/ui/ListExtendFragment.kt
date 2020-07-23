package com.shxhzhxx.sdk.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.scwang.smartrefresh.header.MaterialHeader
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.shxhzhxx.sdk.CoroutineFragment
import com.shxhzhxx.sdk.R
import kotlinx.android.synthetic.main.fragment_extend_list.*
import java.util.*

abstract class ListExtendFragment<M, VH : RecyclerView.ViewHolder, A : RecyclerView.Adapter<VH>> : CoroutineFragment() {
    protected val _list = ArrayList<M>()
    private var loading = false
    private val adapter by lazy { onAdapter() }
    protected val listSize: Int get() = _list.size
    protected val list: List<M> get() = _list.toList()
    protected var showNoMore = false
    private var canLoadMore = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(if (showNoMore) R.layout.fragment_extend_list2 else R.layout.fragment_extend_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onHeader(header)
        listRecyclerView.itemAnimator = onItemAnimator()
        listRecyclerView.layoutManager = onLayoutManager()
        listRecyclerView.adapter = adapter

        smartRefreshLayout.setOnRefreshListener { nextPage() }
        smartRefreshLayout.setOnLoadMoreListener { nextPage() }
        rootLayout.interceptor = { false }
        customizeView(context, view.findViewById(R.id.rooContentFl))
        refresh()
    }

    open fun refresh() {
        removeFoolterView()
        smartRefreshLayout?.autoRefresh()
    }

    open fun onRefresh() {
        removeFoolterView()
        smartRefreshLayout?.autoRefresh()
    }

    open fun getHeaderViewGroup(): LinearLayout {
        return llHeader
    }

    open fun setLoadMoreEnable(enableLoadMore: Boolean) {
        canLoadMore = enableLoadMore
        smartRefreshLayout?.setEnableLoadMore(enableLoadMore)
    }

    open fun setRefreshEnable(enableLoadMore: Boolean) {
        smartRefreshLayout?.setEnableRefresh(enableLoadMore)
    }

    open fun setHeaderView(view: View) {
        smartRefreshLayout?.setEnableHeaderTranslationContent(true)
        llHeader?.removeAllViews()
        llHeader?.addView(view)
    }

    open fun setFoolterView(view: View) {
        llFooter?.removeAllViews()
        llFooter?.addView(view)
    }

    open fun removeFoolterView() {
        llFooter?.removeAllViews()
    }

    open fun getSmartRefreshLayout(): SmartRefreshLayout {
        return smartRefreshLayout
    }

    /**
     * 返回值不要太小，尽量避免一屏高度可以显示一页数据的情况。
     */
    protected open fun pageSize() = 10

    protected open fun pageStartAt() = 0

    protected open fun customizeView(context: Context?, parent: ViewGroup) {}

    protected open fun onHeader(header: MaterialHeader) {}

    protected open fun onLayoutManager(): RecyclerView.LayoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)

    protected fun addItemDecoration(decor: RecyclerView.ItemDecoration) {
        listRecyclerView?.addItemDecoration(decor)
    }

    open fun onItemAnimator(): RecyclerView.ItemAnimator {
        return DefaultItemAnimator()
    }

    protected abstract fun onAdapter(): A

    /**
     * @param onResult 必须调用这个方法来结束加载过程。
     * @param onLoad    调用这个方法代表成功获取指定页面的数据。
     *                  失败时不需要调用。
     *                  这个方法的调用必须在[onResult]后面，且中间不能插入对[ListFragment.nextPage]的调用
     *
     * */
    protected abstract fun onNextPage(page: Int, onResult: () -> Unit, onLoad: (list: List<M>) -> Unit)

    protected operator fun get(position: Int) = _list[position]

    private fun nextPage() {
        if (loading || !isAdded || smartRefreshLayout == null)
            return
        loading = true

        val refresh = smartRefreshLayout.state.isHeader
        val page = pageStartAt() + if (refresh) 0 else _list.size / pageSize()
        if (refresh) {
            smartRefreshLayout?.setEnableLoadMore(false)
        } else {
            smartRefreshLayout?.setEnableRefresh(false)
        }
        onNextPage(page,
                onResult = {
                    if (refresh) {
                        _list.clear()
                        adapter.notifyDataSetChanged()
                    }
                    loading = false
                    smartRefreshLayout?.finishRefresh()
                    smartRefreshLayout?.finishLoadMore()
                    smartRefreshLayout?.setEnableRefresh(true)
                },
                onLoad = { list ->
                    val enableLoadMore = canLoadMore && list.size == pageSize()
                    smartRefreshLayout?.setEnableLoadMore(enableLoadMore)
                    if (!enableLoadMore) {
                        noMoreDataCallBack()
                    }
                    if (list.isNotEmpty()) {
                        val start = _list.size
                        _list.addAll(list)
                        adapter.notifyItemRangeInserted(start, _list.size)
                    }
                })
    }

    open fun noMoreDataCallBack() {

    }
}