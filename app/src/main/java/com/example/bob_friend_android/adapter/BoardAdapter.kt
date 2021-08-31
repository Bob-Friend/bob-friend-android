package com.example.bob_friend_android.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bob_friend_android.model.Board
import com.example.bob_friend_android.R
import com.example.bob_friend_android.model.BoardItem
import com.example.bob_friend_android.view.DetailBoardActivity
import java.util.*

class BoardAdapter(private val context: Context, private val boardList : ArrayList<Board>) : RecyclerView.Adapter<BoardAdapter.ViewHolder>() {

    lateinit var boardItem: BoardItem

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_board,parent,false)
        return ViewHolder(view)
    }

    interface OnItemClickListener{
        fun onItemClick(v:View, data: Board, pos : Int)
    }

    private var listener : OnItemClickListener? = null
    fun setOnItemClickListener(listener : OnItemClickListener) {
        this.listener = listener
    }

    override fun getItemCount(): Int = boardList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(boardList[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val boardTitle: TextView = itemView.findViewById(R.id.boardTitle)
        private val userName: TextView = itemView.findViewById(R.id.boardWriter)
        private val currentNumberOfPeople: TextView = itemView.findViewById(R.id.currentNumberOfParticipants)
        private val totalNumberOfPeople: TextView = itemView.findViewById(R.id.totalNumberOfParticipants)
        private val currentNumberOfComments: TextView = itemView.findViewById(R.id.currentNumberOfComments)
        private val createdAt: TextView = itemView.findViewById(R.id.createDate)

        fun bind(item: Board) {
            boardTitle.text = item.title
            userName.text = item.author?.nickname
            currentNumberOfPeople.text = item.currentNumberOfPeople.toString()
            totalNumberOfPeople.text = item.totalNumberOfPeople.toString()
            createdAt.text = item.createdAt.toString()

            if(item.totalNumberOfPeople!=null) {
                boardItem = BoardItem(item.title, item.content, item.author?.nickname,
                    item.currentNumberOfPeople!!, item.totalNumberOfPeople!!, item.createdAt, item.restaurantName)
            }

            itemView.setOnClickListener {
                Intent(context, DetailBoardActivity::class.java).apply {
                    putExtra("item", boardItem)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.run { context.startActivity(this) }
            }

        }
    }
}