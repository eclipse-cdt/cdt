/*******************************************************************************
 * Copyright (c) 2002, 2015  IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Martin Oberhuber (Wind River) - [175262] IHost.getSystemType() should return IRSESystemType 
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 * Martin Oberhuber (Wind River) - [186640] Add IRSESystemType.testProperty() 
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Martin Oberhuber (Wind River) - [189130] Move SystemIFileProperties from UI to Core
 * Martin Oberhuber (Wind River) - [199573] Fix potential threading issues in SystemTempFileListener
 * David McKnight   (IBM)        - [205297] Editor upload should not be on main thread
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * David McKnight (IBM) 		 - [225747] [dstore] Trying to connect to an "Offline" system throws an NPE
 * David McKnight   (IBM)        - [235221] Files truncated on exit of Eclipse
 * David McKnight   (IBM)        - [251631] NullPointerException in SystemTempFileListener
 * David McKnight   (IBM)        - [256048] Saving a member open in Remote LPEX editor while Working Offline doesn't set the dirty property
 * David McKnight   (IBM)        - [381482] Improper use of save participant is causing a workspace hang
 * David McKnight   (IBM)        - [390609] Cached file opened twice in case of eclipse linked resource..
 * David McKnight   (IBM)        - [468096] NullPointerException in SystemTempFileListener.checkLocalChanges (379)
 *******************************************************************************/

package org.eclipse.rse.files.ui.resources;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.files.ui.Activator;
import org.eclipse.rse.internal.files.ui.FileResources;
import org.eclipse.rse.internal.files.ui.resources.SystemRemoteEditManager;
import org.eclipse.rse.subsystems.files.core.SystemIFileProperties;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileSubSystem;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * This class manages listening for resource changes within our temp file project
 * It is used for listening to saves made in the editor so that we can upload 
 * changes to the remote files.    */
public abstract class SystemTempFileListener implements IResourceChangeListener
{
	private class TempFileSaveParticipant implements ISaveParticipant
	{
		private SystemTempFileListener _listener;

		public TempFileSaveParticipant(SystemTempFileListener listener){
			_listener = listener;
		}
		
		public void doneSaving(ISaveContext context) {	
		}

		public void prepareToSave(ISaveContext context) throws CoreException {			
		}

		public void rollback(ISaveContext context) {
		}

		public void saving(ISaveContext context) throws CoreException {
			// wait for completion of synch
			if (PlatformUI.getWorkbench().isClosing() &&
					context.getKind() == ISaveContext.FULL_SAVE) {
				while (isSynching()){
					try {
						Thread.sleep(1000);				
					}
					catch (Exception e){					
					}
				}
			}
		}
		
		private boolean isSynching()
		{			
			return _isSynching || _changedResources.size() > 0;
		}
		
	}
	
	private ArrayList _changedResources;
	private ArrayList _ignoredFiles = new ArrayList();
	private volatile boolean _isSynching;
	private boolean _isEnabled;

	public SystemTempFileListener()
	{
		_changedResources = new ArrayList();
		_isSynching = false;
		_isEnabled = true;		
		
	    ISaveParticipant saveParticipant = new TempFileSaveParticipant(this);
	    try {
	    	ResourcesPlugin.getWorkspace().addSaveParticipant(Activator.getDefault(), saveParticipant);
	    }
	    catch (CoreException e){
	    	SystemBasePlugin.logError("Exception adding save participant", e); //$NON-NLS-1$
	    }
	}

	public void setEnabled(boolean flag)
	{
		_isEnabled = flag;
	}

	public void addIgnoreFile(IFile toIgnore)
	{
		_ignoredFiles.add(toIgnore);
	}
	
	public void removeIgnoreFile(IFile toNotIgnore)
	{
		_ignoredFiles.remove(toNotIgnore);
	}
	
