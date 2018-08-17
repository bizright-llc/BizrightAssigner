package com.example.william.bizrightAssigner.Bean;


import java.util.Date;

public class EventLog {

    private Integer id;
    private String ProcessName;
    private String EventName;
    private String Ack;
    private String Message;
    private String EnterDate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getProcessName() {
        return ProcessName;
    }

    public void setProcessName(String processName) {
        ProcessName = processName;
    }

    public String getEventName() {
        return EventName;
    }

    public void setEventName(String eventName) {
        EventName = eventName;
    }

    public String getAck() {
        return Ack;
    }

    public void setAck(String ack) {
        Ack = ack;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public String getEnterDate() {
        return EnterDate;
    }

    public void setEnterDate(String enterDate) {
        EnterDate = enterDate;
    }
}
