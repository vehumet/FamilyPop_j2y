package com.j2y.familypop.activity.lobby;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.j2y.engine.ColumnListView;
import com.j2y.familypop.MainActivity;
import com.j2y.familypop.activity.Activity_serverMain;
import com.j2y.familypop.activity.server.Activity_serverCalibration;
import com.j2y.familypop.activity.server.Activity_serverCalibrationLocation;
import com.j2y.familypop.backup.Dialog_MessageBox_ok_cancel;
import com.j2y.familypop.client.FpcRoot;
import com.nclab.familypop.R;

import java.util.ArrayList;

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
// ListViewAdapter_talkHistory
//
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
public class ListViewAdapter_talkHistory extends BaseAdapter implements View.OnClickListener
{
    private Context mContext;

    public ArrayList<ListView_talkRecord> _talkRecord_items;


    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 초기화
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    public ListViewAdapter_talkHistory(Context context)
    {
        super();
        mContext = context;
        _talkRecord_items = new ArrayList<ListView_talkRecord>();
    }


    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // BaseAdapter implement
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Override
    public int getCount()
    {
        return _talkRecord_items.size();
    }

    @Override
    public Object getItem(int position)
    {
        return _talkRecord_items.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    public int getViewTypeCount()
    {
        return 2;
    }



    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // View
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    int _leftRightSort;
    Rect _backPadding;
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {

        if( convertView == null )
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_history2, null, false);
        //view   v = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.listview_item_history, null);
        if(_talkRecord_items.size() <= position)
            return convertView;

        ListView_talkRecord selectedItem = _talkRecord_items.get(position);
        if( selectedItem != null)
        {
            AbsListView.LayoutParams params = (AbsListView.LayoutParams) convertView.getLayoutParams();

//            int height = 600; // _selectedItem._height
//            if (params == null)
//            {
//                params = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, height);
//            }
//            else
//            {
//                params.height = height;
//            }
//            convertView.setLayoutParams(params);
            //view.setBackgroundColor(_selectedItem._color);

            if(position % 2 == 0) {

                convertView.setPadding(0, 50, 0, 150);
            }
            else {
                convertView.setPadding(0, 200, 0, 0);

            }



            TextView text_playTime = (TextView) convertView.findViewById(R.id.listitem_history_text_playtime);
            TextView text_name = (TextView) convertView.findViewById(R.id.listitem_history_text_name);
            TextView text_day = (TextView) convertView.findViewById(R.id.listitem_history_text_day);
            ImageButton button_play  = (ImageButton) convertView.findViewById(R.id.button_listview_item_history_left);
            selectedItem._button_delete = (Button) convertView.findViewById(R.id.button_listview_item_history_delete1);

            ImageView leftPoint = (ImageView) convertView.findViewById(R.id.imageView_list_item_history_leftpoint);
            ImageView rightPoint = (ImageView) convertView.findViewById(R.id.imageView_list_item_history_rightpoint);


            // 글자 왼쪽 오른쪽 정렬

            ColumnListView listview = Activity_talkHistory.Instance._listView_talkRecords;
            listview.setOverScrollMode(ListView.OVER_SCROLL_NEVER);

            LinearLayout.LayoutParams pr = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

            if(null == _backPadding)
                _backPadding = new Rect(listview.getPaddingLeft(),listview.getPaddingTop(),listview.getPaddingRight(),listview.getPaddingBottom());


            if(position % 2 == 0)  //left item
            {
                leftPoint.setVisibility(View.GONE);
                rightPoint.setVisibility(View.VISIBLE);

                pr.gravity = Gravity.RIGHT;
                pr.setMargins(0, 20, 20, 0);


                text_playTime.setLayoutParams(pr);
                text_name.setLayoutParams(pr);
                text_day.setLayoutParams(pr);

//                listview.setPadding(_backPadding.left - 35,
//                        _backPadding.top,
//                        _backPadding.right,
//                        _backPadding.bottom);


            }
            else    //right item
            {
                leftPoint.setVisibility(View.VISIBLE);
                rightPoint.setVisibility(View.GONE);

                pr.gravity = Gravity.LEFT;
                pr.setMargins(20, 20, 0, 0);

                text_playTime.setLayoutParams(pr);
                text_name.setLayoutParams(pr);
                text_day.setLayoutParams(pr);

//                listview.setPadding(_backPadding.left,
//                        _backPadding.top,
//                        _backPadding.right - 50,
//                        _backPadding.bottom);


            }

            text_playTime.setText(selectedItem._text_playTime);
            text_name.setText(selectedItem._text_name);
            text_day.setText(selectedItem._text_day);

            button_play.setOnClickListener(this);
            selectedItem._button_delete.setOnClickListener(this);
            button_play.setTag(selectedItem);
            selectedItem._button_delete.setTag(selectedItem);

            selectedItem._button_delete.setVisibility(View.INVISIBLE);

            _leftRightSort++;
        }

        return convertView;
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // ListView Items
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    public void AddItem(ListView_talkRecord talk, boolean notify)
    {
        talk._color = Color.rgb((int) (Math.random() * 0x80), (int) (Math.random() * 0x80), (int) (Math.random() * 0x80));
        talk._height = (int) (Math.random() * 400 + 800);
        _talkRecord_items.add(talk);

        if(notify)
            notifyDataSetChanged();
    }
    public void RemoveItem(ListView_talkRecord user, boolean notify)
    {
        _talkRecord_items.remove(user);

        if(notify)
            notifyDataSetChanged();
    }
    public void RemoveItem(int index, boolean notify)
    {
        _talkRecord_items.remove(index);

        if(notify)
            notifyDataSetChanged();
    }
    public void ClearItems(boolean notify)
    {
        _talkRecord_items.clear();

        if(notify)
            notifyDataSetChanged();
    }


    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // Click events
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Override
    public void onClick(View v)
    {

        ListView_talkRecord clickItem = (ListView_talkRecord) v.getTag();
        if(null == clickItem)
            return;

        if( v.getId() == R.id.button_listview_item_history_right ||
                v.getId() == R.id.button_listview_item_history_left ||
                v.getId() == R.id.listview_history_button_topitem_play )
        {
            FpcRoot.Instance._selected_talk_record = clickItem._fpcTalkRecord;

            MainActivity.Instance.startActivity(new Intent(MainActivity.Instance, Activity_talkHistoryPlayback.class));
        }
        if( v.getId() == R.id.button_listview_item_history_delete ||
            v.getId() == R.id.button_listview_item_history_delete1)
        {
            FpcRoot.Instance._selected_talk_record = clickItem._fpcTalkRecord;
            Activity_talkHistory.Instance.OpenDialog_deleteSelectedItem();
        }
    }



}
