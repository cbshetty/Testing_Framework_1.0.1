package com.api.payloads;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class Results {

    @SerializedName("projectName")
    @Expose
    private String projectName;
    @SerializedName("environment")
    @Expose
    private String environment;
    @SerializedName("groupName")
    @Expose
    private String groupName;
    @SerializedName("duration")
    @Expose
    private Integer duration;
    @SerializedName("percentage")
    @Expose
    private Integer percentage;
    @SerializedName("totalCases")
    @Expose
    private Integer totalCases;
    @SerializedName("passedCases")
    @Expose
    private Integer passedCases;
    @SerializedName("failedCases")
    @Expose
    private Integer failedCases;
    @SerializedName("resultLink")
    @Expose
    private String resultLink;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getPercentage() {
        return percentage;
    }

    public void setPercentage(Integer percentage) {
        this.percentage = percentage;
    }

    public Integer getTotalCases() {
        return totalCases;
    }

    public void setTotalCases(Integer totalCases) {
        this.totalCases = totalCases;
    }

    public Integer getPassedCases() {
        return passedCases;
    }

    public void setPassedCases(Integer passedCases) {
        this.passedCases = passedCases;
    }

    public Integer getFailedCases() {
        return failedCases;
    }

    public void setFailedCases(Integer failedCases) {
        this.failedCases = failedCases;
    }

    public String getResultLink() {
        return resultLink;
    }

    public void setResultLink(String resultLink) {
        this.resultLink = resultLink;
    }
}
