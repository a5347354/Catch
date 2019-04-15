package com.broadcastone.broadcast;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.broadcastone.broadcast.android_broadcast.R;

import org.apache.commons.lang.SerializationUtils;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import chatLayout.ChatActivity;
import service.CatchService;
import utils.BitmapTools;
import vo.ChatMessage;
import vo.Member;

/**
 * Created by ChuLay on 2015/4/29.
 */
public class AdActivity extends Fragment {

    private View v;
    private static ListView lvMember;
    private BitmapTools bitmapTools = new BitmapTools();
    private static MemberAdapter adapter;
    public static Handler adActivityHandler;
    public final static int REFRESH_VIEW=1;

    @Override
    public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle) {


        //新增測試資料Member
        if(CatchService.memberDAO!=null){
            Member member = new Member("1","7-11",bitmapTools.drawableToBitmap(R.drawable.logo1,getResources()));
            CatchService.memberDAO.addMember(member);
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setId(new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()).toString());// 為訊息產生ID  避免其他裝置收到相同的訊息
            chatMessage.setMacAddress("1");
            chatMessage.setDeviceName("7-11");
            chatMessage.setDateTime(DateFormat.getDateTimeInstance().format(new Date()));
            chatMessage.setText("test");
            chatMessage.setMessageClass("foodClass");
            CatchService.messageDAO.addMessage(chatMessage);
        }


        v = paramLayoutInflater.inflate(R.layout.activity_ad, paramViewGroup, false);
        adapter=new MemberAdapter(getActivity().getApplicationContext());
        lvMember=(ListView)v.findViewById(R.id.lvMember);
        adActivityHandler=new Handler(){
            public void handleMessage(Message msg) {
                //msg.getData().getString("type")!=null && msg.getData().getString("type")
                if(msg.getData()!=null && msg.getData().getByteArray("member")!=null){
                    Member member=(Member) SerializationUtils.deserialize(msg.getData().getByteArray("member"));
                    if(!(member.getImageBytes()!=null && member.getImageBytes().length>1)){
                        if(isAdded()){
                            member.setImageBytes(bitmapTools.bitmapToBytes(bitmapTools.drawableToBitmap(R.drawable.set_photo, getResources())));
                        }
                    }
                    Integer index=adapter.getMemberIndex(member.getMacAddress());
                    if(index!=null){
                        adapter.updateView(index,member);
                    }else{
                        displayMessage(member);
                    }
                }
                if(msg.getData()!=null && msg.getData().getInt("type")==REFRESH_VIEW){
                    if(CatchService.memberDAO!=null && CatchService.messageDAO!=null){
                        CatchService.messageDAO.cleanOutdateHisotry();
                        for(Member m:CatchService.memberDAO.getAllMember()){
                            if(CatchService.messageDAO.getCountByAddress(m.getMacAddress())<=0){
                                CatchService.memberDAO.deleteMember(m.getMacAddress());
                                if(adapter!=null){
                                    int index=adapter.getMemberIndex(m.getMacAddress());
                                    adapter.del(index);
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                }

            }
        };

        //新增測試資料Member
        //CatchService.memberDAO.deleteAllMember();
        if(CatchService.memberDAO!=null){
            Member member = new Member("1","7-11",bitmapTools.drawableToBitmap(R.drawable.logo1,getResources()));
            CatchService.memberDAO.addMember(member);
        }


        lvMember.setAdapter(adapter);
        try {
            lvMember.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    Member member = (Member) parent.getItemAtPosition(position);
                    String text = "index = "+ id +" name = " + member.getName() +" address:"+member.getMacAddress();
                    Toast.makeText(v.getContext(), text, Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(getActivity(), ChatActivity.class);
                    intent.putExtra("type", "ad");
                    intent.putExtra("macAddress", member.getMacAddress());
                    intent.putExtra("supName", member.getName());
                    startActivity(intent);
                }
            });
        }catch(Exception e){
           // Log.e("listView",e.toString());
        }
        return v;
    }

    public static void displayMessage(Member member) {
        adapter.add(member);
        adapter.notifyDataSetChanged();
        scroll();
    }

    private static void scroll() {
        lvMember.setSelection(lvMember.getCount() - 1);
    }



    //適配器
    private class MemberAdapter extends BaseAdapter {
        private LayoutInflater layoutInflater;
        private List<Member> memberList;


        public MemberAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
            memberList = new ArrayList<>();
            if(CatchService.memberDAO!=null)
                memberList = CatchService.memberDAO.getAllMember();
          //  Log.i("Catch","member:"+memberList.toString());
        }

        @Override
        public int getCount() {
            return memberList.size();
        }

        @Override
        public Object getItem(int position) {
            return memberList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void add(Member member) {
            memberList.add(member);
        }

        public void del(int index){
            memberList.remove(index);
        };

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.listview_ad, parent, false);
            }

            Member member = memberList.get(position);
            ChatMessage chatMessage =CatchService.messageDAO.getTheNewAdMessage(member.getMacAddress());
            if(chatMessage!=null)
                member.setNewInfo(chatMessage.getText());
            TextView adTheNewInfo = (TextView) convertView.findViewById(R.id.adTheNewInfo);
            adTheNewInfo.setText(member.getNewInfo());

            ImageView ivImage = (ImageView) convertView.findViewById(R.id.ivImage);
            try {
//                BitmapFactory.Options options = new BitmapFactory.Options();
//                options.inSampleSize = 16;
//                Bitmap bitmap = new BitmapFactory().decodeByteArray(member.getImageBytes(), 0, member.getImageBytes().length,options);
//                bitmap = new BitmapTools().reSizeBitmap(bitmap, 0.1f);
//                ivImage.setImageBitmap(bitmap);
                if(member.getImageBytes()!=null && member.getImageBytes().length>1){
                    ivImage.setImageBitmap(member.getPhotoBitMap());
                }else{ //廠商未設定大頭貼  爪取內部圖片
                    ivImage.setImageResource(R.drawable.set_photo);
                }
            }catch (OutOfMemoryError e) {
              //  Log.e("Catch!", "OutOfMemoryError " + e.toString());
            }catch (Exception e){
               // Log.e("Catch!E","Exception " +e.toString());
            }
            TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
            tvName.setText(member.getName());
            return convertView;
        }




        private Integer getMemberIndex(String macAddress){
            for (int position=0; position<memberList.size(); position++)
                if (memberList.get(position).getMacAddress().equals(macAddress))
                    return position;
            return null;
        }


        private void updateView(int index,Member member){
            View v = lvMember.getChildAt(index -lvMember.getFirstVisiblePosition());
            if(v == null)
                return;
            ImageView ivImage=(ImageView) v.findViewById(R.id.ivImage);
            ivImage.setImageBitmap(member.getPhotoBitMap());
            TextView tvName =(TextView)v.findViewById(R.id.tvName);
            tvName.setText(member.getName());

            TextView adTheNewInfo = (TextView) v.findViewById(R.id.adTheNewInfo);
            adTheNewInfo.setText(member.getNewInfo());


        }



    }
}

