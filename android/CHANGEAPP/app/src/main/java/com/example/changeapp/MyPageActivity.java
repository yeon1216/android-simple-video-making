package com.example.changeapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.changeapp.object.Member;
import com.google.gson.Gson;
import com.soundcloud.android.crop.Crop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MyPageActivity extends AppCompatActivity {

    Uri imageCaptureUri;
    File filePath=null;
    Button personal_next;
    Button pic_upload;
    ImageView personal_pic;
    Member login_member;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("mypage","oncreate");
        setContentView(R.layout.activity_my_page);

        setTitle("프로필사진");
        //완료
        personal_next=findViewById(R.id.personal_next);
        //사진 등록
        pic_upload=findViewById(R.id.pic_upload);
        //개인 사진
        personal_pic=findViewById(R.id.personal_pic);


        if(filePath==null){
                SharedPreferences sharedPreferences = getSharedPreferences("myAppData",MODE_PRIVATE);
                Gson gson = new Gson();
                login_member = gson.fromJson(sharedPreferences.getString("login_member","no_login"), Member.class);


                Log.d(" mypage실행시 이미지 불러오기", "확인");
                Glide.with(MyPageActivity.this)
                        .load("http://35.243.90.95/team/"+login_member.member_id+".jpg")
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .placeholder(R.drawable.profile)
                        .error(R.drawable.profile)
                        .skipMemoryCache(true)
                        .into(personal_pic);
            }else{


                    BitmapFactory.Options imgOptions = new BitmapFactory.Options();
                    imgOptions.inSampleSize = 1;
                    Bitmap bitmap = BitmapFactory.decodeFile(filePath.getAbsolutePath(), imgOptions);
                    personal_pic.setImageBitmap(bitmap);


            }





    }


    @Override
    protected void onStart() {
        super.onStart();


    }



    @Override
    protected void onResume() {
        super.onResume();
        Log.i("personalpic","resume");



        personal_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("personalpic:next", "완료");
                //이때 사진이 정확하게 업로드 되어있는지의 체크가 요구됨.

                /*
                 * 쉐어드에 저장된 멤버 정보 가지고 오기
                 */
                SharedPreferences sharedPreferences = getSharedPreferences("myAppData",MODE_PRIVATE);
                Gson gson = new Gson();
                login_member = gson.fromJson(sharedPreferences.getString("login_member","no_login"), Member.class);


                if (filePath != null) {

                    //네이버 오픈 api 아이디와 비밀번호 -> 공유 노노 <돈 나감>
                    String clientId= "x4HE1I9HGsC3KNfAz58w";
                    String clientSecret = "GRqNXh0k8u";

                    Retrofit client=MyRetrofit22.getRetrofit2();
                    NaverApiInterface service=client.create(NaverApiInterface.class);

                    final RequestBody requestBody=RequestBody.create(MediaType.parse("image/jpeg"),filePath);

                    MultipartBody.Part body1=MultipartBody.Part.createFormData("image", filePath.getName(), requestBody);

                    Call<NaverRepo> call1=service.naverRepo2(clientId,clientSecret,body1);

                    call1.enqueue(new Callback<NaverRepo>() {
                        @Override
                        public void onResponse(Call<NaverRepo> call, Response<NaverRepo> response) {
                            Log.i("naver",response.toString());

                            if(response.isSuccessful()){
                                Log.i("face",response.toString());

                                //얼굴이 제대로 인식 되었을 때가 중요한 것.


                                if(response.body().info.faceCount==0){
                                    //얼굴 인식이 안된다는 것이므로 다른 사진을 요청하자!
                                    androidx.appcompat.app.AlertDialog.Builder alert = new androidx.appcompat.app.AlertDialog.Builder(MyPageActivity.this);
                                    //다이얼로그 창 띄어주기
                                    alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();     //닫기
                                        }
                                    });

                                    alert.setMessage("얼굴 인식이 잘 되지 않습니다.");
                                    alert.show();
                                }else if(response.body().info.faceCount>1){
                                    //사람 얼굴이 2명이상이라는 뜻이므로 한명의 사진만을 요청하자!
                                    androidx.appcompat.app.AlertDialog.Builder alert = new androidx.appcompat.app.AlertDialog.Builder(MyPageActivity.this);
                                    //다이얼로그 창 띄어주기
                                    alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();     //닫기
                                        }
                                    });

                                    alert.setMessage("여러명의 얼굴이 포함된 사진은 등록이 불가합니다.");
                                    alert.show();
                                }else if(response.body().info.faceCount==1){
                                    //인식이 된 것임!! 앗싸!!! 그럼 서버에 파일을 업로드 시키자!
                                    //이미지를 업로드시키는 것.




                                    //여기서 잠깐!pose가 정면인 것만 가지고 오기.
                                    if(response.body().faces[0].getPose().getValue().equals("frontal_face")){
                                        Toast.makeText(getApplicationContext(),"정면 얼굴이 제대로 인식되었습니다.",Toast.LENGTH_SHORT).show();

                                        //서버에 업로드 시킴
                                        UploadService service = MyRetrofit2.getRetrofit2().create(UploadService.class);

                                        //file이라는 이름으로 업로드
                                        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), filePath);
                                        MultipartBody.Part body1=MultipartBody.Part.createFormData("file", filePath.getName(), requestFile);

                                        //id라는 이름으로 업로드<이거는 UploadService에 이름 정해주었음>
                                        RequestBody id = RequestBody.create(
                                                MediaType.parse("multipart/form-data"), login_member.member_id);

                                        Call<ResponseBody> call1 = service.uploadFile(id, body1);


                                        call1.enqueue(new Callback<ResponseBody>() {
                                            @Override
                                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                Log.i("success",response.toString());
                                                //파일이 업로드에 성공했다면,...기존에 저장해두었던 파일을 삭제하고 완료로 넘김.
                                                //즉  shared도 지우고 그리구 sdcards에 저장되어 있는 사진도 지워줍니다요!!!


                                                SharedPreferences personal_pic_save=getSharedPreferences("personal_pic_save", Activity.MODE_PRIVATE);
                                                String personal_pic_save1=personal_pic_save.getString("personal_pic_save","");
                                                File b_filePath= new File(personal_pic_save1);
                                                //파일먼저지우기 -> 저장 안남게 해줄게.....:)
                                                boolean deleted=b_filePath.delete();
                                                //shared 지우기
                                                SharedPreferences.Editor editor=personal_pic_save.edit();
                                                editor.clear();
                                                editor.commit();


                                                //그리고 나서 메인페이지로 넘어갑니다요!
                                                Log.i("메인으로 그다음에 가야지","메인 레고 !");
                                                Intent intent=new Intent(MyPageActivity.this,HomeActivity.class);
                                                startActivity(intent);
                                                finish();


                                            }

                                            @Override
                                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                Log.i("fail",t.toString());
                                            }
                                        });
                                    }else{
                                        // mypage에서 등록버튼 누를 경우  성공 실패와 상관없이 쉐어드에 저장되던 이미지 처리
                                        //업로드 실패하고 , 다른 액티비트를 갔다가 다시 mypage로 온경우 기존에 실패한
                                        // 프로필 이미지를 지운다
                                        SharedPreferences personal_pic_save=getSharedPreferences("personal_pic_save", Activity.MODE_PRIVATE);
                                        String personal_pic_save1=personal_pic_save.getString("personal_pic_save","");
                                        SharedPreferences.Editor editor=personal_pic_save.edit();
                                        editor.clear();
                                        editor.commit();
                                        Log.i("pose를 알아내자",response.body().faces[0].getPose().getValue());
                                        Toast.makeText(getApplicationContext(),"정면 얼굴을 인식해주세요.",Toast.LENGTH_SHORT).show();
                                    }

                                }

                            }else{
                                Log.i("face","false");
                            }

                        }
                        @Override
                        public void onFailure(Call<NaverRepo> call, Throwable t) {
                            Log.i("naver",t.toString());
                        }
                    });





                } else {
                    //사진이 없는 것이므로 다음페이지 못 넘어감.
                    androidx.appcompat.app.AlertDialog.Builder alert = new androidx.appcompat.app.AlertDialog.Builder(getApplicationContext());
                    //다이얼로그 창 띄어주기
                    alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();     //닫기
                        }
                    });

                    alert.setMessage("사진이 업로드 되어야 다음단계로 넘어갈 수 있습니다.");
                    alert.show();
                }
            }
        });

        pic_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("personalpic:upload","사진 올리자!");

                //다이얼로그 창 띄어주기


                DialogInterface.OnClickListener cameraListener=new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i("personalpic:사진","사진촬영");
                        doTakePicture();
                        dialog.dismiss();     //닫기
                    }
                };

                DialogInterface.OnClickListener albumListener=new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i("personalpic:사진","앨범선택");
                        doTakeAlbum();
                        dialog.dismiss();     //닫기
                    }
                };

                DialogInterface.OnClickListener cancelListener=new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i("personalpic:사진","취소");
                        dialog.dismiss();     //닫기
                    }
                };

                //찍어서 올릴건지, 사진 앨범 중 올릴건지 check
                new AlertDialog.Builder(MyPageActivity.this)
                        .setTitle("사진 선택")
                        .setPositiveButton("사진촬영",cameraListener)
                        .setNeutralButton("취소",cancelListener)
                        .setNegativeButton("앨범선택",albumListener)
                        .show();




            }
        });

    }


    //카메라
    public void doTakePicture(){ //카메라 촬영 후 이미지 가져와야 하므로



        try {
            String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/myApp";
            File dir = new File(dirPath);
            if(!dir.exists()) {
                dir.mkdir();
            }

            filePath = File.createTempFile("IMG", ".jpg", dir);
            if(!filePath.exists()) {
                filePath.createNewFile();
            }



            imageCaptureUri = FileProvider.getUriForFile(this, "com.example.changeapp", filePath);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageCaptureUri);
            startActivityForResult(intent, 0);

        }catch (Exception e) {
            e.printStackTrace();
        }

    }


    //앨범
    public void doTakeAlbum(){
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent.createChooser(intent, "사진을 선택하세요."), 1);


        }catch (Exception e) {
            e.printStackTrace();
        }

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode == 0 && resultCode == RESULT_OK) {  //카메라 사진촬영 선택->정상적으로 사진이 찍혔을 때


            if(filePath != null) {

                //crop할 수 있도록 넘어가기
                Log.d("filePath","onActivityResult1"+filePath+"");
                cropImage(imageCaptureUri,"camera");


            }
        }else if(requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK) { //crop


            Glide.with(MyPageActivity.this)
                    .load(filePath)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(personal_pic);
//
//            filePath=filePath.getAbsoluteFile();
//            personal_pic.setImageBitmap(bitmap);


        }else if(requestCode == 1 && resultCode == RESULT_OK) {
            Log.i("이미지","앨범");
            Uri uri = data.getData();
            content://com.android.providers.downloads.documents/document/2182
            Log.i("uri",uri.toString());
            cropImage(uri,"image");

        }

    }



    public void cropImage(Uri imageCaptureUri,String what){
        Uri imageCaptureUri_crop;
        Uri tmp_imageCaptureUri;

        if(what.equals("camera")) { //카메라로 온 경우


            //저장경로
            imageCaptureUri_crop =
                    FileProvider.getUriForFile(this, "com.example.changeapp", filePath);

            //mypage에서 사진찍기를 통해 사진을 촬영하고
            // 촬영한사진 미리보기에서 확인을 누르면 crop으로 넘어간다
            // crop에서 사진이 width가 height 보다 클경우 90 도 회전시키는 로직이다
            //가져온 사진 Uri bitmap으로 만들후 width와 height을 구한다 . 이후  비교하여 matrix.postRotate를 사용하여 회전시킨다.
            // 회전한 bitmap을 다시 crop에 넣어준다

            Bitmap tmp_bitmap = null;
            try {
                tmp_bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageCaptureUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            int height = tmp_bitmap.getHeight();
            int width = tmp_bitmap.getWidth();
            Log.d("사진 width, height 확인",width+", " +height);
            Matrix matrix=new Matrix();



            matrix.postRotate(90);
            if(width>=height){
                Bitmap resizedBitmap = Bitmap.createBitmap(tmp_bitmap, 0, 0, width, height, matrix, true);
                int height1 = resizedBitmap.getHeight();
                int width1 = resizedBitmap.getWidth();
                Log.d("사진 width, height 확인",width1+", " +height1);
                tmp_imageCaptureUri=getImageUri(getApplicationContext(),resizedBitmap);
                Crop.of(tmp_imageCaptureUri, imageCaptureUri_crop).asSquare().start(this);
            }else{
                Crop.of(imageCaptureUri, imageCaptureUri_crop).asSquare().start(this);
            }

            Log.d("filePath crop",filePath+"");



        }else{ //앨범에서 온 경우

            String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/myApp";

            Log.i("album come",dirPath);
            File dir = new File(dirPath);
            //File dir = new File("/sdcard/myApp");


            Log.i("album come1",dir.getAbsolutePath());

            if(!dir.exists()) {

                dir.mkdir();
            }

            try {
                filePath = File.createTempFile("IMG", ".jpg", dir);
                Log.i("album come2",filePath.getAbsolutePath());
                if(!filePath.exists()) {
                    filePath.createNewFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }



            imageCaptureUri_crop =
                    FileProvider.getUriForFile(MyPageActivity.this, "com.example.changeapp", filePath);

            Crop.of(imageCaptureUri, imageCaptureUri_crop).asSquare().start(MyPageActivity.this);


        }

    }

    //이미지 각도가 뭔지 구해보기
    private int getExifOrientation(String filePath) {
        ExifInterface exif = null;

        try {
            exif = new ExifInterface(filePath);
            Log.i("exif",filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (exif != null) {
            Log.i("exif","not null");
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            Log.i("exif orientation",orientation+"");
            if (orientation != -1) {
                Log.i("exif orientation",orientation+"");

                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        Log.i("exif90",orientation+"");
                        return 90;

                    case ExifInterface.ORIENTATION_ROTATE_180:
                        Log.i("exif180",orientation+"");
                        return 180;

                    case ExifInterface.ORIENTATION_ROTATE_270:
                        Log.i("exif270",orientation+"");
                        return 270;

                }
            }
        }

        return 0;
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.i("personalpic","pause");

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);


    }


    private Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);

        return Uri.parse(path);
    }

}