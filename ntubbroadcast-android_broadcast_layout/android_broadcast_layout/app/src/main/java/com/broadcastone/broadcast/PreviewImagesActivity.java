package com.broadcastone.broadcast;

import android.app.Activity;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.broadcastone.broadcast.android_broadcast.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import Adapter.PreviewImagesAdapter;
import service.CatchService;
import utils.FileTools;
import vo.ChatMessage;

/**
 * Created by fan on 2015/11/21.
 * Preview Image Activity 預覽圖片
 */
public class PreviewImagesActivity extends Activity implements CompoundButton.OnCheckedChangeListener, ViewPager.OnPageChangeListener{
    private ViewPager mViewPager;
    private PreviewImagesAdapter adapter;

    private List<ChatMessage> chatHistory;
    private ArrayList<ChatMessage> datas;
    private boolean[] isImageSelected;
    private boolean isAdMessage;
    private int selectedCount = 0;
    private int imageBtnPostition = 0;//default Chat Image
    private String messageId="";
    private String adAmcAddress = "";//如果是廣告訊息，為裝置的藍芽Address
    private ImageButton storeBtn;
    private ImageButton crossBtn;
    private TextView countTextView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_images);
        //從ChatAdapter回傳值
        messageId = getIntent().getExtras().getString("messageId");
        isAdMessage = getIntent().getExtras().getBoolean("isAdMessage");
        imageBtnPostition = getIntent().getExtras().getInt("position");
        if(isAdMessage)
            adAmcAddress = getIntent().getExtras().getString("adAmcAddress");

//        datas = (ArrayList) getIntent().getParcelableArrayListExtra("images");
//        selectedCount = datas.size();
//        isImageSelected = new boolean[datas.size()];
//        for (int i = 0; i < isImageSelected.length; i++) {
//            isImageSelected[i] = true;
//        }

        initViews();
    }

    private void initViews() {
        //mTitleBarTitle.setText("");


        mViewPager = (ViewPager)findViewById(R.id.preview_images_pager);
        storeBtn = (ImageButton)findViewById(R.id.preview_images_store);
        crossBtn = (ImageButton)findViewById(R.id.preview_images_cross);
        countTextView = (TextView)findViewById(R.id.preview_images_text);


        List<ChatMessage> tempHistory= new ArrayList<ChatMessage>();
        if(CatchService.messageDAO!=null) {
            chatHistory = new ArrayList<ChatMessage>();
            if (!isAdMessage) {
                tempHistory = CatchService.messageDAO.getAllPersonalMessage();
            } else {
                tempHistory = CatchService.messageDAO.getAllAdMessage(adAmcAddress);
            }
        }
       int  currentPostition=0;
        int imageCounter=0;
        for(int i=0;i<tempHistory.size();i++){
            if(tempHistory.get(i).getImageBytes()!=null && tempHistory.get(i).getImageBytes().length>1){
                chatHistory.add(tempHistory.get(i));
                imageCounter=imageCounter+1;
                if(tempHistory.get(i).getId().equals(messageId))currentPostition=imageCounter;
            }
        }

        //chatHistory歷史訊息
        adapter = new PreviewImagesAdapter(this, chatHistory);


        mViewPager.setAdapter(adapter);
        //設定ViewPager預設圖片
        mViewPager.setCurrentItem(currentPostition-1);
        mViewPager.setOnPageChangeListener(this);

        countTextView.setText((currentPostition)+"/"+adapter.getCount());
        //下載圖片按鈕
        storeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentChatMessae = mViewPager.getCurrentItem();
                String fileName = chatHistory.get(currentChatMessae).getId();
                ChatMessage chatMessage=CatchService.messageDAO.getMessageById(chatHistory.get(currentChatMessae).getId());
                byte[] imageBytes = chatMessage.getImageBytes();
                File file=new FileTools().saveFile(fileName+".jpg", imageBytes);
                Toast.makeText(getWindow().getContext(),"save:"+file.getAbsolutePath(),Toast.LENGTH_SHORT).show();
                Log.e("storeBtn", "storeBtn");
            }
        });
        //叉叉會關閉Activity--finish()
        crossBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        onBackClicked(null);
    }

//    @Override
//    protected void onBackClicked(View view) {
//        setResult(RESULT_CANCELED, getResultIntent());
//        finish();
//    }
//
//    @Override
//    protected void onTitleClicked(View view) {
//
//    }
//
//    @Override
//    protected void onFunctionClicked(View view) {
//        setResult(RESULT_OK, getResultIntent());
//        finish();
//    }

//    private Intent getResultIntent() {
//
//        ArrayList<ChatMessage> datas = adapter.getDatas();
//
//        for (int i = isImageSelected.length - 1; i >= 0; i--) {
//            if (!isImageSelected[i]) {
//                datas.remove(i);
//            }
//        }
//
//        return new Intent().putParcelableArrayListExtra("photos", datas);
//    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    private boolean byUser = true;

    @Override
    public void onPageSelected(int position) {
        byUser = false;
        byUser = true;
        countTextView.setText((position+1)+"/"+adapter.getCount());
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (byUser) {
            isImageSelected[mViewPager.getCurrentItem()] = isChecked;
            if (isChecked) {
                selectedCount++;
            } else {
                selectedCount--;
            }
        }
        byUser = true;
    }
}
