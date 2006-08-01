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

package org.eclipse.rse.files.ui.resources;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.files.ui.actions.SystemDownloadConflictAction;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.services.clientserver.SystemEncodingUtil;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IVirtualRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileIOException;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.view.ISystemEditableRemoteObject;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;


public class SystemEditableRemoteFile implements ISystemEditableRemoteObject, IPartListener, IResourceChangeListener, IResourceDeltaVisitor, ISystemTextEditorConstants, ISystemRemoteEditConstants
{


	private IRemoteFile remoteFile;
	private String remotePath;
	private IRemoteFileSubSystem subsystem;
	private String root;
	private String localPath;
	private IEditorPart editor;
	private IFile localFile;
	private IWorkbenchPage page;

	/**
	 * Internal class for downloading file
	 */
	private class InternalDownloadFileRunnable extends WorkspaceModifyOperation
	{

		private Exception e;
		private boolean completed = false;

		/**
		 * Constructor for InternalDownloadFileRunnable
		 */
		private InternalDownloadFileRunnable()
		{
		}

		/**
		 * @see WorkspaceModifyOperation#execute(IProgressMonitor)
		 */
		protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException
		{

			try
			{
				completed = SystemEditableRemoteFile.this.download(monitor);
				monitor.done();
			}
			catch (CoreException e)
			{
				throw e;
			}
			catch (InvocationTargetException e)
			{
				throw e;
			}
			catch (InterruptedException e)
			{
				// cancel was pressed by user
				monitor.setCanceled(true);
				throw e;
			}
			catch (Exception e)
			{
				SystemBasePlugin.logError("File can not be downloaded", e);
				this.e = e;
			}
		}

		public boolean didComplete()
		{
			return completed;
		}

		/**
		 * Get the exception that may have been thrown
		 */
		private void throwException() throws Exception
		{

			if (e != null)
			{
				throw e;
			}
		}
	};

	private String _editorId = null;
	private boolean _isRemoteFileMounted = false;
	private String _actualRemoteHost = null;
	private String _actualRemotePath = null;

	/**
	 * Constructor for SystemEditableRemoteFile
	 */
	public SystemEditableRemoteFile(IWorkbenchPage page, IRemoteFile remoteFile, String editorId)
	{
		super();
		this.page = page;
		this.remoteFile = remoteFile;
		this.remotePath = remoteFile.getAbsolutePath();		
		this.subsystem = remoteFile.getParentRemoteFileSubSystem();
		SystemRemoteEditManager mgr = SystemRemoteEditManager.getDefault();
		
		// if remote edit project doesn't exist, create it
		if (!mgr.doesRemoteEditProjectExist())
			mgr.getRemoteEditProject();
		
		this.root = mgr.getRemoteEditProjectLocation().makeAbsolute().toOSString();
		this.localPath = getDownloadPath();
		this._editorId = editorId;
	}

	/**
	 * Constructor for SystemEditableRemoteFile
	 */
	public SystemEditableRemoteFile(IRemoteFile remoteFile, String editorId)
	{
		this(null, remoteFile, editorId);
	}

	/**
	 * Constructor for SystemEditableRemoteFile
	 */
	public SystemEditableRemoteFile(IRemoteFile remoteFile)
	{
		super();
		this.remoteFile = remoteFile;
		this.remotePath = remoteFile.getAbsolutePath();
		this.subsystem = remoteFile.getParentRemoteFileSubSystem();
		this.root = SystemRemoteEditManager.getDefault().getRemoteEditProjectLocation().makeAbsolute().toOSString();
		this.localPath = getDownloadPath();

		// dkm - use registered
		String fileName = remoteFile.getName();
	
		IEditorRegistry registry = getEditorRegistry();
		
		IEditorDescriptor descriptor = registry.getDefaultEditor(fileName);
		if (descriptor == null)
		{
			descriptor = getDefaultTextEditor();		
		}
		String id = descriptor.getId();
		this._editorId = id;
	}
	
	protected IEditorRegistry getEditorRegistry()
	{
		if (PlatformUI.isWorkbenchRunning())
		{
			return PlatformUI.getWorkbench().getEditorRegistry();
		}
		return null;
	}
	
