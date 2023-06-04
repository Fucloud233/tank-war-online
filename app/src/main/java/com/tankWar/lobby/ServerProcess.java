package com.tankWar.lobby;


import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;
import java.util.Date;


//接收到客户端socket发来的信息后进行解析、处理、转发
public class ServerProcess extends Thread {
    //连接相关
    private Socket socket = null;// 定义客户端套接字
    private BufferedReader in;// 定义输入流
    private PrintWriter out;// 定义输出流
    //维护的与这个服务端套接字连接的用户信息
    private String RoomNum;//用户所在的房间号
    private String account;//当前用户的账号
    private String nickname;
    private final String USERLIST_FILE = "E:\\lobby\\app\\src\\main\\java\\com\\tankWar\\lobby\\_user.txt"; // 设定存放用户信息的文件
    //维护的全局信息
    private static ArrayList<Room> rooms=new ArrayList<>();//维护的每个房间
    private static Vector nameUser = new Vector(10,5);//保存在线用户的用户名
    private static Vector onlineUser = new Vector(10, 5);//保存在线用户的用户账号
    private static Vector socketUser = new Vector(10, 5);//保存在线用户的Socket对象
    private String strReceive; //客户端接收的原始信息
    private String strKey; //保存信息的关键字 login talk init reg
    private StringTokenizer st;   //拆分字符串
    //连接数据库
    private final String databaseURL="jdbc:mysql://localhost:3306/project";
    private final String userName="root";
    private final String passWord="yangyt66";

