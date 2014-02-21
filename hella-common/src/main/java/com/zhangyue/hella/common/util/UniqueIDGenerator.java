package com.zhangyue.hella.common.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class UniqueIDGenerator {

    private static long lastID=now();
    private static final String prefix=getPrefix();
    private static final int SLEEP_INTERVAL = 50; 
    
    public synchronized static long generateID(){
        long id=now();
        while(id==lastID){
            try {
                Thread.sleep(SLEEP_INTERVAL);
            } catch (InterruptedException e) {
            }
            id=now();
        }
        lastID=id;
        return id;
    }
    
    public synchronized static String generateIDByPrifix(){
        return prefix+generateID();
    }
    
    private static long now(){
        return System.currentTimeMillis();
    }
    
    private static String getPrefix(){
        try {
            return InetAddress.getLocalHost().getHostAddress().replaceAll("\\.","");
        } catch (UnknownHostException e) {
            return "localhost";
        }
    }
    
    public static void main(String[] args){
        while(true){
            System.out.println(UniqueIDGenerator.generateID());
        }
    }
}
