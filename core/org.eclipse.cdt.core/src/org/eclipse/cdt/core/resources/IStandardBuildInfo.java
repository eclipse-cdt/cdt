package org.eclipse.cdt.core.resources;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

public interface IStandardBuildInfo {
	String getBuildLocation();
	public String[] getPreprocessorSymbols();
    String getFullBuildArguments();
    public String[] getIncludePaths();
    String getIncrementalBuildArguments();
    boolean isStopOnError();
    
	void setBuildLocation(String location);
	public void setPreprocessorSymbols(String[] symbols);
    void setFullBuildArguments(String arguments);
	public void setIncludePaths(String[] paths);
	void setIncrementalBuildArguments(String arguments);
    void setStopOnError(boolean on);

//    boolean isClearBuildConsole();

    boolean isDefaultBuildCmd();
    void setUseDefaultBuildCmd(boolean on);

	/**
	 * @param doc
	 * @param rootElement
	 */
	void serialize(Document doc, Element rootElement);
}

