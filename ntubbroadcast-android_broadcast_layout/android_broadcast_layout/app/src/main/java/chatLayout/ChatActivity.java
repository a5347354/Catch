package chatLayout;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.broadcastone.broadcast.LocationActivity;
import com.broadcastone.broadcast.android_broadcast.R;

import org.apache.commons.lang.SerializationUtils;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import blueToothDevices.DeviceInfo;
import blueToothDevices.DevicesManager;
import service.CatchService;
import utils.BitmapTools;
import utils.FileTools;
import vo.ChatMessage;


public class ChatActivity extends Activity {
    public static final int CHOSE_LOCATION=1;
    public static final int CHOSE_IMAGE=2;
    public static final int CHOSE_RECORDING=3;
    public static final int SEND_IMAGE=4;

    private TextView stateBar;
    private EditText messageET;
    private static ListView messagesContainer;
    private ImageView meeting_back_ImgBtn;
    private ImageButton meeting_addBtn,sendBtn;
    private static ChatAdapter adapter;
    private List<ChatMessage> chatHistory;
    public static Handler  chatActivityHandler;
    public static Handler progressBarHandler;

    private ProgressDialog progressDialog =null;
    public  int progressBarStatus=0;
    private boolean isInterruptProgressBar =true;
    private RelativeLayout meeting_botton;

