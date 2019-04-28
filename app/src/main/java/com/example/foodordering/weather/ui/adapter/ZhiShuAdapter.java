package com.example.foodordering.weather.ui.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.example.foodordering.R;
import com.example.foodordering.weather.dao.greendao.Zhishu;
import com.example.foodordering.weather.util.Constant;

/**
 * Created by xch on 2018/3/10.
 */
public class ZhiShuAdapter extends BaseAdapter {

    private List<Zhishu> zhishuList = new ArrayList<>();
    private Activity context;

    public ZhiShuAdapter(List<Zhishu> zhishuList, Activity context) {
        this.zhishuList = zhishuList;
        this.context = context;
    }


    @Override
    public int getCount() {
        return zhishuList.size();
    }

    @Override
    public Object getItem(int position) {
        return zhishuList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(context, R.layout.item_living_index_simple, null);
            holder.icon = (ImageView) convertView.findViewById(R.id.iv_icon);
            holder.name = (TextView) convertView.findViewById(R.id.tv_nameAndValue);
            holder.details = (TextView) convertView.findViewById(R.id.tv_details);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Zhishu zhishu = zhishuList.get(position);

        holder.icon.setImageResource(Constant.ZHISHU.get(zhishu.getName()));
        if (zhishu.getName().contains("紫外线")) {
            holder.name.setText("防晒指数");
        } else {
            holder.name.setText(zhishu.getName());
        }
        holder.details.setText(zhishu.getDetail());

        return convertView;
    }


    public class ViewHolder {
        public TextView name;
        public TextView details;
        public ImageView icon;
    }
}
