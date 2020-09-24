package com.br.clientehabitual.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Conversoes {
    public String calendarToString(Calendar calendar){
        String dataString = "";
        Date data = calendar.getTime();
        try {
            SimpleDateFormat dataSimple = new SimpleDateFormat("dd/MM/yyyy");
            dataString = dataSimple.format(data);
        }catch (Exception e){}
        return dataString;
    }
    public Calendar stringToCalendar(String dataString){
        Calendar data = Calendar.getInstance();
        SimpleDateFormat dataSimple = new SimpleDateFormat("dd-MM-yyyy");
        try {
            data.setTime(dataSimple.parse(dataString.replaceAll("/","-")));
        }catch (Exception e){}
        return data;
    }
}
