package com.broadcastone.broadcast;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.broadcastone.broadcast.android_broadcast.R;

import net.yanzm.mth.MaterialTabHost;

import java.io.File;
import java.util.Locale;

import chatLayout.ChatActivity;
import service.CatchService;
import utils.FileTools;
import vo.Member;

public class MainActivity extends ActionBarActivity implements AdapterView.OnItemSelectedListener{
    public static final int SETTING_CHOSE_CAMERA =4;
    public static final int SETTING_CHOSE_ALBUMS =5;
    public static final int SETTING_CROP_IMAGE =6;
    private Fragment homeFragment,setFragment,adFragment;
    private SectionsPagerAdapter pagerAdapter;
    public ViewPager viewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //getWindow().setBackgroundDrawableResource(R.drawable.home_background); //設定背景
        viewPager = (ViewPager) findViewById(R.id.pager);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(0);
        }

//      建立自訂型態的變數，用於創造title bar，圖示擺設方式設為FullScreenWidth
        MaterialTabHost tabHost = (MaterialTabHost) findViewById(android.R.id.tabhost);
        tabHost.setType(MaterialTabHost.Type.FullScreenWidth);
//      建立pagerAdapter物件
        pagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
//      設定並建立title bar三個圖示
        tabHost.addTab(R.drawable.tab_home_0);
        tabHost.addTab(R.drawable.tab_activity_0);
        tabHost.addTab(R.drawable.tab_set_0);


        viewPager.setAdapter(pagerAdapter);
        viewPager.setOnPageChangeListener(tabHost);
        tabHost.setOnTabChangeListener(new MaterialTabHost.OnTabChangeListener() {
            @Override
            public void onTabSelected(int position) {
                switch (position){
                    case 0:
                    case 1:
                        if(CatchService.messageDAO!=null)CatchService.messageDAO.cleanOutdateHisotry();
                        if(AdActivity.adActivityHandler!=null){
                            Bundle b = new Bundle();
                            b.putInt("type",AdActivity.REFRESH_VIEW);
                            Message m =AdActivity.adActivityHandler.obtainMessage();
                            m.setData(b);
                            //AdActivity.adActivityHandler.sendMessage(m);  //清除紀錄
                        }
                        break;
                }
                viewPager.setCurrentItem(position);
            }
        });


//      建立Service
        startService(new Intent(MainActivity.this, CatchService.class));

    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position){
                case 1:// ad  page
                    adFragment=new AdActivity();
                    return adFragment;
                case 2:// set  page
                    setFragment=new SetActivity();
                    return setFragment;
                default://home page
                    homeFragment=new PlaceholderFragment().newInstance(position);
                    return homeFragment;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return "home";
                case 1:
                    return "ad";
                case 2:
                    return "set";
            }
            return null;
        }
    }

    public static class PlaceholderFragment extends Fragment {


        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView;
            int count = getArguments().getInt(ARG_SECTION_NUMBER);
            if(count == 1) {
                rootView = inflater.inflate(R.layout.activity_ad, container, false);//ad page
            }else if(count == 2){
                rootView = inflater.inflate(R.layout.activity_set, container, false);
            }else{
                rootView = inflater.inflate(R.layout.activity_home, container, false);
            }
            super.onViewCreated(rootView, savedInstanceState);
            return rootView;
        }


        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            if(view.findViewById(R.id.home_public_chat)!=null){
                view.findViewById(R.id.home_public_chat).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        Handler mHandler = new Handler(){
                            public void handleMessage(android.os.Message msg){
                                Intent intent = new Intent();
                                view.findViewById(R.id.home_public_chat).setEnabled(true);
                                intent.setClass(view.getContext(), ChatActivity.class);
                                startActivity(intent);    //觸發換頁

                            }
                        };
                        view.findViewById(R.id.home_public_chat).setEnabled(false);
                        mHandler.sendEmptyMessageDelayed(0, 1000);
                    }
                });
            }
        }
    }



    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {}

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {}

//    @Override
//    public void onBackPressed(){
//
//        ImageButton main_left_imgbtn = (ImageButton) findViewById(R.id.leftBtn);
//        super.onBackPressed();
//    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){

        if(keyCode == KeyEvent.KEYCODE_BACK){
            this.finish();
        }
        return super.onKeyDown(keyCode, event);
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



    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();        //呼叫gargabe collection
    }

    private void unbindDrawables(View view) {                 //回收記憶體判斷圖片
//        if (view.getBackground() != null) {
//            view.getBackground().setCallback(null);
//        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Log.i("Catch!", "resultCode:" + resultCode);
        //Log.i("Catch!","requestCode:"+requestCode);
        //Log.i("Catch!","data==null:"+(data==null));
        //Log.i("Catch!","Activity.RESULT_OK:"+Activity.RESULT_OK);
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == SETTING_CHOSE_CAMERA){
                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(Uri.fromFile(SetActivity.cameraPhotoFile), "image/*");
                intent.putExtra("crop", "true");
                intent.putExtra("outputX", 200);
                intent.putExtra("outputY", 200);
                intent.putExtra("outputFormat", "JPEG");// 圖片格式
                intent.putExtra("return-data", true);
                startActivityForResult(intent, SETTING_CROP_IMAGE);
            }
            if(data!=null && requestCode== SETTING_CHOSE_ALBUMS){
                Uri uri = data.getData();
              //  Log.i("Catch!","uri:"+uri.toString());
                Intent intent = new Intent("com.android.camera.action.CROP");
                File imageFile = new File(uri.toString());

                if(!imageFile.exists()){
                     String imageFilePath=new FileTools().getRealPathFromURI(getApplicationContext(),uri);
                     imageFile =new File(imageFilePath);
                     uri=Uri.fromFile(imageFile);
                }

                intent.setDataAndType(uri, "image/*");
                intent.putExtra("crop", "true");
                intent.putExtra("outputX", 200);
                intent.putExtra("outputY", 200);
                intent.putExtra("outputFormat", "JPEG");// 圖片格式
                intent.putExtra("return-data", true);
                startActivityForResult(intent, SETTING_CROP_IMAGE);
            }
            if(data!=null && requestCode== SETTING_CROP_IMAGE){
                Bitmap photoImage = data.getParcelableExtra("data");

                Member member=new Member();
                member.setMacAddress(BluetoothAdapter.getDefaultAdapter().getAddress());
                member.setPhoto(photoImage);

                if(CatchService.memberDAO.checkExists(member.getMacAddress())){
                    CatchService.memberDAO.updateMember(member);
                }else{
                    CatchService.memberDAO.addMember(member);
                }
                Fragment fragment = (Fragment )viewPager.getAdapter().instantiateItem(viewPager, viewPager.getCurrentItem());
                ((ImageView) fragment.getView().findViewById(R.id.set_photo)).setImageBitmap(photoImage);

                if(SetActivity.cameraPhotoFile!=null && SetActivity.cameraPhotoFile.exists()){
                     SetActivity.cameraPhotoFile.getAbsoluteFile().delete();
                }

            }
        }
    }
}