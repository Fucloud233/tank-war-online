package com.tankWar.server;


import com.tankWar.game.server.Config;
import com.tankWar.game.server.GameServer;

import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;
import java.util.Date;


//接收到客户端socket发来的信息后进行解析、处理、转发
public class LobbyServer extends Thread {
    //连接相关
    ServerSocket serverSocket = null; // 创建服务器端套接字
    // 服务端监听端口
    int serverPort;
    // 状态
    public boolean bServerIsRunning = false;

    // 游戏状态信息
    boolean gameStart = false;

    //维护的全局信息
    private static ArrayList<Room> rooms=new ArrayList<>();//维护的每个房间
    private static Vector<String> nameUser = new Vector<>(10,5);//保存在线用户的用户名
    private static Vector<String> onlineUser = new Vector<>(10, 5);//保存在线用户的用户账号
    private static Vector<Socket> socketUser = new Vector<>(10, 5);//保存在线用户的Socket对象

    DBOperator operator = null;


    public LobbyServer(int port){
        this.serverPort = port;
        // 初始化数据库操作函数
        operator = new DBOperator();
    }

    public LobbyServer(){
        this(Config.port);
    }

    @Override
    public void run() {
        // 1. 启动服务端套接字
        try {
            serverSocket = new ServerSocket(this.serverPort); // 启动服务
//            serverSocket.bind(new InetSocketAddress(serverAddress, this.serverPort));
            bServerIsRunning = true;
        } catch (BindException e) {
            System.out.println("[error] 端口使用中....");
            System.out.println("[error] 请关掉相关程序并重新运行服务器！");
            System.exit(0);
        } catch (IOException e) {
            System.out.println("[error] Cound not start server." + e);
        }

        // 2. 输出打印信息
//        System.out.println("[info] 服务器名称:"+serverAddress.getHostName());
//        System.out.println("[info] 服务器IP:" + serverAddress.getHostAddress());
        System.out.println("[info] 服务器端口:" + serverPort);
        System.out.println("[info] 服务器正在运行中...");


        // 3. 循环启动服务器线程
        while (true) {
            try {
                // 监听客户端的连接请求，并返回客户端socket
                Socket socket = serverSocket.accept();
                new ClientProcess(socket).start(); // 创建一个新线程来处理与该客户的通讯
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // 消息的发送
    private void sendInLobby(String strSend) {
        try {
            for (Socket socket: socketUser) {
                PrintWriter outSend = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())),
                        true);
                outSend.println(strSend);
                outSend.close();
            }
        } catch (IOException e) {
            System.out.println("[ERROR] send all fail!");
        }
    }

    // 当服务端接受了Socket连接 就会创建一个线程
    class ClientProcess extends Thread {
        Socket socket ;// 定义客户端套接字
        BufferedReader in;// 定义输入流
        PrintWriter out;// 定义输出流

        String strKey; //保存信息的关键字 login talk init reg
        StringTokenizer st;

        //维护的与这个服务端套接字连接的用户信息
        Room curRoom = null;

        private String account;//当前用户的账号
        private String nickname;
        private int[] gamePortList = {};

