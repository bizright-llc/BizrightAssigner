package com.example.william.bizrightAssigner.Bean;

public class SmallLocationPool {


    private Integer id;
    private String sku;
    private String upc;
    private  Integer assigned; // total assigned
    private Integer moveIn; //total need to move in
    private Integer moveOut;
    private Integer Inv;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getUpc() {
        return upc;
    }

    public void setUpc(String upc) {
        this.upc = upc;
    }

    public Integer getAssigned() {
        return assigned;
    }

    public void setAssigned(Integer assigned) {
        this.assigned = assigned;
    }

    public Integer getMoveIn() {
        return moveIn;
    }

    public void setMoveIn(Integer moveIn) {
        this.moveIn = moveIn;
    }

    public Integer getMoveOut() {
        return moveOut;
    }

    public void setMoveOut(Integer moveOut) {
        this.moveOut = moveOut;
    }

    public Integer getInv() {
        return Inv;
    }

    public void setInv(Integer inv) {
        Inv = inv;
    }
}
