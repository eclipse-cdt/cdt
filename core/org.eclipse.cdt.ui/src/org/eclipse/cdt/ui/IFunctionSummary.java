package org.eclipse.cdt.ui;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

public interface IFunctionSummary {
	
	/**
	 * Get the name of the function
	 */
	public String getName();
	
	/**
	 * Get the function summary
	 */
	public String getSummary();
	
	/**
	 * Get the function prototype
	 */
	public String getPrototype();
	
	/**
	 * Get the function synopsis
	 */
	public String getSynopsis();
	
	/**
	 * Get headers required by this function
	 */
	public IRequiredInclude[] getIncludes();
}

