package eu.se_bastiaan.popcorntimeremote.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.gson.internal.LinkedTreeMap;

import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.Bind;
import eu.se_bastiaan.popcorntimeremote.R;
import eu.se_bastiaan.popcorntimeremote.utils.LogUtils;

/**
 * Created by Sebastiaan on 09-10-14.
 */
public class PlayerAdapter extends BaseAdapter {

    private final List<LinkedTreeMap<String, String>> mData;
    private final LayoutInflater mInflater;

    class ViewHolder {
        @Bind(android.R.id.text1)
        TextView text1;

        public ViewHolder(View v) {
            ButterKnife.bind(this, v);
        }
    }

    public PlayerAdapter(Context context, List<LinkedTreeMap<String, String>> data) {
        mData = data;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public String getItem(int position) {
        return mData.get(position).get("name");
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null) {
            convertView = mInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            holder = new ViewHolder(convertView);
            holder.text1.setPadding(32, 0, 0, 0);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.text1.setText(getItem(position));

        return convertView;
    }
}
