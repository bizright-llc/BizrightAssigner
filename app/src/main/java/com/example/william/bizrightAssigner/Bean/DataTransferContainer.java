package com.example.william.bizrightAssigner.Bean;

public class DataTransferContainer {

    private String SKU;
    private Integer QTY;
    private String Area;
    private String PONum;
    private String Full;
    private  String checkSKU;
    private  String status;
    private  String moveIn;
    private String ItemName;
    private String imgUrl;

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getItemName() {
        return ItemName;
    }

    public void setItemName(String itemName) {
        ItemName = itemName;
    }

    public String getMoveIn() {
        return moveIn;
    }

    public void setMoveIn(String moveIn) {
        this.moveIn = moveIn;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSKU() {
        return SKU;
    }

    public void setSKU(String SKU) {
        this.SKU = SKU;
    }

    public Integer getQTY() {
        return QTY;
    }

    public void setQTY(Integer QTY) {
        this.QTY = QTY;
    }

    public String getArea() {
        return Area;
    }

    public void setArea(String area) {
        Area = area;
    }

    public String getPONum() {
        return PONum;
    }

    public void setPONum(String PONum) {
        this.PONum = PONum;
    }

    public String getFull() {
        return Full;
    }

    public void setFull(String full) {
        Full = full;
    }

    public String getCheckSKU() {
        return checkSKU;
    }

    public void setCheckSKU(String checkSKU) {
        this.checkSKU = checkSKU;
    }
}
