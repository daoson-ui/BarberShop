package com.barbershop.dto;

public class TimeSlotDTO {

    private String time;
    private boolean busy;

    public TimeSlotDTO(String time, boolean busy) {
        this.time = time;
        this.busy = busy;
    }

    public String getTime() {
        return time;
    }

    public boolean isBusy() {
        return busy;
    }
}