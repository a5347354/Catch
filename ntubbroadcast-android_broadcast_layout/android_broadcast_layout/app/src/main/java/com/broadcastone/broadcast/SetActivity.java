package com.broadcastone.broadcast;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.broadcastone.broadcast.android_broadcast.R;
import com.sevenheaven.iosswitch.ShSwitchView;

import java.io.File;

import preferencesSeting.PreferencesSeting;
import service.CatchService;
import utils.FileTools;
import vo.Member;

/**
 * Created by ChuLay on 2015/4/29.
 */
public class SetActivity extends Fragment {

    private ImageView set_photo,set_edit;
    private ShSwitchView foodSwitch,clothes_switch,entertainment_switch;
    private TextView personal_text;
    private   View v;
    public static File cameraPhotoFile =null;


    @Override
    public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup,    Bundle paramBundle){
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_set);
        v = paramLayoutInflater.inflate(R.layout.activity_set, paramViewGroup, false);

        //個人資料  設定Your Name
        personal_text = (TextView) v.findViewById(R.id.personal_text);
        personal_text.setText(new PreferencesSeting().getYouerName());
        personal_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final  AlertDialog dialog= new AlertDialog.Builder(v.getContext()).create();
                dialog.setTitle("姓名");
                final EditText yourNameEditText = new EditText(v.getContext());
                yourNameEditText.setText(personal_text.getText());
                dialog.setView(yourNameEditText);
                dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        new PreferencesSeting().setYourName(yourNameEditText.getText().toString());
                        personal_text.setText(yourNameEditText.getText().toString());
                    }
                });
                dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                });
                dialog.show();
                yourNameEditText.addTextChangedListener(new TextWatcher() {//輸入的姓名  長度 <1 將 OK button disable
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                    @Override
                    public void afterTextChanged(Editable editable) {
                        if(yourNameEditText.getText().toString().length()>0){
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        }else{
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                        }
                    }
                });


            }
        });

        //設定大頭貼
        set_photo=(ImageView)v.findViewById(R.id.set_photo);
        set_edit = (ImageView) v.findViewById(R.id.set_edit);
        Member member =CatchService.memberDAO.getMember(BluetoothAdapter.getDefaultAdapter().getAddress());
        if(member !=null)set_photo.setImageBitmap(member.getPhotoBitMap());
        set_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder set_edit_Dialog = new AlertDialog.Builder(v.getContext());
                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(v.getContext(),android.R.layout.select_dialog_item);
                arrayAdapter.add("照相");
                arrayAdapter.add("相簿");
                set_edit_Dialog.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        String str = arrayAdapter.getItem(which);
                        Toast.makeText(v.getContext(), str, Toast.LENGTH_SHORT).show();
                        Intent intent;
                        if (str.equals("照相")) {
                            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            // place where to store camera taken picture
                            cameraPhotoFile = new FileTools().saveTempFile("photoImage",".jpg");
                            cameraPhotoFile.delete();
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraPhotoFile));
                            getActivity().startActivityForResult(intent, MainActivity.SETTING_CHOSE_CAMERA);
                        } else {
                            intent=new Intent();
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            getActivity().startActivityForResult(intent, MainActivity.SETTING_CHOSE_ALBUMS);
                        }
                    }
                });
                set_edit_Dialog.show();
            }
        }); 

        //偏好設定
        foodSwitch= (ShSwitchView) v.findViewById(R.id.food_switch);
        clothes_switch= (ShSwitchView)v.findViewById(R.id.clothes_switch);
        entertainment_switch= (ShSwitchView)v.findViewById(R.id.entertainment_switch);

        foodSwitch.setOn(PreferencesSeting.getPreferences(PreferencesSeting.FOOD));
        clothes_switch.setOn(PreferencesSeting.getPreferences(PreferencesSeting.CLOTHES));
        entertainment_switch.setOn(PreferencesSeting.getPreferences(PreferencesSeting.ENTERTAINMENT));

        foodSwitch.setOnSwitchStateChangeListener(new SwitchListener(PreferencesSeting.FOOD));
        clothes_switch.setOnSwitchStateChangeListener(new SwitchListener(PreferencesSeting.CLOTHES));
        entertainment_switch.setOnSwitchStateChangeListener(new SwitchListener(PreferencesSeting.ENTERTAINMENT));

        return v;
    }
    private class SwitchListener implements ShSwitchView.OnSwitchStateChangeListener{
        private String messageClass;
        private SwitchListener(String messageClass){
            this.messageClass =messageClass;
        }
        @Override
        public void onSwitchStateChange(boolean b) {
            PreferencesSeting.setPreferences(messageClass,b);
        }
    }
}

