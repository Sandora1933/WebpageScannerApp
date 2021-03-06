package com.example.webpagescannerapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.webpagescannerapp.R;
import com.example.webpagescannerapp.model.RequestInfo;

import java.util.ArrayList;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    Context context;    // Activity context
    ArrayList<RequestInfo> requestList;     // List to work with recycler view

    public RequestAdapter(Context context, ArrayList<RequestInfo> list){
        this.context = context;
        requestList = list;
    }

    // ViewHolder container for RequestInfo elements from list
    public class RequestViewHolder extends RecyclerView.ViewHolder{

        TextView urlTextView, matchesNumberTextView, threadsNumber;
        ImageView statusImageView;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            urlTextView = itemView.findViewById(R.id.urlTextView);
            matchesNumberTextView = itemView.findViewById(R.id.matchesTextView);
            threadsNumber = itemView.findViewById(R.id.threadNumTextView);
            statusImageView = itemView.findViewById(R.id.statusImageView);
        }
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        RequestInfo requestInfo = requestList.get(position);
        holder.urlTextView.setText(requestInfo.getUrl());

        // If error then show in red color
        if (requestInfo.getMatchesCount().matches("[A-Za-z]")){
            holder.matchesNumberTextView.setTextColor(context.getResources().getColor(R.color.colorRed));
        }

        holder.matchesNumberTextView.setText("Matches : " + requestInfo.getMatchesCount());
        holder.threadsNumber.setText(requestInfo.getThreadName());

        switch(requestInfo.getStatus()){
            case STATUS_FOUND:
                holder.statusImageView.setBackgroundResource(R.drawable.ic_baseline_check_circle_24);
                break;
            case STATUS_NOT_FOUND:
                holder.statusImageView.setBackgroundResource(R.drawable.ic_baseline_cancel_24);
                break;
            case STATUS_ERROR:
                holder.statusImageView.setBackgroundResource(R.drawable.ic_baseline_error_24);
                break;
        }

    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

}
