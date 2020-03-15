<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>Document</title>
</head>
<body>
<h>用户登录</h>
    <label>姓名：</label><input type="text" name="username" placeholder="请输入姓名">
    <label>密码：</label><input type="password" name="password" placeholder="密码">
    <button type="button" id="button">登录 </button>
<script src="http://code.jquery.com/jquery-3.4.1.js" crossorigin="anonymous"></script>
<script>

  $('#button').click(function(){
    $.ajax({
      type:'post',
      url:"/user/login.do",
      data:{
        username:'admin',
        password:'admin'
      },
      success:function(res){
        if(res.status==0){
          alert('登陆成功')
        }
      }
    })
  })

</script>
</body>
</html>