	public boolean isIgnorable(IFile file)
	{
	    if (_ignoredFiles.contains(file))
	    {
	        return true;
	    }
	    else
	    {
	    	IPath location = file.getLocation();
	    
	    	if (location == null){
	    		// linked into remote file system -- ignore in tempfile listener 
	    		return true;
	    	}
	    	else {
	    		String path = location.toString().toLowerCase();
	        
	    		for (int i = 0; i < _ignoredFiles.size(); i++)
	    		{
	    			IFile cfile = (IFile)_ignoredFiles.get(i);
	    			String cpath = cfile.getLocation().toString().toLowerCase();
	    			if (path.equals(cpath))
	    			{
	    				return true;
	    			}
	    		}
	    	}
	    }
	    return false;
	}


	
	/**
	 * @see IResourceChangeListener#resourceChanged(IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event)
	{
		if (_isEnabled)
		{
			IResourceDelta delta = event.getDelta();
			if (delta != null)
			{
				if (preScanForTempFiles(delta))
				{
					// a temp file has changed
					// synchronize temp file with remote file
					processDelta(delta);

					if (_changedResources.size() > 0 && !_isSynching)
					{					
						// indicating synching here instead of in SynchResourcesJob because
						// otherwise two calls can get into here creating two jobs
						_isSynching = true;
						synchRemoteResourcesOnThread();
					}
				}
				else
				{
					if (!RSECorePlugin.getThePersistenceManager().isBusy())
					{
						List changes = new ArrayList();
						checkLocalChanges(delta, changes);
						refreshRemoteResourcesOnMainThread(changes);
					}
				}
			} 
		}
	}
	
	public class RefreshResourcesJob extends UIJob
	{
		private List _resources;
		public RefreshResourcesJob(List resources)
		{
			super(FileResources.RSEOperation_message);
			_resources = resources;
		}
		
		public IStatus runInUIThread(IProgressMonitor monitor)
		{
			monitor.beginTask(FileResources.MSG_SYNCHRONIZE_PROGRESS, _resources.size());
			for (int i = 0; i < _resources.size(); i++)
			{
				Object resource = _resources.get(i);
				refreshRemoteResource(resource);
				monitor.worked(1);
			}
			monitor.done();
			return Status.OK_STATUS;
		}	
	}

	/***
	 * @deprecated don't use this class, it's only here because to remove it would be
	 * an API change, and we can't do that until 3.0.  Instead of using this, 
	 * SynchResourcesJob should be used.
	 */
	public class RefreshResourcesUIJob extends WorkbenchJob
	{
		public RefreshResourcesUIJob()
		{
			super(FileResources.RSEOperation_message);
		}
		
		public IStatus runInUIThread(IProgressMonitor monitor)
		{
			_isSynching = true;
			try {
				IFile[] filesToSync;
				synchronized(_changedResources) {
					filesToSync = (IFile[])_changedResources.toArray(new IFile[_changedResources.size()]);
					_changedResources.clear();
				}
				monitor.beginTask(FileResources.MSG_SYNCHRONIZE_PROGRESS, IProgressMonitor.UNKNOWN);
				setName(FileResources.MSG_SYNCHRONIZE_PROGRESS);
				for (int i = 0; i < filesToSync.length; i++)
				{
					synchronizeTempWithRemote(filesToSync[i], monitor);
				}
			} finally {
				_isSynching = false;
				monitor.done();
			}
			return Status.OK_STATUS;
		}
	}


	
	/**
	 * Used for doing the upload from a job
	 * @author dmcknigh
	 *
	 */
	private class SynchResourcesJob extends Job
	{
		public SynchResourcesJob()
		{
			super(FileResources.RSEOperation_message);
		}
		
		public IStatus run(IProgressMonitor monitor)
		{
			try {
				// using while loop because changed resources could get added after the original batch
				while (!_changedResources.isEmpty()){
					IFile[] filesToSync;
					synchronized(_changedResources) {
						filesToSync = (IFile[])_changedResources.toArray(new IFile[_changedResources.size()]);
						_changedResources.clear();
					}
					
					monitor.beginTask(FileResources.MSG_SYNCHRONIZE_PROGRESS, IProgressMonitor.UNKNOWN);
					setName(FileResources.MSG_SYNCHRONIZE_PROGRESS);
					for (int i = 0; i < filesToSync.length; i++)
					{
						synchronizeTempWithRemote(filesToSync[i], monitor);
					}
				}
			} 
			catch (Exception e){
				e.printStackTrace();
			}			
			finally {
				_isSynching = false;
				monitor.done();
			}
			return Status.OK_STATUS;
		}
	}
	
	private void refreshRemoteResourcesOnMainThread(List resources)
	{
		RefreshResourcesJob job = new RefreshResourcesJob(resources);
		job.setPriority(Job.INTERACTIVE);
		job.schedule();
	}
	
	private void synchRemoteResourcesOnThread()
	{
		SynchResourcesJob job = new SynchResourcesJob();
		job.setPriority(Job.INTERACTIVE);
		job.schedule();
	}
	
