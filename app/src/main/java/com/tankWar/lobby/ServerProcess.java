package com.tankWar.lobby;


import java.io.*;
import java.net.*;
import java.util.*;

//接收到客户端socket发来的信息后进行解析、处理、转发
public class ServerProcess extends Thread {
    private Socket socket = null;// 定义客户端套接字
    private BufferedReader in;// 定义输入流
    private PrintWriter out;// 定义输出流
    //    private static Room[] rooms={new Room(0),new Room(1),new Room(2),new Room(3)
//            ,new Room(4),new Room(5),new Room(6),new Room(7),new Room(8)
//            ,new Room(9),new Room(10),new Room(11)};//************************************************
    ///////////////////
    private static ArrayList<Room> rooms=new ArrayList<>();
    private String RoomNum;
    //////////////////////
    private static Vector onlineUser = new Vector(10, 5);//保存在线用户的用户名
    private static Vector socketUser = new Vector(10, 5);//保存在线用户的Socket对象
    private String strReceive; //客户端接收的原始信息
    private String strKey; //保存信息的关键字 login talk init reg
    private StringTokenizer st;   //拆分字符串
    private final String USERLIST_FILE = "E:\\lobby\\app\\src\\main\\java\\com\\tankWar\\lobby\\_user.txt"; // 设定存放用户信息的文件

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
                }else if (strKey.equals("roomTalk")) {//////////////////////////////
                    //发言
                    roomTalk();
                } else if (strKey.equals("init")) {
                    //刷新在线用户列表
                    freshClientsOnline();
                } else if (strKey.equals("reg")) {
                    //注册
                    register();
                } else if (strKey.equals("Create")) {
                    createroom();
                } else if (strKey.equals("select room")) {
                    selectroom();
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
        }
    }

    //判断注册的用户是否重复
    private boolean isExistUser(String name) {
        String strRead;
        try {
            FileInputStream inputfile = new FileInputStream(USERLIST_FILE);
            DataInputStream inputdata = new DataInputStream(inputfile);
            while ((strRead = inputdata.readLine()) != null) {
                StringTokenizer stUser = new StringTokenizer(strRead, "|");
                if (stUser.nextToken().equals(name)) {
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

    //判断用户名和密码是否正确
    private boolean isUserLogin(String name, String password) {
        String strRead;
        try {
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

    //注册
    private void register() throws IOException {
        String name = st.nextToken(); // 得到用户名称
        String password = st.nextToken().trim();// 得到用户密码
        Date t = new Date();

        if (isExistUser(name)) {
            System.out.println("[ERROR] " + name + " Register fail!");
            out.println("warning|该用户已存在，请改名!");
        } else {
            RandomAccessFile userFile = new RandomAccessFile(USERLIST_FILE, "rw");
            userFile.seek(userFile.length()); // 在文件尾部加入新用户信息
            userFile.writeBytes(name + "|" + password + "\r\n");
            System.out.println("Constants.USER" + name + "注册成功, " + "注册时间:" + t.toLocaleString());
            userLoginSuccess(name); // 自动登陆聊天室
        }
    }

    //创建房间
    public void createroom(){
        String roomnum=st.nextToken();
        String user_num=st.nextToken();
        String password=st.nextToken().trim();
//        if (rooms[Integer.parseInt(roomnum)].isIs_used()){
//            out.println("Create|Failed");
//        } else {
//            rooms[Integer.parseInt(roomnum)].setIs_used(true);
//            rooms[Integer.parseInt(roomnum)].setPassword(password);
//            rooms[Integer.parseInt(roomnum)].setUser_num(Integer.parseInt(user_num));
//            out.println("Create|Success");
//        }
        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            if(room.getRoomNum().equals(roomnum)){
                out.println("Create|Failed");
                return;
            }
        }
//        sendAll("Create|"+roomnum+"|"+"0/"+user_num);
        out.println("Create|success");
        rooms.add(new Room(roomnum,Integer.parseInt(user_num),password));
        String strOnline = "lobby";
        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            strOnline += "|" + room.getRoomNum();
            strOnline += "|" + String.valueOf(room.getEnter_num());
            strOnline += "|" + String.valueOf(room.getUser_num());

        }
        sendAll(strOnline);

    }
    //进入房间
//    public void selectroom() throws IOException {
//        String roomnum=st.nextToken();
//        String password=st.nextToken();
//        String userid=st.nextToken();
//        if (!rooms[Integer.parseInt(roomnum)].isIs_used()){
//            out.println("select room|no created");
//        }else if (!rooms[Integer.parseInt(roomnum)].compareWithPassword(password)) {
//
//            out.println("select room|password error");
//        } else if (rooms[Integer.parseInt(roomnum)].findUserId(userid)) {
//            System.out.println("why");
//            out.println("select room|success");
//        }else if (!rooms[Integer.parseInt(roomnum)].compareUserNum()) {
//
//            out.println("select room|userNum error");
//        } else  {
//            System.out.println("password:"+password);
////            System.out.println();
//            rooms[Integer.parseInt(roomnum)].addPlayer(userid);
//            RoomNum=Integer.parseInt(roomnum);/////////////////////////////////////
//            userEnterRoomSuccess(userid);
//        }
//
//    }
    public void selectroom() throws IOException {
        String roomnum=st.nextToken();
        String password=st.nextToken();
        String userid=st.nextToken();
        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            if (room.getRoomNum().equals(roomnum)){
                if(!room.getPassword().equals(password)){
                    out.println("select room|password error");
                    return;
                } else if (room.findUserId(userid)) {
                    out.println("select room|success");
                }else {
                    room.addPlayer(userid);
                    RoomNum=roomnum;

                    userEnterRoomSuccess(userid);
                }
            }
            break;

        }

    }

    //登录
    private void login() throws IOException {
        String name = st.nextToken(); // 得到用户名称
        String password = st.nextToken().trim();// 得到用户密码
        boolean succeed = false;
        Date t = new Date();
        System.out.println("用户" + name + "正在登陆..." + "\n" + "密码 :" + password + "\n" + "端口 "
                + socket + t.toLocaleString());
        //调用用户名和密码的判断
        if (isUserLogin(name, password)) {      // 判断用户名和密码 -》转为在数据库中寻找
            userLoginSuccess(name);
            succeed = true;
        }
        if (!succeed) {
            out.println("warning|" + name + "登陆失败，请检查您的输入!");
            System.out.println("用户 "+ name + "登陆失败！" + t.toLocaleString());
        }
    }

    //在注册成功后自动登录
    private void userLoginSuccess(String name) throws IOException {
        Date t = new Date();
        //返回给客户端成功登录的内容
        out.println("login|succeed");
//        sendAll("online|" + name);
        onlineUser.addElement(name);
        socketUser.addElement(socket);
        System.out.println("用户：" + name + "登录成功，" + "登录时间:" + t.toLocaleString());
        freshClientsOnline();
        freshClientsLobbyOnline();
        sendAll("talk|>>>欢迎 " + name + " 进来与我们一起交谈!");
        System.out.println("[SYSTEM] " + name + " login succeed!");
    }
    ///////////////////成功进入房间//////////////////////////////
//    private void userEnterRoomSuccess(String name) throws IOException {
//        Date t = new Date();
//        //返回给客户端成功登录的内容
//        out.println("select room|success");
//        sendRoomAll("online|" + name);
////        onlineUser.addElement(name);
////        socketUser.addElement(socket);
//        rooms[RoomNum].addOnlineUser(name);
//        rooms[RoomNum].addSocketUser(socket);
////        System.out.println("用户：" + name + "登录成功，" + "登录时间:" + t.toLocaleString());
//        freshClientsRoomOnline();
//        sendRoomAll("talk|>>>欢迎 " + name + " 加入游戏");
//        System.out.println("[SYSTEM] " + name + " Enter room succeed!");
//    }
    private void userEnterRoomSuccess(String name) throws IOException {
        Date t = new Date();
        //返回给客户端成功登录的内容
        out.println("select room|success");
        String strOnline = "lobby";
        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            strOnline += "|" + room.getRoomNum();
            strOnline += "|" + String.valueOf(room.getEnter_num());
            strOnline += "|" + String.valueOf(room.getUser_num());

        }
        sendAll(strOnline);
//        sendRoomAll("online|" + name);
        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            if(room.getRoomNum().equals(RoomNum)){
                room.addSocketUser(socket);
                room.addOnlineUser(name);
                freshClientsRoomOnline();
                sendRoomAll("roomTalk|>>>欢迎 " + name + " 加入游戏");
                break;
            }
        }
    }

    //////////////////////////////////////////////////////////

    //发言
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
                for (int i = 0; i < onlineUser.size(); i++) {
                    if (strReceiver.equals(onlineUser.elementAt(i))) {
                        socketSend = (Socket) socketUser.elementAt(i);
                        outSend = new PrintWriter(new BufferedWriter(
                                new OutputStreamWriter(
                                        socketSend.getOutputStream())), true);
                        outSend.println("talk|" + strSender + " 对你说："
                                + strTalkInfo);
                    } else if (strSender.equals(onlineUser.elementAt(i))) {
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
                        for (int j=0;j<room.sizeOfOnlineUser();j++){
                            if (strReceiver.equals(room.findOnlineUser(i))) {
                                socketSend = room.findSocketUser(i);
                                outSend = new PrintWriter(new BufferedWriter(
                                        new OutputStreamWriter(
                                                socketSend.getOutputStream())), true);
                                outSend.println("roomTalk|" + strSender + " 对你说："
                                        + strTalkInfo);
                            } else if (strSender.equals(room.findOnlineUser(i))) {
                                socketSend = room.findSocketUser(i);
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
    //////////////////////////////////////////////////////////////

    //在线用户列表
    private void freshClientsOnline() throws IOException {
        String strOnline = "online";
        String[] userList = new String[20];
        String useName = null;
        for (int i = 0; i < onlineUser.size(); i++) {
            strOnline += "|" + onlineUser.elementAt(i);
            useName = " " + onlineUser.elementAt(i);
            userList[i] = useName;
        }
        System.out.println("当前在线人数:"+onlineUser.size());
//        out.println(strOnline);
        sendAll(strOnline);
    }
    ////////////////////////房间内的在线用户///////////////////
    private void freshClientsRoomOnline() throws IOException {
        String strOnline = "room online";
        String[] userList = new String[20];
        String useName = null;
        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            if(room.getRoomNum().equals(RoomNum)){
                for (int j = 0; j < room.sizeOfOnlineUser(); j++) {
                    strOnline += "|" + room.findOnlineUser(j);
                    useName = " " + room.findOnlineUser(j);
                    userList[j] = useName;
                }
                System.out.println("当前在线人数:"+room.sizeOfOnlineUser());
//                out.println(strOnline);
                sendRoomAll(strOnline);
            }
        }

    }
    private void freshClientsLobbyOnline() throws IOException {
        String strOnline = "lobby";
        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            strOnline += "|" + room.getRoomNum();
            strOnline += "|" + String.valueOf(room.getEnter_num());
            strOnline += "|" + String.valueOf(room.getUser_num());

        }
        out.println(strOnline);

    }

    ////////////////////////////////////////////////////////

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
                    for (int j = 0; j < room.sizeOfOnlineUser(); j++) {
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

    /////////////////////////////////////////////////////
/////////////////////这里要加上把房间内的在线列表删除///////////////
    //关闭套接字，并将用户信息从在线列表中删除
    private String closeSocket() throws IOException {
        String strUser = "";
        for (int i = 0; i < socketUser.size(); i++) {
            if (socket.equals((Socket) socketUser.elementAt(i))) {
                strUser = onlineUser.elementAt(i).toString();
                socketUser.removeElementAt(i);
                onlineUser.removeElementAt(i);
                try {
                    freshClientsOnline();
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                sendAll("remove|" + strUser);
                freshClientsOnline();
            }
        }
        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            if (room.getRoomNum().equals(RoomNum)){
                for (int j = 0; j < room.sizeOfOnlineUser(); j++) {
                    if (socket.equals(room.findSocketUser(j))){
                        room.removeOnlineUser(i);
                        room.removeSocketUser(i);
                        freshClientsRoomOnline();
                        freshClientsLobbyOnline();
                    }
                }
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