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
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.ui.validators.ISystemValidator;




/**
 * Interface for new Connection (ie Definition) wizard main page classes
 */
public interface ISystemNewConnectionWizardMainPage extends ISystemWizardPage
{            
	
	public String getSystemType();
	public String getConnectionName();    	
	public String getHostName();    
	public String getConnectionDescription();
	public String getDefaultUserId();    
	public int getDefaultUserIdLocation();    	
	public String getProfileName();
    /**
     * Call this to restrict the system type that the user is allowed to choose
     */
    public void restrictSystemType(String systemType);
    /**
     * Call this to restrict the system types that the user is allowed to choose
     */
    public void restrictSystemTypes(String[] systemTypes);

	/**
	 * Call this to specify a validator for the connection name. It will be called per keystroke.
	 */
	public void setConnectionNameValidators(ISystemValidator[] v);
	/**
	 * Call this to specify a validator for the hostname. It will be called per keystroke.
	 */
	public void setHostNameValidator(ISystemValidator v);
	/**
	 * Call this to specify a validator for the userId. It will be called per keystroke.
	 */
	public void setUserIdValidator(ISystemValidator v);
    /**
     * This method allows setting of the initial user Id. Sometimes subsystems
     *  like to have their own default userId preference page option. If so, query
     *  it and set it here by calling this.
     */
    public void setUserId(String userId);	
	/**
	 * Preset the connection name
	 */
	public void setConnectionName(String name);
	/**
	 * Preset the host name
	 */
	public void setHostName(String name);
    /**
     * Set the profile names to show in the combo
     */
    public void setProfileNames(String[] names);    
    /**
     * Set the profile name to preselect
     */
    public void setProfileNamePreSelection(String name);
    /**
     * Set the currently selected connection so as to better initialize input fields
     */
    public void setCurrentlySelectedConnection(IHost connection);
}