	protected void checkLocalChanges(IResourceDelta delta, List changes)
	{
		IResourceDelta[] children = delta.getAffectedChildren();
		for (int i = 0; i < children.length; i++)
		{
			IResourceDelta child = children[i];

			// DKM - case where local resource changes that local subsystem happens to be looking at
			int kind = child.getKind();
			boolean noChange = (kind == IResourceDelta.NO_CHANGE);
			if (noChange)
				return;
			
			boolean isChanged = (kind == IResourceDelta.CHANGED);
			boolean isDeleted = (kind == IResourceDelta.REMOVED);
			boolean isAdded = (kind == IResourceDelta.ADDED);
			boolean isMovedTo = (kind == IResourceDelta.MOVED_TO);
			boolean isMovedFrom = (kind == IResourceDelta.MOVED_FROM);
						
			IResource resource = child.getResource();
			
			String pathOfChild = null;
			String pathOfParent = null;
			
			IPath location = resource.getLocation();
			if (location == null)
			{
				// deleted resource
				String projectPath = child.getFullPath().toOSString();
				String workspacePath = SystemBasePlugin.getWorkspaceRoot().getLocation().toOSString();
				
				pathOfChild = workspacePath + projectPath;
				pathOfParent = (new File(pathOfChild)).getParent();
			}
			else
			{
				pathOfChild = resource.getLocation().toOSString();
				IContainer container = resource.getParent();
				if (container != null){
					IPath containerPath = container.getLocation();
					if (containerPath != null){
						pathOfParent = containerPath.toOSString();
					}
				}
			}
		
	

			if (isChanged)
			{
				checkLocalChanges(child, changes);
			}
			else
			{ 
				RemoteFileSubSystem fs = (RemoteFileSubSystem)getLocalFileSubSystem();
				if (fs == null) return; // MJB: Defect 45678
				
				IRemoteFile remoteFile = fs.getCachedRemoteFile(pathOfChild);
				if (remoteFile != null)
				{
					remoteFile.markStale(true);
				}
				
				IRemoteFile cachedParent = fs.getCachedRemoteFile(pathOfParent);
				if (cachedParent == null)
					return;
				
				cachedParent.markStale(true);
				if (!changes.contains(cachedParent))
				{
					changes.add(cachedParent);
				}
				if (isDeleted)				
				{										
					//System.out.println("deleted="+isDeleted);					
				}
				else if (isAdded)
				{
					//System.out.println("added="+isAdded);					
				}
				else if (isMovedTo)
				{
					//System.out.println("movedto="+isMovedTo);
				}
				else if (isMovedFrom)
				{
					//System.out.println("movedfrom="+isMovedFrom);
				}
			}
			
		}
	}
	
