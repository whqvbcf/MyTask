package com.iflytek.mytask.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.donkingliang.imageselector.utils.ImageUtil;
import com.donkingliang.imageselector.utils.UriUtils;
import com.donkingliang.imageselector.utils.VersionUtils;
import com.iflytek.mytask.R;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<String> mImages;
    private LayoutInflater mInflater;
    private boolean isAndroidQ = VersionUtils.isAndroidQ();

    public ImageAdapter(Context context) {
        mContext = context;
        this.mInflater = LayoutInflater.from(mContext);
    }

    public ArrayList<String> getImages() {
        return mImages;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.adapter_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if (position == mImages.size() - 1) {
            Glide.with(mContext)
                    .load(R.mipmap.add)
                    .into(holder.ivImage);
        } else {
            final String image = mImages.get(position);
            // 是否是剪切返回的图片
            boolean isCutImage = ImageUtil.isCutImage(mContext, image);
            if (isAndroidQ && !isCutImage) {
                Glide.with(mContext)
                        .load(UriUtils.getImageContentUri(mContext, image))
                        .into(holder.ivImage);
            } else {
                Glide.with(mContext).load(image).into(holder.ivImage);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mImages == null ? 0 : mImages.size();
    }

    public void refresh(ArrayList<String> images) {
        mImages = images;
        notifyDataSetChanged();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView ivImage;

        public ViewHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            ivImage.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(getAdapterPosition());
            }
        }
    }

    //设置长按以及点击监听方法
    //第一步：自定义一个回调接口来实现click和OnClick事件
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    //第二步：声明自定义的接口
    public OnItemClickListener mOnItemClickListener;

    //第三步：定义方法并暴露给外面的调用者
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }
}
