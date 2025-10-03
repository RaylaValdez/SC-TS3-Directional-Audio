package com.teamspeak.ts3sdkclient.helper;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.teamspeak.ts3sdkclient.R;

import java.util.List;


/**
 * TeamSpeak 3 sdk client sample
 *
 * Copyright (c) 2007-2017 TeamSpeak-Systems
 *
 * @author Anna
 * Creation date: 09.02.17
 *
 * Simple RecyclerViewAdapter to display info messages
 */
public class MyInfoItemRecyclerViewAdapter extends RecyclerView.Adapter<MyInfoItemRecyclerViewAdapter.ViewHolder> {

    private final List<InfoMessage> mValues;
    private Context mContext;

    public MyInfoItemRecyclerViewAdapter(List<InfoMessage> items, Context context) {
        mValues = items;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_info, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mMessageView.setText(mValues.get(position).getMessage());
        holder.mMessageView.setTextColor(mValues.get(position).getColor());
        holder.mTimeStampView.setText(mValues.get(position).getTimeStampString(mContext));
        holder.mTimeStampView.setTextColor(mValues.get(position).getColor());
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTimeStampView;
        public final TextView mMessageView;
        public InfoMessage mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTimeStampView = (TextView) view.findViewById(R.id.timestamp);
            mMessageView = (TextView) view.findViewById(R.id.message);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mMessageView.getText() + "'";
        }
    }


}