	protected IRunnableContext getRunnableContext(Shell shell)
	{
		IRunnableContext irc = RSEUIPlugin.getTheSystemRegistryUI().getRunnableContext();
		if (irc != null)
		{
			return irc;
		}
		else
		{
			// for other cases, use statusbar
			IWorkbenchWindow win = SystemBasePlugin.getActiveWorkbenchWindow();
			
			if (win != null)
			{
				Shell winShell = SystemBasePlugin.getActiveWorkbenchShell();
				if (winShell != null && !winShell.isDisposed() && winShell.isVisible())
				{
					shell = winShell;
					return win;
				}
				else
				{
					win = null;
				}
			}

			return new ProgressMonitorDialog(shell);
		}
	}
	/**
	 * Check the delta for changed temporary files.  If
	 * any are found, synchronize the temporary files with
	 * the corresponding remote files.
	 * 
	 * @param delta the delta to compare	 
	 */
	protected void processDelta(IResourceDelta delta)
	{
		IResourceDelta[] children = delta.getAffectedChildren();
		for (int i = 0; i < children.length; i++)
		{
			IResourceDelta child = children[i];

			IResource resource = child.getResource();

			if (resource instanceof IFile)
			{

				// see if this temp file has been changed
				int ckind = child.getKind();
				boolean isChanged = ckind == IResourceDelta.CHANGED;
				int flags = child.getFlags();
				boolean contentChanged = (isChanged && (flags & IResourceDelta.CONTENT) != 0);

				if (contentChanged)
				{
				//TODO	PROBLEM!! - Eclipse threading means the file may not be ignorable when this event comes in
					  //        - we need to know if this is via edit or not!
					if (!_changedResources.contains(resource) && !isIgnorable((IFile)resource))
					{
					    SystemIFileProperties properties = new SystemIFileProperties(resource);
					    long t1 = properties.getDownloadFileTimeStamp();
					    
					    // get the modified timestamp from the File, not the IFile
						// for some reason, the modified timestamp from the IFile does not always return
						// the right value. There is a Javadoc comment saying the value from IFile might be a
						// cached value and that might be the cause of the problem.
					    long t2 = resource.getLocation().toFile().lastModified();
					    
						if (t1 != t2)
						{
						    String ssStr = properties.getRemoteFileSubSystem();
						    if (ssStr != null)
						    {
						        ISubSystem ss = RSECorePlugin.getTheSystemRegistry().getSubSystem(ssStr);
						        if (doesHandle(ss))
						        {
						        	synchronized(_changedResources) {
						        		//avoid ConcurrentModificationException
							            _changedResources.add(resource);
						        	}
						        }
						    }
						}
					}
					
					// KM - commenting out everything below to avoid doing checks as to whether
					// the file is opened in an editor. The check means that for cases
					// where the remote file was opened using an external editor, there
					// won't be a save back to the host, even when the user refreshes the temp project
					// or the file.
					
					/* DKM - can't remember why I did this, but it might be obsolete now
					 * need to be able to save remotely regardless whether it's being edited that way or not
					 */
					///*
					// check if this file is being edited
					/*IWorkbenchWindow window = RSEUIPlugin.getActiveWorkbenchWindow();
					if (window == null)
					{
						// DKM:
						//no window, so forget about editors...just save
						// whenever refresh() is done (regardless of changes we hit this
						// we should only do something here when on main thread
						//_changedResources.add(resource);
					}
					else
					{
						IWorkbenchPage page = window.getActivePage();
						if (page != null)
						{
							IEditorReference[] references = page.getEditorReferences();
							for (int e = 0; e < references.length; e++)
							{
								IEditorReference ref = references[e];
								IEditorPart editorPart = ref.getEditor(false);
								if (editorPart != null)
								{
									IEditorInput input = editorPart.getEditorInput();
									if (input != null && input instanceof FileEditorInput)
									{
										FileEditorInput finput = (FileEditorInput) input;
										IFile eFile = finput.getFile();
										String eLoc = eFile.getLocation().toString();

										if (eLoc.equals(loc))
										{
											// add to list of pending resource changes
											if (!_changedResources.contains(resource))
											{
												_changedResources.add(resource);
											}
											/// DKM - shouldn't return
											//return;
										}
									}
									else if (input != null && input instanceof SystemCompareInput)
									{
										SystemCompareInput compareInput = (SystemCompareInput) input;
										IResource lFile = compareInput.getLeftResource();
										IResource rFile = compareInput.getRightResource();

										String lLoc = lFile.getLocation().toString();
										if (lLoc.equals(loc))
										{
											// add to list of pending resource changes
											if (!_changedResources.contains(resource))
											{
												_changedResources.add(resource);
											}
										}

										String rLoc = rFile.getLocation().toString();
										if (rLoc.equals(loc))
										{
											// add to list of pending resource changes
											if (!_changedResources.contains(resource))
											{
												_changedResources.add(resource);
											}
										}
									}
								}
							}
						}
					}*/

					// DKM - shouldn't return
					//return;
				}
			}
			else
			{
				// recursively check the subdelta of this delta
				processDelta(child);
			}
		}
	}