	protected IEditorDescriptor getDefaultTextEditor()
	{
		IEditorRegistry registry = getEditorRegistry();
		return registry.findEditor("org.eclipse.ui.DefaultTextEditor");
	}
	
	
	/**
	 * Returns an instance of this class given a local copy of a remote file.
	 * @param file the local file to create it from.
	 * @return 
	 */
	public static SystemEditableRemoteFile getInstance(IFile file) {
		
		// first determine associated remote file
		IPath path = file.getFullPath();
		int numSegments = path.segmentCount();

		// first we need to find the right RemoteFileSubSystem for the remote file
		SystemIFileProperties properties = new SystemIFileProperties(file);
		
		ISubSystem fs = null;

		// get the subsystem ID property from the temporary file
		String subsystemId = properties.getRemoteFileSubSystem();

		// the subsystem ID may not exist if the temporary file existed before this feature
		// to handle migration of this smoothly, we can use another method to determine the subsystem                         
		if (subsystemId != null)
		{
			ISystemRegistry registry = RSEUIPlugin.getTheSystemRegistry();
			fs = registry.getSubSystem(subsystemId);
		}

		if (fs != null)
		{
			// use the remote file path property of the temp file to determine the path of the remote file 
			// on the remote system
			String remotePath = properties.getRemoteFilePath();

			// the uploadPath property may not exist if the temporary file existed before this feature
			// to handle migration of this smoothly, we can use another method to determine the remote file path
			if (remotePath == null)
			{
				// derive the path from the temporary file path
				IHost connection = fs.getHost();

				// on windows systems, we need to take into account drives and different separators
				boolean isWindows = connection.getSystemType().equals("Local") || fs.getHost().getSystemType().equals("Windows");

				char fileSeparator = isWindows ? '\\' : '/';
				StringBuffer tempRemotePath = new StringBuffer("");
				for (int i = 3; i < numSegments; i++)
				{
					if (i == 3)
					{
						if (!isWindows)
						{
							tempRemotePath.append(fileSeparator);
						}
					}
					if (i > 3)
					{
						if (i == 4)
						{
							if (isWindows)
							{
								tempRemotePath.append(":");
							}
						}

						tempRemotePath.append(fileSeparator);
					}

					String seg = path.segment(i);
					tempRemotePath.append(seg);
				}

				remotePath = tempRemotePath.toString();
			}
			
			try {
				if (remotePath != null && fs instanceof IRemoteFileSubSystem) {
					IRemoteFile remoteFile = ((IRemoteFileSubSystem)fs).getRemoteFileObject(remotePath);
					
					if (remoteFile != null) {
						return new SystemEditableRemoteFile(remoteFile);
					}
					else {
						return null;
					}
				}
				else {
					return null;
				}
			}
			catch (SystemMessageException e) {
				SystemBasePlugin.logError("Error getting remote file object " + remotePath, e);
			}
		}
		
		return null;
	}

	/**
	 * Set the remote file
	 */
	public void setRemoteFile(IRemoteFile remoteFile)
	{	
		this.remoteFile = remoteFile;
		this.remotePath = remoteFile.getAbsolutePath();		
		this.subsystem = remoteFile.getParentRemoteFileSubSystem();
		this.localPath = getDownloadPath();
		this.localFile = null;
	}

	/**
	 * Get the remote file
	 */
	public IRemoteFile getRemoteFile()
	{
		return remoteFile;
	}

	/**
	 * Set the local path
	 */
	public void setLocalPath(String localPath)
	{
		this.localPath = localPath;
	}

	/**
	 * Get the local path
	 */
	public String getLocalPath()
	{
		return localPath;
	}

	/**
	 * Set the editor
	 */
	public void setEditor(IEditorPart editor)
	{
		this.editor = editor;
	}

	/**
	 * Get the editor
	 */
	public IEditorPart getEditor()
	{
		return editor;
	}

	/**
	 * Check if user has write authority to the file.
	 * @return true if the file is readonly
	 */
	public boolean isReadOnly()
	{

		if (!subsystem.isConnected())
		{
			try
			{
				subsystem.connect();
			}
			catch (Exception e)
			{
			}
		}

		return !remoteFile.canWrite();
	}

	/**
	 * Indicate whether the file can be edited
	 */
	public void setReadOnly(boolean isReadOnly)
	{
		if (isReadOnly)
		{
			setEditorAsReadOnly();
		}
		else if (editor instanceof ISystemTextEditor)
		{
			((ISystemTextEditor) editor).setReadOnly(false);
		}
	}

	/**
	 * Download the file.
	 * @param if the shell is null, no progress monitor will be shown
	 * @return true if successful, false if cancelled
	 */
	public boolean download(Shell shell) throws Exception
	{

		if (shell != null)
		{
			
			
			InternalDownloadFileRunnable downloadFileRunnable = new InternalDownloadFileRunnable();
			ProgressMonitorDialog pmd = new ProgressMonitorDialog(shell);
			IFile localFile = getLocalResource();
			SystemUniversalTempFileListener listener = SystemUniversalTempFileListener.getListener();
			listener.addIgnoreFile(localFile);
			
			pmd.run(false, true, downloadFileRunnable);
			
			listener.removeIgnoreFile(localFile);
			downloadFileRunnable.throwException();
			return downloadFileRunnable.didComplete();
		}
		else
		{
			return download((IProgressMonitor) null);
		}
	}
	
	protected void setReadOnly(IFile file, boolean flag)
	{
		ResourceAttributes attrs = file.getResourceAttributes();
		attrs.setReadOnly(flag);
		try
		{
			file.setResourceAttributes(attrs);
		}
		catch (CoreException e)
		{			
		}
	}

