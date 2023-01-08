package com.krish.jobspot.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.krish.jobspot.databinding.TpoCardLayoutBinding
import com.krish.jobspot.model.Tpo

class TpoAdapter : RecyclerView.Adapter<TpoAdapter.TpoViewHolder>() {

    private val tpoList: MutableList<Tpo> = mutableListOf()

    inner class TpoViewHolder(
        private val binding: TpoCardLayoutBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tpo: Tpo) {
            binding.ivProfileTpo.load(tpo.imageUri)
            binding.tvTpoName.text = tpo.username
            binding.tvTpoEmail.text = tpo.email
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TpoViewHolder {
        val binding = TpoCardLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TpoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TpoViewHolder, position: Int) {
        holder.bind(tpoList[position])
    }

    override fun getItemCount(): Int = tpoList.size


    fun setData(newTpoList: List<Tpo>) {
        tpoList.clear()
        tpoList.addAll(newTpoList)
        notifyDataSetChanged()
    }

}