/*******************************************************************************
 * Copyright (c) 2002, 2013 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 * Martin Oberhuber (Wind River) - [183824] Forward SystemMessageException from IRemoteFileSubsystem
 * Martin Oberhuber (Wind River) - [186640] Add IRSESystemType.testProperty()
 * Martin Oberhuber (Wind River) - [186128][refactoring] Move IProgressMonitor last in public base classes
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Martin Oberhuber (Wind River) - [189130] Move SystemIFileProperties from UI to Core
 * David McKnight   (IBM)        - [187130] New Folder/File, Move and Rename should be available for read-only folders
 * Kevin Doyle      (IBM) 		 - [197976] Changing a file to read-only when it is open doesn't update local copy
 * David McKnight   (IBM)        - [186363] get rid of obsolete calls to ISubSystem.connect()
 * David McKnight   (IBM)        - [209660] check for changed encoding before using cached file
 * David McKnight   (IBM)        - [210812] for text transfer, need to tell editor to use local encoding
 * Xuan Chen        (IBM)        - [210816] Archive testcases throw ResourceException if they are run in batch
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 * David McKnight   (IBM)        - [222406] Need to be able to override local encoding
 * David McKnight   (IBM)        - [224377] "open with" menu does not have "other" option
 * Kevin Doyle		(IBM)		 - [224162] SystemEditableRemoteFile.saveAs does not work because FileServiceSubSytem.upload does invalid check
 * David McKnight (IBM) 		 - [225747] [dstore] Trying to connect to an "Offline" system throws an NPE
 * David McKnight     (IBM)      - [229610] [api] File transfers should use workspace text file encoding
 * David McKnight   (IBM)        - [235221] Files truncated on exit of Eclipse
 * David McKnight   (IBM)        - [247189] SystemEditableRemoteFile.openEditor() not updating the default editor properly
 * David McKnight   (IBM)        - [249544] Save conflict dialog appears when saving files in the editor
 * David McKnight   (IBM)        - [267247] Wrong encoding
 * David McKnight   (IBM)        - [272772] Exception handling in SystemEditableRemoteFile
 * David McKnight   (IBM)        - [284420] nullprogressmonitor is needed
 * David McKnight   (IBM)        - [310215] SystemEditableRemoteFile.open does not behave as expected
 * David McKnight   (IBM)        - [324519] SystemEditableRemoteFile throws NPE when used in headless mode
 * David McKnight   (IBM)        - [325502] The default editor for a file is not updated when opened in RSE explorer
 * David McKnight   (IBM)        - [334839] File Content Conflict is not handled properly
 * David McKnight   (IBM)        - [249031] Last used editor should be set to SystemEditableRemoteFile
 * David McKnight   (IBM)        - [359704] SystemEditableRemoteFile does not release reference to editor
 * Rick Sawyer      (IBM)        - [376535] RSE does not respect editor overrides
 * David McKnight   (IBM)        - [357111] [DSTORE]File with invalid characters can't be opened in editor
 * David McKnight   (IBM)        - [385420] double-click to open System editor from Remote Systems view not working
 * David McKnight   (IBM)        - [385416] NPE during shutdown with remote editor open
 * David McKnight   (IBM)        - [390609] Cached file opened twice in case of eclipse linked resource..
 * Xuan Chen        (IBM)        - [399101] RSE edit actions on local files that map to actually workspace resources should not use temp files
 * Xuan Chen        (IBM)        - [399752] Cannot download remote file due to scoping rule
 * David McKnight   (IBM)        - [412571] Truncate file when compare file in workspace by Remote System Explorer
 *******************************************************************************/

