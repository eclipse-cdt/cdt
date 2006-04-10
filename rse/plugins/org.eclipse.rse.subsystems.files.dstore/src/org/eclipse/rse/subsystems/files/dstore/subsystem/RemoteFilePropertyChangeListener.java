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

package org.eclipse.rse.subsystems.files.dstore.subsystem;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.core.subsystems.CommunicationsEvent;
import org.eclipse.rse.core.subsystems.ICommunicationsListener;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.dstore.universal.miners.IUniversalDataStoreConstants;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.subsystems.files.core.SystemFileResources;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.dstore.model.DStoreFile;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.UIJob;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.extra.internal.extra.DomainEvent;
import org.eclipse.dstore.extra.internal.extra.IDomainListener;



public class RemoteFilePropertyChangeListener implements IDomainListener,
        ICommunicationsListener
{

    protected DataStore dataStore;

    protected FileServiceSubSystem _fileSubSystem;

    protected Shell shell;

    protected ISystemRegistry _registry;

    protected IConnectorService system;

    protected boolean _networkDown = false;

    protected class FindShell implements Runnable
    {

        private Shell shell;

        /**
         * @see Runnable#run()
         */
        public void run()
        {
            try
            {
                Shell[] shells = Display.getCurrent().getShells();
                for (int loop = 0; loop < shells.length && shell == null; loop++)
                {
                    if (shells[loop].isEnabled())
                    {
                        shell = shells[loop];
                    }
                }
            }
            catch (Exception e)
            {
                SystemBasePlugin.logError(
                        "StatusChangeListener.FindShell exception: ", e);
            }
        }
    }
    
    public class DecorateJob extends UIJob
    {
    	private DStoreFile[] _files;
    	private DStoreFile   _parentFile;
    	public DecorateJob(DStoreFile[] files, DStoreFile parentFile)
    	{
    		super(SystemFileResources.RESID_JOB_DECORATEFILES_NAME);
    		_files= files;
    		_parentFile = parentFile;
    	}

		public IStatus runInUIThread(IProgressMonitor monitor)
		{
			for (int i = 0; i < _files.length; i++)
			{			
			  _registry.fireEvent(new
                      org.eclipse.rse.model.SystemResourceChangeEvent(_files[i],
                      ISystemResourceChangeEvents.EVENT_ICON_CHANGE,
                        _parentFile));
            }
              
			/*
			_registry.fireEvent(new
                      org.eclipse.rse.model.SystemResourceChangeEvent(_files,
                      ISystemResourceChangeEvent.EVENT_REPLACE_CHILDREN,
                        _parentFile));
                        */
			  return Status.OK_STATUS;
		}
    	
    }

    public RemoteFilePropertyChangeListener(Shell shell, IConnectorService system,
            DataStore dataStore, FileServiceSubSystem fileSS)
    {
        this.shell = shell;
        this._fileSubSystem = fileSS;
        this.dataStore = dataStore;
        this.system = system;
        this._registry = SystemPlugin.getTheSystemRegistry();
        system.addCommunicationsListener(this);
        dataStore.getDomainNotifier().addDomainListener(this);
    }

    public DataStore getDataStore()
    {
        return dataStore;
    }
    
    /**
     * @see IDomainListener#listeningTo(DomainEvent)
     */
    public boolean listeningTo(DomainEvent event)
    {
 
        DataElement parent = (DataElement) event.getParent();

        if (dataStore == parent.getDataStore())
        {
            String dataElementType = parent.getType();
            if (dataElementType
                    .equals(IUniversalDataStoreConstants.UNIVERSAL_FOLDER_DESCRIPTOR) ||
                    dataElementType
                    .equals(IUniversalDataStoreConstants.UNIVERSAL_FILE_DESCRIPTOR)	
            )
            {
                return true;
            }
            else
            {
                return false;
            }
        }

        return false;
    }

    public void finish()
    {
        dataStore.getDomainNotifier().removeDomainListener(this);
    }

    /**
     * @see IDomainListener#domainChanged(DomainEvent)
     */
    public void domainChanged(DomainEvent event)
    {
        DataElement parent = (DataElement)event.getParent();
         
        List children = parent.getNestedData();
        if (children != null)
        {
        
	        StringBuffer parentPath = new StringBuffer(parent.getAttribute(DE.A_VALUE));
	        parentPath.append(_fileSubSystem.getSeparatorChar());
	    	parentPath.append(parent.getName());
	    	DStoreFile parentFile = (DStoreFile) _fileSubSystem.getCachedRemoteFile(parentPath.toString());
	    	
	        boolean refreshParent = false;
	        List toUpdate = new ArrayList();
	        for (int i = 0; i < children.size(); i++)
	        {
	
	            DataElement subject = (DataElement) children.get(i);
	            String type = subject.getType();
	            if (type
	                    .equals(IUniversalDataStoreConstants.UNIVERSAL_FILE_DESCRIPTOR) ||
	                    type.equals(IUniversalDataStoreConstants.UNIVERSAL_FOLDER_DESCRIPTOR))
	                
	            {
	            	StringBuffer path = new StringBuffer(subject.getAttribute(DE.A_VALUE));
	            	path.append(_fileSubSystem.getSeparatorChar());
	            	path.append(subject.getName());
	
	    
	                // find cached copy
	                try
	                {
	                    DStoreFile updated = (DStoreFile) _fileSubSystem.getCachedRemoteFile(path.toString());
	               
	                    if (updated != null)
	                    {
	                         String classification = updated.getClassification();
	                        if (!classification.equals("file") && !classification.equals("directory"))
	                        {
	                            refreshParent = true;
	                            toUpdate.add(updated);
	                        }
	                      
	                    }
	                }
	                catch (Exception e)
	                {
	                    e.printStackTrace();
	                }
	            }
	        }
	        
	        if (refreshParent)
	        {
	
	        	DecorateJob job = new DecorateJob((DStoreFile[])toUpdate.toArray(new DStoreFile[toUpdate.size()]), parentFile);
	        	job.schedule();
	        }
        }

    }

    /**
     * @see IDomainListener#getShell()
     */
    public Shell getShell()
    {
        // dy: DomainNotifier (which calls this method) requires the shell not
        // be disposed
        //if (shell == null) {
        if (shell == null || shell.isDisposed())
        {
            FindShell findShell = new FindShell();
            Display.getDefault().syncExec(findShell);
            shell = findShell.shell;
        }
        return shell;
    }

    /**
     * @see ICommunicationsListener#communicationsStateChange(CommunicationsEvent)
     */
    public void communicationsStateChange(CommunicationsEvent e)
    {
        if (e.getState() == CommunicationsEvent.CONNECTION_ERROR)
        {
            _networkDown = true;
        }
        else if (e.getState() == CommunicationsEvent.BEFORE_DISCONNECT)
        {
            finish();
        }
    }

    /**
     * @see org.eclipse.rse.core.subsystems.ICommunicationsListener#isPassiveCommunicationsListener()
     */
    public boolean isPassiveCommunicationsListener()
    {
        return false;
    }

}