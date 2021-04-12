package com.example.changeapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.changeapp.R;
import com.example.changeapp.JoinActivity;
import com.example.changeapp.etc.AppHelper;
import com.example.changeapp.etc.MYURL;
import com.example.changeapp.object.Member;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    EditText id_et; // id edit text
    EditText password_et; // password edit text
    Button login_btn; // login button
    TextView join_tv; // join text view

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(TAG,"onCreate()");
        id_et = findViewById(R.id.id_et); // id edit text
        password_et = findViewById(R.id.password_et); // password edit text
        login_btn = findViewById(R.id.login_btn); // login button
        join_tv = findViewById(R.id.join_tv); // join text view

        /*
            자동로그인 체크
         */
        SharedPreferences sharedPreferences = getSharedPreferences("myAppData",MODE_PRIVATE);
        Gson gson = new Gson();
        String member_str = sharedPreferences.getString("login_member","no_login");
        if(!"no_login".equals(member_str)){ // 쉐어드에 로그인 되어있는 상태
            Member login_member = gson.fromJson(member_str,Member.class);
            Intent intent = new Intent(getApplicationContext(),HomeActivity.class);
            startActivity(intent);
            finishAffinity(); // 모든 액티비티 클리어
        }

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = id_et.getText().toString();
                String password = password_et.getText().toString();
                if(id.length()==0){ // 아이디를 입력하지 않은 경우
                    Toast.makeText(getApplicationContext(),"아이디를 입력해주세요",Toast.LENGTH_SHORT).show();
                }else if(password.length()==0){ // 비밀번호를 입력하지 않은 경우
                    Toast.makeText(getApplicationContext(),"비밀번호를 입력해주세요",Toast.LENGTH_SHORT).show();
                }else{ // 서버에 로그인 요청
                    loginRequest(id, password);
                }
            }
        });

        join_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 회원가입 클릭 이벤트
                startActivity(new Intent(getApplicationContext(), JoinActivity.class));
            }
        });

    } // onCreate()

    /**
     * 로그인 요청 메소드
     */
    private void loginRequest(final String id, final String pw){
        StringRequest login_request = new StringRequest(
                Request.Method.POST,
                MYURL.URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) { // 응답 성공
                        Log.d(TAG,"로그인 요청 응답 성공: "+response);
                        if("-1".equals(response)){ // 로그인 실패
                            Toast.makeText(getApplicationContext(),"로그인 실패",Toast.LENGTH_LONG).show();
                        }else{ // 로그인 성공
                            getLoginMemberInfo(Integer.parseInt(response.trim()));

                        }
                    }
                },
                new Response.ErrorListener() { // 응답 실패
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG,"로그인 요청 응답 에러: "+error.toString());
                        Toast.makeText(getApplicationContext(),error.toString(),Toast.LENGTH_SHORT).show();
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String,String>();
                params.put("mode","login_action");
                params.put("id",id);
                params.put("pw",pw);
                return params;
            }
        };

        /*
         * requestQueue가 없으면 requestQueue 생성
         */
        if(AppHelper.requestQueue == null){ // requestQueue 가 없을 경우
            AppHelper.requestQueue = Volley.newRequestQueue(getApplicationContext()); // requestQueue 생성
        }

        AppHelper.requestQueue.add(login_request); // 요청을 requestQueue에 담음
    } // loginRequest()

    /**
     * getLoginMemberInfo() 메소드
     * - 로그인한 멤버 번호로 멤버의 정보를 가져와 sharedreference에 저장하기
     * @param login_member_no 로그인한 멤버 번호
     */
    public void getLoginMemberInfo(int login_member_no){
        Log.d(TAG,"getLoginMemberInfo() 호출");
        Log.d(TAG,"login_member_no: "+login_member_no);

        // Post 인자
        Map<String,String> params = new HashMap<String,String>();
        params.put("mode","find_login_member");
        params.put("login_member_no",Integer.toString(login_member_no));

        JSONObject jsonObject = new JSONObject(params); // Map을 json으로 만듬

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                MYURL.URL,
                jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) { // 응답 성공
                        Log.d(TAG,"응답 성공: "+response.toString());

                        /*
                         * 쉐어드에 로그인 멤버 정보 저장
                         */
                        SharedPreferences sharedPreferences = getSharedPreferences("myAppData",MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("login_member",response.toString()).commit();

                        Toast.makeText(getApplicationContext(), "로그인 성공", Toast.LENGTH_SHORT).show();

                        /*
                         * 화면 전환
                         */
                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finishAffinity();  // 모든 액티비티 클리어

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) { // 응답 실패
                Log.d(TAG,"응답 실패: "+error.toString());
                Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_SHORT).show();
            }
        }
        );

        AppHelper.requestQueue.add(jsonObjectRequest); // 요청 큐에 위 요청 추가

    } // getLoginMemberInfo()

} // LoginActivity class