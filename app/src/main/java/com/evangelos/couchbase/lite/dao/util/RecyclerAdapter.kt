package com.evangelos.couchbase.lite.dao.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

abstract class RecyclerAdapter<T, VH : RecyclerViewHolder<T>> : RecyclerView.Adapter<VH>(), ViewHolderClickListener {

    var dataSet: List<T> = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var adapterClickListener: AdapterClickListener<T>? = null

    override fun getItemViewType(position: Int): Int {
        return getViewType(dataSet[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view =
            LayoutInflater.from(parent.context).inflate(getResId(viewType), parent, false)
        return getViewHolder(view, viewType)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(dataSet[position])
        holder.clickListener = this
    }

    override fun getItemCount() = dataSet.size

    override fun onClick(view: View, index: Int) {
        adapterClickListener?.onClick(view, dataSet[index], index)
    }

    override fun onLongClick(view: View, index: Int) {
        adapterClickListener?.onLongClick(view, dataSet[index], index)
    }

    protected open fun getViewType(model: T) = 0

    @LayoutRes
    protected abstract fun getResId(viewType: Int): Int
    protected abstract fun getViewHolder(view: View, viewType: Int): VH

}