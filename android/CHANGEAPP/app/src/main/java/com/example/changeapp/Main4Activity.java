package com.example.changeapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.changeapp.object.Member;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


//신세계
public class Main4Activity extends AppCompatActivity implements   View.OnClickListener, MediaPlayer.OnCompletionListener {
    //Volley
    Button pick, upload1;
    private int PICK_FILE_REQUEST = 1;

    String fileName;
    String extension;
    byte[] bytes;
    ArrayList<File> up_file;
    ArrayList<byte[]> up_byte;


    Uri filePath;
    private List<Fileupload> upload = new ArrayList();
    private FileuploadAdapter mAdapter;

    String timeSet="";

    //  RecyclerView recyclerView;

    ProgressDialog pd;
    ProgressDialog pd1;
    ProgressDialog pd2;
    // 미리 상수 선언
    private static final int REC_STOP = 0;
    private static final int WAIT = 3;
    private static final int RECORDING = 1;
    private static final int PLAY_STOP = 0;
    private static final int PLAYING = 1;
    private static final int PLAY_PAUSE = 2;

    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    private int mRecState = REC_STOP;
    private int mPlayerState = PLAY_STOP;
    private ProgressDialog progressBar;
    private SeekBar mRecProgressBar, mPlayProgressBar;
    private Button mBtnStartRec, mBtnStartPlay, mBtnStopPlay;
    private String mFilePath, mFileName = null;
    private TextView mTvPlayMaxPoint;

    private int mCurRecTimeMs = 0;
    private int mCurProgressTimeDisplay = 0;

    //보내기 버튼시 로딩중 다이얼로그 위한 handler
    private Handler mHandler;
    //보내기 버튼시 로딩중 다이얼로그
    private ProgressDialog mProgressDialog;

    public final static String VIDEO_URL = "http://35.243.90.95/zzal/3.mp4";
    private static final String OUTPUT_FILE_PATH = "/storage/emulated/0/Dowmload/_2_Rec.mp4";
    public final static int URL = 1;
    public final static int SDCARD = 2;
    VideoView videoView;
    Button btnStart, btnStop;

    MediaRecorder recorder;
    String filename=null;

    MediaPlayer player;
    int position = 0; // 다시 시작 기능을 위한 현재 재생 위치 확인 변수


    // 녹음시 SeekBar처리
    Handler mProgressHandler = new Handler() {
        public void handleMessage(Message msg) {
            mCurRecTimeMs = mCurRecTimeMs + 100;
            mCurProgressTimeDisplay = mCurProgressTimeDisplay + 100;

            // 녹음시간이 음수이면 정지버튼을 눌러 정지시켰음을 의미하므로
            // SeekBar는 그대로 정지시키고 레코더를 정지시킨다.
            if (mCurRecTimeMs < 0) {
            }
            // 녹음시간이 아직 최대녹음제한시간보다 작으면 녹음중이라는 의미이므로
            // SeekBar의 위치를 옮겨주고 0.1초 후에 다시 체크하도록 한다.
            else if (mCurRecTimeMs < 20000) {
                mRecProgressBar.setVisibility(View.VISIBLE);
                mRecProgressBar.setProgress(mCurProgressTimeDisplay);
                mProgressHandler.sendEmptyMessageDelayed(0, 100);
            }
            // 녹음시간이 최대 녹음제한 시간보다 크면 녹음을 정지 시킨다.
            else {
                mBtnStartRecOnClick();
            }
        }
    };

    // 재생시 SeekBar 처리
    Handler mProgressHandler2 = new Handler() {
        public void handleMessage(Message msg) {
            if (mPlayer == null) return;

            try {
                if (mPlayer.isPlaying()) {
                    mPlayProgressBar.setVisibility(View.VISIBLE);
                    mPlayProgressBar.setProgress(mPlayer.getCurrentPosition());
                    mProgressHandler2.sendEmptyMessageDelayed(0, 100);
                }
            } catch (IllegalStateException e) {
            } catch (Exception e) {
            }
        }
    };