    //处理从客户端Socket接收到的信息
    public ServerProcess(Socket client) throws IOException {
        socket = client;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));  // 客户端接收
        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                socket.getOutputStream())), true);  // 客户端输出
    }

    //使用while循环来连续读取从客户端发送来的信息  根据不同的信息进行不同的操作执行
    public void run() {
        try {
            while (true) {
                strReceive = in.readLine();  // 从服务器端接收一条信息后拆分、解析，并执行相应操作
                st = new StringTokenizer(strReceive, "|");
                strKey = st.nextToken();
                if (strKey.equals("login")) {
                    //登录
                    login();
                } else if (strKey.equals("talk")) {
                    //发言
                    talk();
                }else if (strKey.equals("roomTalk")) {
                    //房间内发言
                    roomTalk();
                } else if (strKey.equals("init")) {
                    //刷新在线用户列表
                    freshClientsOnline();
                } else if (strKey.equals("register")) {
                    //注册
                    /*register();*/
                } else if (strKey.equals("Create")) {
                    ///接收到CreateRoomWindow传来的Create 和相关参数
                    //处理创建房间的消息
                    createroom();
                } else if (strKey.equals("Select room")) {
                    //处理用户选择房间的消息   客户端点击选择房间后传来的Select room
                    selectroom();
                } else if (strKey.equals("password")) {
                    //选择的房间若设置了密码，需要验证
                    validPassword();
                }else if(strKey.equals("exitRoom")){
                    //退出房间
                    exitroom();
                }else if(strKey.equals("isReady")){
                    //有用户准备
                    userReady();
                }else if(strKey.equals("cancelReady")){
                    //有用户取消准备
                    userCancelReady();
                }
            }
        } catch (IOException e) { // 用户关闭客户端造成此异常，关闭该用户套接字。
            String leaveUser = null;
            try {
                leaveUser = closeSocket();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            Date t = new Date();
            System.out.println("用户" + leaveUser + "已经退出" + "退出时间" + t.toLocaleString());
            try {
                freshClientsOnline();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //用户取消准备
    private void userCancelReady() {
        //取消准备的玩家
        String cancelReadyname=st.nextToken();
        //传递给客户端的房间在线用户
        String strOnline = "room online";
        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            if(room.getRoomNum().equals(RoomNum)){
                for (int j = 0; j < room.getEnter_num(); j++) {
                    //如果是房主 后续在房间人员列表中  设置有房主的标识///////////
                    if(room.findNameUser(j).equals(room.getHostName())){
                        strOnline += "|" + room.findNameUser(j)+"(房主)";
                    }
                    else if(room.findOnlineUser(j).equals(cancelReadyname))  //确定取消准备的人
                    {
                        room.changeStatusUser(j);  //切换向量列表中玩家的状态
                        strOnline += "|" + room.findNameUser(j)+"(未准备)";
                    }
                    else{
                        strOnline += "|" + room.findNameUser(j)+"("+room.findStatusUser(j)+")";
                    }
                }
                System.out.println("当前在线人数:"+room.getEnter_num());
                //向房间内所有用户发送  发送房间内全部人员的名字
                sendRoomAll(strOnline);
            }
        }
    }

    //用户选择进行准备
    private void userReady() {
        //准备的玩家
        String Readyname=st.nextToken();
        //传递给客户端的房间在线用户
        String strOnline = "room online";
        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            if(room.getRoomNum().equals(RoomNum)){
                for (int j = 0; j < room.getEnter_num(); j++) {
                    //进行打印测试
                    System.out.println(Readyname);
                    System.out.println(room.findOnlineUser(j));

                    //如果是房主 后续在房间人员列表中  设置有房主的标识///////////
                    if(room.findNameUser(j).equals(room.getHostName())){
                        strOnline += "|" + room.findNameUser(j)+"(房主)";
                    }
                    else if(room.findOnlineUser(j).equals(Readyname))  //确定准备的人
                    {
                        System.out.println("is rrrready");
                        room.changeStatusUser(j);  //切换向量列表中玩家的状态
                        strOnline += "|" + room.findNameUser(j)+"(已准备)";
                    }
                    else{
                        strOnline += "|" + room.findNameUser(j)+"("+room.findStatusUser(j)+")";
                    }
                }
                System.out.println("当前在线人数:"+room.getEnter_num());
                //向房间内所有用户发送  发送房间内全部人员的名字
                sendRoomAll(strOnline);
            }
        }
    }






    /*
    //判断注册用户的昵称是否重复
    private boolean isExistUserName(String name) {
        String query = "SELECT * FROM users WHERE nickname = ?";

        try (Connection connection = DriverManager.getConnection(databaseURL, userName, passWord);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;

    }

    //判断注册的用户账号是否重复
    private boolean isExistUser(String account) {
        String query = "SELECT * FROM users WHERE account = ?";

        try (Connection connection = DriverManager.getConnection(databaseURL, userName, passWord);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, account);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;

    }

    //判断用户名和密码是否正确
    private boolean isUserLogin(String account, String password) {
        String query = "SELECT * FROM users WHERE account = ? AND password = ?";

        try (Connection connection = DriverManager.getConnection(databaseURL, userName, passWord);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, account);
            statement.setString(2, password);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    //注册
    private void register() throws SQLException {
        String name = st.nextToken(); // 得到用户名称
        String account=st.nextToken();// 得到用户账号
        String password = st.nextToken().trim(); // 得到用户密码
        if (isExistUserName(name)){//昵称重复
            System.out.println("[ERROR] " + name + " Register fail!");
            out.println("register|name");

        } else if (isExistUser(account)) {//账号重复
            System.out.println("[ERROR] " + account + " Register fail!");
            out.println("register|account");
        } else {
            String insertQuery = "INSERT INTO users (nickname, account,password) VALUES (?, ?,?)";
            try (Connection connection = DriverManager.getConnection(databaseURL, userName, passWord);
                 PreparedStatement statement = connection.prepareStatement(insertQuery)) {
                statement.setString(1, name);
                statement.setString(2, account);
                statement.setString(3, password);
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("User " + name + " 注册成功");
                    out.println("register|success");
                }
            }
        }
    }
*/

    //判断用户名和密码是否正确 原始版本！！！！！！！！！！！！！！
    private boolean isUserLogin(String name, String password) {
        String strRead;
        try {
            //打开文件
            FileInputStream inputfile = new FileInputStream(USERLIST_FILE);
            DataInputStream inputdata = new DataInputStream(inputfile);
            //与文件中的账户名的密码一一比较  找到了则登陆成功
            while ((strRead = inputdata.readLine()) != null) {
                if (strRead.equals(name + "|" + password)) {
                    return true;
                }
            }
        } catch (FileNotFoundException fn) {
            System.out.println("[ERROR] User File has not exist!" + fn);
            out.println("warning|读写文件时出错!");
        } catch (IOException ie) {
            System.out.println("[ERROR] " + ie);
            out.println("warning|读写文件时出错!");
        }
        return false;
    }

    //创建房间,分有密码和无密码的情况
    public void createroom() throws IOException {
        String isPassword=st.nextToken();
        if (isPassword.equals("password")){
            String userName=st.nextToken();
            String account=st.nextToken(); //传来的账号
            String roomName=st.nextToken();
            String userNum=st.nextToken();
            String password=st.nextToken();
            RoomNum=account;//对于房主来说，房间号就是他的账号
            //////////////////////////////修改传参房主名称  方便进行测试！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
            rooms.add(new Room(true,nickname,account,roomName,userNum,password,socket));
            OwnerEnterRoomSuccess();//房主创建房间后就会成功进入房间
        } else if (isPassword.equals("no password")) {
            String userName=st.nextToken();
            String account=st.nextToken();
            String roomName=st.nextToken();
            String userNum=st.nextToken();
            RoomNum=account;
            //////////////////////////////修改传参  方便进行测试！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
            rooms.add(new Room(false,nickname,account,roomName,userNum,"",socket));
            OwnerEnterRoomSuccess();//房主创建房间后就会成功进入房间
        }
        freshClientsLobbyOnline();//每次创建了一个房间，服务端要刷新所有客户端的房间列表
    }
    //进入房间
    public void selectroom() throws IOException {
        String roomnum=st.nextToken();//获取到用户选择的房间号
        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            if (room.getRoomNum().equals(roomnum)){
                if (room.getEnter_num()>=room.getUser_num()||room.getRoomStatus().equals("游戏中")){//房间达到人数上限或者房间正在游戏时，无法进入房间
                    out.println("select room|failed");
                } else if (room.getIs_password()) {//提示有密码
                    out.println("select room|password");
                }else {//成功进入房间
                    RoomNum=roomnum;//这是用户选择的房间
                    userEnterRoomSuccess();
                }
            }
            break;
        }
    }
    //验证进入房间的密码
    public void validPassword() throws IOException {
        String roomnum=st.nextToken();//在获取一次房间号，因为有可能在输入密码期间有其他人进入房间了
        String password=st.nextToken();
        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            if (room.getRoomNum().equals(roomnum)){
                if (room.getEnter_num()>=room.getUser_num()||room.getRoomStatus().equals("游戏中")){//房间达到人数上限或者房间正在游戏时，无法进入房间
                    out.println("select room|failed");
                } else if (!room.getPassword().equals(password)) {//密码错误
                    out.println("select room|password error");
                }else {//成功进入房间
                    RoomNum=roomnum;//这是用户选择的房间
                    userEnterRoomSuccess();
                }
            }
            break;
        }

    }