    private boolean isAdMessage =false;
    private String adAmcAddress="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting);
        stateBar=(TextView) findViewById(R.id.stateBar);
        stateBar.setText("connected count :" + CatchService.currentDevicesInfo.size());
        Intent intent = getIntent();
        if(intent!=null && intent.getStringExtra("type")!=null  && intent.getStringExtra("type").equals("ad")){
          //  Log.i("Catch!","ad message");
            meeting_botton=(RelativeLayout)findViewById(R.id.meeting_botton);
            meeting_botton.setVisibility(View.GONE);
            isAdMessage=true;
            adAmcAddress=intent.getStringExtra("macAddress");
            ((TextView)findViewById(R.id.chatTitle)).setText(intent.getStringExtra("supName") + "  ad");

            chatActivityHandler=new Handler(){
                public void handleMessage(Message msg) {
                    if(msg.getData().getString("stateBar")!=null){
                        stateBar.setText(msg.getData().getString("stateBar").toString());
                    }
                    if(msg.getData()!=null && msg.getData().getByteArray("adMessage")!=null){
                        ChatMessage chatChatMessage =(ChatMessage)SerializationUtils.deserialize(msg.getData().getByteArray("adMessage"));
                        displayMessage(chatChatMessage);
                    }
                }
            };
        }else{
          //  Log.i("Catch!","chart message");
             ((TextView)findViewById(R.id.chatTitle)).setText("Public Chat");
            chatActivityHandler=new Handler(){
                public void handleMessage(Message msg) {
                    if(msg.getData().getString("stateBar")!=null){
                        stateBar.setText(msg.getData().getString("stateBar").toString());
                    }else{
                        if(msg.getData().getString("type")!=null && msg.getData().getString("type").equals("sendFail")){
                            Toast.makeText(ChatActivity.this,"send fail", Toast.LENGTH_LONG).show();
                        } else{
                            if(msg.getData().getByteArray("chatMessage")!=null){
                                ChatMessage chatChatMessage =(ChatMessage)SerializationUtils.deserialize(msg.getData().getByteArray("chatMessage"));
                                displayMessage(chatChatMessage);
                            }
                        }
                    }
                }
            };

            progressBarHandler = new Handler(){
                public void handleMessage(Message msg) {

                    switch (msg.getData().getString("type")){
                        case "show":
                            progressDialog = new ProgressDialog(ChatActivity.this);
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                            progressDialog.setTitle(msg.getData().getString("title"));
                            progressDialog.setMessage(msg.getData().getString("message"));
                            progressDialog.setCancelable(false);
                            progressDialog.setProgress(0);
                            final int max=msg.getData().getInt("max");
                            progressDialog.setMax(max);
                            progressDialog.show();

                            new Thread(new Runnable() {
                                public void run() {
                                    while (progressBarStatus < max && !isInterruptProgressBar) {
                                        progressBarHandler.post(new Runnable() {
                                            public void run() {
                                                if (progressDialog != null)
                                                    progressDialog.setProgress(progressBarStatus);
                                            }
                                        });
                                        try {
                                            Thread.sleep(1000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if (progressBarStatus >= max) {
                                        try {
                                            Thread.sleep(2000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        if (progressDialog != null)
                                            progressDialog.dismiss();
                                    }
                                }
                            }).start();

                            break;
                        case "increase":
                            if(progressDialog !=null)
                                progressDialog.incrementProgressBy(msg.getData().getInt("len",0));

                            progressBarStatus=progressBarStatus+msg.getData().getInt("len",0);
                          //  Log.i("Catch!","progressBarStatus:"+progressBarStatus);
                            break;
                        case "stop":
                            isInterruptProgressBar = true;
                            if (progressDialog != null) {
                                progressDialog.dismiss();
//
                                progressDialog = null;
                            }
                            break;

                    }
                }
            };
        }
        initControls();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initControls() {
        messagesContainer = (ListView) findViewById(R.id.messagesContainer);
        messageET = (EditText) findViewById(R.id.meeting_text);

        meeting_back_ImgBtn = (ImageView) findViewById(R.id.backBtn);
        meeting_back_ImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent = new Intent();
                //intent.setClass(ChatActivity.this, MainActivity.class);
                //startActivity(intent);    //觸發換頁
                finish();   //結束本頁
            }
        });

        meeting_addBtn =(ImageButton) findViewById(R.id.meeting_add);
        meeting_addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog alertDialog = new Dialog(ChatActivity.this);
                alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                View myView = LayoutInflater.from(ChatActivity.this).inflate(R.layout.activity_meeting_plus, null);    //取得alelrtDialg內容VIEW
                alertDialog.setContentView(myView);
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));    //alertDialog背景透明
                WindowManager.LayoutParams lp;
                Window window = alertDialog.getWindow();    //alertDialog交給Window控管

                DisplayMetrics metrics = new DisplayMetrics();   //screen size function
                getWindowManager().getDefaultDisplay().getMetrics(metrics);   //get screen size
                int alertdialogLocation ;
                alertdialogLocation = (metrics.heightPixels /2)*65/96;   //計算高度

               // findViewById(R.id.statusTitle).setVisibility(View.GONE);   //顯示
                findViewById(R.id.statusTitle).setVisibility(View.VISIBLE);   //隱藏
                lp = window.getAttributes();
                // lp.alpha = 0.5f;//透明度
                lp.x = 0;   //
                lp.y = alertdialogLocation;//高度  (正向在下)
                lp.dimAmount = 0f; //背景不變黑  0f~1F
                window.setAttributes(lp);

                ImageButton add_location = (ImageButton) myView.findViewById(R.id.add_location);
                ImageButton add_photo = (ImageButton) myView.findViewById(R.id.add_photo);
                ImageButton add_voice = (ImageButton) myView.findViewById(R.id.add_voice);
                add_location.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                        Toast.makeText(ChatActivity.this, "Open location", Toast.LENGTH_SHORT).show();     //location按鈕事件在此
                        Intent intent = new Intent();
                        intent.setClass(v.getContext(), LocationActivity.class);
                        startActivityForResult(intent, ChatActivity.CHOSE_LOCATION);    //觸發換頁
                        //finish();
                    }
                });
                add_photo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                        Toast.makeText(ChatActivity.this, "photo", Toast.LENGTH_SHORT).show();    //photo按鈕事件在此
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");  // 選擇圖片
                        startActivityForResult(intent, ChatActivity.CHOSE_IMAGE);
                    }
                });
                add_voice.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                        Toast.makeText(ChatActivity.this, "voice", Toast.LENGTH_SHORT).show();    //voice按鈕事件在此
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("audio/amr"); //錄音    多個音訊類型 :  audio/amr;audio/mp3
                        startActivityForResult(intent, ChatActivity.CHOSE_RECORDING);
                    }
                });
                alertDialog.show();
            }
        });


        sendBtn = (ImageButton) findViewById(R.id.meeting_send);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageET.getText().toString();
                if (TextUtils.isEmpty(messageText)) {
                    return;
                }
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setText(messageText);
                sendMessage(chatMessage);
                progressBarStatus=0;
            }
        });
        loadDummyHistory();
    }


    public void sendMessage(final ChatMessage chatMessage){
        messageET = (EditText) findViewById(R.id.meeting_text);
        chatMessage.setId(new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()).toString());// 為訊息產生ID  避免其他裝置收到相同的訊息
        chatMessage.setMacAddress(BluetoothAdapter.getDefaultAdapter().getAddress());
        chatMessage.setDeviceName(BluetoothAdapter.getDefaultAdapter().getName());//必須從 PreferencesSeting 取的使用者自訂的名稱  目前先用裝置名稱代替
        chatMessage.setDateTime(DateFormat.getDateTimeInstance().format(new Date()));

        //計算 要把訊息傳給多少人
        int sendNumber=0;
        Iterator<Map.Entry<String, DeviceInfo>> it = CatchService.currentDevicesInfo.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, DeviceInfo> entry = it.next();
            if(entry .getValue().getDevice().getName().startsWith("Catch")){
                sendNumber=sendNumber+1;
            }
        }
        final int dataLenght=SerializationUtils.serialize(chatMessage).length*sendNumber ;
        //if(CatchService.currentDevicesInfo!=null && tempNumber>=1 ){ //有連線裝置數量>0

        if(true ){ //demo 測試使用

            Message msgObj = progressBarHandler.obtainMessage();
            progressBarStatus=0;
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    if(chatMessage.getImageBytes().length>1){
                        Bundle b = new Bundle();
                        b.putString("type", "show");
                        b.putString("title", "Wait");
                        b.putString("message", "Sending Image...");
                        b.putInt("max", dataLenght);
                        Message msgObj =progressBarHandler.obtainMessage();
                        msgObj.setData(b);
                        progressBarHandler.sendMessage(msgObj);
                    }
                    if(chatMessage.getRecordingBytes().length>1){
                        Bundle b = new Bundle();
                        b.putString("type", "show");
                        b.putString("title", "Wait");
                        b.putString("message", "Sending Recording File...");
                        b.putInt("max", dataLenght);
                        Message msgObj =progressBarHandler.obtainMessage();
                        msgObj.setData(b);
                        progressBarHandler.sendMessage(msgObj);
                    }
                    chatMessage.setMessageClass("personal");//設定訊息為"私人訊息"

                   if( DevicesManager.sendMessageToAll(chatMessage)>=1){ // 至少 成功  傳送 給一個人  才能存入資料庫裡
                       CatchService.messageDAO.addMessage(chatMessage);//儲存 自己傳送的訊息
                       Bundle b = new Bundle();
                       b.putSerializable("chatMessage", SerializationUtils.serialize(chatMessage));
                       if(chatActivityHandler!=null){
                           Message msgObj = chatActivityHandler.obtainMessage();
                           msgObj.setData(b);
                           chatActivityHandler.sendMessage(msgObj);
                       }
                       ChatActivity.this.runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               messageET.setText("");
                           }
                       });
                   }else{
                       Bundle b = new Bundle();
                       b.putString("type", "sendFail");
                       if(chatActivityHandler!=null){
                           Message msgObj =chatActivityHandler.obtainMessage();
                           msgObj.setData(b);
                           chatActivityHandler.sendMessage(msgObj);
                       }
                   }
                    Bundle b = new Bundle();//發送成功 or 失敗  強制關閉 進度條 否則畫面會卡住
                    b.putString("type", "stop");

                    Message msgObj = ChatActivity.progressBarHandler.obtainMessage();
                    msgObj.setData(b);
                    ChatActivity.progressBarHandler.sendMessage(msgObj);
                }
            });
            t.start();
        }
    }

    public static void displayMessage(ChatMessage chatMessage) {
        adapter.add(chatMessage);
        adapter.notifyDataSetChanged();
        scroll();
    }

    private static void scroll() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }
    /**
     * 從資料庫中取得歷史訊息，並判斷是廣告訊息還是個人訊息
     * */
    private void loadDummyHistory(){
        if(CatchService.messageDAO!=null){
            chatHistory = new ArrayList<ChatMessage>();

            if(!isAdMessage){
                chatHistory =CatchService.messageDAO.getAllPersonalMessage();
            }else {
                chatHistory =CatchService.messageDAO.getAllAdMessage(adAmcAddress);
            }

            adapter = new ChatAdapter(ChatActivity.this, new ArrayList<ChatMessage>() ,isAdMessage ,adAmcAddress);
            messagesContainer.setAdapter(adapter);
            for(int i=0; i<chatHistory.size(); i++) {
                ChatMessage chatMessage = chatHistory.get(i);
                displayMessage(chatMessage);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        try {
            ChatMessage chatMessage =new ChatMessage();
            //Log.i("Catch!","resultCode="+resultCode);
            //Log.i("Catch!","requestCode="+requestCode);
            //Log.i("Catch!","data!=null="+(data!=null));
            //Log.i("Catch!","data.getData()="+(data.getData()));
            if(data!=null &&  resultCode== Activity.RESULT_OK){
                Uri fileUri=null;
                if(data.getData()!=null){
                    fileUri = data.getData();    //取得 選擇 圖片、錄音擋的路徑
                    String filepath =new FileTools().getRealPathFromURI(this, fileUri);
                    File file=new File(filepath);
                    if(file.exists())fileUri=Uri.fromFile(file);
                }

                switch(requestCode){
                    case CHOSE_LOCATION:
                        String s = data.getExtras().getString("location");
                        Toast.makeText(ChatActivity.this, s, Toast.LENGTH_SHORT).show();
                        chatMessage = new ChatMessage();
                        chatMessage.setText(s);
                        chatMessage.setSendNumber(5);//有圖片 or 錄音檔案  設定始能發送一次
                        sendMessage(chatMessage);//目前直接傳送  跳過二次確認
                        break;
                    case CHOSE_IMAGE:
                        if(fileUri!=null){
                            Intent intent = new Intent("com.android.camera.action.CROP");
                            intent.setDataAndType(fileUri, "image/*");
                            intent.putExtra("crop", "true");
                            intent.putExtra("aspectX", 300);
                            intent.putExtra("aspectY", 400);
                            intent.putExtra("outputX", 300);
                            intent.putExtra("outputY", 400);
                            intent.putExtra("outputFormat", "JPEG");// 圖片格式
                            intent.putExtra("return-data", true);
                            startActivityForResult(intent, SEND_IMAGE);
                        }
                        break;
                    case CHOSE_RECORDING:
                        File recorderFile=new File(fileUri.getPath());   //建立 來源檔案
                        if(recorderFile.exists()){
                            //Log.i("Catch!", recorderFile.getAbsolutePath());
                            chatMessage.setRecordingBytes(new FileTools().getBytesFromFile(recorderFile));// 使用 FileTools.getBytesFromFile(file) 將檔案轉成bytes
                            chatMessage.setSendNumber(1);//有圖片 or 錄音檔案  設定始能發送一次
                            sendMessage(chatMessage);//目前直接傳送  跳過二次確認
                        }else{
                            //Log.i("Catch!", "recorderFile.exists() :"+recorderFile.exists());
                        }
                        break;
                    case SEND_IMAGE:
                        Bitmap bitmap= data.getParcelableExtra("data");
                        //Log.i("Catch!","bitmap :"+ bitmap.getByteCount() + "");
                        //if(bitmap.getByteCount()>1000000)
                        bitmap =new BitmapTools().reSizeBitmap(bitmap,0.9f);
                        //Log.i("Catch!","bitmap :"+ bitmap.getByteCount() + "");
                        chatMessage.setImageBitmap(bitmap);
                        chatMessage.setSendNumber(1);//有圖片 or 錄音檔案  設定始能發送一次
                        sendMessage(chatMessage);//目前直接傳送  跳過二次確認
                        break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        chatActivityHandler=null;
        super.onDestroy();
        System.gc();        //呼叫gargabe collection
    }
    @Override
    protected void onStart(){
        super.onStart();
//        Intent intent2 = getIntent();
//        try{
//            if (intent2 != null) {
//                // 获取该Intent所携带的数据
//                String s = intent2.getExtras().getString("location");
//                Toast.makeText(ChatActivity.this, s, Toast.LENGTH_SHORT).show();
//                ChatMessage chatMessage = new ChatMessage();
//                chatMessage.setText(s);
//                chatMessage.setSendNumber(1);//有圖片 or 錄音檔案  設定始能發送一次
//                sendMessage(chatMessage);//目前直接傳送  跳過二次確認
//            }
//        }catch (NullPointerException e){
//            Toast.makeText(ChatActivity.this, "Public Chat", Toast.LENGTH_SHORT).show();
//        }
    }




}
