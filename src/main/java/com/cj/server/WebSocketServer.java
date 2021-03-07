package com.cj.server;


import com.cj.utils.GsonTool;
import com.cj.utils.OSUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import oshi.util.Util;

import javax.annotation.PostConstruct;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

@ServerEndpoint("/ws/monitor")
@Component
public class WebSocketServer {

    private static Logger logger = LoggerFactory.getLogger(WebSocketServer.class);
    private static final AtomicInteger OnlineCount = new AtomicInteger(0);
    // concurrent threadsafe set, store websocket session
    private static CopyOnWriteArraySet<Session> SessionSet = new CopyOnWriteArraySet<Session>();
    private static Thread broadCastThread;

    private volatile boolean toStop = false;

    @PostConstruct
    public void init() {
        logger.info("monitor websocket init");

    }
    /**
     * socket open event
     */
    @OnOpen
    public void onOpen(Session session) {
        SessionSet.add(session);
        int cnt = OnlineCount.incrementAndGet(); // 在线数加1
        logger.info("websocket connect, current count：{}", cnt);
        SendMessage(session, "connect success");
        //有连接了开启群发线程
        //开启群发

        if(broadCastThread == null || !broadCastThread.isAlive()) {
            broadCastThread = null;
            broadCastThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (SessionSet.size() > 0) {
                        try {
                            Map<String, Object> result = new HashMap<String, Object>();;
                            Date currentDate = new Date();
                            double cpuRate = OSUtil.cpuUsage();
                            double memRate =OSUtil.memUsage();
                            List<Object[]> cpuList = new ArrayList<Object[]>();
                            List<Object[]> memList = new ArrayList<Object[]>();
                            Object[] cpuItme = new Object[]{currentDate,cpuRate};
                            Object[] memItem = new Object[]{currentDate,memRate};
                            cpuList.add(cpuItme);
                            memList.add(memItem);

                            result.put("monitorCPU", cpuList);
                            result.put("monitorMem", memList);
                            Util.sleep(1000);
                            BroadCastSystemInfo(result);
                        } catch (IOException e) {
                            logger.error("running error: {}", e.getMessage());
                            //e.printStackTrace();
                        }

                    }
                    if(SessionSet.size() == 0){
                        Thread.currentThread().interrupt();
                    }
                }
            });
            broadCastThread.setName("xxl-job, admin BroadCastThread");
            broadCastThread.start();
        }
    }

    /**
     * socket close event
     */
    @OnClose
    public void onClose(Session session) {
        SessionSet.remove(session);
        int cnt = OnlineCount.decrementAndGet();
        logger.info("websocket close. current count：{}", cnt);
    }

    /**
     * receive message from client
     *
     * @param message
     *            message
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        logger.info("receive client message：{}",message);
        SendMessage(session, "message receive ："+message);

    }

    /**
     * when error
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        logger.error("websocket error：{}，Session ID： {}",error.getMessage(),session.getId());
        //error.printStackTrace();
    }

    /**
     * send message
     * @param session
     * @param message
     */
    public static void SendMessage(Session session, String message) {
        try {
            session.getBasicRemote().sendText(String.format("%s (From Server，Session ID=%s)",message,session.getId()));
        } catch (IOException e) {
            logger.error("websocket send message error：{}", e.getMessage());
            //e.printStackTrace();
        }
    }

    /**
     * send system usage data
     * @param session
     * @param result
     */
    public static void SendSystemInfo(Session session, Map<String, Object> result) {
        try {
            session.getBasicRemote().sendText(GsonTool.toJson(result));
        } catch (IOException e) {
            logger.error("websocket send message error：{}", e.getMessage());
            //e.printStackTrace();
        }
    }

    /**
     * broadcast message to each client
     * @param message
     * @throws IOException
     */
    public static void BroadCastInfo(String message) throws IOException {
        for (Session session : SessionSet) {
            if(session.isOpen()){
                SendMessage(session, message);
            }
        }
    }

    /**
     * broadcast system usage data
     * @param result
     * @throws IOException
     */
    public static void BroadCastSystemInfo(Map<String, Object> result) throws IOException {
        for (Session session : SessionSet) {
            if(session.isOpen()){
                SendSystemInfo(session, result);
            }
        }
    }

    /**
     * send message to specified session
     * @param sessionId
     * @param message
     * @throws IOException
     */
    public static void SendMessage(String message,String sessionId) throws IOException {
        Session session = null;
        for (Session s : SessionSet) {
            if(s.getId().equals(sessionId)){
                session = s;
                break;
            }
        }
        if(session!=null){
            SendMessage(session, message);
        }
        else{
            logger.warn("can not find your session：{}",sessionId);
        }
    }


}