	/**
	 * Synchronize a temporary file with it's corresponding remote file.
	 * First we need to determine what the corresponding remote file is.
	 * Then we need to compare timestamps to determine how to synchronize.
	 * 
	 * @param file the temporary file to synchronize
	 */
	protected void synchronizeTempWithRemote(IFile file, IProgressMonitor monitor)
	{
		// first determine associated remote file
		IPath path = file.getFullPath();
		int numSegments = path.segmentCount();

		// first we need to find the right RemoteFileSubSystem for the remote file
		SystemIFileProperties properties = new SystemIFileProperties(file);

		// before doing anything, check that the stored time stamp is not empty
		if (properties.getRemoteFileTimeStamp() == 0)
		{
			// we just downloaded this and that's why we hit an event
			return;
		}
		
		ISubSystem fs = null;

		// get the subsystem ID property from the temporary file
		String subsystemId = properties.getRemoteFileSubSystem();

		// the subsystem ID may not exist if the temporary file existed before this feature
		// to handle migration of this smoothly, we can use another method to determine the subsystem                         
		if (subsystemId != null)
		{
			ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
			fs = registry.getSubSystem(subsystemId);
		}

		if (fs != null)
		{
			// use the remote file path property of the temp file to determine the path of the remote file 
			// on the remote system
			String uploadPath = properties.getRemoteFilePath();

			// the uploadPath property may not exist if the temporary file existed before this feature
			// to handle migration of this smoothly, we can use another method to determine the remote file path
			if (uploadPath == null)
			{
				// derive the path from the temporary file path
				IRSESystemType systemType = fs.getHost().getSystemType();

				// on windows systems, we need to take into account drives and different separators
				boolean isWindows = systemType.isWindows();

				char fileSeparator = isWindows ? '\\' : '/';
				StringBuffer remotePath = new StringBuffer(""); //$NON-NLS-1$
				for (int i = 3; i < numSegments; i++)
				{
					if (i == 3)
					{
						if (!isWindows)
						{
							remotePath.append(fileSeparator);
						}
					}
					if (i > 3)
					{
						if (i == 4)
						{
							if (isWindows)
							{
								remotePath.append(":"); //$NON-NLS-1$
							}
						}

						remotePath.append(fileSeparator);
					}

					String seg = path.segment(i);
					remotePath.append(seg);
				}

				uploadPath = remotePath.toString();
			}

			// attempt the remote file synchronization      
			if (doesHandle(fs))
			{
				if (!fs.isOffline()){				
					// see if we're connected
					try
					{
						// check that the remote file system is connected
						// if not, attempt to connect to it
						if (!fs.isConnected())
						{
							// make sure we connect synchronously from here
							fs.connect(monitor, false);
						}
					}
					catch (Exception e)
					{
						// unable to connect to the remote server
						// do not attempt synchronization
						// instead, defer synchronization to later but allow user to edit
						// set the dirty flag to indicate that this file needs resynchronization
						properties.setDirty(true);
						
						// as per bug 256048 - comment#6 if we're not connected follow through to
						// doResourceSynchronization so we have the change to mark the SystemTextEditor dirty
					}
				}
				doResourceSynchronization(fs, file, uploadPath, monitor);		
			}
		}
	}

	protected void refreshRemoteResource(Object parent)
	{
		ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
		// refresh
		if (parent != null)
		{							
			registry.fireEvent(new SystemResourceChangeEvent(parent, ISystemResourceChangeEvents.EVENT_REFRESH, null));
			
	
			List filterReferences = registry.findFilterReferencesFor(parent, getLocalFileSubSystem());
			for (int i = 0; i < filterReferences.size(); i++)
			{
				ISystemFilterReference filterRef = (ISystemFilterReference)filterReferences.get(i);
				filterRef.markStale(true);
				registry.fireEvent(new SystemResourceChangeEvent(filterRef, ISystemResourceChangeEvents.EVENT_REFRESH, null));
			}
		}
	}

	
	/**
	 * Synchronize the specified remote file with the temporary local file using the 
	 * specified remote file subsystem.
	 * 
	 * @param subsystem the remote file subsystem of the remote file
	 * @param tempFile the temporary file
	 * @param resourceId the remote file
	 * @param monitor the progress monitor
	 */
	protected abstract void doResourceSynchronization(ISubSystem subsystem, IFile tempFile, String resourceId, IProgressMonitor monitor);

	/**
	 * Indicate whether this tempfile listener handles the specified
	 * @param subsystem the subsystem to check
	 * @return whether it handles this or not
	 */
	protected abstract boolean doesHandle(ISubSystem subsystem);

	private IRemoteFileSubSystem getLocalFileSubSystem()
	{
		ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
		IHost con = registry.getLocalHost();
		if (con != null)
		{
			return RemoteFileUtility.getFileSubSystem(con);
		}
		return null;
	}
	
	
	/**
	  * Prescan for changes that correspond the temp files project
	  */
	protected boolean preScanForTempFiles(IResourceDelta delta)
	{
		if (delta == null)
		{
			return true; // not sure when we'd get this
		}
		
		// does temp files exist
		if (!SystemRemoteEditManager.getInstance().doesRemoteEditProjectExist())
			return false;
		
		IResourceDelta[] subdeltas = delta.getAffectedChildren();
		for (int i = 0; i < subdeltas.length; i++)
		{
			IResource resource = subdeltas[i].getResource();
			if ((resource != null) && (resource.getType() == IResource.PROJECT))
			{
				if (resource.getName().equals(SystemRemoteEditManager.REMOTE_EDIT_PROJECT_NAME))
				{
					return true;
				}
			}
		}
		return false;
	}
	


}
