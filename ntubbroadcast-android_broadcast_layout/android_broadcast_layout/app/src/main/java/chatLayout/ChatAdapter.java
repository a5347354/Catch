package chatLayout;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.broadcastone.broadcast.LocationCheckActivity;
import com.broadcastone.broadcast.PreviewImagesActivity;
import com.broadcastone.broadcast.android_broadcast.R;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import utils.BitmapTools;
import utils.FileTools;
import vo.ChatMessage;

/**
 * Created by Technovibe on 17-04-2015.
 */
public class ChatAdapter extends BaseAdapter {

    private final List<ChatMessage> chatChatMessages;
    private Activity context;
    private ImageView recordingPlaying;
    private MediaPlayer recordingPlayer;
    private final int REQUEST_PREVIEW_IMAGES = 1;
    private final int CLICK_LOCATION = 2;
    private boolean isAdMessage;
    private String adAmcAddress;


    public ChatAdapter(Activity context, List<ChatMessage> chatChatMessages) {
        this.context = context;
        this.chatChatMessages = chatChatMessages;
    }

    public ChatAdapter(Activity context, List<ChatMessage> chatChatMessages, boolean isAdMessage, String adAmcAddress) {
        this.context = context;
        this.chatChatMessages = chatChatMessages;
        this.isAdMessage = isAdMessage;
        this.adAmcAddress = adAmcAddress;
    }

    @Override
    public int getCount() {
        if (chatChatMessages != null) {
            return chatChatMessages.size();
        } else {
            return 0;
        }
    }

