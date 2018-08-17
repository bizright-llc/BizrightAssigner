package com.example.william.bizrightAssigner.Util;

import android.widget.BaseAdapter;
import android.widget.TextView;


import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.example.william.bizrightAssigner.Bean.DataTransferContainer;
import com.example.william.bizrightAssigner.Bean.Item;
import com.example.william.bizrightAssigner.R;

public class TableAdapter extends BaseAdapter {

    private List<DataTransferContainer> list;
    private LayoutInflater inflater;

    public TableAdapter(Context context, List<DataTransferContainer> list){
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DataTransferContainer data = (DataTransferContainer) this.getItem(position);
        ViewHolder viewHolder;
        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.my_list_item, null);
            viewHolder.sku = (TextView) convertView.findViewById(R.id.sku);
            viewHolder.qty = (TextView) convertView.findViewById(R.id.qty);
            viewHolder.moveIn = (TextView) convertView.findViewById(R.id.moveIn);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.sku.setText(data.getSKU());
        viewHolder.qty.setText(data.getQTY().toString());
        viewHolder.moveIn.setText(data.getMoveIn());


        return convertView;
    }

    public static class ViewHolder{
        public TextView sku;
        public TextView qty;
        public TextView moveIn;

    }
}
