package ovh.snacking.snacking.controller;

import io.realm.Realm;
import io.realm.RealmObject;
import ovh.snacking.snacking.R;
import ovh.snacking.snacking.model.Product;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Alex on 29/01/2017.
 */

public class CustomExpandableListAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private ArrayList<ExpandableListGroup> mGroups;

    public CustomExpandableListAdapter(Context context, ArrayList<ExpandableListGroup> groups) {
        this.mGroups = groups;
        this.mContext = context;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ExpandableListGroup group = (ExpandableListGroup) getGroup(groupPosition);

        final CustomExpandableListAdapter.ViewHolder groupHolder;
        if (convertView == null) {
            groupHolder = new CustomExpandableListAdapter.ViewHolder();
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.expandablelist_group_item, parent, false);
            groupHolder.name = (TextView) convertView.findViewById(R.id.group_header);
            groupHolder.image = (ImageView) convertView.findViewById(R.id.group_img);
            convertView.setTag(groupHolder);
        } else {
            groupHolder = (CustomExpandableListAdapter.ViewHolder) convertView.getTag();
        }

        if (isExpanded) {
            groupHolder.image.setImageResource(R.drawable.ic_expand_more_black_24dp);
        } else {
            groupHolder.image.setImageResource(R.drawable.ic_expand_less_black_24dp);
        }
        groupHolder.name.setText(String.valueOf(group.getName()));

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        Product child = (Product) getChild(groupPosition, childPosition);
        final CustomExpandableListAdapter.ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new CustomExpandableListAdapter.ViewHolder();
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);
            viewHolder.name = (TextView) convertView.findViewById(android.R.id.text1);
            viewHolder.tag = (TextView) convertView.findViewById(android.R.id.text2);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (CustomExpandableListAdapter.ViewHolder) convertView.getTag();
        }

        viewHolder.name.setText(String.valueOf(child.getRef()));
        viewHolder.tag.setText(String.valueOf(child.getLabel()));

        return convertView;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        ArrayList chList = mGroups.get(groupPosition).getItems();
        return chList.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        ArrayList chList = mGroups.get(groupPosition).getItems();
        return chList.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mGroups.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return mGroups.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private static class ViewHolder {
        TextView name;
        TextView tag;
        ImageView image;
    }

    public static class ExpandableListChild {
        private String name;
        private String tag;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }
    }

    public static class ExpandableListGroup {
        private String name;
        private int icon;
        private ArrayList items;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getIcon() {
            return icon;
        }

        public void setIcon(int icon) {
            this.icon = icon;
        }

        public void setItems(ArrayList items) {
            this.items = items;
        }

        public ArrayList getItems() {
            return items;
        }
    }
}
