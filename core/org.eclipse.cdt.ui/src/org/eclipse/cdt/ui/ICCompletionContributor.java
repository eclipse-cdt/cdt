package org.eclipse.cdt.ui;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

public interface ICCompletionContributor {
	
	/**
	 * Initialize the completion contributor class
	 */
	void initialize();
	
	/**
	 * get the matching function of a given name
	 */
	IFunctionSummary getFunctionInfo(String name);
	
	/**
	 * Get array of matching functions starting with this prefix
	 */
	IFunctionSummary[] getMatchingFunctions(String prefix);
}