    @Override
    public ChatMessage getItem(int position) {
        if (chatChatMessages != null) {
            return chatChatMessages.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    /**
     * 設定Adapter資料呈現方式
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        final ChatMessage chatMessage = getItem(position);
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//        if (convertView == null){
//
//        }
        convertView = vi.inflate(R.layout.list_item_chat_message, null);
        holder = createViewHolder(convertView);

        boolean myMsg = chatMessage.getMacAddress().equals(BluetoothAdapter.getDefaultAdapter().getAddress());//Just a dummy check to simulate whether it me or other sender
        setAlignment(holder, myMsg);

        holder.txtInfo.setText(chatMessage.getDateTime());


        //判斷訊息為文字、圖片或著錄音檔 Judge Text or Picture or Record
        if (chatMessage.getText() != null && !chatMessage.getText().equals("")) {
            //TextView is visible
            holder.txtMessage.setVisibility(View.VISIBLE);
            holder.txtMessage.setText(chatMessage.getText());

            //點選位置訊息時瀏覽位置
            holder.txtMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (chatMessage.getText().substring(0, 12).equals("My Position：")) {
                        Intent intent = new Intent();
                        intent.setClass(context, LocationCheckActivity.class);
                        intent.putExtra("address", chatMessage.getText().substring(13));

                        if (isAdMessage)
                            intent.putExtra("adAmcAddress", adAmcAddress);
                        //intent.putParcelableArrayListExtra("photos", new ArrayList<?>(chatChatMessages));
                        context.startActivityForResult(intent, CLICK_LOCATION);
                    }
                }
            });


        } else if (chatMessage.getImageBytes() != null && chatMessage.getImageBytes().length > 1) {
            try {
                //TextView is gone
                holder.txtMessage.setVisibility(View.GONE);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                Bitmap bitmap = new BitmapFactory().decodeByteArray(chatMessage.getImageBytes(), 0, chatMessage.getImageBytes().length);
                bitmap = new BitmapTools().reSizeBitmap(bitmap, 1.2f);
                holder.imageView.setImageBitmap(bitmap);
                //點選圖片時預覽圖片
                holder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        intent.setClass(context, PreviewImagesActivity.class);
                        intent.putExtra("messageId", chatMessage.getId());
                        intent.putExtra("isAdMessage", isAdMessage);
                        intent.putExtra("position", position);
                        if (isAdMessage)
                            intent.putExtra("adAmcAddress", adAmcAddress);
                        Log.e("postiitonput", String.valueOf(position));
                        //intent.putParcelableArrayListExtra("photos", new ArrayList<?>(chatChatMessages));
                        context.startActivity(intent);
                    }
                });
            } catch (OutOfMemoryError e) {
                //  Log.i("Catch!", "OutOfMemoryError " + e.getMessage());
            }
        } else if (chatMessage.getRecordingBytes() != null && chatMessage.getRecordingBytes().length > 0) {
//            final Button playBtn = new Button(meetingActivity);
            holder.txtMessage.setVisibility(View.GONE);
            holder.imageView.setImageResource(R.drawable.play);
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.imageView.setImageResource(R.drawable.stop);
                    startPlay(chatMessage, holder.imageView);
                }
            });

//            playBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {startPlay(chatMessage, playBtn);}
//            });

        }

        return convertView;
    }

    public void add(ChatMessage chatMessage) {
        chatChatMessages.add(chatMessage);
    }

    public void add(List<ChatMessage> chatMessages) {
        chatChatMessages.addAll(chatMessages);
    }

    private void setAlignment(ViewHolder holder, boolean isMe) {
        if (isMe) {//判斷訊框是在左還右check message bubble is right or left
            holder.contentWithBG.setBackgroundResource(R.drawable.in_message_bg);

            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.contentWithBG.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            holder.content.setLayoutParams(lp);
            layoutParams = (LinearLayout.LayoutParams) holder.txtMessage.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.txtMessage.setLayoutParams(layoutParams);

            layoutParams = (LinearLayout.LayoutParams) holder.txtInfo.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.txtInfo.setLayoutParams(layoutParams);
        } else {
            holder.contentWithBG.setBackgroundResource(R.drawable.out_message_bg);

            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.contentWithBG.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            holder.content.setLayoutParams(lp);
            layoutParams = (LinearLayout.LayoutParams) holder.txtMessage.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.txtMessage.setLayoutParams(layoutParams);

            layoutParams = (LinearLayout.LayoutParams) holder.txtInfo.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.txtInfo.setLayoutParams(layoutParams);
        }
    }


    private void startPlay(ChatMessage chatMessage, final ImageView readyRecordingPlay) {
        try {
            if (readyRecordingPlay.equals(recordingPlaying)) {
                stopPlay();
            } else {
                stopPlay();
                readyRecordingPlay.setImageResource(R.drawable.stop);
                recordingPlaying = readyRecordingPlay;
                recordingPlayer = new MediaPlayer();
                File recordingFile = new File(context.getCacheDir() + File.separator + chatMessage.getId());
                //   Log.i("Catch!", "recordingFile exists  " + context.getCacheDir()+File.separator+ chatMessage.getId()+" "+recordingFile.exists());
                FileInputStream fis;
                if (!recordingFile.exists()) {
                    fis = new FileInputStream(new FileTools().saveFile(chatMessage.getId(), chatMessage.getRecordingBytes()));
                } else {
                    fis = new FileInputStream(recordingFile);
                }
                recordingPlayer.setDataSource(fis.getFD());
                recordingPlayer.prepare();
                recordingPlayer.start();
                recordingPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        recordingPlaying = null;
                        readyRecordingPlay.setImageResource(R.drawable.play);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Log.i("Catch!", "Error " + e.getMessage());
        }
    }

    private void stopPlay() {
        if (recordingPlaying != null)
            recordingPlaying.setImageResource(R.drawable.play);
        if (recordingPlayer != null)
            recordingPlayer.stop();
        this.recordingPlaying = null;
    }

    private ViewHolder createViewHolder(View v) {
        ViewHolder holder = new ViewHolder();
        holder.txtMessage = (TextView) v.findViewById(R.id.txtMessage);
        holder.imageView = (ImageView) v.findViewById(R.id.imageView);
        holder.content = (LinearLayout) v.findViewById(R.id.content);
        holder.contentWithBG = (LinearLayout) v.findViewById(R.id.contentWithBackground);
        holder.txtInfo = (TextView) v.findViewById(R.id.txtInfo);

        return holder;
    }


    private class ViewHolder {
        public TextView txtMessage;
        public TextView txtInfo;
        public ImageView imageView;
        public LinearLayout content;
        public LinearLayout contentWithBG;
    }
}