	/**
	 * Download the file.
	 * @param the progress monitor
	 * @return true if the operation was successful.  false if the user cancels.
	 */
	public boolean download(IProgressMonitor monitor) throws Exception
	{

		// DY:  check if the file exists and is read-only (because it was previously opened
		// in the system editor)
		IFile file = getLocalResource();
		SystemIFileProperties properties = new SystemIFileProperties(file);
		boolean newFile = !file.exists();
		if (file.isReadOnly())
		{
			setReadOnly(file, false);
		}
		properties.setReadOnly(false);

		// detect whether there exists a temp copy already
		if (!newFile && file.exists())
		{
			// we have a local copy of this file, so we need to compare timestamps

			// get stored modification stamp
			long storedModifiedStamp = properties.getRemoteFileTimeStamp();
 
			// get updated remoteFile so we get the current remote timestamp
			//remoteFile.markStale(true);
			remoteFile = subsystem.getRemoteFileObject(remoteFile.getAbsolutePath());

			// get the remote modified stamp
			long remoteModifiedStamp = remoteFile.getLastModified();

			// get dirty flag
			boolean dirty = properties.getDirty();

			boolean remoteNewer = (storedModifiedStamp != remoteModifiedStamp);
			if (dirty)
			{
				// we have a dirty file with pending changes
				// user may want to replace this with the remote file
				// here we prompt user ****	Prompt Dialog 2 or 3
				// 		1) replace pending changes with remote file
				//		2) open editor with our pending changes
				SystemDownloadConflictAction conflictAction = new SystemDownloadConflictAction(file, remoteNewer);
				Display.getDefault().syncExec(conflictAction);

				switch (conflictAction.getState())
				{
					case SystemDownloadConflictAction.CANCELLED :
						return false;
					case SystemDownloadConflictAction.OPEN_WITH_LOCAL :
						return true;
					case SystemDownloadConflictAction.REPLACE_WITH_REMOTE :
					default :
						return doDownload(properties, monitor);
				}
			}
			else
			{
				if (remoteNewer)
				{
					return doDownload(properties, monitor);
				}
				else
				{
					if (properties.getUsedBinaryTransfer() == remoteFile.isBinary())
					{
						// we already have same file, use the current file
						refresh();
					}
					else
					{
						// we transferred a different way last time, so we need to transfer again	
						return doDownload(properties, monitor);
					}
				}
			}
		}

		else
		{
			return doDownload(properties, monitor);
		}

		return true;
	}

	private boolean doDownload(SystemIFileProperties properties, IProgressMonitor monitor) throws Exception
	{
		// file hasn't been downloaded before, so do the download now
		/*		SystemMessage copyMessage = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COPYTHINGGENERIC_PROGRESS);
				copyMessage.makeSubstitution(remoteFile.getName());
				monitor.beginTask(copyMessage.getLevelOneText(), (int)remoteFile.getLength());
		*/
		if (!subsystem.isConnected())
		{
			// don't try to download file if not connected
			return false;	
		}
		
		subsystem.downloadUTF8(remoteFile, localPath, monitor);

		// get fresh remote file object
		remoteFile = subsystem.getRemoteFileObject(remoteFile.getAbsolutePath());

		refresh();

		// get fresh file object
		IFile file = getLocalResource();
		properties.setRemoteFileTimeStamp(remoteFile.getLastModified());
		properties.setDownloadFileTimeStamp(file.getLocalTimeStamp());
		properties.setDirty(false);
		properties.setUsedBinaryTransfer(remoteFile.isBinary());
		
	    // get the modified timestamp from the File, not the IFile
		// for some reason, the modified timestamp from the IFile does not always return
		// the right value. There is a Javadoc comment saying the value from IFile might be a
		// cached value and that might be the cause of the problem.
		properties.setDownloadFileTimeStamp(file.getLocation().toFile().lastModified());

		return true;
	}

	/**
	 * Saves the local file and uploads it to the host immediately, rather than, in response to a resource change
	 * event.
	 */
	public boolean doImmediateSaveAndUpload()
	{
		if (editor != null)
		{
			editor.doSave(null);
		}

		SystemUniversalTempFileListener listener = SystemUniversalTempFileListener.getListener();
		listener.setEnabled(false);

		IRemoteFile remoteFile = getRemoteFile();
		IFile tempFile = getLocalResource();
		IRemoteFileSubSystem fs = remoteFile.getParentRemoteFileSubSystem();
		SystemIFileProperties properties = new SystemIFileProperties(tempFile);

		// reget the remote file so that we have the right timestamps
		try
		{
			remoteFile = fs.getRemoteFileObject(remoteFile.getAbsolutePath());
		}
		catch (Exception e)
		{
			return false;
		}
		listener.upload(fs, remoteFile, tempFile, properties, properties.getRemoteFileTimeStamp(), this, null);

		listener.setEnabled(true);
		
		return !properties.getDirty();
		//return true;
	}

	/**
	 * Upload the file
	 */
	private void upload() throws Exception
	{

		if (!subsystem.isConnected())
		{
			subsystem.connect();
		}

		subsystem.uploadUTF8(localPath, remoteFile, null);

		// update timestamp
		IFile file = getLocalResource();
		SystemIFileProperties properties = new SystemIFileProperties(file);
		
		//DKM- saveAS fix
		remoteFile = subsystem.getRemoteFileObject(remoteFile.getAbsolutePath());
		properties.setRemoteFileTimeStamp(remoteFile.getLastModified());
	}

