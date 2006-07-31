/********************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.files.ui.propertypages;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystemConfiguration;
import org.eclipse.rse.ui.propertypages.SystemAbstractPropertyPageExtensionAction;
import org.eclipse.rse.ui.propertypages.SystemBasePropertyPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPropertyPage;



/**
 * This is a base class to simplify the creation of property pages supplied via the
 * org.eclipse.rse.ui.propertyPages extension point, targeting remote files
 * and/or remote folders.
 * <p>
 * The only method you must implement is {@link #createContentArea(Composite)}.
 * <p>
 * The benefits of this class are:</p>
 * <ul>
 *   <li>From {@link SystemBasePropertyPage}: Adds a message line and {@link org.eclipse.rse.ui.messages.ISystemMessageLine} message methods. 
 *   <li>From {@link SystemBasePropertyPage}: Automatically assigns mnemonics to controls on this page, simplifying this common task. See {#wantMnemonics()}. 
 *   <li>From {@link SystemBasePropertyPage}: For pages with input controls, simplifies the page validation burden: only one method need be overridden: {@link #verifyPageContents()}
 *   <li>From {@link SystemBasePropertyPage}: If no Default and Apply buttons wanted, the area reserved for this is removed, removing extra white space.
 *   <li>Supplies helper method {@link #getRemoteFile()} for querying the selected remote file/folder.
 *   <li>Supplies helper methods to query the {@link #getRemoteFileSubSystem() file-subsystem} and {@link #getSystemConnection() connection} containing the selected remote file.
 * </ul>
 */
public abstract class SystemAbstractRemoteFilePropertyPageExtensionAction 
       extends SystemAbstractPropertyPageExtensionAction implements IWorkbenchPropertyPage 
{

	/**
	 * Constructor 
	 */
	public SystemAbstractRemoteFilePropertyPageExtensionAction() 
	{
		super();
	}

	// ----------------------------------
	// OVERRIDABLE METHODS FROM PARENT...
	// ----------------------------------
	/**
	 * <i><b>Abstract</b>. You must override.</i><br>
	 * This is where child classes create their content area versus createContent,
	 *  in order to have the message line configured for them and mnemonics assigned.
	 */
	protected abstract Control createContentArea(Composite parent);
	
    /**
	 * <i>You may override if your page has input fields. By default returns true.</i><br>
     * Validate all the widgets on the page. Based on this, the Eclipse framework will know whether
     *  to veto any user attempt to select another property page from the list on the left in the 
     *  Properties dialog.
	 * <p>
	 * Subclasses should override to do full error checking on all the widgets on the page. Recommendation:<br>
	 * <ul>
	 * <li>If an error is detected, issue a {@link org.eclipse.rse.ui.messages.SystemMessage} via {@link #setErrorMessage(SystemMessage)} or text message via {@link #setErrorMessage(String)}.
	 * <li>If no errors detected, clear the message line via {@link #clearErrorMessage()}
	 * </ul>
	 * 
	 * @return true if there are no errors, false if any errors were found.
     */
    protected boolean verifyPageContents()
    {
    	return true;
    }

	// ------------------------------------------------------------------
	// CONVENIENCE METHODS WE ADD SPECIFICALLY FOR REMOTE FILE ACTIONS...
	// ------------------------------------------------------------------
	/**
	 * Retrieve the input selected object, as an IRemoteFile, for convenience.
	 */
	public IRemoteFile getRemoteFile()
	{
		return (IRemoteFile)super.getRemoteObject();
	}

    /**
     * Get the remote file subsystem from which the selected objects were resolved.
     * This has many useful methods in it, including support to transfer files to and
     * from the local and remote systems.
     */
    public IRemoteFileSubSystem getRemoteFileSubSystem()
    {
    	return (IRemoteFileSubSystem)getSubSystem();
    }
    
    /**
     * Returns the remote file subsystem factory which owns the subsystem from which the 
     * selected remote objects were resolved. This has some useful methods in it, 
     * including isUnixStyle() indicating if this remote file system is unix or windows.
     */
    public IRemoteFileSubSystemConfiguration getRemoteFileSubSystemConfiguration()
    {
    	return (IRemoteFileSubSystemConfiguration)getSubSystemConfiguration();
    }    
    
}