package com.example.fang.fkznote.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.fang.fkznote.adapter.MyAdapter;
import com.example.fang.fkznote.db.DBManager;
import com.example.fang.fkznote.model.Note;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.xp.note.R;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {

//    private EditText et_search;
//    private Button bt_clear;
    private FloatingActionButton addBtn;
    private DBManager dm;
    private List<Note> noteDataList = new ArrayList<>();
    private MyAdapter adapter;
    private ListView listView;
    private TextView emptyListTextView;
    long waitTime = 2000;
    long touchTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    //初始化
    private void init() {
        dm = new DBManager(this);
        dm.readFromDB(noteDataList);
        listView = (ListView) findViewById(R.id.list);
        addBtn = (FloatingActionButton) findViewById(R.id.add);
        emptyListTextView = (TextView) findViewById(R.id.empty);
//        et_search = (EditText) findViewById(R.id.et_search);
//        bt_clear = (Button) findViewById(R.id.bt_clear);
//        bt_clear.setVisibility(GONE);
//        et_search.addTextChangedListener(this);
//        bt_clear.setOnClickListener(this);
        addBtn.setOnClickListener(this);
        adapter = new MyAdapter(this, noteDataList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new NoteClickListener());
        listView.setOnItemLongClickListener(new NoteLongClickListener());
        setStatusBarColor();
        updateView();
    }

    //空数据更新
    private void updateView() {
        if (noteDataList.isEmpty()) {
            listView.setVisibility(GONE);
            emptyListTextView.setVisibility(VISIBLE);
        } else {
            listView.setVisibility(VISIBLE);
            emptyListTextView.setVisibility(GONE);
        }
    }

    //设置状态栏同色
    public void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        // 创建状态栏的管理实例
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        // 激活状态栏设置
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setTintColor(Color.parseColor("#ff6cb506"));
    }

    //button单击事件
    @Override
    public void onClick(View view) {
        Intent i = new Intent(this, EditNoteActivity.class);
        switch (view.getId()) {
            case R.id.add:
                startActivity(i);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                finish();
//                break;
//            case R.id.bt_clear:
//                clearAllText();
        }
    }

//    private void clearAllText() {
//        et_search.setText("");
//    }

    //listView单击事件
    private class NoteClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            MyAdapter.ViewHolder viewHolder = (MyAdapter.ViewHolder) view.getTag();
            String noteId = viewHolder.tvId.getText().toString().trim();
            Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
            intent.putExtra("id", Integer.parseInt(noteId));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    //listView长按事件
    private class NoteLongClickListener implements AdapterView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(final AdapterView<?> adapterView, View view, final int i, long l) {
            final Note note = ((MyAdapter) adapterView.getAdapter()).getItem(i);
            if (note == null) {
                return true;
            }
            final int id = note.getId();
            new MaterialDialog.Builder(MainActivity.this)
                    .content(R.string.are_you_sure)
                    .positiveText(R.string.delete)
                    .negativeText(R.string.cancel)
                    .callback(new MaterialDialog.ButtonCallback() {
                                  @Override
                                  public void onPositive(MaterialDialog dialog) {
                                      DBManager.getInstance(MainActivity.this).deleteNote(id);
                                      adapter.removeItem(i);
                                      updateView();
                                  }
                              }
                    ).show();

            return true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                MaterialDialog dialog = new MaterialDialog.Builder(this)
                        .title(R.string.about)
                        .customView(R.layout.dialog_webview, false)
                        .positiveText(android.R.string.ok)
                        .build();
                WebView webView = (WebView) dialog.getCustomView().findViewById(R.id.webview);
                webView.loadUrl("file:///android_asset/webview.html");
                dialog.show();
                break;
            case R.id.action_clean:
                new MaterialDialog.Builder(MainActivity.this)
                        .content(R.string.are_you_sure)
                        .positiveText(R.string.clean)
                        .negativeText(R.string.cancel)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                for (int id = 0; id < 100; id++)
                                    DBManager.getInstance(MainActivity.this).deleteNote(id);
                                adapter.removeAllItem();
                                updateView();
                            }
                        }).show();

                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //按返回键时
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - touchTime) >= waitTime) {
            Toast.makeText(this, R.string.exit, Toast.LENGTH_SHORT).show();
            touchTime = currentTime;
        } else {
            finish();
        }
    }

//    @Override
//    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//    }
//
//    @Override
//    public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//    }

//    @Override
//    public void afterTextChanged(Editable editable) {
//        /**获取输入文字**/
//        String input = et_search.getText().toString().trim();
//        if (input.isEmpty()) {
//            bt_clear.setVisibility(GONE);
//        } else {
//            bt_clear.setVisibility(VISIBLE);
//        }
//    }
}
