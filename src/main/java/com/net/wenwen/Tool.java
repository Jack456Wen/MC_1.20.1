package com.net.wenwen;

import java.time.LocalDate;

public class Tool {
    public static String name="wenwen8973";
    public static boolean isGame=false;
    public static boolean IsDay(int y,int r)
    {
        LocalDate today = LocalDate.now();
        return (today.getMonthValue() == y && today.getDayOfMonth() == r);
    }

    public static boolean IsSr()
    {
        return IsDay(12,30);
    }

}
