package com.j2y.familypop.activity.lobby;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.j2y.engine.ColumnListView;
import com.j2y.familypop.MainActivity;
import com.j2y.familypop.activity.BaseActivity;
import com.j2y.familypop.activity.TouchMove;
import com.j2y.familypop.activity.client.Activity_clientStart;
import com.j2y.familypop.activity.server.Activity_serverStart;
import com.j2y.familypop.backup.Dialog_MessageBox_ok_cancel;
import com.j2y.familypop.client.FpcRoot;
import com.j2y.familypop.client.FpcTalkRecord;
import com.nclab.familypop.R;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
// Activity_talkHistory
//
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

public class Activity_talkHistory extends BaseActivity implements View.OnClickListener, AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener , View.OnLongClickListener
{
    public static Activity_talkHistory Instance;

    private ScrollView _scrollView_talkRecords;
    private ListViewAdapter_talkHistory _listView_adapter;
    public ColumnListView _listView_talkRecords;
    private ImageButton _button_addHistory;


    // 리스트뷰 해더
    private TextView _header_text_playtime;
    private TextView _header_text_name;
    private TextView _header_text_day;
    private ImageButton _header_button_play;
    private Button _header_button_delete;

    private RelativeLayout _header_layout_top_frame;



    // todo: 액션바로 분리
    // topmenu_role
    ImageView _background_role;
    ImageButton _button_role;
    Button _button_client;
    Button _button_locator;
    Button _button_server;

    // topmenu_setting
    ImageView _background_setting;
    ImageButton _button_setting;
    Button _button_setting1;
    Button _button_setting2;
    Button _button_setting3;



    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 초기화/종료
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        Instance = this;
        Log.i("[J2Y]", "Activity_talkHistory:onCreate");

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dialogue_history);


        ColumnListView listView = init_listView();

        init_actionBar();
        Load_talkRecords();

        setListViewHeightBasedOnChildren(listView);
    }


    @Override
    public void onDestroy()
    {
        Log.i("[J2Y]", "Activity_talkHistory:onDestroy");
        Instance = null;
        super.onDestroy();
    }



    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // ListView
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


    //------------------------------------------------------------------------------------------------------------------------------------------------------
    private ColumnListView init_listView()
    {

        _listView_adapter = new ListViewAdapter_talkHistory(this);
        ColumnListView listView = (ColumnListView) findViewById(R.id.listview_history);
        listView.setAdapter(_listView_adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
//                if (mSelectMode) {
//                    _listView_adapter.toggleSelected(position);
//                } else {
//                    Toast.makeText(Activity_talkHistory.this, "Clicked item: " + position, Toast.LENGTH_SHORT).show();
//                }
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id) {

                ListView_talkRecord clickItem = (ListView_talkRecord) _listView_adapter.getItem(position);
                if (null != clickItem) {
                    clickItem._button_delete.setVisibility(View.VISIBLE);
                }
                //Toast.makeText(Activity_talkHistory.this, "onItemLongClick: " + position, Toast.LENGTH_SHORT).show();

                return true;
            }
        });



        _header_text_playtime = (TextView) findViewById(R.id.listview_header_playtime);
        _header_text_name = (TextView) findViewById(R.id.listview_header_name);
        _header_text_day = (TextView) findViewById(R.id.listview_header_day);
        _header_button_play = (ImageButton) findViewById(R.id.listview_header_play);
        _header_button_delete = (Button) findViewById(R.id.listview_header_delete);

        _header_button_play.setOnClickListener(this);
        _header_button_delete.setOnClickListener(this);


        _scrollView_talkRecords = (ScrollView) findViewById(R.id.scrollView_talkRecords);
        _listView_talkRecords = listView;

        _listView_talkRecords.setOnItemClickListener(this);




        _header_layout_top_frame = (RelativeLayout) findViewById(R.id.layout_history_listview_top_frame);
        //_header_layout_top_frame.setOnClickListener(this);
        _header_layout_top_frame.setOnLongClickListener(this);


        return listView;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    public void setListViewHeightBasedOnChildren(ColumnListView listView) {
        //ListAdapter listAdapter = listView.getAdapter();
//        if (listAdapter == null) {
//            // pre-condition
//            return;
//        }

        // todo: honor 폰 에서 죽음

        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);
        int itemHeight = 0;
        for (int i = 0; i < _listView_adapter.getCount(); i++) {
            View listItem = _listView_adapter.getView(i, null, listView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            itemHeight = (listItem.getMeasuredHeight() / 2);
            totalHeight += itemHeight;
        }
        if(_listView_adapter.getCount() % 2 == 1)
            totalHeight += itemHeight;

        if(totalHeight < 900) // 하나일 때 이상하게 스크롤 됨
            totalHeight = 900;

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        //params.height = totalHeight + (listView.getDividerHeight() * (_listView_adapter.getCount() - 1));
        params.height = totalHeight;// + (300 * (_listView_adapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }





    //------------------------------------------------------------------------------------------------------------------------------------------------------
    public void Load_talkRecords() {

//        _listView_adapter = new ListViewAdapter_talkHistory(getApplicationContext());
//        _listView_talkRecord = (ListView)findViewById(R.id.test_listview);
//
//        _listView_talkRecord.setAdapter(_listView_adapter);
//        _listView_talkRecord.setOnItemLongClickListener(this);

        if( FpcRoot.Instance == null ) return;
        if(FpcRoot.Instance._talk_records != null) // ??
        {
            FpcRoot.Instance._talk_records.clear();
            FpcRoot.Instance.LoadTalkRecords(this); //
        }

        // add data
        ArrayList<FpcTalkRecord> talk_records = FpcRoot.Instance._talk_records;
        int record_count = talk_records.size();

        if(record_count >= 1) {

            if(record_count >= 2) {

                for (int i = record_count - 2; i >= 0; --i)
                {
                    FpcTalkRecord talk = FpcRoot.Instance._talk_records.get(i);
                    ListView_talkRecord item = new ListView_talkRecord();


                    item._text_name = talk._name;
                    item._text_day = CalculateDate(talk);
                    item._text_playTime = CalculatePlayTime(talk);
                    item._fpcTalkRecord = talk;
                    _listView_adapter.AddItem(item, false);
                }
            }

            // 해더
            FpcTalkRecord header = FpcRoot.Instance._talk_records.get(record_count - 1);
            ListView_talkRecord item = new ListView_talkRecord();
            _header_text_playtime.setText(CalculatePlayTime(header));
            _header_text_name.setText(header._name);
            _header_text_day.setText(CalculateDate(header));

            //test
            RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout_history_listEmpty);
            layout.setVisibility(View.INVISIBLE);
        }
        else
        {
            _scrollView_talkRecords.setVisibility(View.INVISIBLE);

            //test
            RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout_history_listEmpty);
            layout.setVisibility(View.VISIBLE);
        }

        _listView_talkRecords.setAdapter(_listView_adapter);

        _listView_adapter.notifyDataSetChanged();
        //_listView_talkRecords.reloadViews();
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // 재생시간 계산
    public static String CalculatePlayTime(FpcTalkRecord talk) {
        long diffSec = 0;
        long diffMin = 0;

        if(talk._endTime > talk._startTime)
        {
            diffSec = ((talk._endTime - talk._startTime) / 1000) % 60;// 초
            diffMin = (talk._endTime - talk._startTime) / 60000;       // 분
        }
        return String.format("0:%02d:%02d", diffMin, diffSec);
    }

    public static String CalculateDate(FpcTalkRecord talk)
    {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        Date start_time = new Date(talk._startTime);

        return sdf.format(start_time);
    }


    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // Click events
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.button_history_topmenu_role:
                //Popup_TopMenu_Role cdd = new Popup_TopMenu_Role(this);
                //cdd.getWindow().setGravity(Gravity.TOP | Gravity.LEFT);
                //cdd.show();

                // View backgroundimage = findViewById(R.id.background_popup_topmenu_role);
                //Drawable background = backgroundimage.getBackground();