	/**
	 * Get the local resource
	 */
	public IFile getLocalResource()
	{
		/* DKM - don't use this because workspace can't always handle case-sensitivity
		System.out.println("getting file for " + localPath);
		File file = new File(localPath);
		System.out.println("file= " + file);
		try
		{
			String path = file.getCanonicalPath();
			return RSEUIPlugin.getWorkspaceRoot().getFileForLocation(new Path(path));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		*/
		if (localFile == null || !localFile.exists())
		{
			IPath path = getLocalPathObject();
			localFile = SystemBasePlugin.getWorkspaceRoot().getFileForLocation(path);
		}
		return localFile;
	}

	private IPath getLocalPathObject()
	{
		IPath actualPath = null;
		IPath expectedPath = new Path(localPath);

		IPath rootPath = new Path(root);
		IContainer container = SystemBasePlugin.getWorkspaceRoot().getContainerForLocation(rootPath);

		if (container != null && container.exists())
		{
			IResource lastMatch = null;
			for (int i = rootPath.segmentCount(); i < expectedPath.segmentCount(); i++)
			{
				String expectedFolderOrFile = expectedPath.segment(i).toLowerCase();
				IResource match = null;

				try
				{
					IResource[] resources = container.members();

					for (int r = 0; r < resources.length && match == null; r++)
					{
						IResource resource = resources[r];
						
						if (resource instanceof IContainer)
						{
							String resName = resource.getName().toLowerCase();
							if (expectedFolderOrFile.equals(resName))
							{
								match = resource;
								lastMatch = match;
							}
						}
						// file match - i.e. last file
						else if (i == expectedPath.segmentCount() - 1)
						{
						    String resName = resource.getName().toLowerCase();
						    if (expectedFolderOrFile.equals(resName))
						    {
						        //match = resource;
						        //lastMatch = match;
						        // we found the resource - need to continue
						        return resource.getLocation();
						    }
						}
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				if (match != null)
				{
					container = (IContainer) match;
				}
				else if (lastMatch != null)
				{
					IPath newPath = lastMatch.getLocation();
					while (i < expectedPath.segmentCount())
					{
						newPath = newPath.append(expectedPath.segment(i));
						i++;
					}

					return newPath;
				}
				else
				{
					return expectedPath;
				}
			}

			String fileName = expectedPath.segment(expectedPath.segmentCount() - 1);
			actualPath = container.getLocation().append(fileName);
			return actualPath;
		}

		return expectedPath;
	}

	/**
	 * Delete the local resource
	 */
	public void delete()
	{

		IPath rootPath = (new Path(root)).removeTrailingSeparator();

		String rootLocation = rootPath.makeAbsolute().toOSString();

		String resourceLocation = getLocalResource().getLocation().makeAbsolute().toOSString();

		File tempFile = new File(resourceLocation);

		boolean deleteResult = tempFile.delete();

		if (!deleteResult)
		{
			return;
		}

		while (resourceLocation.startsWith(rootLocation))
		{

			tempFile = tempFile.getParentFile();
			resourceLocation = tempFile.getAbsolutePath();

			if (resourceLocation.equals(rootLocation))
			{ // do not delete the root folder itself
				break;
			}

			deleteResult = tempFile.delete();

			if (!deleteResult)
			{
				break;
			}
		}

		// refresh after delete
		refresh();
	}

	/**
	 * Get the download path
	 */
	private String getDownloadPath()
	{

		IPath path = new Path(root);

		_actualRemoteHost = getActualHostFor(remotePath);

		// DKM - now we're using only the hostname to prefix the remote path.  Thus multiple connections to the same place will
		//       yield the temp files
		//path = path.append("/" + subsystem.getSystemProfileName() + "/" + subsystem.getSystemConnectionName() + "/");
		path = path.append("/" + _actualRemoteHost + "/");

		String absolutePath = getWorkspaceRemotePath(remotePath);

		int colonIndex = absolutePath.indexOf(IPath.DEVICE_SEPARATOR);

		if (colonIndex != -1)
		{

			if (colonIndex == 0)
			{
				absolutePath = absolutePath.substring(1);
			}
			else if (colonIndex == (absolutePath.length() - 1))
			{
				absolutePath = absolutePath.substring(0, colonIndex);
			}
			else
			{
				absolutePath = absolutePath.substring(0, colonIndex).toLowerCase() + absolutePath.substring(colonIndex + 1);
			}
		}

		// DY:  We should only be escaping the remote portion of the path
		IPath remote = new Path(absolutePath);
		absolutePath = SystemFileNameHelper.getEscapedPath(remote.toOSString());
		_actualRemotePath = absolutePath;

		//path = path.append(absolutePath);
		//return SystemFileNameHelper.getEscapedPath(path.makeAbsolute().toOSString());

		return path.makeAbsolute().toOSString() + absolutePath;
	}

	/**
	 * Gets the path to use in the workspace for saving the local replica remote file.  In most cases
	 * this path will be the same thing as the remote path however, this mechanism exists so that 3rd parties
	 * can customize where temp files are saved.
	 * 
	 * @param remotePath the absolute path to the resource on the host
	 * @return the workspace mapping of the remote path
	 */
	public String getWorkspaceRemotePath(String remotePath)
	{
		if (subsystem != null && subsystem.getHost().getSystemType().equals("Local"))
		{
			return SystemRemoteEditManager.getDefault().getWorkspacePathFor(subsystem.getHost().getHostName(), remotePath);
		}
		return remotePath;
	}

	public String getActualHostFor(String remotePath)
	{
		String hostname = subsystem.getHost().getHostName();
		if (subsystem != null && subsystem.getHost().getSystemType().equals("Local"))
		{
			String result = SystemRemoteEditManager.getDefault().getActualHostFor(hostname, remotePath);
			if (!result.equals(hostname))
			{
				_isRemoteFileMounted = true;
			}
			return result;
		}
		return hostname;	
	}
	

	/**
	 * Returns the open IEditorPart for this remote object if there is one.
	 */
	public IEditorPart getEditorPart()
	{
		return editor;
	}

	/**
	 * Returns the remote object that is editable
	 */
	public IAdaptable getRemoteObject()
	{
		return (IAdaptable) remoteFile;
	}

	/**
	 * Is the local file open in an editor
	 */
	public int checkOpenInEditor()
	{
		// first we check if it's open in the active page
		IWorkbenchPage activePage = this.page;
		IWorkbench wb = PlatformUI.getWorkbench();
		if (activePage == null)
		{
			IWorkbenchWindow activeWindow = wb.getActiveWorkbenchWindow();
			activePage = activeWindow.getActivePage();
		}

		IEditorReference[] activeReferences = activePage.getEditorReferences();

		IEditorPart part;
		java.io.File lFile = new java.io.File(localPath);

		for (int k = 0; k < activeReferences.length; k++)
		{

			// Need to think about whether to restore the editor here,
			// i.e. whether the argument to the getEditor() should be true
			part = activeReferences[k].getEditor(false);

			//DKM***if (part instanceof SystemTextEditor) 
			if (part != null)
			{

				IEditorInput editorInput = part.getEditorInput();

				if (editorInput instanceof IFileEditorInput)
				{
					IPath path = ((IFileEditorInput) editorInput).getFile().getLocation();
					java.io.File pathFile = new java.io.File(path.toOSString());

					if (pathFile.compareTo(lFile) == 0)
					{
						//if (path.makeAbsolute().toOSString().equalsIgnoreCase(localPath))
						//{
						return OPEN_IN_SAME_PERSPECTIVE;
					}
				}
			}
		}

		IWorkbenchWindow[] windows = wb.getWorkbenchWindows();

		for (int i = 0; i < windows.length; i++)
		{

			IWorkbenchPage[] pages = windows[i].getPages();

			for (int j = 0; j < pages.length; j++)
			{

				IEditorReference[] references = pages[j].getEditorReferences();

				if (pages[j] == activePage)
				{
					continue;
				}

				IEditorPart temp;

				for (int k = 0; k < references.length; k++)
				{

					// Need to think about whether to restore the editor here,
					// i.e. whether the argument to the getEditor() should be true
					temp = references[k].getEditor(false);

					IEditorInput editorInput = temp.getEditorInput();

					if (editorInput instanceof IFileEditorInput)
					{
						IPath path = ((IFileEditorInput) editorInput).getFile().getLocation();

						if (path.makeAbsolute().toOSString().equalsIgnoreCase(localPath))
						{
							return OPEN_IN_DIFFERENT_PERSPECTIVE;
						}
					}

				}
			}
		}

		return NOT_OPEN;
	}

	/**
	 * Open in editor
	 */
	public void open(Shell shell)
	{
		open(shell, false);
	}

	/**
	 * Open in editor
	 */
	public void open(Shell shell, boolean readOnly)
	{
		
		try
		{
			

			// first check if file is already open in an editor
			int result = checkOpenInEditor();
			// ensure the file is stale
			remoteFile.markStale(true, false);
			{
				remoteFile = remoteFile.getParentRemoteFileSubSystem().getRemoteFileObject(remoteFile.getAbsolutePath());
			}
			
			if (!remoteFile.exists())
			{
				SystemMessage message = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_ERROR_FILE_NOTFOUND);
				message.makeSubstitution(remotePath, subsystem.getHost().getHostName());
				SystemMessageDialog dialog = new SystemMessageDialog(shell, message);
				dialog.open();
				return;
			}
			
			if (result == NOT_OPEN)
			{
				if (readOnly)
				{
					if (download(shell))
					{
						setLocalResourceProperties();
						openEditor();
						setEditorAsReadOnly();
					}
				}
				else if (!isReadOnly())
				{ // we have write access
					if (download(shell))
					{
						addAsListener();
						setLocalResourceProperties();
						openEditor();
					}
				}
				else
				{ // we do not have write access

					IRemoteFile fakeRemoteFile = subsystem.getRemoteFileObject(remotePath);
					if (!fakeRemoteFile.exists())
					{ // this could be because file doesn't exist
						download(shell);
					}

					SystemMessage message = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_DOWNLOAD_NO_WRITE);
					message.makeSubstitution(remotePath, subsystem.getHost().getHostName());
					SystemMessageDialog dialog = new SystemMessageDialog(shell, message);

					boolean answer = dialog.openQuestion();

					if (answer)
					{
						if (download(shell))
						{
							setLocalResourceProperties();
							setReadOnly(getLocalResource(), true);
							openEditor();
							setEditorAsReadOnly();
						}
					}
				}
			}
			else if (result == OPEN_IN_SAME_PERSPECTIVE)
			{
				openEditor();
			}
			else if (result == OPEN_IN_DIFFERENT_PERSPECTIVE)
			{
				SystemMessage message = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_DOWNLOAD_ALREADY_OPEN_IN_EDITOR);
				message.makeSubstitution(remotePath, subsystem.getHost().getHostName());
				SystemMessageDialog dialog = new SystemMessageDialog(shell, message);

				boolean answer = dialog.openQuestion();

				if (answer)
				{
					openEditor();
					setEditorAsReadOnly(); // put editor in read only mode, but not file
				}
			}
		}
		catch (Exception e)
		{

			if (e instanceof InterruptedException)
			{
				// do nothing since user pressed cancel
			}
			else if (e instanceof SystemMessageException)
			{
				SystemMessageDialog dialog = new SystemMessageDialog(shell, ((SystemMessageException) e).getSystemMessage());
				dialog.open();
			}
			else
			{
				RemoteFileIOException exc = new RemoteFileIOException(e);
				SystemMessageDialog dialog = new SystemMessageDialog(shell, exc.getSystemMessage());
				dialog.open();
			}
		}
	}

	/**
	 * Open in system editor
	 */
	public void openInSystemEditor(Shell shell)
	{

		try
		{
			if (remoteFile.getSystemConnection().getSystemType().equals("Local"))
			{
				// Open local files "in-place", i.e. don't copy them to the 
				// RemoteSystemsTempFiles project first
				if (remoteFile instanceof IVirtualRemoteFile)
				{
					Program.launch(remoteFile.getAbsolutePath());
				}
				else
				{
					Program.launch(remotePath);
				}
				
			}
			else
			{
				download(shell);
				IFile file = getLocalResource();
				// DY:  set resource as read only when launching in external editor
				// because we do not get notified of save events (unless the user selects
				// "Refresh from local" on the project) and therefore cannot
				// push changes back to the server.
				setReadOnly(file, true);
				openSystemEditor();
			}
		}
		catch (Exception e)
		{

			if (e instanceof SystemMessageException)
			{
				SystemMessageDialog dialog = new SystemMessageDialog(shell, ((SystemMessageException) e).getSystemMessage());
				dialog.open();
			}
			else
			{
				RemoteFileIOException exc = new RemoteFileIOException(e);
				SystemMessageDialog dialog = new SystemMessageDialog(shell, exc.getSystemMessage());
				dialog.open();
			}
		}
	}
	
		/**
	 * Open in in place editor
	 */
	public void openInInPlaceEditor(Shell shell)
	{

		try
		{
				download(shell);
				IFile file = getLocalResource();
				// DY:  set resource as read only when launching in external editor
				// because we do not get notified of save events (unless the user selects
				// "Refresh from local" on the project) and therefore cannot
				// push changes back to the server.
				setReadOnly(file, true);
				openInPlaceEditor();
			
		}
		catch (Exception e)
		{

			if (e instanceof SystemMessageException)
			{
				SystemMessageDialog dialog = new SystemMessageDialog(shell, ((SystemMessageException) e).getSystemMessage());
				dialog.open();
			}
			else
			{
				RemoteFileIOException exc = new RemoteFileIOException(e);
				SystemMessageDialog dialog = new SystemMessageDialog(shell, exc.getSystemMessage());
				dialog.open();
			}
		}
	}


	/**
	 * Set local resource properties
	 */
	public void setLocalResourceProperties() throws CoreException
	{
		IFile file = getLocalResource();

		SystemIFileProperties properties = new SystemIFileProperties(file);

		String profileID = subsystem.getParentRemoteFileSubSystemConfiguration().getEditorProfileID();
		properties.setEditorProfileType(profileID);

		// need this to get a reference back to the object
		properties.setRemoteFileObject(this);

		// set remote properties
		ISystemRegistry registry = RSEUIPlugin.getTheSystemRegistry();
		String subSystemId = registry.getAbsoluteNameForSubSystem(subsystem);
		properties.setRemoteFileSubSystem(subSystemId);
		properties.setRemoteFilePath(remoteFile.getAbsolutePath());
		
		properties.setRemoteFileMounted(_isRemoteFileMounted);
		if (_isRemoteFileMounted)
		{
			properties.setResolvedMountedRemoteFileHost(_actualRemoteHost);
			properties.setResolvedMountedRemoteFilePath(_actualRemotePath);
		}
		
		// if we have an xml file, find the local encoding of the file
		SystemEncodingUtil util = SystemEncodingUtil.getInstance();
		String encoding = null;
		
		String tempPath = file.getLocation().toOSString();
		
		if (util.isXML(tempPath)) {
			
			try {
				encoding = util.getXMLFileEncoding(tempPath);
			}
			catch (IOException e) {
				IStatus s = new Status(IStatus.ERROR, RSEUIPlugin.PLUGIN_ID, IStatus.ERROR, e.getLocalizedMessage(), e);
				throw new CoreException(s);
			}
		}
		else {
			
			if (!properties.getUsedBinaryTransfer()) {
				encoding = SystemEncodingUtil.ENCODING_UTF_8;
			}
		}
		
		try
		{
			if (encoding != null) 
			{
				if (!file.getCharset().equals(encoding))
				{
					file.setCharset(encoding, null);
				}
			}
		}
		catch (Exception e)
		{			
		}
	}

	/**
	 * Register as listener for various events
	 */
	public void addAsListener()
	{
		try
		{
			if (!isReadOnly())
			{
				if (SystemBasePlugin.getActiveWorkbenchWindow() != null)
				{
					if (SystemBasePlugin.getActiveWorkbenchWindow().getActivePage() != null)
					{
						SystemBasePlugin.getActiveWorkbenchWindow().getActivePage().addPartListener(this);
					}
				}
				SystemUniversalTempFileListener.getListener().registerEditedFile(this);
			}
		}
		catch (Exception e)
		{
		}
	}

	/**
	 * Open the editor
	 */
	public void openEditor() throws PartInitException
	{
		IWorkbenchPage activePage = this.page;
		IWorkbench wb = PlatformUI.getWorkbench();
		if (activePage == null)
		{			
			activePage = wb.getActiveWorkbenchWindow().getActivePage();
		}
		IFile file = getLocalResource();
		
		// set editor as preferred editor for this file
		IDE.setDefaultEditor(file, _editorId);

		FileEditorInput finput = new FileEditorInput(file);
		
		// check for files already open

		// DKM - when _editorId is not lpex, this causes problem
		// DY - changed editor from SystemTextEditor to IEditorPart
		//editor = (SystemTextEditor)activePage.openEditor(file, _editorId);
		editor = activePage.openEditor(finput, _editorId);
	}

	/**
	 * Open the system editor
	 */
	private void openSystemEditor() throws PartInitException
	{
		IWorkbenchPage activePage = this.page;
		if (activePage == null)
		{			
			activePage = SystemBasePlugin.getActiveWorkbenchWindow().getActivePage();
		}
		IFile file = getLocalResource();
	
		IEditorRegistry registry = getEditorRegistry();
		FileEditorInput fileInput = new FileEditorInput(file);
		activePage.openEditor(fileInput, IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);

	}
	
	/**
	 * Open the in place editor
	 */
	private void openInPlaceEditor() throws PartInitException
	{
		IWorkbenchPage activePage = this.page;
		if (activePage == null)
		{			
			activePage = SystemBasePlugin.getActiveWorkbenchWindow().getActivePage();
		}
		IFile file = getLocalResource();
	
		IEditorRegistry registry = getEditorRegistry();
		FileEditorInput fileInput = new FileEditorInput(file);
		activePage.openEditor(fileInput, IEditorRegistry.SYSTEM_INPLACE_EDITOR_ID);

	}

	/**
	 * Set editor to read only
	 */
	private void setEditorAsReadOnly()
	{
		if (editor instanceof ISystemTextEditor)
		{
			((ISystemTextEditor) editor).setReadOnly(true);
		}
		IFile file = getLocalResource();

		SystemIFileProperties properties = new SystemIFileProperties(file);
		properties.setReadOnly(true);
	}

	/**
	 * Refresh
	 */
	private void refresh()
	{

		SystemRemoteEditManager.getDefault().refreshRemoteEditContainer(localFile.getParent());
	}

	/**
	 * @see IPartListener#partActivated(IWorkbenchPart)
	 */
	public void partActivated(IWorkbenchPart part)
	{
	}

	/**
	 * @see IPartListener#partBroughtToTop(IWorkbenchPart)
	 */
	public void partBroughtToTop(IWorkbenchPart part)
	{
	}

	/**
	 * @see IPartListener#partClosed(IWorkbenchPart)
	 */
	public void partClosed(IWorkbenchPart part)
	{
		/*
		        if (editor == part)
		        {
		            delete();
		        }
		*/
		SystemUniversalTempFileListener.getListener().unregisterEditedFile(this);

		IWorkbenchPage page = SystemBasePlugin.getActiveWorkbenchWindow().getActivePage();

		if (page != null)
		{
			page.removePartListener(this);
		}
	}

	/**
	 * @see IPartListener#partDeactivated(IWorkbenchPart)
	 */
	public void partDeactivated(IWorkbenchPart part)
	{
	}

	/**
	 * @see IPartListener#partOpened(IWorkbenchPart)
	 */
	public void partOpened(IWorkbenchPart part)
	{
	}

	/**
	 * @see IResourceChangeListener#resourceChanged(IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event)
	{
		/*
		if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
			
			IResourceDelta delta = event.getDelta();
			
			try {
				delta.accept(this);
			}
			catch (CoreException e) {
				RSEUIPlugin.logError("Error accepting delta", e);
				RemoteFileIOException exc = new RemoteFileIOException(e);
				SystemMessageDialog dialog = new SystemMessageDialog(RSEUIPlugin.getActiveWorkbenchShell(), exc.getSystemMessage());
				dialog.open();
			}
		}
		*/
	}

	/**
	 * @see IResourceDeltaVisitor#visit(IResourceDelta)
	 */
	public boolean visit(IResourceDelta delta) throws CoreException
	{

		if (delta.getKind() == IResourceDelta.CHANGED)
			process(delta);

		return true;
	}

	/**
	 * Process the resource delta
	 */
	private void process(IResourceDelta delta)
	{

		IResource resource = delta.getResource();

		try
		{

			if (resource.getLocation().equals(getLocalResource().getLocation()))
			{
				upload();
			}
		}
		catch (Exception e)
		{

			SystemBasePlugin.logError("Error uploading file", e);

			if (e instanceof SystemMessageException)
			{
				SystemMessageDialog dialog = new SystemMessageDialog(SystemBasePlugin.getActiveWorkbenchShell(), ((SystemMessageException) e).getSystemMessage());
				dialog.open();
			}
			else
			{
				RemoteFileIOException exc = new RemoteFileIOException(e);
				SystemMessageDialog dialog = new SystemMessageDialog(SystemBasePlugin.getActiveWorkbenchShell(), exc.getSystemMessage());
				dialog.open();
			}
		}
	}

	/**
	 * Save as to a remote location
	 */
	public boolean saveAs(IRemoteFile newRemoteFile, IProgressMonitor progressMonitor)
	{
		if (editor == null)
		{
			try
			{
				openEditor();
			}
			catch (Exception e)
			{
			}
		}

		final IDocumentProvider documentProvider = ((ITextEditor) editor).getDocumentProvider();

		SystemEditableRemoteFile tempFile = new SystemEditableRemoteFile(newRemoteFile);

		IFile newFile = tempFile.getLocalResource();

		if (newFile.getLocation().equals(getLocalResource().getLocation()))
		{

			if (editor != null)
			{
				editor.doSave(progressMonitor);
				return true;
			}
			else
			{
				return true;
			}
		}

		final IFileEditorInput newInput = new FileEditorInput(newFile);

		WorkspaceModifyOperation op = new WorkspaceModifyOperation()
		{

			public void execute(final IProgressMonitor monitor) throws CoreException
			{
				documentProvider.saveDocument(monitor, newInput, documentProvider.getDocument(editor.getEditorInput()), true);
			}
		};

		boolean success = false;

		if (editor instanceof ISystemTextEditor)
		{
			documentProvider.aboutToChange(newInput);
			ISystemTextEditor systemEditor = (ISystemTextEditor) editor;
			systemEditor.refresh();
			try
			{
				new ProgressMonitorDialog(SystemBasePlugin.getActiveWorkbenchShell()).run(false, true, op);
				success = true;
			}
			catch (InterruptedException e)
			{
			}
			catch (InvocationTargetException e)
			{
				SystemBasePlugin.logError("Error in performSaveAs", e);
				SystemMessage message = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_ERROR_UNEXPECTED);
				SystemMessageDialog dialog = new SystemMessageDialog(SystemBasePlugin.getActiveWorkbenchShell(), message);
				dialog.open();
				return true;
			}
			finally
			{
				documentProvider.changed(newInput);

				if (success)
				{
					systemEditor.setInput(newInput);

					// Delete the local resource associated with this object
					this.delete();

					// change properties of this object to the new file now
					this.setRemoteFile(newRemoteFile);

					try
					{
						this.setLocalResourceProperties();
						this.upload();
					}
					catch (Exception e)
					{
						SystemBasePlugin.logError("Error in performSaveAs", e);
						SystemMessage message = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_ERROR_UNEXPECTED);
						SystemMessageDialog dialog = new SystemMessageDialog(SystemBasePlugin.getActiveWorkbenchShell(), message);
						dialog.open();
						return true;
					}
				}
			}
		}

		if (progressMonitor != null)
		{
			progressMonitor.setCanceled(!success);
		}

		return true;
	}

	public void updateDirtyIndicator()
	{
		//  for lpex dirty indicator
		if (editor != null)
		{
			if (editor instanceof ISystemTextEditor)
			{
				((ISystemTextEditor) editor).updateDirtyIndicator();
			}
		}
	}

	public boolean isDirty()
	{
		if (editor != null)
			return editor.isDirty();
		return false;
	}
	

	public String getAbsolutePath() {
		return remotePath;
	}

	public ISubSystem getSubSystem() {
		return subsystem;
	}
	
	public boolean exists()
	{
		return remoteFile.exists();
	}

	public boolean isStale()
	{
		return remoteFile.isStale();
	}

}