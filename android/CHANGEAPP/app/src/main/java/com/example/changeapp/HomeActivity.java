package com.example.changeapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.changeapp.object.Member;
import com.google.gson.Gson;

public class HomeActivity extends AppCompatActivity {


    int CameraPermission;
    int StoragePermission;
    int AudioPermission;
    Member login_member;
    Button logout_btn; // 로그아웃 버튼
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        logout_btn  = findViewById(R.id.logout_btn); // 로그아웃 버튼
        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 로그아웃 버튼 클릭시 이벤트
                /*
                 * 쉐어드에서 로그인멤버 제거
                 */
                SharedPreferences sharedPreferences = getSharedPreferences("myAppData",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                Gson gson = new Gson();
                Member login_member = gson.fromJson(sharedPreferences.getString("login_member","no_login"), Member.class);
                editor.remove("login_member").commit();

                Toast.makeText(getApplicationContext(),"로그아웃 되었습니다",Toast.LENGTH_SHORT).show();

                /*
                 * 로그인 화면으로 이동
                 */
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finishAffinity(); // 모든 액티비티 클리어
            }
        });


        //권한 체크
        CameraPermission= ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        StoragePermission= ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        AudioPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);

        if(CameraPermission== PackageManager.PERMISSION_GRANTED&&StoragePermission==PackageManager.PERMISSION_GRANTED&&AudioPermission==PackageManager.PERMISSION_GRANTED){

        }else{
            CheckPerMission();
        }



        Button button1 = (Button)findViewById(R.id.btn); /*app 시작 전환버튼*/
        Button button2 = (Button)findViewById(R.id.btn_mypage); /*마이페이지 전환버튼*/
        ImageView user_image=findViewById(R.id.user_image);

        SharedPreferences sharedPreferences = getSharedPreferences("myAppData",MODE_PRIVATE);
        Gson gson = new Gson();
        login_member = gson.fromJson(sharedPreferences.getString("login_member","no_login"), Member.class);



        //저장된 프로필 사진 불러오기.
        Glide.with(HomeActivity.this)
                .load("http://35.243.90.95/team/"+login_member.member_id+".jpg")
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(R.drawable.profile)
                .error(R.drawable.profile)
                .skipMemoryCache(true)
                .into(user_image);
        /*app 시작 전환버튼*/
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, StartActivity.class);
                startActivity(intent);
            }
        });

        /*마이페이지 전환버튼*/
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this,MyPageActivity.class);
                startActivity(intent);
            }
        });
    }

    //권한 설정
    private void CheckPerMission(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO},1);
    }

}