//                RelativeLayout rLayout = (RelativeLayout) findViewById(R.id.layout_history_topmenu_role);
//                rLayout.setVisibility(View.VISIBLE);
                //rLayout.setVisibility(View.INVISIBLE);

                active_topmenu_setting(false);
                active_topmenu_role(true);

                break;
            case R.id.button_history_topmenu_setting:
                active_topmenu_setting(true);
                active_topmenu_role(false);
                break;
            // topmenu_role
            case R.id.button_history_topmenu_client: startActivity(new Intent(MainActivity.Instance, Activity_clientStart.class));   active_topmenu_role(false); break;
            case R.id.button_history_topmenu_locator: startActivity(new Intent(MainActivity.Instance, Activity_locatorStart.class)); active_topmenu_role(false); break;
            case R.id.button_history_topmenu_server:  startActivity(new Intent(MainActivity.Instance, Activity_serverStart.class));  active_topmenu_role(false); break;
            //case R.id.button_history_topmenu_server:  StartActivity(this, Activity_serverStart.class);  active_topmenu_role(false); break;
            case R.id.button_add_history:
                if( _touchMove_button_addHistory._isClick)
                {
                    startActivity(new Intent(MainActivity.Instance, Activity_mainRole.class));
                }
                break;

            case R.id.listview_header_play:
            {

                int record_count = FpcRoot.Instance._talk_records.size();
                if(record_count > 0) {
                    FpcRoot.Instance._selected_talk_record = FpcRoot.Instance._talk_records.get(record_count - 1);
                    MainActivity.Instance.startActivity(new Intent(MainActivity.Instance, Activity_talkHistoryPlayback.class));
                }
            }
                break;

            case R.id.listview_header_delete:
            {

                int record_count = FpcRoot.Instance._talk_records.size();
                if(record_count > 0)
                {

                    FpcRoot.Instance._selected_talk_record = FpcRoot.Instance._talk_records.get(record_count - 1);
                    OpenDialog_deleteSelectedItem();
                }

                _header_button_delete.setVisibility(View.INVISIBLE);
            }
                break;

            case R.id.layout_history_listview_top_frame:
                _header_button_delete.setVisibility(View.INVISIBLE);
                break;


            // topmenu_setting
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        //Toast.makeText(this, "sdfsdfsdf", Toast.LENGTH_SHORT).show();

        // 선택된 리스트 아이템 얻오오기.
        //test_listView_User clickItem = (test_listView_User) view.getTag();
        //test_listView_User clickItem = (test_listView_User) adapter.getItem(position);
        //adapter.remove(clickItem);
        //adapter.notifyDataSetChanged();
        if( view.getId() == R.id.test_listview_layout )
        {
            Button delect = (Button) findViewById(R.id.button_listview_item_history_delete1);
            delect.setVisibility(View.VISIBLE);
        }

        return true;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    public boolean onTouchEvent(MotionEvent e)
    {
        active_topmenu_role(false);
        active_topmenu_setting(false);

        return super.onTouchEvent(e);
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    // [이벤트] 백버튼 막기
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_BACK: {
                for(ListView_talkRecord item : _listView_adapter._talkRecord_items) {
                    if (item._button_delete.getVisibility() == View.VISIBLE) {
                        item._button_delete.setVisibility(View.INVISIBLE);
                        return true;
                    }
                }
            }
            break;
        }
        return super.onKeyDown(keyCode, event);
    }



    public void OpenDialog_deleteSelectedItem()
    {
        Dialog_MessageBox_ok_cancel megBox = new Dialog_MessageBox_ok_cancel(this, "OK", "Cancel")
        {
            @Override
            protected void onCreate(Bundle savedInstanceState)
            {
                super.onCreate(savedInstanceState);

                _content.setText("Do you really want to delete the record data?");
                _editText.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onClick(View v)
            {
                super.onClick(v);

                switch (v.getId())
                {
                    case R.id.button_custom_dialog_ok: // ok

                        FpcRoot.Instance.RemoveTalkRecord(FpcRoot.Instance._selected_talk_record);
                        FpcRoot.Instance.SaveTalkRecords();
                        FpcRoot.Instance._selected_talk_record = null;

                        _listView_adapter.ClearItems(false);
                        Load_talkRecords();
                        cancel();
                        break;

                    case R.id.button_custom_dialog_cancel:
                        cancel();
                        break;
                }
            }
        };

        megBox.show();
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // 액션바, GUI 세팅
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    private TouchMove _touchMove_button_addHistory;
    //------------------------------------------------------------------------------------------------------------------------------------------------------
    private void init_actionBar() {
        //ui
        _button_addHistory = (ImageButton) findViewById(R.id.button_add_history);
        _button_addHistory.setOnClickListener(this);

        _touchMove_button_addHistory = new TouchMove(_button_addHistory, 780.0f, 0.0f);


        //top menu role
        _button_role = (ImageButton) findViewById(R.id.button_history_topmenu_role);
        _background_role = (ImageView) findViewById(R.id._background_history_topmenu_role);
        _button_client = (Button) findViewById(R.id.button_history_topmenu_client);
        _button_locator = (Button) findViewById(R.id.button_history_topmenu_locator);
        _button_server = (Button) findViewById(R.id.button_history_topmenu_server);

        _button_role.setOnClickListener(this);
        _button_client.setOnClickListener(this);
        _button_locator.setOnClickListener(this);
        _button_server.setOnClickListener(this);

        //top menu setting
        _button_setting = (ImageButton) findViewById(R.id.button_history_topmenu_setting);
        _background_setting = (ImageView) findViewById(R.id._background_history_topmenu_setting);
        _button_setting1 = (Button) findViewById(R.id.button_history_topmenu_setting1);
        _button_setting2 = (Button) findViewById(R.id.button_history_topmenu_setting2);
        _button_setting3 = (Button) findViewById(R.id.button_history_topmenu_listclear);

        _button_setting.setOnClickListener(this);
        _button_setting1.setOnClickListener(this);
        _button_setting2.setOnClickListener(this);
        _button_setting3.setOnClickListener(this);

        active_topmenu_role(false);
        active_topmenu_setting(false);
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    public void active_topmenu_role(boolean active)
    {
        // RelativeLayout rLayout = (RelativeLayout) findViewById(R.id.layout_history_topmenu_role);
        if( active)
        {
            //rLayout.setVisibility(View.VISIBLE);
            _background_role.setVisibility(View.VISIBLE);
            _button_client.setVisibility(View.VISIBLE);
            _button_locator.setVisibility(View.VISIBLE);
            _button_server.setVisibility(View.VISIBLE);
        }
        else
        {
            _background_role.setVisibility(View.INVISIBLE);
            _button_client.setVisibility(View.INVISIBLE);
            _button_locator.setVisibility(View.INVISIBLE);
            _button_server.setVisibility(View.INVISIBLE);
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    public void active_topmenu_setting(boolean active)
    {
        // RelativeLayout rLayout = (RelativeLayout) findViewById(R.id.layout_history_topmenu_role);
        if( active)
        {
            //rLayout.setVisibility(View.VISIBLE);
            _background_setting.setVisibility(View.VISIBLE);
            _button_setting1.setVisibility(View.VISIBLE);
            _button_setting2.setVisibility(View.VISIBLE);
            _button_setting3.setVisibility(View.VISIBLE);
        }
        else
        {
            _background_setting.setVisibility(View.INVISIBLE);
            _button_setting1.setVisibility(View.INVISIBLE);
            _button_setting2.setVisibility(View.INVISIBLE);
            _button_setting3.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Activity_talkHistory.Instance.active_topmenu_setting(false);
        Activity_talkHistory.Instance.active_topmenu_role(false);
    }

    @Override
    public boolean onLongClick(View v)
    {
        switch (v.getId())
        {
            case R.id.layout_history_listview_top_frame:    _header_button_delete.setVisibility(View.VISIBLE);   break;
        }
        return false;
    }

    // class
//    public class TouchMove implements View.OnTouchListener
//    {
//        private final int _START_DRAG = 0;
//        private final int _END_DRAG = 1;
//        private int _isMoving;
//        private float _offset_x, _offset_y;
//        private float _action_downX;
//        private float _action_downY;
//
//        private boolean _actionDown = false;
//        private boolean _start_yn = true;
//        public boolean _isClick = false;
//        public boolean _move = true;
//
//        private float _startPosx = 780.0f;
//        private float _startPosy;
//
//
//        public float _normalX = 0.0f;
//        public float _normalY = 0.0f;
//
//        View _src;
//        public TouchMove(View src , float startPosX, float startPosy )
//        {
//            _src = src;
//            _src.setOnTouchListener(this);
//
//            _startPosx = startPosX;
//            _startPosy = startPosy;
//
//            if( src != null)
//            {
//                _src.setX(_startPosx);
//                _src.setY(_startPosy);
//            }
//        }
//
//        @Override
//        public boolean onTouch(View v, MotionEvent event)
//        {
//
//            int eventaction = event.getAction();
//            switch (eventaction)
//            {
//                case MotionEvent.ACTION_DOWN: // 손가락이 스크린에 닿았을 때
//
//                    if (_start_yn)
//                    {
//                        _offset_x = event.getRawX();
//                        _offset_y = event.getRawY();
//                        _start_yn = false;
//                    }
//
//                    if( !_actionDown )
//                    {
//                        _actionDown = true;
//
//                        _action_downX = event.getRawX();
//                        _action_downY = event.getRawY();
//                    }
//
//                    _isMoving = _START_DRAG;
//
//                    _isClick = true;
//
//                    break;
//                case MotionEvent.ACTION_MOVE: // 닿은 채로 손가락을 움직일 때
//
//                    int posx = (int) ((event.getRawX()) - (_offset_x - _startPosx));
//                    int posy = (int) (event.getRawY() - _offset_y);
//
//                    float nposx = event.getRawX() - _action_downX;
//                    float nposy = event.getRawY() - _action_downY;
//
//                    float effectiveAreaX = Math.abs(event.getRawX() - _action_downX );
//                    float effectiveAreaY = Math.abs(event.getRawY() - _action_downY);
//
//                    if( effectiveAreaX > 50 ||
//                            effectiveAreaY  > 50   )
//                    {
//                        _isClick = false;
//                    }
//
//                    if( _move )
//                    {
//                        v.setX(posx);
//                        v.setY(posy);
//                    }
//                    double dv = Math.sqrt(nposx * nposx + nposy * nposy);
//
//                    nposx /= dv;
//                    nposy /= dv;
//
//                    _normalX = nposx;
//                    _normalY = nposy;
//
//                    //Log.i("[J2Y]", "Activity_talkHistory:onDestroy");
//                    Log.i("[J2Y]", "x : " + _normalX+" y : " + _normalY);
//
//                    break;
//                case MotionEvent.ACTION_UP: // 닿았던 손가락을 스크린에서 뗄때
//                    _isMoving = _END_DRAG;
//                    _actionDown = false;
//                    break;
//            }
//            return false;
//        }
//    }
}
