<?php
// 데이터베이스 정보입력
$db_host="localhost";
$db_user="ansgyqja";
$db_password="Akwldrk1!";
$db_name="team";

// 데이터베이스 연결
$con=mysqli_connect($db_host,$db_user,$db_password,$db_name);

// 연결에 실패할 경우 예외처리
if(!$con){ die("연결 실패 : ".mysqli_connect_error()); }


/**
 * 어떤 요청인지 확인
 * - jsonObject 또는 jsonArray인 경우에는 디코드해주기
 */
$data = '';
$data_arr = '';
if(substr(file_get_contents("php://input"),0,1)=='['){ // jsonArray
    // echo file_get_contents("php://input");
    $data_arr = json_decode(file_get_contents("php://input"));
}else{ //jsonObject
    // echo file_get_contents("php://input");
    $data = json_decode(file_get_contents("php://input"));
}


/**
 * 테스트
 */
if(($_GET['mode'])=='test'){
  echo "test";
}

/**
 * 로그인 요청
 */
if(($_POST['mode'])=='login_action'){

   $id = $_POST['id'];
  $pw = $_POST['pw'];

   $sql = "SELECT * FROM member WHERE member_id='$id'";
   $result = mysqli_query($con,$sql);
  $login_member = mysqli_fetch_assoc($result);
  $login_member_no = $login_member['member_no'];
  if(!$login_member['member_id'] || !($pw === $login_member['member_pw'])){ // 로그인 실패
    echo -1;
  }else{ // 로그인 성공
    echo $login_member_no;
   }
}

/**
 * 회원가입 요청
 */
if(($_POST['mode'])=='join_action'){

  $id = $_POST['id'];
  $pw = $_POST['pw'];

    if (!mysqli_query($con, "INSERT INTO member (member_id, member_pw) VALUES ('$id','$pw');")) {
        echo "회원가입 실패 (Error: ".mysqli_error($con).")";
    }else{
        if(!mysqli_query($con,"UPDATE member set member_pic='./team/$id.jpg' where member_id='$id';")){
          echo "이미지 수정 실패 (Error: ".mysqli_error($con).")";
        }else{
		if(!mysqli_query($con,"UPDATE member set crop_pic='./team1/$id.jpg' where member_id='$id';")){

		}else{
			echo "0";
		}
          echo "0";
        }
    }
}
/**
 * 로그인 멤버의 정보를 얻는 요청 (JSONObject)
 */
if($data->mode=='find_login_member'){
    /**
     * 요청 인자 받기
     */
    $member_no = $data->login_member_no;

    /**
     * SQL문 시작
     */
    $sql = "SELECT * FROM member WHERE member_no='$member_no';";
    $result = mysqli_query($con,$sql);
    $row = mysqli_fetch_assoc($result);

    $member_no = $row['member_no']; // 멤버 번호
    $member_id = $row['member_id']; // 멤버 아이디
    $member_pw = $row['member_pw']; // 멤버 비밀번호

    $member = ['member_no'=>$member_no,'member_id'=>$member_id,'member_pw'=>$member_pw];
    echo json_encode($member);
}

// /**
//  * 게임 추가 요청
//  */
// if(($_POST['mode'])=='add_game'){
//
//     $sql = "SELECT count(*) c FROM game;";
//     $result = mysqli_query($con,$sql);
//     $row = mysqli_fetch_assoc($result);
//     $game_no = $row['c']+1;
//
//     $game_name = $_POST['game_name'];
//     $game_admin_id = $_POST['game_admin_id'];
//     $game_admin_member_no = $_POST['game_admin_member_no'];
//     $game_join_user = $game_admin_id.',null,null,null';
//     if (!mysqli_query($con, "INSERT INTO game (game_name, game_admin_id, game_join_user,is_game_exit)
//                                 VALUES ('$game_name','$game_admin_id','$game_join_user',0);")) {
//         echo -1;
//         // echo "게임 추가 실패 (Error: ".mysqli_error($con).")";
//     }else{
//         echo $game_no;
//     }
// }
//
// /**
//  * 게임 리스트 받는 요청 (JSONArray)
//  */
// if($data_arr[0]->mode=='get_game_list'){
//     $games=[];
//     $sql ="SELECT * FROM game;";
//     $result = mysqli_query($con,$sql);
//     if(mysqli_num_rows($result)>0){
//         while($game = mysqli_fetch_array($result)){
//
//             /**
//              * 게임 정보 가지고 오기
//              */
//             $game_no = $game['game_no'];
//             $game_name = $game['game_name'];
//             $game_admin_id = $game['game_admin_id'];
//             $game_join_user = $game['game_join_user'];
//             $is_game_exit = $game['is_game_exit'];
//
//             $temp_game_item = ['game_no'=>$game_no, 'game_name'=>$game_name, 'game_admin_id'=>$game_admin_id, 'game_join_user'=>$game_join_user, 'is_game_exit'=>$is_game_exit];
//
//             array_push($games,$temp_game_item);
//
//         } // end while
//     }else{
//
//     }
//
//     echo json_encode($games);
// }
//
// /**
//  * 게임방 종료
//  */
// if(($_POST['mode'])=='exit_game'){
//     $game_no = $_POST['game_no'];
//     if (!mysqli_query($con, "UPDATE game SET is_game_exit=-1 WHERE game_no='$game_no';")) {
//         echo -1;
//     }else{
//         echo 0;
//     }
// }
//
// /**
//  * 게임 참여자를 얻는 요청
//  */
// if(($_POST['mode'])=='get_game_join_user'){
//     $game_no = $_POST['game_no'];
//     $member_id = $_POST['member_id'];
//     $sql ="SELECT * FROM game WHERE game_no='$game_no';";
//     $result = mysqli_query($con,$sql);
//     $row = mysqli_fetch_assoc($result);
//     $game_join_user = $row['game_join_user'];
//     echo $game_join_user;
// }
//
// /**
//  * 게임방 참여
//  */
// if(($_POST['mode'])=='game_join'){
//     $game_no = $_POST['game_no'];
//     $temp_game_join_user = $_POST['temp_game_join_user'];
//     if (!mysqli_query($con, "UPDATE game SET game_join_user='$temp_game_join_user' WHERE game_no='$game_no';")) {
//         echo -1;
//     }else{
//         echo 0;
//     }
// }
//
// /**
//  * 게임방 나감
//  */
// if(($_POST['mode'])=='game_quit'){
//     $game_no = $_POST['game_no'];
//     $temp_game_join_user = $_POST['temp_game_join_user'];
//     if (!mysqli_query($con, "UPDATE game SET game_join_user='$temp_game_join_user' WHERE game_no='$game_no';")) {
//         echo -1;
//     }else{
//         echo 0;
//     }
// }

?>
