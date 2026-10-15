package com.ems.model;

public class Equipment {
    private long id;
    private String assetTag;
    private String name;
    private String categoryName;
    private String locationName;
    private String conditionState;
    private String status;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getAssetTag() { return assetTag; }
    public void setAssetTag(String assetTag) { this.assetTag = assetTag; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }
    public String getConditionState() { return conditionState; }
    public void setConditionState(String conditionState) { this.conditionState = conditionState; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
