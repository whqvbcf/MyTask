package com.iflytek.mytask;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.net.Uri;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.donkingliang.imageselector.utils.ImageSelector;
import com.iflytek.mytask.adapter.ImageAdapter;


public class EditActivity extends Activity {
    private static String TAG = EditActivity.class.getSimpleName();
    private static int PHOTO_FROM_GALLERY = 106;
    DBService myDb;
    private Button btnCancel;
    private Button btnSave;
    private Button btnPhoto;
    private EditText titleEditText;
    private EditText contentEditText;
    private TextView timeTextView;
    private TextView photoText;

    private static final int REQUEST_CODE = 0x00000011;
    private RecyclerView rvImage;
    private ImageAdapter mAdapter;
    private static ArrayList<String> images  = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_editor);

        init();
        if (timeTextView.getText().length() == 0)
            timeTextView.setText(getTime());
    }

    private void init() {

        myDb = new DBService(this);
        SQLiteDatabase db = myDb.getReadableDatabase();
        titleEditText = findViewById(R.id.et_title);
        contentEditText = findViewById(R.id.et_content);
        timeTextView = findViewById(R.id.edit_time);
        photoText = findViewById(R.id.pic_uri);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSave = findViewById(R.id.btn_save);
        btnPhoto = findViewById(R.id.btn_pic);
        //按钮点击事件
        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SQLiteDatabase db = myDb.getWritableDatabase();
                ContentValues values = new ContentValues();

                String title = titleEditText.getText().toString();
                String content = contentEditText.getText().toString();
                String time = timeTextView.getText().toString();
                String photo = photoText.getText().toString();

                if ("".equals(titleEditText.getText().toString())) {
                    Toast.makeText(EditActivity.this, "标题不能为空", Toast.LENGTH_LONG).show();
                    return;
                }
                if ("".equals(contentEditText.getText().toString())) {
                    Toast.makeText(EditActivity.this, "内容不能为空", Toast.LENGTH_LONG).show();
                    return;
                }
                values.put(DBService.TITLE, title);
                values.put(DBService.CONTENT, content);
                values.put(DBService.TIME, time);
                values.put(DBService.PIC, photo);
                db.insert(DBService.TABLE, null, values);
                Toast.makeText(EditActivity.this, "保存成功", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(EditActivity.this, MainActivity.class);
                startActivity(intent);
                db.close();
            }
        });
        btnPhoto.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                getPhoto();
            }
        });


        rvImage = findViewById(R.id.rv_image);
        rvImage.setLayoutManager(new GridLayoutManager(this, 3));
        mAdapter = new ImageAdapter(this);
        rvImage.setAdapter(mAdapter);
        if(!images.contains("添加数据，使加号始终保持在最后一个！")){
            images.add("添加数据，使加号始终保持在最后一个！");
        }
        mAdapter.refresh(images);
        mAdapter.setOnItemClickListener(new ImageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if(position == images.size() - 1){
                    ImageSelector.builder()
                            .useCamera(true) // 设置是否使用拍照
                            .setSingle(false)  //设置是否单选
                            .canPreview(true) //是否点击放大图片查看,，默认为true
                            .setMaxSelectCount(0) // 图片的最大选择数量，小于等于0时，不限数量。
                            .start(EditActivity.this, REQUEST_CODE); // 打开相册
                }
            }
        });
    }

    public void getPhoto() {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
        } else {
            intent = new Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        startActivityForResult(intent, PHOTO_FROM_GALLERY);
    }


    //获取当前时间
    private String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String str = sdf.format(date);
        return str;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //ContentResolver resolver = getContentResolver();
        super.onActivityResult(requestCode, resultCode, data);
        //第一层switch
//        if (requestCode == PHOTO_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
//            if (data != null) {
//                Uri uri = data.getData();//获取Intent uri
//                photoText.setText(uri.toString());
//            } else {
//                Toast.makeText(this, "获取图片uri失败", Toast.LENGTH_SHORT).show();
//            }
//        }
        if (requestCode == REQUEST_CODE && data != null) {
            images = data.getStringArrayListExtra(ImageSelector.SELECT_RESULT);
            for (int i = 0;i < images.size(); i++){
                Log.e(TAG,"选中的图片分别为：" + images.get(i));
            }
            if(!images.contains("添加数据，使加号始终保持在最后一个！")){
                images.add("添加数据，使加号始终保持在最后一个！");
            }
            mAdapter.refresh(images);
        }
    }

}
