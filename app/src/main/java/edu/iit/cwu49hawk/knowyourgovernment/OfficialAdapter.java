package edu.iit.cwu49hawk.knowyourgovernment;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by wsy37 on 4/16/2017.
 */

public class OfficialAdapter extends RecyclerView.Adapter<OfficialViewHolder>
{
    private List<Official> officials;
    private MainActivity mainActivity;

    public OfficialAdapter(List<Official> officials, MainActivity mainActivity)
    {
        this.officials = officials;
        this.mainActivity = mainActivity;
    }

    @Override
    public OfficialViewHolder onCreateViewHolder(final ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.official_entry, parent, false);

        itemView.setOnClickListener(mainActivity);

        return new OfficialViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(OfficialViewHolder holder, int position)
    {
        Official official = officials.get(position);
        holder.officeName.setText(official.getOfficeName());
        holder.officialInfo.setText(official.getName() + " (" + official.getParty() + ")");
    }

    @Override
    public int getItemCount() {
        return this.officials.size();
    }
}