package org.eclipse.rse.files.ui.resources;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.files.ui.Activator;
import org.eclipse.rse.internal.files.ui.FileResources;
import org.eclipse.rse.internal.files.ui.ISystemFileConstants;
import org.eclipse.rse.internal.files.ui.actions.SystemDownloadConflictAction;
import org.eclipse.rse.internal.files.ui.resources.SystemFileNameHelper;
import org.eclipse.rse.internal.files.ui.resources.SystemRemoteEditManager;
import org.eclipse.rse.internal.subsystems.files.core.model.SystemFileTransferModeMapping;
import org.eclipse.rse.internal.subsystems.files.core.model.SystemFileTransferModeRegistry;
import org.eclipse.rse.services.clientserver.SystemEncodingUtil;
import org.eclipse.rse.services.clientserver.messages.CommonMessages;
import org.eclipse.rse.services.clientserver.messages.ICommonMessageIds;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.RemoteFileIOException;
import org.eclipse.rse.subsystems.files.core.SystemIFileProperties;
import org.eclipse.rse.subsystems.files.core.model.ISystemFileTransferModeMapping;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IVirtualRemoteFile;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
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
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class SystemEditableRemoteFile implements ISystemEditableRemoteObject, IPartListener, IResourceChangeListener, IResourceDeltaVisitor
{
	private IRemoteFile remoteFile;
	private String remotePath;
	private IRemoteFileSubSystem subsystem;
	private String root;
	private String localPath;
	private IEditorPart editor;
	private IFile localFile;
	private IWorkbenchPage page;
	private boolean _usingDefaultDescriptor = false;

	/**
	 * Internal class for downloading file
	 */
	private class InternalDownloadFileRunnable extends Job
	//extends WorkspaceModifyOperation
	{

		private Exception e;
		private boolean completed = false;
		private boolean failed = false;

		/**
		 * Constructor for InternalDownloadFileRunnable
		 */
		private InternalDownloadFileRunnable()
		{
			super("Download"); // TODO - need to externalize //$NON-NLS-1$
		}

		/**
		 *
		 */
		protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException
		{

			try
			{
				failed = !SystemEditableRemoteFile.this.download(monitor);
				completed = true;
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
				SystemBasePlugin.logError("File can not be downloaded", e); //$NON-NLS-1$
				this.e = e;
			}
		}

		public boolean didComplete()
		{
			return completed;
		}

		public boolean didFail()
		{
			return failed;
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

		public IStatus run(IProgressMonitor monitor) {
			try
			{
				execute(monitor);
			}
			catch (Exception e)
			{
				return Status.CANCEL_STATUS;
			}
			return Status.OK_STATUS;
		}
	}

	private IEditorDescriptor _editorDescriptor = null;
	private boolean _isRemoteFileMounted = false;
	private String _actualRemoteHost = null;
	private String _actualRemotePath = null;

	/**
	 * Constructor for SystemEditableRemoteFile
	 *
	 * @since 3.0 changed String editorId into IEditorDescriptor
	 */
	public SystemEditableRemoteFile(IWorkbenchPage page, IRemoteFile remoteFile, IEditorDescriptor editorDescriptor)
	{
		super();
		this.page = page;
		this.remoteFile = remoteFile;
		this.remotePath = remoteFile.getAbsolutePath();
		this.subsystem = remoteFile.getParentRemoteFileSubSystem();
		SystemRemoteEditManager mgr = SystemRemoteEditManager.getInstance();

		// if remote edit project doesn't exist, create it
		if (!mgr.doesRemoteEditProjectExist())
			mgr.getRemoteEditProject();

		this.root = mgr.getRemoteEditProjectLocation().makeAbsolute().toOSString();
		this.localPath = getDownloadPath();
		this._editorDescriptor = editorDescriptor;
	}

	/**
	 * Constructor for SystemEditableRemoteFile
	 * 
	 * @since 3.0 changed String editorId into IEditorDescriptor
	 */
	public SystemEditableRemoteFile(IRemoteFile remoteFile, IEditorDescriptor editorDescriptor)
	{
		this(null, remoteFile, editorDescriptor);
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
		this.root = SystemRemoteEditManager.getInstance().getRemoteEditProjectLocation().makeAbsolute().toOSString();
		this.localPath = getDownloadPath();

		IFile localResource = getLocalResource();
		
		// first look for editor corresponding to this particular file
		IEditorDescriptor descriptor = null;
		try {
			descriptor = IDE.getEditorDescriptor(localResource);
			
			if (!localResource.exists()){
				_usingDefaultDescriptor = true;
			}
		} catch (PartInitException e) {	
		}	
		
		if (descriptor == null){
			descriptor = getDefaultTextEditor();
		}
			
		this._editorDescriptor = descriptor;		
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
		return registry.findEditor("org.eclipse.ui.DefaultTextEditor"); //$NON-NLS-1$
	}


	/**
	 * Returns an instance of this class given a local copy of a remote file.
	 * @param file the local file to create it from.
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
			ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
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
				IRSESystemType systemType = fs.getHost().getSystemType();

				// on windows systems, we need to take into account drives and different separators
				boolean isWindows = systemType.isWindows();

				char fileSeparator = isWindows ? '\\' : '/';
				StringBuffer tempRemotePath = new StringBuffer(""); //$NON-NLS-1$
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
								tempRemotePath.append(":"); //$NON-NLS-1$
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
					IRemoteFile remoteFile = ((IRemoteFileSubSystem)fs).getRemoteFileObject(remotePath, new NullProgressMonitor());

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
				SystemBasePlugin.logError("Error getting remote file object " + remotePath, e); //$NON-NLS-1$
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
	 * @return true if the file is read-only
	 */
	public boolean isReadOnly()
	{

		if (!subsystem.isConnected())
		{
			try
			{
				if (Display.getCurrent() == null) {
					subsystem.connect(new NullProgressMonitor(), false);
				} else {
					subsystem.connect(false, null);
				}
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
			setFileAsReadOnly(); // added since setEditorAsReadOnly() no longer sets the file properties
		}
		else
		{
			if (editor instanceof ISystemTextEditor) {
				((ISystemTextEditor) editor).setReadOnly(false);
			}
			IFile file = getLocalResource();
			setReadOnly(file, false);
			SystemIFileProperties properties = new SystemIFileProperties(file);
			properties.setReadOnly(false);
		}
	}

	/**
	 * Download the file.
	 * @param shell if the shell is null, no progress monitor will be shown
	 * @return true if successful, false if cancelled
	 */
	public boolean download(Shell shell) throws Exception
	{

		if (shell != null)
		{


			InternalDownloadFileRunnable downloadFileRunnable = new InternalDownloadFileRunnable();
			//ProgressMonitorDialog pmd = new ProgressMonitorDialog(shell);
			IFile localFile = getLocalResource();
			SystemUniversalTempFileListener listener = SystemUniversalTempFileListener.getListener();
			listener.addIgnoreFile(localFile);

			//pmd.run(false, true, downloadFileRunnable);
			//Probably in the early day of eclipse, we need to explicitly set those schedule rule.  But now, since the change of eclipse scheduling rule, 
			//those rule settings are not necessary, and will probable also cause problem.
			//Removed the explicit rule setting fixed the problem.
			//downloadFileRunnable.setRule(getRemoteFile());
			downloadFileRunnable.schedule();
			Display display = Display.getDefault();
			try {
				while (!downloadFileRunnable.didComplete())
				{
					while (display!=null && display.readAndDispatch()) {
						//Process everything on event queue
					}
					if (!downloadFileRunnable.didComplete()) Thread.sleep(200);
				}
			} catch(InterruptedException e) {
				/*stop waiting*/
			}

			listener.removeIgnoreFile(localFile);
			downloadFileRunnable.throwException();
			return !downloadFileRunnable.didFail();
		}
		else
		{
			return download(new NullProgressMonitor());
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
	 * @param monitor the progress monitor
	 * @return true if the operation was successful.  false if the user cancels.
	 */
	public boolean download(IProgressMonitor monitor) throws Exception
	{
		// DY:  check if the file exists and is read-only (because it was previously opened
		// in the system editor)
		IFile file = getLocalResource();
		IProject rseTempFilesProject = SystemRemoteEditManager.getInstance().getRemoteEditProject();
		
		// Don't download files that are not in temp files project.
		// With bug 399101, local RSE files that map to workspace project files 
		//   no longer get downloaded to the temp files project.
		if (file.exists() && !file.getProject().equals(rseTempFilesProject)){
			return true;
		}
		
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
			remoteFile = subsystem.getRemoteFileObject(remoteFile.getAbsolutePath(), monitor);

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
					String encoding = properties.getEncoding();
					if (properties.getUsedBinaryTransfer() == remoteFile.isBinary() &&
							encoding != null && encoding.equals(remoteFile.getEncoding()) // changed encodings matter too
					)
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

		try
		{
			subsystem.download(remoteFile, localPath, remoteFile.getEncoding(), monitor);
		}
		catch (SystemMessageException e)
		{
			if (remoteFile.isText() && specializeFile(true)){ // turn the file into binary type to allow for binary transfer
				// try again
				try
				{
					subsystem.download(remoteFile, localPath, remoteFile.getEncoding(), monitor);
				}
				catch (SystemMessageException ex){
					specializeFile(false); // turn it back to text mode
					SystemMessageDialog.displayMessage(e); // throw original exception
					return false;
				}
			}
			else {
				SystemMessageDialog.displayMessage(e);
				return false;
			}
		}

		if (monitor.isCanceled())
		{
			return false;
		}

		remoteFile = subsystem.getRemoteFileObject(remoteFile.getAbsolutePath(), monitor);

		refresh();

		// get fresh file object
		IFile file = getLocalResource();
		properties.setRemoteFileTimeStamp(remoteFile.getLastModified());
		properties.setDownloadFileTimeStamp(file.getLocalTimeStamp());
		properties.setDirty(false);
		properties.setUsedBinaryTransfer(remoteFile.isBinary());

		boolean readOnly = !remoteFile.canWrite();
		properties.setReadOnly(readOnly);

	    // get the modified timestamp from the File, not the IFile
		// for some reason, the modified timestamp from the IFile does not always return
		// the right value. There is a Javadoc comment saying the value from IFile might be a
		// cached value and that might be the cause of the problem.
		properties.setDownloadFileTimeStamp(file.getLocation().toFile().lastModified());

		return true;
	}

	private boolean specializeFile(boolean binary){
		String fname = remoteFile.getName();
		int dotIndex = fname.lastIndexOf('.');				
		String name = null;
		String extension = null;
		if (dotIndex > 0){
			name = fname.substring(0, dotIndex);
			extension = fname.substring(dotIndex+1);
		}
		else {
			name = fname;
		}
		
		if (name == null){
			return false;
		}
		
		// make this file binary, and try again
		SystemFileTransferModeRegistry reg = (SystemFileTransferModeRegistry)RemoteFileUtility.getSystemFileTransferModeRegistry();
		SystemFileTransferModeMapping[] newMappings = null;
		ISystemFileTransferModeMapping[] mappings = reg.getModeMappings();
		
		SystemFileTransferModeMapping mapping = null;
		for (int m = 0; m < mappings.length && mapping == null; m++){
			ISystemFileTransferModeMapping map = mappings[m];
			if (name.equals(map.getName())){
				String ext = map.getExtension();
				if (extension != null && extension.equals(ext)){
					mapping = (SystemFileTransferModeMapping)map;
				}
				else if (extension == ext){
					mapping = (SystemFileTransferModeMapping)map;
				}
			}
		}
		if (mapping != null){
			newMappings = new SystemFileTransferModeMapping[mappings.length];
			for (int i = 0; i < mappings.length; i++) {
				newMappings[i] = (SystemFileTransferModeMapping)mappings[i];
			}
		}		
		else {
			newMappings = new SystemFileTransferModeMapping[mappings.length + 1];				
			int i = 0;
			for (; i < mappings.length; i++) {
				newMappings[i] = (SystemFileTransferModeMapping)mappings[i];
			}
	
			newMappings[i] = mapping = new SystemFileTransferModeMapping(name, extension);
		}
		if (binary){
			mapping.setAsBinary();
		}
		else {
			mapping.setAsText();
		}
		
		reg.setModeMappings(newMappings);
		reg.saveAssociations();	
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
			remoteFile.markStale(true); // as per bug 249544, we should mark stale to ensure we get a fresh copy
			remoteFile = fs.getRemoteFileObject(remoteFile.getAbsolutePath(), new NullProgressMonitor());
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
	private void upload(IProgressMonitor monitor) throws Exception
	{

		if (!subsystem.isConnected())
		{
			if (Display.getCurrent() == null) {
				subsystem.connect(new NullProgressMonitor(), false);
			} else {
				subsystem.connect(false, null);
			}
		}

		IFile file = getLocalResource();
		String srcEncoding = RemoteFileUtility.getSourceEncoding(file);
			
		subsystem.upload(localPath, remoteFile, srcEncoding, monitor);

		// update timestamp
		SystemIFileProperties properties = new SystemIFileProperties(file);

		//DKM- saveAS fix
		remoteFile = subsystem.getRemoteFileObject(remoteFile.getAbsolutePath(), monitor);
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
		//If this remote file is actually local, and it is part of a project in this workspace, just return the absolute path of the remote file.
		IFile file = null;
		if (remoteFile.getHost().getSystemType().isLocal())
		{
			String absolutePath = remoteFile.getAbsolutePath();
			file = getProjectFileForLocation(absolutePath);
		}
		if (file != null) {
			return remotePath;
		}

		IPath path = new Path(root);

		_actualRemoteHost = getActualHostFor(remotePath);

		// DKM - now we're using only the hostname to prefix the remote path.  Thus multiple connections to the same place will
		//       yield the temp files
		//path = path.append("/" + subsystem.getSystemProfileName() + "/" + subsystem.getSystemConnectionName() + "/");
		path = path.append("/" + _actualRemoteHost + "/"); //$NON-NLS-1$  //$NON-NLS-2$

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

		String os = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
		if (!os.startsWith("win")) //$NON-NLS-1$
			absolutePath = absolutePath.replace('\\', '/');

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
		if (subsystem != null)
		{
			return SystemRemoteEditManager.getInstance().getWorkspacePathFor(subsystem.getHost().getHostName(), remotePath, subsystem);
		}
		return remotePath;
	}

	public String getActualHostFor(String remotePath)
	{
		String hostname = subsystem.getHost().getHostName();
		if (subsystem != null
		//DKM		&& subsystem.getHost().getSystemType().isLocal()
				)
		{
			String result = SystemRemoteEditManager.getInstance().getActualHostFor(hostname, remotePath, subsystem);
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
			if (activeWindow != null)
			{
				activePage = activeWindow.getActivePage();
			}
			else
			{
				IWorkbenchWindow[] windows  = wb.getWorkbenchWindows();
				if (windows != null && windows.length > 0)
				{
					activePage = windows[0].getActivePage();
				}
			}
		}
		
		if (activePage == null){
			return NOT_OPEN;
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
					IFile file = ((IFileEditorInput) editorInput).getFile();
					IPath path = file.getLocation();
					if (path!=null && lFile.compareTo(new java.io.File(path.toOSString()))==0) {
						if (!file.isLinked()){ // linked resources need to be treated differently						
							//if (path.makeAbsolute().toOSString().equalsIgnoreCase(localPath))
							return OPEN_IN_SAME_PERSPECTIVE;
						}
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

						if (path!=null && path.makeAbsolute().toOSString().equalsIgnoreCase(localPath))
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
				remoteFile = remoteFile.getParentRemoteFileSubSystem().getRemoteFileObject(remoteFile.getAbsolutePath(), new NullProgressMonitor());
			}

			if (!remoteFile.exists())
			{
				String msgTxt = NLS.bind(FileResources.MSG_ERROR_FILE_NOTFOUND, remotePath, subsystem.getHost().getHostName());
				SystemMessage message = new SimpleSystemMessage(Activator.PLUGIN_ID,
						ISystemFileConstants.MSG_ERROR_FILE_NOTFOUND,
						IStatus.ERROR, msgTxt);

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
						setFileAsReadOnly();
						openEditor(remoteFile, readOnly);
						setEditorAsReadOnly();
					}
				}
				else if (!isReadOnly())
				{ // we have write access
					if (download(shell))
					{
						addAsListener();
						setLocalResourceProperties();
						openEditor(remoteFile, readOnly);
					}
				}
				else
				{ // we do not have write access

					IRemoteFile fakeRemoteFile = subsystem.getRemoteFileObject(remotePath, new NullProgressMonitor());
					if (!fakeRemoteFile.exists())
					{ // this could be because file doesn't exist
						download(shell);
					}

					String msgTxt = NLS.bind(FileResources.MSG_DOWNLOAD_NO_WRITE, remotePath, subsystem.getHost().getHostName());
					String msgDetails = NLS.bind(FileResources.MSG_DOWNLOAD_NO_WRITE_DETAILS, remotePath, subsystem.getHost().getHostName());
					SystemMessage message = new SimpleSystemMessage(Activator.PLUGIN_ID,
							ISystemFileConstants.MSG_DOWNLOAD_NO_WRITE,
							IStatus.WARNING, msgTxt, msgDetails);
					SystemMessageDialog dialog = new SystemMessageDialog(shell, message);

					boolean answer = dialog.openQuestion();

					if (answer)
					{
						if (download(shell))
						{
							setLocalResourceProperties();
							setFileAsReadOnly();
							openEditor(remoteFile, readOnly);
							setEditorAsReadOnly();
						}
					}
				}
			}
			else if (result == OPEN_IN_SAME_PERSPECTIVE)
			{
				openEditor(remoteFile, readOnly);
			}
			else if (result == OPEN_IN_DIFFERENT_PERSPECTIVE)
			{
				String msgTxt = NLS.bind(FileResources.MSG_DOWNLOAD_ALREADY_OPEN_IN_EDITOR, remotePath, subsystem.getHost().getHostName());
				String msgDetails = NLS.bind(FileResources.MSG_DOWNLOAD_ALREADY_OPEN_IN_EDITOR_DETAILS, remotePath, subsystem.getHost().getHostName());
				SystemMessage message = new SimpleSystemMessage(Activator.PLUGIN_ID,
						ISystemFileConstants.MSG_DOWNLOAD_ALREADY_OPEN_IN_EDITOR,
						IStatus.WARNING, msgTxt, msgDetails);

				SystemMessageDialog dialog = new SystemMessageDialog(shell, message);

				boolean answer = dialog.openQuestion();

				if (answer)
				{
					setFileAsReadOnly();
					openEditor(remoteFile, readOnly);
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
	 * Open in editor
	 */
	public void open(IProgressMonitor monitor)
	{
		open(false, monitor);
	}

	/**
	 * Open in editor
	 */
	public void open(boolean readOnly, IProgressMonitor monitor)
	{

		try
		{

			// ensure the file is stale
			remoteFile.markStale(true, false);
			{
				remoteFile = remoteFile.getParentRemoteFileSubSystem().getRemoteFileObject(remoteFile.getAbsolutePath(), monitor);
			}

			if (!remoteFile.exists())
			{
				String msgTxt = NLS.bind(FileResources.MSG_ERROR_FILE_NOTFOUND, remotePath, subsystem.getHost().getHostName());
				SystemMessage message = new SimpleSystemMessage(Activator.PLUGIN_ID,
						ISystemFileConstants.MSG_ERROR_FILE_NOTFOUND,
						IStatus.ERROR, msgTxt);

				DisplayMessageDialog dd = new DisplayMessageDialog(message);
				Display.getDefault().syncExec(dd);
				return;
			}

			// assumption is that editor is not open

				if (readOnly)
				{
					if (download(monitor))
					{
						setLocalResourceProperties();
						setFileAsReadOnly();
						openEditor(remoteFile, readOnly);
						setEditorAsReadOnly();
					}
				}
				else if (!isReadOnly())
				{ // we have write access
					if (download(monitor))
					{
						addAsListener();
						setLocalResourceProperties();
						openEditor(remoteFile, readOnly);
					}
				}
				else
				{ // we do not have write access

					IRemoteFile fakeRemoteFile = subsystem.getRemoteFileObject(remotePath, monitor);
					if (!fakeRemoteFile.exists())
					{ // this could be because file doesn't exist
						download(monitor);
					}

					String msgTxt = NLS.bind(FileResources.MSG_DOWNLOAD_NO_WRITE, remotePath, subsystem.getHost().getHostName());
					String msgDetails = NLS.bind(FileResources.MSG_DOWNLOAD_NO_WRITE_DETAILS, remotePath, subsystem.getHost().getHostName());
					SystemMessage message = new SimpleSystemMessage(Activator.PLUGIN_ID,
							ISystemFileConstants.MSG_DOWNLOAD_NO_WRITE,
							IStatus.WARNING, msgTxt, msgDetails);

					DisplayQuestionDialog dd = new DisplayQuestionDialog(message);
					Display.getDefault().syncExec(dd);
					boolean answer = dd.getResponse();


					if (answer)
					{
						if (download(monitor))
						{
							setLocalResourceProperties();
							setFileAsReadOnly();
							openEditor(remoteFile, readOnly);
							setEditorAsReadOnly();
						}
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
				DisplayMessageDialog dd = new DisplayMessageDialog(((SystemMessageException)e).getSystemMessage());
				Display.getDefault().syncExec(dd);
			}
			else
			{
				RemoteFileIOException exc = new RemoteFileIOException(e);
				DisplayMessageDialog dd = new DisplayMessageDialog(exc.getSystemMessage());
				Display.getDefault().syncExec(dd);
			}
		}
		}


	public class DisplayMessageDialog implements Runnable
	{
		protected SystemMessage _msg;
		public DisplayMessageDialog(SystemMessage msg)
		{
			_msg = msg;
		}

		public void run()
		{
			SystemMessageDialog dialog = new SystemMessageDialog(SystemBasePlugin.getActiveWorkbenchShell(), _msg);
			dialog.open();
		}
	}

	public class DisplayQuestionDialog implements Runnable
	{
		protected SystemMessage _msg;
		public boolean _responce = false;
		public DisplayQuestionDialog(SystemMessage msg)
		{
			_msg = msg;
		}

		public boolean getResponse()
		{
			return _responce;
		}

		public void run()
		{
			SystemMessageDialog dialog = new SystemMessageDialog(SystemBasePlugin.getActiveWorkbenchShell(), _msg);
			try
			{
				_responce = dialog.openQuestion();
			}
			catch (Exception e)
			{

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
			if (remoteFile.getHost().getSystemType().isLocal())
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
		ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
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
		String encoding = remoteFile.getEncoding();
		properties.setEncoding(encoding);

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


		try
		{
			if (encoding != null)
			{
				if (remoteFile.isBinary()){
					if (!file.isSynchronized(IResource.DEPTH_ZERO))
					{
						file.refreshLocal(IResource.DEPTH_ZERO, null/*monitor*/);
					}
					if (!file.getCharset().equals(encoding))
					{
						file.setCharset(encoding, null);
					}
				}
				else {
					// using text mode so the char set needs to be local
					if (properties.getLocalEncoding() != null){
						String localEncoding = properties.getLocalEncoding();
						file.setCharset(localEncoding, null);
					}

					// otherwise, the default charset is inherited so no need to set
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
		// get fresh remote file object
		remoteFile.markStale(true); // make sure we get the latest remote file (with proper permissions and all)
		if (!remoteFile.getParentRemoteFileSubSystem().isOffline()){
			try
			{
				remoteFile = subsystem.getRemoteFileObject(remoteFile.getAbsolutePath(), new NullProgressMonitor());
			}
			catch (Exception e)
			{
				SystemMessageDialog.displayExceptionMessage(SystemMessageDialog.getDefaultShell(), e);
				return;
			}
		}
		boolean readOnly = !remoteFile.canWrite();
		openEditor(remoteFile, readOnly);
	}

	
	/**
	 * Method to open the editor given an IRemoteFile and a specified readOnly property.
	 */
	private void openEditor(IRemoteFile remoteFile, boolean readOnly) throws PartInitException
	{
		IWorkbenchPage activePage = this.page;
		IWorkbench wb = PlatformUI.getWorkbench();
		if (activePage == null)
		{
			activePage = wb.getActiveWorkbenchWindow().getActivePage();
		}
		IFile file = getLocalResource();
		ResourceAttributes attr = file.getResourceAttributes();
		if (attr!=null) {
			attr.setReadOnly(readOnly);
			try
			{
				file.setResourceAttributes(attr);
			}
			catch (Exception e)
			{

			}
		}

		// set editor as preferred editor for this file
		String editorId = null;
		if (_editorDescriptor != null){
			if (_usingDefaultDescriptor){
				_editorDescriptor = IDE.getEditorDescriptor(file);
				editorId = _editorDescriptor.getId();
				_usingDefaultDescriptor = false;
			}	
			else {
				editorId = _editorDescriptor.getId();
			}
		}

		IDE.setDefaultEditor(file, editorId);
		if (_editorDescriptor.isOpenExternal()){
			openSystemEditor(); // opening regular way doesn't work anymore
		}
		else {
			FileEditorInput finput = new FileEditorInput(file);
	
			// check for files already open
	
			// DKM - when _editorId is not lpex, this causes problem
			// DY - changed editor from SystemTextEditor to IEditorPart
			//editor = (SystemTextEditor)activePage.openEditor(file, _editorId);
			if (_editorDescriptor != null && _editorDescriptor.isOpenExternal()){
				editor = ((WorkbenchPage)activePage).openEditorFromDescriptor(new FileEditorInput(file), _editorDescriptor, true, null);
			}
			else {
				editor =  activePage.openEditor(finput, _editorDescriptor.getId());
			}
	
	
			SystemIFileProperties properties = new SystemIFileProperties(file);
			properties.setRemoteFileObject(this);
			if (properties.getDirty()){
				updateDirtyIndicator();
			}
		}
	}

	/**
	 * Open the system editor
	 */
	public void openSystemEditor() throws PartInitException
	{
		IWorkbenchPage activePage = this.page;
		if (activePage == null)
		{
			activePage = SystemBasePlugin.getActiveWorkbenchWindow().getActivePage();
		}
		IFile file = getLocalResource();

		// set editor as preferred editor for this file
		String editorId = null;
		if (_editorDescriptor != null)
			editorId = _editorDescriptor.getId();
		IDE.setDefaultEditor(file, editorId);

		FileEditorInput fileInput = new FileEditorInput(file);
		activePage.openEditor(fileInput, IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);

		SystemIFileProperties properties = new SystemIFileProperties(file);
		properties.setRemoteFileObject(this);
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
	}
	
	private void setFileAsReadOnly()
	{
		IFile file = getLocalResource();
		setReadOnly(file, true);

		SystemIFileProperties properties = new SystemIFileProperties(file);
		properties.setReadOnly(true);
	}

	/**
	 * Refresh
	 */
	private void refresh()
	{

		SystemRemoteEditManager.getInstance().refreshRemoteEditContainer(localFile.getParent());
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
		if (editor == part){
			//delete();
		       
			SystemUniversalTempFileListener.getListener().unregisterEditedFile(this);
			IWorkbenchWindow win = SystemBasePlugin.getActiveWorkbenchWindow();
			if (win != null){ // if the window is already closed, the listener doesn't matter
				IWorkbenchPage page = win.getActivePage();			
				if (page != null){
					page.removePartListener(this);
					editor = null;
				}
			}			
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
				upload(new NullProgressMonitor());
			}
		}
		catch (Exception e)
		{

			SystemBasePlugin.logError("Error uploading file", e); //$NON-NLS-1$

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
				SystemBasePlugin.logError("Error in performSaveAs", e); //$NON-NLS-1$
				String msgTxt = CommonMessages.MSG_ERROR_UNEXPECTED;

				SystemMessage message = new SimpleSystemMessage(Activator.PLUGIN_ID,
						ICommonMessageIds.MSG_ERROR_UNEXPECTED,
						IStatus.ERROR, msgTxt);
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
						this.upload(progressMonitor);
						systemEditor.setInput(newInput);
					} catch (SystemMessageException e) {
						SystemMessageDialog dialog = new SystemMessageDialog(SystemBasePlugin.getActiveWorkbenchShell(), e.getSystemMessage());
						dialog.open();
						return true;
					} catch (Exception e)
					{
						SystemBasePlugin.logError("Error in performSaveAs", e); //$NON-NLS-1$
						String msgTxt = CommonMessages.MSG_ERROR_UNEXPECTED;

						SystemMessage message = new SimpleSystemMessage(Activator.PLUGIN_ID,
								ICommonMessageIds.MSG_ERROR_UNEXPECTED,
								IStatus.ERROR, msgTxt);
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

	private void markEditorDirty(){
		ITextEditor textEditor = (ITextEditor)editor;
		IDocumentProvider provider = textEditor.getDocumentProvider();
		if (provider != null){
			IDocument doc = provider.getDocument(textEditor.getEditorInput());
			String content = doc.get();
			try {
				doc.replace(0, content.length(), content);
			} catch (BadLocationException e) {
			}
		}
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
			else if (editor instanceof ITextEditor){ // mark dirty by updating editor contents
				// only do this if we need to mark it as dirty
				SystemIFileProperties properties = new SystemIFileProperties(localFile);
				if (properties.getDirty()){
					if (Display.getCurrent() == null){ // if we're not on a UI thread
						Display.getDefault().asyncExec(new Runnable() {						
							public void run() {
								markEditorDirty();
							}
						});					
					}
					else {
						markEditorDirty();
					}
				}
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

	/**
	 * Get the editor descriptor to be associated with the remote file
	 * @return the editor descriptor associated with this remote file
	 * @since 3.2 
	 */
	public IEditorDescriptor getEditorDescriptor(){
		return _editorDescriptor;
	}
	
	/**
	 * Set the editor descriptor to be associated with the remote file
	 * @param descriptor the new editor descriptor
	 * @since 3.2
	 */
	public void setEditorDescriptor(IEditorDescriptor descriptor){
		_editorDescriptor = descriptor;
	}
	
	private static IFile getProjectFileForLocation(String absolutePath)
	{
		IPath workspacePath = new Path(absolutePath);
		IFile file = SystemBasePlugin.getWorkspaceRoot().getFileForLocation(workspacePath);
		return file;
	}
	
}
