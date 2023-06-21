package com.tankWar.server;

import javafx.util.Pair;

import java.io.*;
import java.net.*;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.Date;

import static com.tankWar.server.ServerPrompt.*;

// 接收到客户端socket发来的信息后进行解析、处理、转发
public class Main {
    // 连接相关
    // 参考代码: https://blog.csdn.net/hao134838/article/details/113185058
    // https://www.cnblogs.com/binarylei/p/12643807.html
    ServerSocketChannel serverSocket = null;
    Selector selector = null;
    // 服务端监听端口
    int serverPort;

    /* 只在用户成功登陆后存储 */
    HashMap<SocketChannel, User> users = new HashMap<>();
    /* 只在房间成功创建后存储 */
    // 根据房间来进行索引
    HashMap<String, Room> rooms = new HashMap<>();

    // 数据操作函数
    DBOperator operator;

    public Main(int port){
        this.serverPort = port;
        // 初始化数据库操作函数
        operator = new DBOperator();
    }

    public Main(){
        this(Config.port);
    }

    public void start() {
        // 1. 启动服务端套接字
        try {
            // 启动服务
            serverSocket = ServerSocketChannel.open();
            serverSocket.bind(new InetSocketAddress(serverPort));
            // 设置服务端Socket非阻塞
            serverSocket.configureBlocking(false);
            // 设置多路复用器 并注册服务端Socket
            selector = Selector.open();
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);

            // 2. 输出打印信息
            infoServerRunning(serverPort);
        } catch (BindException e) {
            errorPortUsed(serverPort);
            System.exit(0);
        } catch (IOException e) {
            errorServerStartFail();
        }

