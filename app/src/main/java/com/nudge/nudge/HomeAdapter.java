package com.nudge.nudge;

import android.content.Context;
import android.content.DialogInterface;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by STAR on 10/13/2017.
 */

public class HomeAdapter extends ArrayAdapter<HomeModel> implements View.OnClickListener {

    private ArrayList<HomeModel> dataSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        public TextView txtNickName;
        public TextView txtNudgeTime;
        public Button deleteButton;
        public TextView nudgeBack;

    }

    public HomeAdapter(ArrayList<HomeModel> data, Context context) {
        super(context, R.layout.cell_activity, data);
        this.dataSet = data;
        this.mContext=context;
    }

    @Override
    public void onClick(View v) {

        int position=(Integer) v.getTag();
        Object object= getItem(position);
        HomeModel homeModel =(HomeModel)object;

//        switch (v.getId())
//        {
//            case R.id.BusStop_Cell:
//                busStop_FromListView.setVisibility(View.INVISIBLE);
//                break;
//        }
    }

    private int lastPosition = -1;

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final HomeModel homeModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        final ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.cell_activity, parent, false);
            viewHolder.txtNickName = (TextView) convertView.findViewById(R.id.name_TextView);
            viewHolder.txtNudgeTime = (TextView) convertView.findViewById(R.id.time_TextView);
            viewHolder.deleteButton = (Button) convertView.findViewById(R.id.delete_button);
            viewHolder.nudgeBack = (TextView) convertView.findViewById(R.id.Back_TextView);

            result=convertView;

            viewHolder.deleteButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    GlobalApplication.dataModels.remove(position);
                    GlobalApplication.adapter.notifyDataSetChanged();
                    String timeStamp = homeModel.getNudgeTime();
                    HomeActivity homeActivity = HomeActivity.instance;
                    if (homeActivity != null) {
                        homeActivity.deleteNudgeDatas(timeStamp);
                    }
                }
            });

            viewHolder.nudgeBack.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    GlobalApplication.dataModels.remove(position);
                    GlobalApplication.adapter.notifyDataSetChanged();
                }
            });

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

//        Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
//        result.startAnimation(animation);

        lastPosition = position;

        viewHolder.txtNickName.setText(homeModel.getNickName());
        viewHolder.txtNudgeTime.setText(homeModel.getNudgeTime());
        final View finalConvertView = convertView;
        // Return the completed view to render on screen
        return convertView;
    }
}
