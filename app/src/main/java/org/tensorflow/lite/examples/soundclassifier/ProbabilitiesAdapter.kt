package org.tensorflow.lite.examples.soundclassifier

import android.R
import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.recyclerview.widget.RecyclerView
import org.tensorflow.lite.examples.soundclassifier.databinding.ItemProbabilityBinding
import org.tensorflow.lite.support.label.Category

internal class ProbabilitiesAdapter : RecyclerView.Adapter<ProbabilitiesAdapter.ViewHolder>() {
  var categoryList: List<Category> = emptyList()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val binding =
      ItemProbabilityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    return ViewHolder(binding)

  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val category = categoryList[position]
    holder.bind(position, category.label, category.score)
  }

  override fun getItemCount() = categoryList.size

  class ViewHolder(private val binding: ItemProbabilityBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(position: Int, label: String, score: Float) {
      with(binding) {
        //вывод название птицы после определения
        labelTextView.text = label.drop(2)
        progressBar.progressBackgroundTintList = progressColorPairList[position % 3].first
        progressBar.progressTintList = progressColorPairList[position % 3].second

        val newValue = (score * 100).toInt()
        // анимация плавных скачков графика
        val animation =
          ObjectAnimator.ofInt(progressBar, "progress", progressBar.progress, newValue)
        animation.duration = 100
        animation.interpolator = AccelerateDecelerateInterpolator()
        animation.start()
      }
    }

    companion object {
      /** Цвета объектов определенных */
      private val progressColorPairList = listOf(
        ColorStateList.valueOf(0xfff9e7e4.toInt()) to ColorStateList.valueOf(0xffd97c2e.toInt()),
        ColorStateList.valueOf(0xfff7e3e8.toInt()) to ColorStateList.valueOf(0xffc95670.toInt()),
        ColorStateList.valueOf(0xffecf0f9.toInt()) to ColorStateList.valueOf(0xff714Fe7.toInt()),
      )
    }
  }
}
