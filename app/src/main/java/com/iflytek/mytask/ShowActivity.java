package com.iflytek.mytask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.donkingliang.imageselector.utils.ImageSelector;
import com.iflytek.mytask.adapter.ImageAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class ShowActivity extends Activity {
    private static String TAG = ShowActivity.class.getSimpleName();
    private Button btnSave;
    private Button btnCancel;
    private TextView showTime;
    private EditText showContent;
    private EditText showTitle;

    private Values value;
    DBService myDb;

    private static final int REQUEST_CODE = 0x00000012;
    private RecyclerView rvImage;
    private ImageAdapter mAdapter;
    private static ArrayList<String> images = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        init();
    }

    public void init() {
        myDb = new DBService(this);
        btnCancel = findViewById(R.id.show_cancel);
        btnSave = findViewById(R.id.show_save);
        showTime = findViewById(R.id.show_time);
        showTitle = findViewById(R.id.show_title);
        showContent = findViewById(R.id.show_content);

        Intent intent = this.getIntent();
        if (intent != null) {
            value = new Values();

            value.setTime(intent.getStringExtra(DBService.TIME));
            value.setPic(intent.getStringExtra(DBService.PIC));
            value.setTitle(intent.getStringExtra(DBService.TITLE));
            value.setContent(intent.getStringExtra(DBService.CONTENT));
            value.setId(Integer.valueOf(intent.getStringExtra(DBService.ID)));

            showTime.setText(value.getTime());
            showTitle.setText(value.getTitle());
            showContent.setText(value.getContent());
        }

        //按钮点击事件
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pic = String.join(",", images);

                SQLiteDatabase db = myDb.getWritableDatabase();
                ContentValues values = new ContentValues();
                String content = showContent.getText().toString();
                String title = showTitle.getText().toString();

                values.put(DBService.TIME, getTime());
                values.put(DBService.PIC, pic);
                values.put(DBService.TITLE, title);
                values.put(DBService.CONTENT, content);

                db.update(DBService.TABLE, values, DBService.ID + "=?", new String[]{value.getId().toString()});
                Toast.makeText(ShowActivity.this, "修改成功", Toast.LENGTH_LONG).show();
                db.close();
                Intent intent = new Intent(ShowActivity.this, TaskBoard.class);
                startActivity(intent);
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String content = showContent.getText().toString();
                final String title = showTitle.getText().toString();

                new AlertDialog.Builder(ShowActivity.this)
                        .setTitle("提示框")
                        .setMessage("是否保存当前内容?")
                        .setPositiveButton("yes",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String pic = String.join(",", images);

                                        SQLiteDatabase db = myDb.getWritableDatabase();
                                        ContentValues values = new ContentValues();
                                        values.put(DBService.TIME, getTime());
                                        values.put(DBService.PIC, pic);
                                        values.put(DBService.TITLE, title);
                                        values.put(DBService.CONTENT, content);
                                        db.update(DBService.TABLE, values, DBService.ID + "=?", new String[]{value.getId().toString()});
                                        Toast.makeText(ShowActivity.this, "修改成功", Toast.LENGTH_LONG).show();
                                        db.close();
                                        Intent intent = new Intent(ShowActivity.this, TaskBoard.class);
                                        startActivity(intent);
                                    }
                                })
                        .setNegativeButton("no",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(ShowActivity.this, TaskBoard.class);
                                        startActivity(intent);
                                    }
                                }).show();
            }
        });
        images = new ArrayList(Arrays.asList(value.getPic().split(",")));

        rvImage = findViewById(R.id.rv_image);
        rvImage.setLayoutManager(new GridLayoutManager(this, 3));
        mAdapter = new ImageAdapter(this);
        rvImage.setAdapter(mAdapter);
        if (!images.contains("添加数据，使加号始终保持在最后一个！")) {
            images.add("添加数据，使加号始终保持在最后一个！");
        }
        mAdapter.refresh(images);
        mAdapter.setOnItemClickListener(new ImageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (position == images.size() - 1) {
                    ImageSelector.builder()
                            .useCamera(true) // 设置是否使用拍照
                            .setSingle(false)  //设置是否单选
                            .canPreview(true) //是否点击放大图片查看,，默认为true
                            .setMaxSelectCount(0) // 图片的最大选择数量，小于等于0时，不限数量。
                            .start(ShowActivity.this, REQUEST_CODE); // 打开相册
                }
            }
        });
        for (int i = 0; i < images.size(); i++) {
            Log.e(TAG, "init 的图片分别为：" + images.get(i));
        }
    }

    String getTime() {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
        //获取当前时间
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //ContentResolver resolver = getContentResolver();
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && data != null) {
            images = data.getStringArrayListExtra(ImageSelector.SELECT_RESULT);
            for (int i = 0; i < images.size(); i++) {
                Log.e(TAG, "选中的图片分别为：" + images.get(i));
            }
            if (!images.contains("添加数据，使加号始终保持在最后一个！")) {
                images.add("添加数据，使加号始终保持在最后一个！");
            }
            mAdapter.refresh(images);
        }
        String str = String.join(",", images);
        //photoText.setText(str);
    }
}
