/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.subsystems.processes.core.subsystem;




import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.services.clientserver.processes.IHostProcess;
import org.eclipse.rse.services.clientserver.processes.IHostProcessFilter;


/**
 * 
 * @author mjberger
 * 
 * This interface represents a handle to a remote process object
 * It differs from an IHostProcess in that it contains extra, RSE-specific
 * information.
 * <ul>
 *   <li>This is an interface, so there are no static methods
 *   <li>This are no action methods, only read methods. The action methods
 *        such as kill are found in the process subsystem interface.
 *        All remote commands/actions are funnelled through subsystems in this
 *        remote system framework.
 *   <li>Similarly, you do not construct these objects directly. Rather, there are
 *        factory methods in IRemoteProcessSubSystem to create them.
 * </ul>
 * <p>
 */
public interface IRemoteProcess extends IHostProcess, IAdaptable
{
	
	/**
	 * Get parent subsystem
	 */
	public RemoteProcessSubSystem getParentRemoteProcessSubSystem();
	
    /**
     * Return the connection this remote process is from.
     */
    public IHost getSystemConnection();
    
    /**
     * Return the filter string resolved to get this object
     */
    public IHostProcessFilter getFilterString();
     
    /**
     * Set the filter string resolved to get this object
     */
    public void setFilterString(IHostProcessFilter filterString);

    /**
     * Get fully qualified connection and folder name of associated object in /proc filesystem: profile.connection/path/procfolder.
     * Note the separator character between the profile name and the connection name is always '.'
     * Note the separator character between the connection and qualified-folder is always ':'
     */
    public String getAbsolutePathPlusConnection();
    
    /**
     * Get the parent remote process of this process
     */
    public IRemoteProcess getParentRemoteProcess();
    
    /**
     * Return the context associated with this remote file
     */
    public IRemoteProcessContext getContext();
    
    /**
     * Get fully qualified name of associated object in /proc filesystem: root plus path plus pid. No connection name.
     */
    public String getAbsolutePath();
    
    /**
     * Get the underlying object that this object wrappers, whether that is a 
     * DataElement or an IRemoteServerProcess, or something else is implementation specific.
     */
    public Object getObject();
}