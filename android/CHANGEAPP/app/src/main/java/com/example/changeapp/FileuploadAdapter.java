package com.example.changeapp;



import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Created by arunkumar on 11/01/17.
 */

public class FileuploadAdapter extends RecyclerView.Adapter<FileuploadAdapter.ViewHolder> {

    private List<Fileupload> contactlist;
    Main2Activity activity;

    public FileuploadAdapter(List<Fileupload> contactlist, Main2Activity activity){
        this.contactlist = contactlist;
        this.activity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.singleitem,parent,false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Fileupload upload = contactlist.get(position);
        holder.name.setText(contactlist.get(position).getName());
        holder.type.setText(contactlist.get(position).getType());

        holder.delete.setOnClickListener(delete_file(position,holder));


    }

    @Override
    public int getItemCount() {
        return contactlist.size();
    }


    public View.OnClickListener delete_file(final int position, final ViewHolder holder)
    {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            activity.delete_file(position);

            }
        };
    }



    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name,type,delete;
        public ViewHolder(View itemView) {
            super(itemView);

            name = (TextView)itemView.findViewById(R.id.txt_name);
            type = (TextView)itemView.findViewById(R.id.txt_type);
            delete = (TextView)itemView.findViewById(R.id.txt_delete);


        }
    }
}