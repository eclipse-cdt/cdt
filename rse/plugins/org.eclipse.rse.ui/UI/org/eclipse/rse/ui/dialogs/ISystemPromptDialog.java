/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.ui.dialogs;


/**
 * Suggested interface for dialogs used in actions in remote system framework.
 */
public interface ISystemPromptDialog 
{
	/**
	 * For explicitly setting input object
	 */
	public void setInputObject(Object inputObject);
	
	/**
	 * For explicitly getting input object
	 */
	public Object getInputObject();
	
	/**
	 * For explicitly getting output object after dialog is dismissed. Set by the
	 * dialog's processOK method.
	 */
	public Object getOutputObject();
	
	/**
	 * Allow caller to determine if window was cancelled or not.
	 */
	public boolean wasCancelled();
	
    /**
     * Expose inherited protected method convertWidthInCharsToPixels as a publicly
     *  excessible method
     */
    public int publicConvertWidthInCharsToPixels(int chars); 
    /**
     * Expose inherited protected method convertHeightInCharsToPixels as a publicly
     *  excessible method
     */
    public int publicConvertHeightInCharsToPixels(int chars); 

}