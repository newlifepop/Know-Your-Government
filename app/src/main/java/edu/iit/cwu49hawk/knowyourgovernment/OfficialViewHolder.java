package edu.iit.cwu49hawk.knowyourgovernment;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by wsy37 on 4/16/2017.
 */

public class OfficialViewHolder extends RecyclerView.ViewHolder
{
    public TextView officeName;
    public TextView officialInfo;

    public OfficialViewHolder(View view)
    {
        super(view);
        officeName = (TextView)view.findViewById(R.id.officeName);
        officialInfo = (TextView)view.findViewById(R.id.officialInfo);
    }
}
