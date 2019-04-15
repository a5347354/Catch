package Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.davemorrissey.labs.subscaleview.ScaleImageView;

import java.util.List;

import service.CatchService;
import utils.BitmapTools;
import vo.ChatMessage;


/**
 * Created by fan on 2015/11/21.
 * Preview Images Adapter
 */
public class PreviewImagesAdapter extends PagerAdapter{
    private Context context;
    private List<ChatMessage> chatMessageList;

    public PreviewImagesAdapter(Context context,List<ChatMessage> chatMessagesList ) {
        this.context = context;
        this.chatMessageList = chatMessagesList;
    }

    public List<ChatMessage> getChatMessageList() {
        return chatMessageList;
    }

    public void setDatas(List<ChatMessage> chatMessagesList) {
        this.chatMessageList = chatMessagesList;
    }

    @Override
    public int getCount() {
        if (chatMessageList == null) {
            return 0;
        }
        return chatMessageList.size();
    }

    @Override
    public View instantiateItem(ViewGroup container, int position) {
        //GestureImageView photoView = new GestureImageView(container.getContext());
        //photoView.setImageURI(datas.get(position));
        //ImageLoader.getInstance().loadImage(datas.get(position).toString(),photoView);

        // Now just add PhotoView to ViewPager and return it
        //container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

//        WebView webView = new WebView(context);
//        String html = "<img src=\"" + datas.get(position).toString() + "\" width=\"100%\"/>";
//        webView.loadDataWithBaseURL("file:///", html, "text/html", "utf-8", null);
//        webView.getSettings().setSupportZoom(true);
//        webView.setHorizontalScrollBarEnabled(false);
//        webView.setVerticalScrollBarEnabled(false);
//        webView.setPadding(0,0,0,0);
        ScaleImageView imageView = new ScaleImageView(context);
        if(CatchService.messageDAO!=null){
            ChatMessage chatMessage= CatchService.messageDAO.getMessageById(chatMessageList.get(position).getId());
            Bitmap bitmap = new BitmapFactory().decodeByteArray(chatMessage.getImageBytes(), 0, chatMessage.getImageBytes().length);
            bitmap =new BitmapTools().reSizeBitmap(bitmap,1.5f);
            imageView.setImageBitmap(bitmap);
            container.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View view = (View)object;
        ((ViewPager) container).removeView(view);
        view = null;
        System.gc();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
    }
