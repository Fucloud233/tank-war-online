package com.tankWar.server;

import com.tankWar.game.server.Config;
import com.tankWar.game.server.GameServer;

import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;
import java.util.Date;


//接收到客户端socket发来的信息后进行解析、处理、转发
public class LobbyServer {
    // 连接相关
    ServerSocket serverSocket = null; // 创建服务器端套接字
    // 服务端监听端口
    int serverPort;

    // rooms 存储所有房间信息
    // users 存储所有玩家信息(玩家信息 + Socket + Status)
    static ArrayList<Room> rooms = new ArrayList<>();//维护的每个房间
    static Vector<UserInfo> users = new Vector<>(10,5);//保存在线用户的用户名

    // 数据操作函数
    DBOperator operator = null;

    public LobbyServer(int port){
        this.serverPort = port;
        // 初始化数据库操作函数
        operator = new DBOperator();
    }

    public LobbyServer(){
        this(Config.port);
    }

    public void start() {
        // 1. 启动服务端套接字
        try {
            serverSocket = new ServerSocket(this.serverPort); // 启动服务
//            serverSocket.bind(new InetSocketAddress(serverAddress, this.serverPort));
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

    // 当服务端接受了Socket连接 就会创建一个线程
    class ClientProcess extends Thread {
        DataInputStream in;// 定义输入流
        DataOutputStream out;// 定义输出流

        String strKey; //保存信息的关键字 login talk init reg
        StringTokenizer st;

        //维护的与这个服务端套接字连接的用户信息
        Room curRoom = null;
        UserInfo curUser = new UserInfo();


        //处理从客户端Socket接收到的信息
        public ClientProcess(Socket client) throws IOException {
            curUser.setSocket(client);

            Socket curSocket = curUser.getSocket();
            in = new DataInputStream(curSocket.getInputStream());
            // 客户端接收
            out = new DataOutputStream(curSocket.getOutputStream());  // 客户端输出
        }

        // 接受线程
        // 使用while循环来连续读取从客户端发送来的信息  根据不同的信息进行不同的操作执行
        @Override
        public void run() {
            while (true) {
                // 1. 从服务端读取信息
                try {
                    // 从服务器端接收一条信息后拆分、解析，并执行相应操作
                    if(curRoom == null || !curRoom.getStatus()) {
                        String strReceive = in.readUTF();
                        System.out.println("str:" + strReceive);
                        // 初始化分词器
                        st = new StringTokenizer(strReceive, "|");
                    } else {
                        continue;
                    }
                } catch (Exception e) {
//                    // 用户关闭客户端造成此异常，关闭该用户套接字。
//                    String leaveUser = null;
//                    try {
//                        leaveUser = closeSocket();
//                    } catch (IOException ex) {
//                        throw new RuntimeException(ex);
//                    }
//                    Date t = new Date();
//                    System.out.println("[info] 用户" + leaveUser + "已经退出" + "退出时间" + t.toLocaleString());
//                    try {
//                        freshClientsOnline();
//                    } catch (IOException e1) {
//                        e1.printStackTrace();
//                    }
                    e.printStackTrace();
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


                        /* 房间操作 */
                        //有用户准备
                        case "isReady" -> userReady();
                        //有用户取消准备
                        case "cancelReady" -> userCancelReady();
                        //检查房间内的用户是否都准备好了
                        case "check status" -> {
                            checkIfAllReady();
                        }
//                        case "gameOver" ->
//                            // 游戏结束处理
//                                processGameOver();
                        //退出房间
                        case "exitRoom" -> exitRoom();

                        /* 聊天功能 */
                        case "talk" -> talk();
                        // 房间内发言
                        case "roomTalk" -> roomTalk();
                        // 刷新在线用户列表
                        case "init" -> sendAllUser();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


        // 登录
        public void login(String account, String password) throws IOException, SQLException {
            // 判断该用户是否登陆过了
            for (User user:users){
                if (account.equals(user.getAccount())){
                    //不能重复登陆的提示
                    out.writeUTF("warning|double");
                    return;
                }
            }

            Date t = new Date();
            System.out.println("[info] 用户 " + account + " 正在登陆..." + "  密码:" + password + t.toString());

            // 判断用户名和密码 ->转为在数据库中寻找
            if (!operator.checkLogin(account, password)) {
                out.writeUTF("warning|" + account + "登陆失败，请检查您的输入!");
                System.out.println("[error] 用户 "+ account + " 登陆失败！" + t.toString());
            }

            // 获取用户昵称
            String nickName = operator.getNickName(account);
            if (nickName == null) {
                return;
            }

            // 保存用户信息
            this.curUser.setUser(nickName, account);
            users.addElement(curUser);

            // 向客户端发送信息(登陆成功 + 欢迎语录 + 所有用户姓名)
            out.writeUTF("login|succeed" + "|" + nickName);
            sendToLobby("talk|>>>欢迎 " + nickName + " 进来与我们一起交谈!");
            sendAllUser();
            sendRooms();

            System.out.println("[info] 用户 " + account + " 登录成功，" + "登录时间:" + t.toString());
        }

        // 注册
        private void register(String name, String account, String password) throws IOException {
            if (operator.isExistUserName(name)) {
                // 验证昵称是否重复
                System.out.println("[ERROR] " + name + " Register fail!");
                out.writeUTF("register|name");
            } else if (operator.isExistUser(account)) {
                // 验证账号重复
                System.out.println("[ERROR] " + account + " Register fail!");
                out.writeUTF("register|account");
            } else {
                // 创建用户
                boolean flag = operator.createPlayer(name, account, password);

                if (flag) {
                    System.out.println("[info] User " + name + " 注册成功");
                    out.writeUTF("register|success");
                }
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
            if (havePassword) {
                String password = st.nextToken();
                curRoom = new Room(account, roomName, userNum, password);
            } else {
                curRoom = new Room(account, roomName, userNum);
            }

            // 添加房主的信息 并将房间添加进全局中
            curRoom.addOnlineUser(curUser);
            rooms.add(curRoom);

            // 更新房间里面的信息，并刷新客户端的游戏大厅和游戏房间里的聊天框
            // 用户进入房间，房间里面的人员信息会增加
            // 用户进入房间，大厅里的房间人数会变
            sendToRoom("roomTalk|>>> 欢迎 " + userName + " 加入房间");
            sendRooms();
            sendRoomUsers();

        }

        //进入房间
        public void selectRoom() throws IOException {
            String roomNum = st.nextToken();//获取到用户选择的房间号
            for (Room room : rooms) {
                if (room.getRoomNum().equals(roomNum)) {
                    if (room.isFull() || room.getStatus()) {
                        //房间达到人数上限或者房间正在游戏时，无法进入房间
                        out.writeUTF("select room|failed");
                    } else if (room.havePassword()) {//提示有密码
                        out.writeUTF("select room|password");
                    } else {
                        curRoom = room;
                        curRoom.addOnlineUser(curUser);//加昵称、账号和套接字

                        // 成功进入房间
                        out.writeUTF("select room|success");
                        // 用户进入房间，大厅里的房间人数会变
                        sendRoomUsers();
                        // 发送欢迎消息
                        sendToRoom("roomTalk|>>>欢迎 " + curUser.getNickName() + " 加入房间");
                    }
                    break;
                }
            }
        }

        //验证进入房间的密码
        public void validPassword() throws IOException {
            //在获取一次房间号，因为有可能在输入密码期间有其他人进入房间了
            String roomNum = st.nextToken();
            String password = st.nextToken();
            for (Room room : rooms) {
                if (room.getRoomNum().equals(roomNum)) {
                    if (room.isFull() || room.getStatus()) {
                        //房间达到人数上限或者房间正在游戏时，无法进入房间
                        out.writeUTF("select room|failed");
                    } else if (!room.checkPassword(password)) {//密码错误
                        out.writeUTF("select room|password error");
                    } else {
                        curRoom = room;
                        curRoom.addOnlineUser(curUser);//加昵称、账号和套接字
                        sendRooms();//用户进入房间，大厅里的房间人数会变
                        sendToRoom("roomTalk|>>>欢迎 " + curUser.getNickName() + " 加入房间");

                        //成功进入房间
                        out.writeUTF("select room|success");
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
            sendToRoom("roomTalk|" + readyName + " 已准备");
            curRoom.changeUserStatus(readyName, UserStatus.Ready);
        }

        // 用户取消准备
        private void userCancelReady() {
            //取消准备的玩家
            String readyName = st.nextToken();
            //传递给客户端的房间在线用户
            sendToRoom("roomTalk|" + readyName + " 已取消准备");
            curRoom.changeUserStatus(readyName, UserStatus.NoReady);
        }

        // 验证是否全部用户准备好
        private void checkIfAllReady() throws IOException {
            curRoom.changeUserStatus(0, UserStatus.Ready);
            if (!curRoom.checkAllUsersReady()) {
                out.writeUTF("begin game|failed");
                curRoom.changeUserStatus(0, UserStatus.NoReady);
                return;
            }

            // 用户全都准备好了 开始游戏  改变房间状态
            curRoom.setStatus(true);

            // 把游戏开始信息发送给房间内所有用户
            sendToRoom("begin game|succeed");
            // 刷新大厅中这个房间的状态
            sendRooms();
            // 创建游戏服务端
            startGameServer(curRoom.getAllSockets());
        }

        // 创建游戏服务端
        void startGameServer(Socket[] sockets) {
            System.out.println("[info] New game server, port: " + serverPort);
            GameServer server  = new GameServer(sockets);
            server.run();
        }

        //退出房间
        public void exitRoom() throws IOException {
            // 如果是房主退出了，则解散房间
            if (curRoom.isHost(curUser.nickName)) {
                // 先要进行刷新  防止场景切换后出现问题  客户端需要新的Rooms列表进行大厅场景的切换
                sendRooms();
                // 向房间内的所有客户端发送 房间解散的信息
                sendToRoom("Owner exitRoom|" + curRoom.getRoomNum());
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
                sendToRoom("roomTalk|>>>再见 " + curUser.getNickName() + " 退出房间");
            }
            sendRooms();//刷新大厅内的房间列表
        }


        ////////////////////////////////大厅发言///////////////////////////////////////
        private void talk() throws IOException {
            String strTalkInfo = st.nextToken(); // 得到聊天内容;
            String strSender = st.nextToken(); // 得到发消息人
            String strReceiver = st.nextToken(); // 得到接收人
            System.out.println("[TALK_" + strReceiver + "] " + strTalkInfo);
            Date t = new Date();

            // 得到当前时间
            GregorianCalendar calendar = new GregorianCalendar();
            String strTime = "(" + calendar.get(Calendar.HOUR) + ":"
                    + calendar.get(Calendar.MINUTE) + ":"
                    + calendar.get(Calendar.SECOND) + ")";
            strTalkInfo += strTime;

            //记录事件
            System.out.println("[info] Constants.USER" + strSender + "对 " + strReceiver + "说:" + strTalkInfo
                    + t.toString());

            if (strReceiver.equals("All")) {
                LobbyServer.this.sendToLobby("talk|" + strSender + " 对所有人说：" + strTalkInfo);
            } else {
                if (strSender.equals(strReceiver)) {
                    out.writeUTF("talk|>>>不能自言自语哦!");
                } else {
//                    for (int i = 0; i < nameUser.size(); i++) {
                    for(UserInfo user: users) {
                        Socket socketSend = user.getSocket();

                        if (strReceiver.equals(user.getNickName())) {//更新接收方的消息
                            DataOutputStream outSend = new DataOutputStream(user.getSocket().getOutputStream());
                            outSend.writeUTF("talk|" + strSender + " 对你说："
                                    + strTalkInfo);

                        } else if (strSender.equals(user.getNickName())) {//更新发送方的消息
                            DataOutputStream outSend = new DataOutputStream(user.getSocket().getOutputStream());
                            outSend.writeUTF("talk|你对 " + strReceiver + "说："
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
                sendToRoom("roomTalk|" + strSender + " 对所有人说：" + strTalkInfo);
            } else {
                if (strSender.equals(strReceiver)) {
                    out.writeUTF("roomTalk|>>>不能自言自语哦!");
                } else {
                    for (int j = 0; j < curRoom.getCurUserNum(); j++) {
                        if (strReceiver.equals(curRoom.findNameUser(j))) {//更新接收方的消息
                            socketSend = curRoom.findSocketUser(j);
                            DataOutputStream outSend = new DataOutputStream(socketSend.getOutputStream());
                            outSend.writeUTF("roomTalk|" + strSender + " 对你说："
                                    + strTalkInfo);
                        } else if (strSender.equals(curRoom.findNameUser(j))) {//更新发送方的消息
                            socketSend = curRoom.findSocketUser(j);
                            DataOutputStream outSend = new DataOutputStream(socketSend.getOutputStream());
                            outSend.writeUTF("roomTalk|你对 " + strReceiver + "说："
                                    + strTalkInfo);
                        }
                    }
                }
            }
        }

        // 向客户端发送房间内的所有学生
        private void sendRoomUsers() throws IOException {
            //传递给客户端的房间在线用户
            String strOnline = "room online";

            String[] names = curRoom.getAllNickNames();
            for (String name : names) {
                System.out.println("[info] username" + name);
                System.out.println("[info] roomname" + curRoom.getRoomName());
                strOnline += "|" + name;
            }

            System.out.println("[info] 当前在线人数:" + curRoom.getCurUserNum());
            //向房间内所有用户发送  发送房间内全部人员的名字
            sendToRoom(strOnline);
        }

        ///////////////////////////刷新游戏大厅的在线房间///////////////////
        private void sendRooms() throws IOException {
            String strOnline = "lobby";

            int i = 0;
            for (Room room : rooms) {
                i++;
                strOnline += "|" + room.getRoomNum();//房间号
                strOnline += "|" + String.valueOf(i);//号码
                strOnline += "|" + room.getRoomName();//房间名
                strOnline += "|" + room.getHostName();//房主名字
                strOnline += "|" + String.valueOf(room.getCurUserNum());
                strOnline += "|" + String.valueOf(room.getMaxUserNum());
                strOnline += "|" + room.getStatus();//房间状态
            }
            sendToLobby(strOnline);

            System.out.println("[info] 在线人数:" + strOnline);
        }

        // todo 关闭套接字，并将用户信息从在线列表中删除
        private String closeSocket() throws IOException {
//            String strUser = "";
//            for (int i = 0; i < socketUser.size(); i++) {//删除用户信息
//                if (curSocket.equals((Socket) socketUser.elementAt(i))) {
//                    strUser = onlineUser.elementAt(i).toString();
//                    socketUser.removeElementAt(i);
//                    onlineUser.removeElementAt(i);
//                    nameUser.removeElementAt(i);
//                    try {
//                        freshClientsOnline();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }

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

//            try {
//                in.close();
//                out.close();
//                curSocket.close();
//            } catch (IOException e) {
//                System.out.println("[ERROR] " + e);
//            }
//            return strUser;

            return null;
        }

        // 向给指定的Sockets发送信息
        private void sendToRoom(String strSend) {
            try {
                for (Socket socket : curRoom.getAllSockets()) {
                    DataOutputStream outSend = new DataOutputStream(socket.getOutputStream());
                    outSend.writeUTF(strSend);
                }
            } catch (IOException e) {
                System.out.println("[ERROR] send all fail!");
            }
        }

    }

    // 刷新大厅内的在线用户
    private void sendAllUser() throws IOException {
        String strOnline = "online";
        for (UserInfo user: users) {
            strOnline += "|" + user.getNickName();
        }
        System.out.println("[info] 当前在线人数:" + users.size());
        sendToLobby(strOnline);
    }

    // 向大厅发送信息
    private void sendToLobby(String strSend) {
        try {
            for (UserInfo user: users) {
                System.out.println(strSend);
                DataOutputStream outSend = new DataOutputStream(user.getSocket().getOutputStream());
                outSend.writeUTF(strSend);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[ERROR] send all fail!");
        }
    }
}