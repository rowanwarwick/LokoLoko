//package com.example.hack
//
//import android.annotation.SuppressLint
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.appcompat.content.res.AppCompatResources
//import androidx.recyclerview.widget.RecyclerView
//import com.example.hack.databinding.ItemResultBinding
//import com.example.hack.ui.model.ModelForResult
//import com.example.hack.ui.model.Question
//
//class AdaptorQestion(val fromGame: Boolean, val list: MutableList<Question>): RecyclerView.Adapter<AdaptorQestion.ResultViewHolder>() {
//
//    class ResultViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val item = ItemResultBinding.bind(view)
//        fun bind(department: Question) {
//            item.question.text = department.text
//            item.answer.text = when (department.rightVariant) {
//                1 -> department.one
//                2 -> department.two
//                3 -> department.three
//                else -> department.four
//            }
//            item.ivAvatar.setImageResource(if (isMan) R.drawable.man_item_image else R.drawable.car_item_image)
//            item.nameStreet.text = department.address
//            item.durection.text = department.time
//            itemView.setOnClickListener {
//                listenerClick.onClick(department, isMan)
//            }
//        }
//    }
//
//    override fun onCreateViewHolder(
//        parent: ViewGroup,
//        viewType: Int
//    ): ResultViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_result, parent, false)
//        return ResultViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
//        holder.bind(list[position])
//    }
//
//    override fun getItemCount(): Int = list.size
//
//}