    Member login_member;
    Switch voice_change_switch; // 음성변조 활성화 스위치
    String is_change_voice; // 음성변조 활성화시 1, 음성변조 비활성화시 0

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        permissionCheck();
        /**
         * 영상을 출력하기 위한 비디오뷰
         * SurfaceView를 상속받아 만든 클래스
         * 웬만하면 VideoView는 그때 그때 생성해서 추가 후 사용
         * 화면 전환 시 여러 UI가 있을 때 화면에 제일 먼저 그려져서 보기에 좋지 않을 때가 있다
         * 예제에서 xml에 추가해서 해봄
         */

        is_change_voice="0";
        voice_change_switch = findViewById(R.id.voice_change_switch);
        voice_change_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) { //  활성화시 true, 비활성화시 false
                Log.d("yeon","boolean : "+b);
                if(b){
                    is_change_voice = "1";
                    //기존의 파일
                    File filePre =
                            new File("/sdcard/", mFileName);


                    mFileName = "/" + login_member.member_id + "_3_"+is_change_voice+"_"+timeSet+".mp4";


                    File fileNow =  new File("/sdcard/", mFileName);




                    Log.d("yeon","a : "+is_change_voice);
                }else{
                    is_change_voice = "0";

                    File filePre =
                            new File("/sdcard/", mFileName);


                    mFileName = "/" + login_member.member_id + "_3_"+is_change_voice+"_"+timeSet+".mp4";


                    File fileNow =  new File("/sdcard/", mFileName);



                    Log.d("yeon","a : "+is_change_voice);
                }
            }
        });


        /*
         * 쉐어드에 저장된 멤버 정보 가지고 오기
         */
        SharedPreferences sharedPreferences = getSharedPreferences("myAppData", MODE_PRIVATE);
        Gson gson = new Gson();
        login_member = gson.fromJson(sharedPreferences.getString("login_member", "no_login"), Member.class);


     //   Toast.makeText(getApplicationContext(), "로그인 성공", Toast.LENGTH_SHORT).show();


        // 미디어 레코더 저장할 파일 생성
        mFilePath = "/sdcard/";

        // 파일명을 년도월일시간분초 로 생성 겹치는 상황 없애기
        SimpleDateFormat timeStampFormat = new SimpleDateFormat(
                "yyyyMMddHHmmss");


        mFileName = "/" + login_member.member_id + "_3_"+is_change_voice+"_"+timeSet+".mp4";

        //녹음 시작 버튼
        mBtnStartRec = (Button) findViewById(R.id.btnStartRec);
        //녹음 재생 버튼
        mBtnStartPlay = (Button) findViewById(R.id.btnStartPlay);
        mBtnStopPlay = (Button) findViewById(R.id.btnStopPlay);
        mRecProgressBar = (SeekBar) findViewById(R.id.recProgressBar);
        mPlayProgressBar = (SeekBar) findViewById(R.id.playProgressBar);
        mTvPlayMaxPoint = (TextView) findViewById(R.id.tvPlayMaxPoint);

        mBtnStartRec.setOnClickListener((View.OnClickListener) this);
        mBtnStartPlay.setOnClickListener((View.OnClickListener) this);
        mBtnStopPlay.setOnClickListener((View.OnClickListener) this);

        //레이아웃 위젯 findViewById
        videoView = (VideoView) findViewById(R.id.view);
        btnStart = (Button) findViewById(R.id.btnStart);
      //  btnStop = (Button) findViewById(R.id.btnStop);

        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard, "recorded.mp4");
        filename = file.getAbsolutePath();
        Log.d("MainActivity", "저장할 파일 명 : " + filename);



        pd = new ProgressDialog(Main4Activity.this);
        pd1 = new ProgressDialog(Main4Activity.this);
        pd2 = new ProgressDialog(Main4Activity.this);

        up_file = new ArrayList<>();
        up_byte = new ArrayList<>();

        permissionCheck();

        upload1 = (Button) findViewById(R.id.btn_upload);
        pick = (Button) findViewById(R.id.btn_pick);




        //영상이 준비되기 전까지 다이얼로그를 !!!!!!!!!!!