        // 3. 循环启动服务器线程
        while (true) {
            // Select
            try { selector.select(); } catch(IOException e) {
                warnSelectError();
            }

            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            for (SelectionKey key : selectionKeys) {
                SocketChannel socket = null;
                try {
                    if (key.isAcceptable()) {
                        // 如果轮询到服务Socket 则建立Socket连接
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        // 接收客户端的Socket
                        socket = server.accept();
                        // 设置非阻塞IO
                        socket.configureBlocking(false);
                        // 将该Socket注册在Selector中
                        socket.register(selector, SelectionKey.OP_READ);
                        infoClientConnectSuccess(socket.socket().getInetAddress(), socket.socket().getPort());
                    } else if (key.isReadable()) {
                        // 如果轮询到Socket 则处理接收连接
                        socket = (SocketChannel) key.channel();
                        User user = users.get(socket);

                        // 根据用户状态来选择处理对象
                        if (user == null || user.getStatus().isInLobby()) {
                            // 大厅处理对象
                            new LobbyHandler(socket).handle();
                        } else if (user.getStatus().isInRoom()) {
                            // 房间处理对象
                            new RoomHandler(socket).handle();
                        } else if (user.getStatus().isPlaying()) {
                            // 游戏处理对象(这个需要传入用户所在的玩家
                            new GameHandler(socket, user.getRoom()).handle();
                        }
                    }
                } catch (SocketException e) {
                    // 当发生错误时 则是socket中断连接
                    String userName = "Somebody";
                    if(socket!=null) {
                        // 1. 标记该用户
                        User user = users.get(socket);
                        if(user!=null) {
                            userName = user.getNickName();
                            // 2. 删除该用户
                            users.remove(socket);
                        }

                        // 3. 关闭Socket
                        try { socket.close(); } catch (IOException ignored) {}
                    }

                    warnPlayerLeave(userName);
                } catch (IOException e) {
                    // 发生IO错误
                    e.printStackTrace();
                }

                // 在处理完删除key 防止重复处理
                selectionKeys.remove(key);

            }
        }
    }

    // 大厅接收处理者
    class LobbyHandler extends FrontHandler{

        // 构造函数
        public LobbyHandler(SocketChannel socketChannel) {
            super(socketChannel);
        }

        // 用来处理大厅接收到的消息
        public void handle() throws IOException {
            // 1. 读取消息
            String text = receive();
//            System.out.println("[info] LobbyHandler recv:" + text);

            if(text == null || text.isEmpty())
                return;

            // 2. 解析对应的函数
            // 从服务器端接收一条信息后拆分、解析，并执行相应操作
            StringTokenizer st = new StringTokenizer(text, "|");
            String strKey = st.nextToken(), returnMsg = null;
            try {
                switch (strKey) {
                    /* 登陆和注册 */
                    case "register" -> returnMsg = register(st);
                    case "login" -> returnMsg = login(st);

                    /* 大厅内的操作 */
                    // 创建房间
                    case "Create" -> createRoom(st);
                    // 处理用户选择房间的消息   客户端点击选择房间后传来的Select room
                    case "Select room" -> selectRoom(st);
                    // 选择的房间若设置了密码，需要验证
                    case "password" -> validPassword(st);
                    // 在大厅中聊天
                    case "talk" ->talk(st);
                    // 刷新在线用户列表
                    case "init" -> sendAllUser();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 3. 发送返回消息
            if(returnMsg != null)
                this.send(returnMsg);
        }
        // 登录
        String login(StringTokenizer st) {
            String account = st.nextToken();
            String password = st.nextToken().trim();

            // 判断该用户是否登陆过了
            for(User user: users.values()) {
                if (account.equals(user.getAccount())){
                    //不能重复登陆的提示
                    return "warning|double";
                }
            }

            Date t = new Date();
//            System.out.println("[info] 用户 " + account + " 正在登陆..." + "  密码:");

            // 判断用户名和密码 ->转为在数据库中寻找
            if (!operator.checkLogin(account, password)) {
                warnLoginFail(account);
                return "warning|" + account + "登陆失败，请检查您的输入!";
            }

            // 获取用户昵称
            String nickName = operator.getNickName(account);
            if (nickName == null) {
                return "warning|" + account + "登陆失败，请检查您的输入!";
            }

            // 保存用户信息 (只有当用户成功登陆时才添加线程)
            users.put(curSocket, new User(nickName, account));

            // 向客户端发送信息(登陆成功 + 欢迎语录 + 所有用户姓名)
            send("login|succeed" + "|" + nickName);
            sendToLobby("talk|>>>欢迎 " + nickName + " 进来与我们一起交谈!");
            sendAllUser();
            sendRooms();

            infoLoginSuccess(account, users.size());

            return null;
        }

        // 注册
        String register(StringTokenizer st)  {
            String name = st.nextToken();
            String account = st.nextToken();
            String password = st.nextToken();

            if (operator.isExistUserName(name)) {
                // 验证昵称是否重复
                warnRegisterFail(name);
                return "register|name";
            } else if (operator.isExistUser(account)) {
                // 验证账号重复
                warnRegisterFail(account);
                return "register|account";
            } else {
                // 创建用户
                boolean flag = operator.createPlayer(name, account, password);

                if (flag) {
                    infoRegisterSuccess(name);
                    return "register|success";
                }
            }

            return null;
        }

        // 创建房间,分有密码和无密码的情况
        void createRoom(StringTokenizer st) {
            // 验证是否存在密码
            String isPassword = st.nextToken();
            boolean havePassword = isPassword.equals("password");

            String userName = st.nextToken();
            String account = st.nextToken(); //传来的账号
            String roomName = st.nextToken();
            int userNum = Integer.parseInt(st.nextToken());

            // 创建房间 (对于房主来说 房号就是他的账号)
            Room room = new Room(account, roomName, userNum);
            if (havePassword) {
                String password = st.nextToken();
                room.setPassword(password);
            }

            // 添加房主的信息 并将房间添加进全局中
            User user = users.get(curSocket);
            room.addOnlineUser(curSocket, user);
            rooms.put(room.getRoomNum(), room);

            // 更新房间里面的信息，并刷新客户端的游戏大厅和游戏房间里的聊天框
            // 用户进入房间，房间里面的人员信息会增加
            // 用户进入房间，大厅里的房间人数会变
            sendToRoom(room, "roomTalk|>>> 欢迎 " + userName + " 加入房间");
            sendAllUsersToRoom(room);
            sendRooms();

            infoCreateRoom(user.getNickName(), room.getRoomName());
        }

        // 进入房间
        void selectRoom(StringTokenizer st) {
            // 获取到用户选择的房间号
            String roomNum = st.nextToken();
            for (Room room : rooms.values()) {
                if (room.getRoomNum().equals(roomNum)) {
                    if (room.isFull() || room.isPlaying()) {
                        //房间达到人数上限或者房间正在游戏时，无法进入房间
                        send("select room|failed");
                    } else if (room.havePassword()) {//提示有密码
                        send("select room|password");
                    } else {
                        // 处理玩家和用户的之间的关联关系
                        User user = users.get(curSocket);
                        room.addOnlineUser(curSocket, user);
                        // 成功进入房间
                        send("select room|success");
                        // 发送欢迎消息
                        sendToRoom(room, "roomTalk|>>>欢迎 " + user.getNickName() + " 加入房间");
                        // 用户进入房间，大厅里的房间人数会变
                        sendAllUsersToRoom(room);
                    }
                    break;
                }
            }
        }

        // 验证进入房间的密码
        void validPassword(StringTokenizer st) {
            //在获取一次房间号，因为有可能在输入密码期间有其他人进入房间了
            String roomNum = st.nextToken();
            String password = st.nextToken();
            for (Room room : rooms.values()) {
                if (room.getRoomNum().equals(roomNum)) {
                    if (room.isFull() || room.isPlaying()) {
                        //房间达到人数上限或者房间正在游戏时，无法进入房间
                        send("select room|failed");
                    } else if (!room.checkPassword(password)) {//密码错误
                        send("select room|password error");
                    } else {
                        // 处理玩家和用户的之间的关联关系
                        User user = users.get(curSocket);
                        room.addOnlineUser(curSocket, user);
                        //成功进入房间
                        send("select room|success");
                        //用户进入房间，大厅里的房间人数会变
                        sendRooms();
                        sendToRoom(room, "roomTalk|>>>欢迎 " + user.getNickName() + " 加入房间");
                    }
                    break;
                }
            }
        }

        // 大厅发言
        void talk(StringTokenizer st) throws IOException {
            String strTalkInfo = st.nextToken(); // 得到聊天内容;
            String strSender = st.nextToken(); // 得到发消息人d
            String strReceiver = st.nextToken(); // 得到接收人
            System.out.println("[TALK_" + strReceiver + "] " + strTalkInfo);
            Date t = new Date();

            // 得到当前时间
            GregorianCalendar calendar = new GregorianCalendar();
            String strTime = "(" + calendar.get(Calendar.HOUR) + ":"
                    + calendar.get(Calendar.MINUTE) + ":"
                    + calendar.get(Calendar.SECOND) + ")";
            strTalkInfo += strTime;

            // 记录事件
            System.out.println("[info] Constants.USER" + strSender + "对 " + strReceiver + "说:" + strTalkInfo
                    + t);

            if (strReceiver.equals("All")) {
                this.sendToLobby("talk|" + strSender + " 对所有人说：" + strTalkInfo);
            } else if (strSender.equals(strReceiver)) {
                this.send("talk|>>>不能自言自语哦!");
            } else {
                for(Map.Entry<SocketChannel, User> pair: users.entrySet()) {
                    User user = pair.getValue();
                    //更新接收方的消息
                    // todo 封装Write函数
                    if (strReceiver.equals(user.getNickName())) {
                        SocketChannel socket = pair.getKey();
                        String text = "talk|" + strSender + " 对你说：" + strTalkInfo;
                        //更新发送方的消息
                        this.send(socket,text);
                        this.send("talk|你对 " + strReceiver + "说：" + strTalkInfo);
                        break;
                    }
                }
            }
        }
    }

    // 房间处理
    class RoomHandler extends FrontHandler{
        // 构造函数
        public RoomHandler(SocketChannel socketChannel) {
            super(socketChannel);
        }

        // 用来处理大厅接收到的消息
        @Override
        public void handle() throws IOException {
            // 1. 读取消息
            String text = super.receive();
//            System.out.println("[info] RoomHandler recv:" + text);
            if(text == null)
                return;

            // 2. 解析对应的函数
            // 从服务器端接收一条信息后拆分、解析，并执行相应操作
            StringTokenizer st = new StringTokenizer(text, "|");
            String strKey = st.nextToken(), returnMsg = null;
            try {
                switch (strKey) {
                    /* 房间操作 */
                    // 有用户准备
                    case "isReady" -> userReady(st);
                    // 有用户取消准备
                    case "cancelReady" -> userCancelReady(st);
                    // 检查房间内的用户是否都准备好了
                    case "check status" -> returnMsg = checkIfAllReady();
                    // 游戏结束处理
                    case "gameOver" -> processGameOver();
                    // 退出房间
                    case "exitRoom" -> exitRoom();

                    /* 聊天功能 */
                    // 房间内发言
                    case "roomTalk" ->roomTalk(st);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 3. 发送返回消息
            if(returnMsg != null)
                this.send(returnMsg);
        }

        // 用户选择进行准备
        private void userReady(StringTokenizer st) {
            //准备的玩家
            String readyName = st.nextToken();
            //传递给客户端的房间在线用户\
            User user = users.get(curSocket);
            sendToRoom(user.getRoom(), "roomTalk|" + readyName + " 已准备");
            user.setStatus(UserStatus.Ready);
            //重新刷新房间内的列表
            Room room=user.getRoom();
            sendAllUsersToRoom(room);

        }

        // 用户取消准备
        private void userCancelReady(StringTokenizer st) {
            //取消准备的玩家
            String readyName = st.nextToken();
            //传递给客户端的房间在线用户
            User user = users.get(curSocket);
            sendToRoom(user.getRoom(),"roomTalk|" + readyName + " 已取消准备");
            user.setStatus(UserStatus.NoReady);
            //重新刷新房间内的列表
            Room room=user.getRoom();
            sendAllUsersToRoom(room);
        }

        // 验证是否全部用户准备好
        String checkIfAllReady() throws IOException {
            User user = users.get(curSocket);
            Room room = user.getRoom();

            // 切换房主状态
            user.setStatus(UserStatus.Ready);

            if (!room.checkAllUsersReady()) {
                user.setStatus(UserStatus.NoReady);
                return "begin game|failed";
            }

            // 刷新大厅中这个房间的状态
            sendRooms();

            // 修改游戏房间内用户状态
            changeOtherNoready(user);

            // 把游戏开始信息发送给房间内所有用户
            sendToRoom(room, "begin game|succeed");

            // 开始游戏 并发送初始化信息
            room.startGame();
            new GameHandler(curSocket, room).sendInitMsg();
            infoGameStart(room.getRoomName());

            return null;
        }

        void changeOtherNoready(User user){
            for(User users : user.getRoom().getAllUsers()){
                if(users==user) continue;
                sendToRoom(user.getRoom(),"roomTalk|" + users.getNickName() + " 已取消准备");
                users.setStatus(UserStatus.NoReady);
            }
            //重新刷新房间内的列表
            Room room=user.getRoom();
            sendAllUsersToRoom(room);
        }

        // 退出房间
        void exitRoom() {
            User user = users.get(curSocket);
            Room room = user.getRoom();

            // 如果是房主退出了，则解散房间
            if (room.isHost(user.getAccount())) {
                // 先要进行刷新  防止场景切换后出现问题  客户端需要新的Rooms列表进行大厅场景的切换
                sendRooms();
                infoHostExitRoom(room.getRoomName());
                // 向房间内的所有客户端发送 房间解散的信息
                sendToRoom(room, "Owner exitRoom|" + room.getRoomNum());

                //调用函数 清空房间内部的所有玩家 并修改状态
                room.cleanAll();
                //从总房间列表中将这个房间删除
                rooms.remove(room.getRoomNum());
            }
            //普通用户退出该房间
            else {
                // 从房间中移除该用户 使用Room中的函数
                room.removeOnlineUser(curSocket);

                //用户退出房间，房间里面的人员信息会修改
                sendAllUsersToRoom(room);
                // 发送退出房间消息给其他用户
                sendToRoom(room,"roomTalk|>>>再见 " + user.getNickName() + " 退出房间");
            }

            sendRooms();//刷新大厅内的房间列表
        }

        void processGameOver() {
            User user = users.get(curSocket);
            Room room = user.getRoom();
            room.endGame();
            // 刷新大厅中这个房间的状态
            sendRooms();
        }

        // todo 在房间内部发言 (待优化)
        void roomTalk(StringTokenizer st) throws IOException {
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
                    + t);

            if (strReceiver.equals("All")) {
                Room room = users.get(curSocket).getRoom();
                sendToRoom(room,"roomTalk|" + strSender + " 对所有人说：" + strTalkInfo);
            } else  if (strSender.equals(strReceiver)) {
                this.send("roomTalk|>>>不能自言自语哦!");
            } else {
                // 获得接收方的
                Room room =  users.get(curSocket).getRoom();
                Pair<SocketChannel, User> recvPair = room.getUser(strReceiver);
                if(recvPair==null)
                    return ;

                //更新接收方的消息
                SocketChannel recvSocket = recvPair.getKey();
//                String text = "roomTalk|" + strSender + " 对你说：" + strTalkInfo;
                //recvSocket.write(ByteBuffer.wrap(text.getBytes()));

                //更新发送方的消息
                this.send(recvSocket,"roomTalk|" + strSender + " 对你说：" + strTalkInfo);
                this.send("roomTalk|你对 " + strReceiver + "说：" + strTalkInfo);
            }
        }
    }

    // 封装向大厅和房间发送消息的函数
    // 使其能够被Lobby/Room Handler共享
    abstract class FrontHandler extends Handler{
        public FrontHandler(SocketChannel socket) {
            super(socket);
        }

        // 向大厅发送信息
        protected void sendToLobby(String strSend) {
            for (SocketChannel socket: users.keySet()) {
                this.send(socket, strSend);
            }
        }

        // 刷新游戏大厅的在线房间
        protected void sendRooms() {
            String strOnline = "lobby";

            int i = 0;
            for (Room room : rooms.values()) {
                i++;
                strOnline += "|" + room.getRoomNum();//房间号
                strOnline += "|" + String.valueOf(i);//号码
                strOnline += "|" + room.getRoomName();//房间名
                strOnline += "|" + room.getHostName();//房主名字
                strOnline += "|" + String.valueOf(room.getOnlineUserNum());
                strOnline += "|" + String.valueOf(room.getMaxUserNum());
                strOnline += "|" + room.isPlaying();//房间状态
            }

            sendToLobby(strOnline);
        }


        // 刷新大厅内的在线用户
        protected void sendAllUser() {
            String strOnline = "online";
            for (User user: users.values()) {
                strOnline += "|" + user.getNickName();
            }
            sendToLobby(strOnline);
        }

        // 向给指定的Sockets发送信息
        protected void sendToRoom(Room room, String strSend) {
            for (SocketChannel socket : room.getAllSockets()) {
                this.send(socket, strSend);
            }
        }

        // 向服务端发送信息
        protected void sendAllUsersToRoom(Room room) {
            // 传递给客户端的房间在线用户
            StringBuilder strOnline = new StringBuilder("room online");

            // 生成传输消息
            Vector<User> users = room.getAllUsers();
            // 1. 首先发送后续用户的数量
            strOnline.append("|").append(users.size());
            for (User user : users) {
                // 传入--序号-- 昵称 状态
                // 顺序已经包含潜在的序号 不要脱裤子放屁 再发一遍序号
                strOnline.append("|").append(user.getNickName()).append("*").append(user.getStatus());
            }
//            ServerPrompt.infoOnlinePlayerNumber(room.getOnlineUserNum());

            //向房间内所有用户发送  发送房间内全部人员的名字
            sendToRoom(room, strOnline.toString());
        }
    }

    // todo 关闭套接字，并将用户信息从在线列表中删除
    private String closeSocket() {
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
}

