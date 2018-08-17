# miaosha
my miaosha system with high concurrency

业务流程如下：
1.登录：
  *   客户端：向服务端登录接口发送ajax请求(login/do_login)，带上手机号和密码
  
  *   服务端：1）根据用户提供的手机号，向数据库中查找用户是否存在，并且验证密码。验证不通过，直接向登录页面返回提示信息；验证通过，进入2)
             2）生成一个token(UUID.random())
             3) 将(token，user)存储到redis中，以后的接口需要用户验证的时候直接查找redis，不用查找mysql，提高访问速度
             4) 将("token", token)加入到response中，准备返回给客户端，并添加到客户端的cookie中，客户端之后再次发送请求需要进行用户验证的时候可以
                直接以token作为key值来获取redis中的user。其中，每次在使用token值获取user后，需要重新在cookie和redis中设置token的失效时间，保证
                token能正常失效
             5）向
             2）生成一个token(UUID.random())ke
             2）生成一个token(UUID.random())