//        progressBar=new ProgressDialog(Main2Activity.this);
//        progressBar.setMessage("잠시만 기다려주세요");
//        progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//        progressBar.setIndeterminate(true);
//        progressBar.setCancelable(false);
//        progressBar.show();

        pd1.setMessage("Loading");
        pd1.show();



        //---------------upload the file --------------
        upload1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //upload_file_multipart();
                //retrofit
                Log.d("test111","upload1BtnStart");
                //서버에 업로드 시킴
                UploadService service = MyRetrofit2.getRetrofit2().create(UploadService.class);

                //file이라는 이름으로 업로드
                mFileName = "/" + login_member.member_id + "_3_"+is_change_voice+"_"+timeSet+".mp4";

                SharedPreferences sharedPreferences=getSharedPreferences("file",0);
                SharedPreferences.Editor editor=sharedPreferences.edit();
                editor.putString("file_name",login_member.member_id+timeSet+".mp4");
                editor.commit();

                File file = new File(mFilePath + mFileName);
                Log.d("test111","upload1BtnStart-fileFind");

                Log.i("filepath",file.getAbsolutePath());

                Log.i("filename",file.getName());

                // 오디오 파일 업로드
                RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                MultipartBody.Part body=MultipartBody.Part.createFormData("file0", file.getName(), requestFile);

                Call<ResponseBody> call = service.uploadVoice(body);

                // 응답 오기 전까지 로딩문구 띄우기
                pd.setMessage("Loading");
                pd.show();
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        //response -> success -> reponse.toString()에서 success로 받음.
                        Log.i("success","오예 파일을다만들었다!!!!");
                        pd.cancel();
                        //다음페이지 넘어가게 코드 부탁드립니다.


                        Intent intent=new Intent(getApplicationContext(),confirm_MyVideo.class);
                        startActivity(intent);
                        finish();

                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.i("fail",t.toString());
                        pd.cancel();
                    }
                });


            }
        });//upload1 끝

        //미디어컨트롤러 추가하는 부분
        MediaController controller = new MediaController(Main4Activity.this);
        videoView.setMediaController(controller);

        //비디오뷰 포커스를 요청함
        videoView.requestFocus();

        int type = URL;
        switch (type) {
            case URL:
                //동영상 경로가 URL일 경우
                videoView.setVideoURI(Uri.parse(VIDEO_URL));
                break;

            case SDCARD:
                //동영상 경로가 SDCARD일 경우
                String path = Environment.getExternalStorageDirectory()
                        + "/TestVideo.mp4";
                videoView.setVideoPath(path);
                break;
        }
        //영상만 보기 버튼
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


//                //미디어컨트롤러 추가하는 부분
//                MediaController controller = new MediaController(Main2Activity.this);
//                videoView.setMediaController(controller);
//
//                //비디오뷰 포커스를 요청함
//                videoView.requestFocus();
//
//                int type = URL;
//                switch (type) {
//                    case URL:
//                        //동영상 경로가 URL일 경우
//                        videoView.setVideoURI(Uri.parse(VIDEO_URL));
//                        break;
//
//                    case SDCARD:
//                        //동영상 경로가 SDCARD일 경우
//                        String path = Environment.getExternalStorageDirectory()
//                                + "/TestVideo.mp4";
//                        videoView.setVideoPath(path);
//                        break;
//                }
//                //동영상이 재생준비가 완료되었을 때를 알 수 있는 리스너 (실제 웹에서 영상을 다운받아 출력할 때 많이 사용됨)
//                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                    @Override
//                    public void onPrepared(MediaPlayer mp) {
//                        //mp.setVolume(0,0);
//                        Toast.makeText(Main2Activity.this,
//                                "동영상이 준비되었습니다. \n'시작' 버튼을 누르세요", Toast.LENGTH_SHORT).show();
//                    }
//                });
                videoView.seekTo(0);
                videoView.start();
