package org.eclipse.cdt.core.resources;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

public interface IBuildInfo {

	String getBuildLocation();
    String getFullBuildArguments();
    String getIncrementalBuildArguments();
    boolean isStopOnError();

	void setBuildLocation(String location);
    void setFullBuildArguments(String arguments);
    void setIncrementalBuildArguments(String arguments);
    void setStopOnError(boolean on);

//    boolean isClearBuildConsole();

    boolean isDefaultBuildCmd();
    void setUseDefaultBuildCmd(boolean on);
}

