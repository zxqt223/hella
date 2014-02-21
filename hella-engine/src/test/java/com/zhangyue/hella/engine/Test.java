package com.zhangyue.hella.engine;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Test {

    // 537164 | 3888553 | 268668 | 146345
    public static void main(String args[]) throws SocketException, UnknownHostException {
         System.out.println(new Date().getTime());
        
         Calendar cal = Calendar.getInstance();
         cal.setTimeInMillis(new Date().getTime());
         SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         System.out.println(df.format(cal.getTime()));
    }
}