//                //동영상 재생이 완료된 걸 알 수 있는 리스너
//                videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                    @Override
//                    public void onCompletion(MediaPlayer mp) {
//                        //동영상 재생이 완료된 후 호출되는 메소드
//                        Toast.makeText(Main2Activity.this,
//                                "동영상 재생이 완료되었습니다.", Toast.LENGTH_SHORT).show();
//                    }
//                });

            }
        });//영상만 보기 버튼 끝





        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        pd1.cancel();
                        Toast.makeText(Main4Activity.this, "동영상이 준비되었습니다. \n'시작' 버튼을 누르세요", Toast.LENGTH_SHORT).show();
                    }
                });

    }//onCreate 끝


    // Calendar를 년월일시분초로 반환 메소드
    public String timeToString(Calendar time) {
        String timeToString = (time.get(Calendar.MONTH) + 1)+""+(time.get(Calendar.DAY_OF_MONTH))+ ""+(time.get(Calendar.HOUR_OF_DAY)) +""+(time.get(Calendar.MINUTE)) +""+(time.get(Calendar.SECOND))+"";
        Log.i("yeon","timeToString : "+timeToString);
        return timeToString;
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~오디오
    // 버튼의 OnClick 이벤트 리스너
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStartRec:
                mBtnStartRecOnClick();
                //보내기 버튼 안보이게
                upload1.setVisibility(View.VISIBLE);
                //녹음 시작 안 누를시 녹음 재생벝튼 안보이게
                mBtnStartPlay.setVisibility(View.VISIBLE);
                //음성변조 안보이게
                voice_change_switch.setVisibility(View.VISIBLE);
                break;
            case R.id.btnStartPlay:
                mBtnStartPlayOnClick();
                break;

            default:
                break;
        }
    }


    private void mBtnStartRecOnClick() {
        if (mRecState == REC_STOP) {
            mRecState = RECORDING;
            pd2.setMessage("Loading");
            pd2.show();

            startRec();
            playVideo(); //동영상 재생
            updateUI();
        } else if (mRecState == RECORDING) {
            mRecState = REC_STOP;
            stopRec();
            updateUI();
        }
    }


    // 오디오 녹음 시작 메소드
    @SuppressLint("WrongConstant")
    private void startRec()
    {


        try
        {

            //미디어컨트롤러 추가하는 부분
            MediaController controller = new MediaController(Main4Activity.this);
            videoView.setMediaController(controller);

            //비디오뷰 포커스를 요청함
            videoView.requestFocus();

            int type = URL;
            switch (type) {
                case URL:
                    //동영상 경로가 URL일 경우
                    videoView.setVideoURI(Uri.parse(VIDEO_URL));
                    break;

                case SDCARD:
                    //동영상 경로가 SDCARD일 경우
                    String path = Environment.getExternalStorageDirectory()
                            + "/TestVideo.mp4";
                    videoView.setVideoPath(path);
                    break;
            }






            //동영상이 재생준비가 완료되었을 때를 알 수 있는 리스너 (실제 웹에서 영상을 다운받아 출력할 때 많이 사용됨)
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setVolume(0,0);


                    if (mRecorder == null)
                    {
                        //새 MediaRecorder 선언
                        mRecorder = new MediaRecorder();
                        mRecorder.reset();
                        //오디오 파일 생성
                        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
                        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

                        timeSet=timeToString(Calendar.getInstance());
                        mFileName = "/" + login_member.member_id + "_3_"+is_change_voice+"_"+timeSet+".mp4";

                        mRecorder.setOutputFile(mFilePath + mFileName);
                        try {
                            mRecorder.prepare();
                            pd2.cancel();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        // 이제 녹음을 하도록...
                        //녹음 값 주고
                        mRecState = RECORDING;
                        //ui바꿔주기 -> 이 때부터 btn click 가능
                        updateUI();
                        //녹음 시작
                        mRecorder.start();
                        mCurRecTimeMs = 0;
                        mCurProgressTimeDisplay = 0;

                        // SeekBar의 상태를 0.1초후 체크 시작
                        mProgressHandler.sendEmptyMessageDelayed(0, 100);

                    }
                    else
                    {
                        mRecorder.reset();
                    }


                    Toast.makeText(Main4Activity.this,
                            "동영상이 준비되었습니다. \n'시작' 버튼을 누르세요", Toast.LENGTH_SHORT).show();

                }
            });

            //동영상 재생이 완료된 걸 알 수 있는 리스너
            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {

                    //동영상 재생이 완료된 후 호출되는 메소드
                    Toast.makeText(Main4Activity.this,
                            "동영상 재생이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                }
            });


        }
        catch (IllegalStateException e)
        {
            Toast.makeText(this, "IllegalStateException", 1).show();
        }

    }

    // 오디오 녹음정지
    private void stopRec() {
        try {
            mRecorder.stop();
        } catch (Exception e) {

        } finally {
          //  mRecorder.release();
        //    mRecorder = null;
        }

        mCurRecTimeMs = -999;
        // SeekBar의 상태를 즉시 체크
        mProgressHandler.sendEmptyMessageDelayed(0, 0);
    }

    private void mBtnStartPlayOnClick() {
        if (mPlayerState == PLAY_STOP) {
            mPlayerState = PLAYING;
            initMediaPlayer();
            startPlay();
            updateUI();
        } else if (mPlayerState == PLAYING) {
            mPlayerState = PLAY_PAUSE;
            pausePlay();
            updateUI();
        } else if (mPlayerState == PLAY_PAUSE) {
            mPlayerState = PLAYING;
            startPlay();
            stopRec();
            updateUI();
        }
    }




    private void initMediaPlayer() {
        // 미디어 플레이어 생성
        if (mPlayer == null)
            mPlayer = new MediaPlayer();
        else
            mPlayer.reset();

        mPlayer.setOnCompletionListener((MediaPlayer.OnCompletionListener) this);
        String fullFilePath = mFilePath + mFileName;

        try {
            mPlayer.setDataSource(fullFilePath);
            mPlayer.prepare();
            int point = mPlayer.getDuration();
            mPlayProgressBar.setMax(point);

            int maxMinPoint = point / 1000 / 60;
            int maxSecPoint = (point / 1000) % 60;
            String maxMinPointStr = "";
            String maxSecPointStr = "";

            if (maxMinPoint < 10)
                maxMinPointStr = "0" + maxMinPoint + ":";
            else
                maxMinPointStr = maxMinPoint + ":";

            if (maxSecPoint < 10)
                maxSecPointStr = "0" + maxSecPoint;
            else
                maxSecPointStr = String.valueOf(maxSecPoint);

            mTvPlayMaxPoint.setText(maxMinPointStr + maxSecPointStr);

            mPlayProgressBar.setProgress(0);
        } catch (Exception e) {
            Log.v("ProgressRecorder", "미디어 플레이어 Prepare Error ==========> " + e);
        }
    }


    // 재생 시작
    @SuppressLint("WrongConstant")
    private void startPlay() {
        // 미디어 플레이어 생성
        if (mPlayer == null)
            mPlayer = new MediaPlayer();
        else
            mPlayer.reset();

        mPlayer.setOnCompletionListener(this);

        String fullFilePath = mFilePath + mFileName;


        Log.i("녹음완료",fullFilePath);

        Log.v("ProgressRecorder", "녹음파일명 ==========> " + fullFilePath);

        try {
            mPlayer.setDataSource(fullFilePath);
            mPlayer.prepare();
            int point = mPlayer.getDuration();
            mPlayProgressBar.setMax(point);

            int maxMinPoint = point / 1000 / 60;
            int maxSecPoint = (point / 1000) % 60;
            String maxMinPointStr = "";
            String maxSecPointStr = "";

            if (maxMinPoint < 10)
                maxMinPointStr = "0" + maxMinPoint + ":";
            else
                maxMinPointStr = maxMinPoint + ":";

            if (maxSecPoint < 10)
                maxSecPointStr = "0" + maxSecPoint;
            else
                maxSecPointStr = String.valueOf(maxSecPoint);

            mTvPlayMaxPoint.setText(maxMinPointStr + maxSecPointStr);
        } catch (Exception e) {
            Log.v("ProgressRecorder", "미디어 플레이어 Prepare Error ==========> " + e);
        }

        if (mPlayerState == PLAYING) {
            mPlayProgressBar.setProgress(0);

            try {
                // SeekBar의 상태를 0.1초마다 체크
                mProgressHandler2.sendEmptyMessageDelayed(0, 100);
                mPlayer.start();
            } catch (Exception e) {
                Toast.makeText(this, "error : " + e.getMessage(), 0).show();
            }
        }
    }


    private void pausePlay() {
        Log.v("ProgressRecorder", "pausePlay().....");

        // 재생을 일시 정지하고
        mPlayer.pause();

        // 재생이 일시정지되면 즉시 SeekBar 메세지 핸들러를 호출한다.
        mProgressHandler2.sendEmptyMessageDelayed(0, 0);
    }

    private void stopPlay() {
        // 재생을 중지하고
        mPlayer.stop();
        mPlayer.release();
        mPlayer = null;
        mPlayProgressBar.setProgress(0);

        // 즉시 SeekBar 메세지 핸들러를 호출한다.
        mProgressHandler2.sendEmptyMessageDelayed(0, 0);
    }


    private void releaseMediaPlayer() {
        Log.v("ProgressRecorder", "releaseMediaPlayer().....");
        mPlayer.release();
        mPlayer = null;
        mPlayProgressBar.setProgress(0);
    }

    public void onCompletion(MediaPlayer mp) {
        mPlayerState = PLAY_STOP; // 재생이 종료됨

        // 재생이 종료되면 즉시 SeekBar 메세지 핸들러를 호출한다.
        mProgressHandler2.sendEmptyMessageDelayed(0, 0);

        updateUI();
    }

    //녹음 버튼 클릭시 변화하는 UI
    private void updateUI()
    {
        //녹음 스탑
        if (mRecState == REC_STOP)
        {
            mBtnStartRec.setText("녹음 다시시작");
            mRecProgressBar.setProgress(0);
        } else if (mRecState == RECORDING)
            mBtnStartRec.setText("녹음 정지");


        //재생 정지
        if (mPlayerState == PLAY_STOP)
        {
            mBtnStartPlay.setText("녹음 듣기");
            mPlayProgressBar.setProgress(0);
        }
        else if (mPlayerState == PLAYING)
            mBtnStartPlay.setText("일시 정지");
        else if (mPlayerState == PLAY_PAUSE)
            mBtnStartPlay.setText("Start");


    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    //시작 버튼 onClick Method
    public void StartButton(View v) {
        playVideo();
    }

    //정지 버튼 onClick Method
    public void StopButton(View v) {
        stopVideo();
    }

    //동영상 재생 Method
    private void playVideo() {
        //비디오를 처음부터 재생할 때 0으로 시작(파라메터 sec)
        videoView.seekTo(0);
        videoView.start();
    }

    //동영상 정지 Method
    private void stopVideo() {
        //비디오 재생 잠시 멈춤
        videoView.pause();


    }


    public void closePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    public void permissionCheck() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 1);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();

            Log.e("URI           " + filePath, "    ");


            Fileupload fileupload = new Fileupload();
            try {
                File file = new File(SelectedFilePath.getPath(getApplicationContext(), filePath));

                fileName = file.getName();


                extension = fileName.substring(fileName.lastIndexOf("."));
                bytes = loadFile(file);
                up_file.add(file);
                up_byte.add(bytes);

                fileupload.setName(fileName);
                fileupload.setType(extension);

                //    upload.add(fileupload);
                //   mAdapter = new FileuploadAdapter(upload,Main2Activity.this);

                Log.d("objl", upload.toString());
                Log.d("objd", up_file.toString());

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "ERROR " + e.getMessage() + "\n" + e.getCause(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }



    private static byte[] loadFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        long length = file.length();
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
        byte[] bytes = new byte[(int) length];

        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }

        is.close();
        return bytes;
    }


    public void delete_file(int position) {
        upload.remove(position);
        up_file.remove(position);
        up_byte.remove(position);
        mAdapter.notifyDataSetChanged();
        Toast.makeText(this, "" + position, Toast.LENGTH_SHORT).show();
    }


    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


}

