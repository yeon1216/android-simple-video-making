package com.example.changeapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.changeapp.R;
import com.example.changeapp.etc.AppHelper;
import com.example.changeapp.etc.MYURL;

import java.util.HashMap;
import java.util.Map;

public class JoinActivity extends AppCompatActivity {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    EditText id_et;
    EditText password_et;
    EditText password_check_et;
    Button join_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        Log.d(TAG,"onCreate()");

        id_et = findViewById(R.id.id_et);
        password_et = findViewById(R.id.password_et);
        password_check_et = findViewById(R.id.password_check_et);
        join_btn = findViewById(R.id.join_btn);

        join_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 회원가입 버튼 클릭 이벤트
                String id = id_et.getText().toString().trim(); // id
                String pw = password_et.getText().toString().trim(); // 비밀번호
                String pw_check = password_check_et.getText().toString().trim(); // 비밀번호 확인

                // cat. 비밀번호와 비밀번호가 일치하는지 검사
                if(!pw.equals(pw_check)){
                    Toast.makeText(getApplicationContext(),"비밀번호와 비밀번호 확인이 일치하지 않습니다",Toast.LENGTH_SHORT).show();
                    return;
                }

                // 2. 회원가입 수행 (서버)
                joinRequest(id,pw);
            }
        });

    } // onCreate()

    /**
     * 회원가입 요청 메소드
     */
    private void joinRequest(final String id, final String pw){
        StringRequest join_request = new StringRequest(
                Request.Method.POST,
                MYURL.URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) { // 응답 성공
                        Log.d(TAG,"회원가입 요청 응답 성공: "+response);
                        String duplicate_error = "회원가입 실패 (Error: Duplicate entry '"+id_et.getText().toString()+"' for key 'member_id')";
                        if(duplicate_error.equals(response)){
                            Toast.makeText(getApplicationContext(),"이미 사용중인 아이디입니다. 다른 아이디를 사용해주세요.",Toast.LENGTH_SHORT).show();
                        }else{
                            if("00".equals(response)){ // 회원가입 성공
                                Toast.makeText(getApplicationContext(),"회원가입 성공"+response,Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivity(intent);
                                finish();
                            }else{ // 로그인 성공
                                Toast.makeText(getApplicationContext(),response.toString(),Toast.LENGTH_LONG).show();

                            }
                        }
                    }
                },
                new Response.ErrorListener() { // 응답 실패
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if("회원가입 실패 (Error: Duplicate entry 'a' for key 'member_id')".equals(error.toString())){
                            Toast.makeText(getApplicationContext(),"이미 사용중인 아이디입니다. 다른 아이디를 사용해주세요.",Toast.LENGTH_SHORT).show();
                        }else{
                            Log.d(TAG,"회원가입 요청 응답 에러: "+error.toString());
                            Toast.makeText(getApplicationContext(),error.toString(),Toast.LENGTH_SHORT).show();
                        }

                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String,String>();
                params.put("mode","join_action");
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

        AppHelper.requestQueue.add(join_request); // 요청을 requestQueue에 담음
    } // loginRequest()

}