        //处理从客户端Socket接收到的信息
        public ClientProcess(Socket client) throws IOException {
            socket = client;
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // 客户端接收
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    socket.getOutputStream())), true);  // 客户端输出
        }

        // 接受线程
        // 使用while循环来连续读取从客户端发送来的信息  根据不同的信息进行不同的操作执行
        @Override
        public void run() {
            while (!gameStart) {
                // 1. 从服务端读取信息
                try {
                    // 从服务器端接收一条信息后拆分、解析，并执行相应操作
                    String strReceive = in.readLine();
                    // 初始化分词器
                    st = new StringTokenizer(strReceive, "|");
                } catch (Exception e) {
                    // 用户关闭客户端造成此异常，关闭该用户套接字。
                    String leaveUser = null;
                    try {
                        leaveUser = closeSocket();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    Date t = new Date();
                    System.out.println("[info] 用户" + leaveUser + "已经退出" + "退出时间" + t.toLocaleString());
                    try {
                        freshClientsOnline();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                    continue;
                }

                // 2. 解析对应的函数
                strKey = st.nextToken();
                try {
                    switch (strKey) {
                        /* 登陆和注册 */
                        case "register" -> register(st.nextToken(), st.nextToken(), st.nextToken().trim());
                        case "login" -> login(st.nextToken(), st.nextToken().trim());

                        /* 大厅内的操作 */
                        // 创建房间
                        case "Create" -> createRoom();
                        //处理用户选择房间的消息   客户端点击选择房间后传来的Select room
                        case "Select room" -> selectRoom();
                        //选择的房间若设置了密码，需要验证
                        case "password" -> validPassword();


                        // 房间操作
                        case "isReady" ->
                            //有用户准备
                                userReady();
                        case "cancelReady" ->
                            //有用户取消准备
                                userCancelReady();
                        case "check status" ->
                            //检查房间内的用户是否都准备好了
                                checkIfAllReady();
                        case "gameOver" ->
                            // 游戏结束处理
                                processGameOver();
                        case "exitRoom" ->
                            //退出房间
                                exitRoom();

                        // 聊天功能
                        case "talk" -> talk();
                        case "roomTalk" ->
                            //房间内发言
                                roomTalk();
                        case "init" ->
                            //刷新在线用户列表
                                freshClientsOnline();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // 向给指定的Sockets发送信息
        private void sendInRoom(String strSend) {
            try {
                for (Socket socket: curRoom.getAllSockets()) {
                    PrintWriter outSend = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream())),
                            true);
                    outSend.println(strSend);
                    outSend.close();
                }
            } catch (IOException e) {
                System.out.println("[ERROR] send all fail!");
            }
        }

        //创建房间,分有密码和无密码的情况
        public void createRoom() throws IOException {
            // 验证是否存在密码
            String isPassword = st.nextToken();
            boolean havePassword = isPassword.equals("password");

            String userName = st.nextToken();
            String account = st.nextToken(); //传来的账号
            String roomName = st.nextToken();
            int userNum = Integer.parseInt(st.nextToken());

            // 创建房间 (对于房主来说 房号就是他的账号)
            Room room = null;
            if (havePassword){
                String password=st.nextToken();
                room = new Room(account, roomName, userNum, password);
            } else {
                room = new Room(account, roomName, userNum);
            }
            // 添加房主的信息
            room.addOnlineUser(new User(userName, account), socket);

            // 将房间信息添加进全局
            rooms.add(room);

            sendInRoom("roomTalk|>>> 欢迎 " + userName + " 加入房间");

            // 更新房间里面的信息，并刷新客户端的游戏大厅和游戏房间里的聊天框
            // 用户进入房间，房间里面的人员信息会增加
            // 向客户端发送消息
            sendRoomUsers();
            // 用户进入房间，大厅里的房间人数会变
            sendRooms();
        }

        //进入房间
        public void selectRoom() throws IOException {
            String roomNum = st.nextToken();//获取到用户选择的房间号
            for (Room room: rooms) {
                if (room.getRoomNum().equals(roomNum)){
                    if (room.isFull() || room.getStatus()){
                        //房间达到人数上限或者房间正在游戏时，无法进入房间
                        out.println("select room|failed");
                    } else if (room.havePassword()) {//提示有密码
                        out.println("select room|password");
                    } else {
                        curRoom = room;
                        curRoom.addOnlineUser(new User(nickname, account), socket);//加昵称、账号和套接字
                        sendRooms();//用户进入房间，大厅里的房间人数会变
                        sendInRoom("roomTalk|>>>欢迎 " + nickname + " 加入房间");

                        //成功进入房间
                        out.println("select room|success");
                    }
                    break;
                }
            }
        }

        //验证进入房间的密码
        public void validPassword() throws IOException {
            //在获取一次房间号，因为有可能在输入密码期间有其他人进入房间了
            String roomNum=st.nextToken();
            String password=st.nextToken();
            for (Room room: rooms) {
                if (room.getRoomNum().equals(roomNum)){
                    if (room.isFull() || room.getStatus()){
                        //房间达到人数上限或者房间正在游戏时，无法进入房间
                        out.println("select room|failed");
                    } else if (!room.checkPassword(password)) {//密码错误
                        out.println("select room|password error");
                    } else {
                        curRoom = room;
                        curRoom.addOnlineUser(new User(nickname, account), socket);//加昵称、账号和套接字
                        sendRooms();//用户进入房间，大厅里的房间人数会变
                        sendInRoom("roomTalk|>>>欢迎 " + nickname + " 加入房间");

                        //成功进入房间
                        out.println("select room|success");
                    }

                    break;
                }
            }
        }

        // 用户选择进行准备
        private void userReady() {
            //准备的玩家
            String readyName = st.nextToken();
            //传递给客户端的房间在线用户
            sendInRoom("roomTalk|" + readyName + " 已准备");
            curRoom.changeUserStatus(readyName, true);
        }

        // 用户取消准备
        private void userCancelReady() {
            //取消准备的玩家
            String readyName = st.nextToken();
            //传递给客户端的房间在线用户
            sendInRoom("roomTalk|" + readyName + " 已取消准备");
            curRoom.changeUserStatus(readyName, false);
        }

        // 验证是否全部用户准备好
        private void checkIfAllReady() throws IOException {
            if(!curRoom.areAllUsersReady()) {
                out.println("begin game|failed");
                return;
            }

            //用户全都准备好了 开始游戏  改变房间状态/////////////////
            curRoom.setStatus(true);
            // 分配端口号
//                    int serverPort = Config.port + i;

            // 创建游戏服务端
//                startGameServer(curRoom.getMaxUserNum(), curRoom.getAllSockets());
            //把游戏开始信息发送给房间内所有用户
            sendInRoom("begin game|succeed");
            //刷新大厅中这个房间的状态
            sendRooms();
        }

        //退出房间
        public void exitRoom() throws IOException {
            // 如果是房主退出了，则解散房间
            if(curRoom.isHost(nickname)){
                // 先要进行刷新  防止场景切换后出现问题  客户端需要新的Rooms列表进行大厅场景的切换
                sendRooms();
                // 向房间内的所有客户端发送 房间解散的信息
                sendInRoom("Owner exitRoom|"+curRoom.getRoomNum());
//                // 从房间中移除该用户 使用Room中的函数
//                curRoom.removeOnlineUser(curRoom.getAccountIndex(account));
//                //调用函数 清空房间内部的所有内容
//                curRoom.ClearALL();

                //从总房间列表中将这个房间删除
                rooms.remove(curRoom);
            }
            //普通用户退出该房间
            else {
//                // 从房间中移除该用户 使用Room中的函数
//                curRoom.removeOnlineUser(room.getAccountIndex(account));
//                //用户退出房间，房间里面的人员信息会修改
                sendRoomUsers();
                // 发送退出房间消息给其他用户
                sendInRoom("roomTalk|>>>再见 " + nickname + " 退出房间");
            }
            sendRooms();//刷新大厅内的房间列表
        }



        ////////////////////////////////大厅发言///////////////////////////////////////
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
            System.out.println("[info] Constants.USER" + strSender + "对 " + strReceiver + "说:" + strTalkInfo
                    + t.toLocaleString());

            if (strReceiver.equals("All")) {
                LobbyServer.this.sendInLobby("talk|" + strSender + " 对所有人说：" + strTalkInfo);
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
            System.out.println("[info] Constants.USER" + strSender + "对 " + strReceiver + "说:" + strTalkInfo
                    + t.toLocaleString());

            if (strReceiver.equals("All")) {
                sendInRoom("roomTalk|" + strSender + " 对所有人说：" + strTalkInfo);
            } else {
                if (strSender.equals(strReceiver)) {
                    out.println("roomTalk|>>>不能自言自语哦!");
                } else {
                    for (int j = 0; j<curRoom.getCurUserNum(); j++){
                        if (strReceiver.equals(curRoom.findNameUser(j))) {//更新接收方的消息
                            socketSend = curRoom.findSocketUser(j);
                            outSend = new PrintWriter(new BufferedWriter(
                                    new OutputStreamWriter(
                                            socketSend.getOutputStream())), true);
                            outSend.println("roomTalk|" + strSender + " 对你说："
                                    + strTalkInfo);
                        } else if (strSender.equals(curRoom.findNameUser(j))) {//更新发送方的消息
                            socketSend = curRoom.findSocketUser(j);
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

        /////////////////////////刷新大厅内的在线用户/////////////////////
        //在线用户列表
        private void freshClientsOnline() throws IOException {
            String strOnline = "online";
            for (int i = 0; i < nameUser.size(); i++) {
                strOnline += "|" + nameUser.elementAt(i);
            }
            System.out.println("[info] 当前在线人数:"+nameUser.size());
            LobbyServer.this.sendInLobby(strOnline);
        }

        // 向客户端发送房间内的所有学生
        private void sendRoomUsers() throws IOException {
            //传递给客户端的房间在线用户
            String strOnline = "room online";

            String[] names = curRoom.getAllNickNames();
            for(String name: names) {
                System.out.println("[info] username" + name);
                System.out.println("[info] roomname" + curRoom.getRoomName());
                strOnline += "|" + name;
            }

            System.out.println("[info] 当前在线人数:" + curRoom.getCurUserNum());
            //向房间内所有用户发送  发送房间内全部人员的名字
            sendInRoom(strOnline);
        }

        ///////////////////////////刷新游戏大厅的在线房间///////////////////
        private void sendRooms() throws IOException {
            String strOnline = "lobby";
            for (Room room: rooms) {
                strOnline += "|" + room.getRoomNum();//房间号
//                strOnline += "|" + String.valueOf(i);//号码
                strOnline += "|" + room.getRoomName();//房间名
                strOnline += "|" + room.getHostName();//房主名字
                strOnline += "|" + String.valueOf(room.getCurUserNum());
                strOnline += "|" + String.valueOf(room.getMaxUserNum());
                strOnline += "|" + room.getStatus();//房间状态
            }
            LobbyServer.this.sendInLobby(strOnline);

            System.out.println("[info] 在线人数:"+strOnline);
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

//            //删除房间信息
//            for (int i = 0; i < rooms.size(); i++) {
//                Room room = rooms.get(i);
//                if (room.getRoomNum().equals(RoomNum)){
//                    for (int j = 0; j < room.getCurUserNum(); j++) {
//                        if (socket.equals(room.findSocketUser(j))){
//                            room.removeOnlineUser(i);
//                            sendRoomUsers();
//                            sendAllRooms();
//                        }
//                    }
//                    rooms.remove(i);
//                    break;
//                }
//            }

            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                System.out.println("[ERROR] " + e);
            }
            return strUser;
        }


        Thread gameServerThread;
        GameServer server;
        // 创建游戏服务端
        void startGameServer(int num, Socket[] sockets){
            gameStart=true;
//        System.out.println("[info] New game server, port: " + port);
            gameServerThread = new Thread(()->{
                server = new GameServer(sockets);
                server.run();
            });
            gameServerThread.start();
        }

        // 处理游戏结束
        void processGameOver(){
            if(gameServerThread!=null){
                // 关闭游戏服务端进程
                gameServerThread.interrupt();
                // 关闭游戏服务端套接字
//            server.closeServer();
                System.out.println("[info] 服务端关闭");
            }
        }

        // 注册
        private void register(String name, String account, String password)  {
            if (operator.isExistUserName(name)){
                // 验证昵称是否重复
                System.out.println("[ERROR] " + name + " Register fail!");
                out.println("register|name");
            } else if (operator.isExistUser(account)) {
                // 验证账号重复
                System.out.println("[ERROR] " + account + " Register fail!");
                out.println("register|account");
            } else {
                // 创建用户
                boolean flag = operator.createPlayer(name, account, password);

                if (flag) {
                    System.out.println("[info] User " + name + " 注册成功");
                    out.println("register|success");
                }
            }
        }

        // 登录
        public void login(String account, String password) throws IOException, SQLException {
            // 判断该用户是否登陆过了
            for (Object userAccount:onlineUser){
                if (account.equals(userAccount)){
                    //不能重复登陆的提示
                    out.println("warning|double");
                    return;
                }
            }

            Date t = new Date();
            System.out.println("[info] 用户" + account + " 正在登陆..." + "  密码:" + password + "  端口:"
                    + socket + t.toString());

            // 调用用户账号和密码的判断
            if (operator.checkLogin(account, password)) {      // 判断用户名和密码 ->转为在数据库中寻找
                openLobbyWindow(account);
            } else {
                out.println("warning|" + account + "登陆失败，请检查您的输入!");
                System.out.println("[error] 用户 "+ account + "登陆失败！" + t.toString());
            }
        }

        // 登陆成功返回给用户它的昵称
        private void openLobbyWindow(String account) throws IOException, SQLException {
            Date t = new Date();
            // 获取用户昵称
            String nickName = operator.getNickName(account);
            if (nickName != null) {
                out.println("login|succeed" + "|" + nickname);

                //保存用户信息
                nameUser.addElement(nickname);
                onlineUser.addElement(account);
                socketUser.addElement(socket);
                System.out.println("[info] 用户：" + nickname + "登录成功，" + "登录时间:" + t.toString());


                freshClientsOnline();
                sendRooms();
                LobbyServer.this.sendInLobby("talk|>>>欢迎 " + nickname + " 进来与我们一起交谈!");
                System.out.println("[SYSTEM] " + nickname + " login succeed!");
            }
        }

    }

}