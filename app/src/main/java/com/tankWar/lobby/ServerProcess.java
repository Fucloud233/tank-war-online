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
    private String nickname;//当前用户的昵称
    private String account;//当前用户的账号
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
                    register();
                } else if (strKey.equals("Create")) {
                    //处理创建房间的消息
                    createroom();
                } else if (strKey.equals("select room")) {
                    //处理用户选择房间的消息
                    selectroom();
                } else if (strKey.equals("password")) {
                    //选择的房间若设置了密码，需要验证
                    validPassword();
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

    //创建房间,分有密码和无密码的情况
    public void createroom() throws IOException {
        String isPassword=st.nextToken();
        if (isPassword.equals("password")){
            String userName=st.nextToken();
            String account=st.nextToken();
            String roomName=st.nextToken();
            String userNum=st.nextToken();
            String password=st.nextToken();
            RoomNum=account;//对于房主来说，房间号就是他的账号
            rooms.add(new Room(true,userName,account,roomName,userNum,password,socket));
            userEnterRoomSuccess();//房主创建房间后就会成功进入房间
        } else if (isPassword.equals("no password")) {
            String userName=st.nextToken();
            String account=st.nextToken();
            String roomName=st.nextToken();
            String userNum=st.nextToken();
            RoomNum=account;
            rooms.add(new Room(false,userName,account,roomName,userNum,"",socket));
            userEnterRoomSuccess();//房主创建房间后就会成功进入房间
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
    ///////////////////成功进入房间//////////////////////////////

    private void userEnterRoomSuccess() throws IOException {
        Date t = new Date();
        //返回给客户端成功登录的内容
        out.println("select room|success");
       //更新房间里面的信息，并刷新客户端的游戏大厅和游戏房间里的聊天框
        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            if(room.getRoomNum().equals(RoomNum)){
                room.addOnlineUser(nickname,account,socket);//加昵称、账号和套接字
                freshClientsRoomOnline();//用户进入房间，房间里面的人员信息会增加
                freshClientsLobbyOnline();//用户进入房间，大厅里的房间人数会变
                sendRoomAll("roomTalk|>>>欢迎 " + nickname + " 加入游戏");
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
        String strOnline = "room online";
        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            if(room.getRoomNum().equals(RoomNum)){
                for (int j = 0; j < room.getEnter_num(); j++) {
                    strOnline += "|" + room.findNameUser(j);
                }
                System.out.println("当前在线人数:"+room.getEnter_num());
                sendRoomAll(strOnline);
            }
        }

    }
    //刷新游戏大厅的在线房间
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