/*
    //登录
    private void login() throws IOException, SQLException {
        account = st.nextToken(); // 得到用户账号
        String password = st.nextToken().trim();// 得到用户密码
        //先判断该用户是否登陆过了
        for (Object userAccount:onlineUser){
            if (account.equals(userAccount)){
                out.println("warning|double");//不能重复登陆的提示
            }
        }
        boolean succeed = false;
        Date t = new Date();
        System.out.println("用户" + account + "正在登陆..." + "\n" + "密码 :" + password + "\n" + "端口 "
                + socket + t.toLocaleString());
        //调用用户账号和密码的判断
        if (isUserLogin(account, password)) {      // 判断用户名和密码 ->转为在数据库中寻找
            userLoginSuccess(account);
            succeed = true;
        }
        if (!succeed) {
            out.println("warning|" + account + "登陆失败，请检查您的输入!");
            System.out.println("用户 "+ account + "登陆失败！" + t.toLocaleString());
        }
    }
*/

    //登录  原始版本！！！！！！！！！！！！！！！！！！！！！！
    private void login() throws IOException, SQLException {
        String name = st.nextToken(); // 得到用户名称
        String password = st.nextToken().trim();// 得到用户密码
        boolean succeed = false;
        Date t = new Date();
        System.out.println("用户" + name + "正在登陆..." + "\n" + "密码 :" + password + "\n" + "端口 "
                + socket + t.toLocaleString());
        //调用用户名和密码的判断
        if (isUserLogin(name, password)) {      // 判断用户名和密码 ->转为在数据库中寻找
            userLoginSuccess(name);
            succeed = true;
        }
        if (!succeed) {
            out.println("warning|" + name + "登陆失败，请检查您的输入!");
            System.out.println("用户 "+ name + "登陆失败！" + t.toLocaleString());
        }
    }
    /*
    //登陆成功返回给用户它的昵称
    private void userLoginSuccess(String account) throws IOException, SQLException {
        Date t = new Date();
        //获取用户昵称
        String insertQuery = "SELECT nickname FROM users WHERE account = ? ";
        try (Connection connection = DriverManager.getConnection(databaseURL, userName, passWord);
             PreparedStatement statement = connection.prepareStatement(insertQuery)) {
            statement.setString(1, account);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                nickname = resultSet.getString("nickname");
                out.println("login|succeed" + "|" + nickname);
                //保存用户信息
                nameUser.addElement(nickname);
                onlineUser.addElement(account);
                socketUser.addElement(socket);
                System.out.println("用户：" + nickname + "登录成功，" + "登录时间:" + t.toString());
                freshClientsOnline();
                freshClientsLobbyOnline();
                sendAll("talk|>>>欢迎 " + nickname + " 进来与我们一起交谈!");
                System.out.println("[SYSTEM] " + nickname + " login succeed!");
            }
        }

    }
    */
    //在注册成功后自动登录 原始版本！！！！！！！！！！！！！！！！！！！！！！
    private void userLoginSuccess(String name) throws IOException {
        Date t = new Date();
        //返回给客户端成功登录的内容
        out.println("login|succeed|"+name);
//        sendAll("online|" + name);
        ///////////////////////由于获取不到昵称 我设置随机数来指定
        // 创建一个随机数生成器对象
        Random random = new Random();
        // 生成随机整数
        int randomInt = random.nextInt();
        nickname="测试者"+randomInt;
        ////////////////////////////////////////////////////测试的nickname

        onlineUser.addElement(name);
        ///////////////////////////进行赋值 防止为空/////////////////////////
        this.account=name;
        socketUser.addElement(socket);
        nameUser.addElement(nickname); //增加用户昵称列表

        System.out.println("用户：" + name + "登录成功，" + "登录时间:" + t.toLocaleString());
        freshClientsOnline();
        freshClientsLobbyOnline();
        sendAll("talk|>>>欢迎 " + name + " 进来与我们一起交谈!");
        System.out.println("[SYSTEM] " + name + " login succeed!");
    }

    ///////////////////普通用户成功进入房间//////////////////////////////
    private void userEnterRoomSuccess() throws IOException {
        Date t = new Date();
        //返回给客户端成功登录的内容   客户端能成功显示窗口
        out.println("select room|success");
       //更新房间里面的信息，并刷新客户端的游戏大厅和游戏房间里的聊天框
        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            if(room.getRoomNum().equals(RoomNum)){
                room.addOnlineUser(nickname,account,socket);//加昵称、账号和套接字
                freshClientsRoomOnline();//用户进入房间，房间里面的人员信息会增加
                freshClientsLobbyOnline();//用户进入房间，大厅里的房间人数会变
                sendRoomAll("roomTalk|>>>欢迎 " + nickname + " 加入房间");
                break;
            }
        }
    }
    ///////////////////房主成功进入房间//////////////////////////////
    private void OwnerEnterRoomSuccess() throws IOException {
        Date t = new Date();
        //不再给客户端传成功创建房间的信息了 游戏窗口在CreateRoomWindow已经打开
        //更新房间里面的信息，并刷新客户端的游戏大厅和游戏房间里的聊天框
        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            if(room.getRoomNum().equals(RoomNum)){
                room.addOnlineUser(nickname,account,socket);//加昵称、账号和套接字
                freshClientsRoomOnline();//用户进入房间，房间里面的人员信息会增加
                freshClientsLobbyOnline();//用户进入房间，大厅里的房间人数会变
                sendRoomAll("roomTalk|>>>欢迎 " + nickname + " 加入房间");
                break;
            }
        }
    }

    //////////////////////////////////////退出房间/////////////////////////
    //退出房间
    public void exitroom() throws IOException {
        // 在房间列表中找到对应的房间
        for (Room room : rooms) {
            if (room.getRoomNum().equals(RoomNum)) {
                //如果是房主退出了，则解散房间
                if(room.getHostName().equals(nickname)){
                    //从总房间列表中将这个房间删除
                    rooms.remove(room);
                    //先要进行刷新  防止场景切换后出现问题  客户端需要新的Rooms列表进行大厅场景的切换
                    freshClientsLobbyOnline();//刷新大厅内的房间列表
                    //向房间内的所有客户端发送 房间解散的信息
                    Socket socketSend;
                    PrintWriter outSend;
                    if (room.getRoomNum().equals(RoomNum)){
                        for (int j = 0; j < room.getEnter_num(); j++) {
                            socketSend = room.findSocketUser(j);
                            outSend = new PrintWriter(new BufferedWriter(
                                    new OutputStreamWriter(socketSend.getOutputStream())),
                                    true);
                            outSend.println("Owner exitRoom|"+room.getRoomNum());
                        }
                        break;
                    }
                    // 从房间中移除该用户 使用Room中的函数
                    room.removeOnlineUser(room.getUserIndex(nickname));
                    //调用函数 清空房间内部的所有内容
                    room.ClearALL();
                    //返回给客户端删除房间的消息
                }
                //普通用户退出该房间
                else {
                    // 从房间中移除该用户 使用Room中的函数
                    room.removeOnlineUser(room.getUserIndex(nickname));
                    freshClientsRoomOnline();//用户退出房间，房间里面的人员信息会增加
                    // 发送退出房间消息给其他用户
                    sendRoomAll("roomTalk|>>>再见 " + nickname + " 退出房间");
                }
                freshClientsLobbyOnline();//刷新大厅内的房间列表
                break;
            }
        }
    }



    //大厅发言
    private void talk() throws IOException {
        String strTalkInfo = st.nextToken(); // 得到聊天内容;
        String strSender = st.nextToken(); // 得到发消息人
        String strReceiver = st.nextToken(); // 得到接收人
        System.out.println("[TALK_" + strReceiver + "] " + strTalkInfo);
        Socket socketSend;
        PrintWriter outSend;
        Date t = new Date();

        // 得到当前时间
        GregorianCalendar calendar = new GregorianCalendar();
        String strTime = "(" + calendar.get(Calendar.HOUR) + ":"
                + calendar.get(Calendar.MINUTE) + ":"
                + calendar.get(Calendar.SECOND) + ")";
        strTalkInfo += strTime;

        //记录事件
        System.out.println("Constants.USER" + strSender + "对 " + strReceiver + "说:" + strTalkInfo
                + t.toLocaleString());

        if (strReceiver.equals("All")) {
            sendAll("talk|" + strSender + " 对所有人说：" + strTalkInfo);
        } else {
            if (strSender.equals(strReceiver)) {
                out.println("talk|>>>不能自言自语哦!");
            } else {
                for (int i = 0; i < nameUser.size(); i++) {
                    if (strReceiver.equals(nameUser.elementAt(i))) {//更新接收方的消息
                        socketSend = (Socket) socketUser.elementAt(i);
                        outSend = new PrintWriter(new BufferedWriter(
                                new OutputStreamWriter(
                                        socketSend.getOutputStream())), true);
                        outSend.println("talk|" + strSender + " 对你说："
                                + strTalkInfo);
                    } else if (strSender.equals(nameUser.elementAt(i))) {//更新发送方的消息
                        socketSend = (Socket) socketUser.elementAt(i);
                        outSend = new PrintWriter(new BufferedWriter(
                                new OutputStreamWriter(
                                        socketSend.getOutputStream())), true);
                        outSend.println("talk|你对 " + strReceiver + "说："
                                + strTalkInfo);
                    }
                }
            }
        }
    }
    ///////////////////////////在房间内部发言////////////////////////
    private void roomTalk() throws IOException {
        String strTalkInfo = st.nextToken(); // 得到聊天内容;
        String strSender = st.nextToken(); // 得到发消息人
        String strReceiver = st.nextToken(); // 得到接收人
        System.out.println("[TALK_" + strReceiver + "] " + strTalkInfo);
        Socket socketSend;
        PrintWriter outSend;
        Date t = new Date();

        // 得到当前时间
        GregorianCalendar calendar = new GregorianCalendar();
        String strTime = "(" + calendar.get(Calendar.HOUR) + ":"
                + calendar.get(Calendar.MINUTE) + ":"
                + calendar.get(Calendar.SECOND) + ")";
        strTalkInfo += strTime;

        //记录事件
        System.out.println("Constants.USER" + strSender + "对 " + strReceiver + "说:" + strTalkInfo
                + t.toLocaleString());

        if (strReceiver.equals("All")) {
            sendRoomAll("roomTalk|" + strSender + " 对所有人说：" + strTalkInfo);
        } else {
            if (strSender.equals(strReceiver)) {
                out.println("roomTalk|>>>不能自言自语哦!");
            } else {
                for (int i = 0; i < rooms.size(); i++) {
                    Room room = rooms.get(i);
                    if(room.getRoomNum().equals(RoomNum)){
                        for (int j=0;j<room.getEnter_num();j++){
                            if (strReceiver.equals(room.findNameUser(j))) {//更新接收方的消息
                                socketSend = room.findSocketUser(j);
                                outSend = new PrintWriter(new BufferedWriter(
                                        new OutputStreamWriter(
                                                socketSend.getOutputStream())), true);
                                outSend.println("roomTalk|" + strSender + " 对你说："
                                        + strTalkInfo);
                            } else if (strSender.equals(room.findNameUser(j))) {//更新发送方的消息
                                socketSend = room.findSocketUser(j);
                                outSend = new PrintWriter(new BufferedWriter(
                                        new OutputStreamWriter(
                                                socketSend.getOutputStream())), true);
                                outSend.println("roomTalk|你对 " + strReceiver + "说："
                                        + strTalkInfo);
                            }
                        }
                    }
                }

            }
        }
    }
    /////////////////////////刷新大厅内的在线用户/////////////////////
    //在线用户列表
    private void freshClientsOnline() throws IOException {
        String strOnline = "online";
        for (int i = 0; i < nameUser.size(); i++) {
            strOnline += "|" + nameUser.elementAt(i);
        }
        System.out.println("当前在线人数:"+nameUser.size());
        sendAll(strOnline);
    }

    ////////////////////////刷新房间内的在线用户///////////////////
    private void freshClientsRoomOnline() throws IOException {
        //传递给客户端的房间在线用户
        String strOnline = "room online";
        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            if(room.getRoomNum().equals(RoomNum)){
                for (int j = 0; j < room.getEnter_num(); j++) {
                    //如果是房主 后续在房间人员列表中  设置有房主的标识///////////
                    if(room.findNameUser(j).equals(room.getHostName())){
                        strOnline += "|" + room.findNameUser(j)+"(房主)";
                        System.out.println(room.findNameUser(j));
                    }else{
                        //不是房主
                        strOnline += "|" + room.findNameUser(j);
                    }
                }
                System.out.println("当前在线人数:"+room.getEnter_num());
                //向房间内所有用户发送  发送房间内全部人员的名字
                sendRoomAll(strOnline);
            }
        }
    }

    ///////////////////////////刷新游戏大厅的在线房间///////////////////
    private void freshClientsLobbyOnline() throws IOException {
        String strOnline = "lobby";
        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            strOnline += "|" + room.getRoomNum();//房间号
            strOnline += "|" + String.valueOf(i);//号码
            strOnline += "|" + room.getRoomName();//房间名
            strOnline += "|" + room.getHostName();//房主名字
            strOnline += "|" + String.valueOf(room.getEnter_num());
            strOnline += "|" + String.valueOf(room.getUser_num());
            strOnline += "|" + room.getRoomStatus();//房间状态
        }
        sendAll(strOnline);
        System.out.println(strOnline);
    }


    //信息群发
    private void sendAll(String strSend) {
        Socket socketSend;
        PrintWriter outSend;
        try {
            for (int i = 0; i < socketUser.size(); i++) {
                socketSend = (Socket) socketUser.elementAt(i);
                outSend = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socketSend.getOutputStream())),
                        true);
                outSend.println(strSend);
            }
        } catch (IOException e) {
            System.out.println("[ERROR] send all fail!");
        }
    }
    ////////////////给房间的所有人发////////////////////////
    private void sendRoomAll(String strSend) {
        Socket socketSend;
        PrintWriter outSend;
        try {
            for (int i = 0; i < rooms.size(); i++) {
                Room room = rooms.get(i);
                if (room.getRoomNum().equals(RoomNum)){
                    for (int j = 0; j < room.getEnter_num(); j++) {
                        socketSend = room.findSocketUser(j);
                        outSend = new PrintWriter(new BufferedWriter(
                                new OutputStreamWriter(socketSend.getOutputStream())),
                                true);
                        outSend.println(strSend);
                    }
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("[ERROR] send all fail!");
        }
    }

    //关闭套接字，并将用户信息从在线列表中删除
    private String closeSocket() throws IOException {
        String strUser = "";
        for (int i = 0; i < socketUser.size(); i++) {//删除用户信息
            if (socket.equals((Socket) socketUser.elementAt(i))) {
                strUser = onlineUser.elementAt(i).toString();
                socketUser.removeElementAt(i);
                onlineUser.removeElementAt(i);
                nameUser.removeElementAt(i);
                try {
                    freshClientsOnline();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        for (int i = 0; i < rooms.size(); i++) {//删除房间信息
            Room room = rooms.get(i);
            if (room.getRoomNum().equals(RoomNum)){
                for (int j = 0; j < room.getEnter_num(); j++) {
                    if (socket.equals(room.findSocketUser(j))){
                        room.removeOnlineUser(i);
                        freshClientsRoomOnline();
                        freshClientsLobbyOnline();
                    }
                }
                rooms.remove(i);
                break;
            }

        }


        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            System.out.println("[ERROR] " + e);
        }
        return strUser;
    }
}