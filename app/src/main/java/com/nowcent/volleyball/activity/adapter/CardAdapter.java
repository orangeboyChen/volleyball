package com.nowcent.volleyball.activity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.nowcent.volleyball.R;
import com.nowcent.volleyball.pojo.BookPojo;
import com.nowcent.volleyball.utils.Utils;

import java.util.List;


public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {

    private List<BookPojo> bookList;
    private Context context;


    public void setBookList(List<BookPojo> bookList) {
        this.bookList = bookList;
    }

    public interface OnItemClickListener{
        void onClick(int position);
    }

    private OnItemClickListener onItemClickListener;

    public CardAdapter(Context context, List<BookPojo> bookList, OnItemClickListener onItemClickListener){
        this.context = context;
        this.bookList = bookList;
        this.onItemClickListener = onItemClickListener;
    }

    public void addData(int position, BookPojo bookPojo) {
        bookList.add(position, bookPojo);
        notifyItemInserted(position);
    }

    public void addData(BookPojo bookPojo) {
        bookList.add(bookPojo);
        notifyItemInserted(bookList.size() - 1);
        notifyDataSetChanged();
    }


    public void removeData(int position) {
        bookList.remove(position);
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookPojo bookPojo = bookList.get(position);
        holder.cardViewNameTextView.setText(bookPojo.getNickname());
        holder.cardViewTimeTextView.setText(Utils.getTimeString(bookPojo.getTime()));
        holder.cardViewMessageTextView.setText("预订了" + bookPojo.getFrom() + "时至" + bookPojo.getTo() + "时的场地");
        holder.cardViewButton.setOnClickListener((v) -> {
            onItemClickListener.onClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        TextView cardViewNameTextView;
        TextView cardViewTimeTextView;
        TextView cardViewMessageTextView;
        Button cardViewButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardViewNameTextView = itemView.findViewById(R.id.cardViewNameTextView);
            cardViewTimeTextView = itemView.findViewById(R.id.cardViewTimeTextView);
            cardViewMessageTextView = itemView.findViewById(R.id.cardViewMessageTextView);
            cardViewButton = itemView.findViewById(R.id.cardViewButton);
        }

    }




}
