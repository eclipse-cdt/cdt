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

package org.eclipse.rse.ui.wizards;
/**
 * Interface for wizard pages
 */
public interface ISystemWizardPage
{    
	/**
	 * For explicitly setting input object for update mode wizards
	 */
	public void setInputObject(Object inputObject);
	
	/**
	 * For explicitly getting input object.
	 */
	public Object getInputObject();
	        
	/**
     * Perform error checking of the page contents, returning true only if there are no errors. 
     * <p>Called by the main wizard when the user presses Finish. The operation will be cancelled if 
     * this method returns false for any page.
     */
	public boolean performFinish();    
	
    /**
     * Set the help context Id (infoPop) for this wizard. This must be fully qualified by
     *  plugin ID.
     * <p>
     * Same as {@link org.eclipse.rse.ui.actions.SystemBaseAction #setHelp(String)}
     * @see org.eclipse.rse.ui.actions.SystemBaseAction #getHelpContextId()
     */
    public void setHelp(String id);
    /**
     * Return the help Id as set in setHelp(String)
     */
    public String getHelpContextId();
	
}