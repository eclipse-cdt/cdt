package org.eclipse.cdt.core.resources;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

public interface IBuildInfo {
	public static final String SEPARATOR = ",";
	
	String getBuildLocation();
	String getDefinedSymbols();
    String getFullBuildArguments();
    String getIncludePaths();
    String getIncrementalBuildArguments();
    boolean isStopOnError();

	void setBuildLocation(String location);
	void setDefinedSymbols(String symbols);
    void setFullBuildArguments(String arguments);
    void setIncludePaths(String paths);
    void setIncrementalBuildArguments(String arguments);
    void setStopOnError(boolean on);

//    boolean isClearBuildConsole();

    boolean isDefaultBuildCmd();
    void setUseDefaultBuildCmd(boolean on);
}

