/********************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation. All rights reserved.
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
 * Kushal Munir (IBM) - moved to internal package
 ********************************************************************************/

package org.eclipse.rse.internal.eclipse.filesystem;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.filesystem.provider.FileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rse.core.subsystems.RemoteChildrenContentsType;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileFilterString;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystemConfiguration;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileContext;
import org.eclipse.swt.widgets.Display;


public class RSEFileStore extends FileStore implements IFileStore 
{
	private IFileStore _parent;
	private IRemoteFileSubSystem _subSystem;
	private String _absolutePath;
	private String _name;
	private IRemoteFile _remoteFile;
	
	/**
	 * Constructor to use if the file store is a handle.
	 * @param parent the parent.
	 * @param name the name of the file store.
	 */
	public RSEFileStore(RSEFileStore parent, IRemoteFileSubSystem subSystem, String parentAbsolutePath, String name) {
		_parent = parent;
		_subSystem = subSystem;
		
		if (!parentAbsolutePath.endsWith(_subSystem.getSeparator())) {
			_absolutePath = parentAbsolutePath + _subSystem.getSeparator() + name;
		}
		else {
			_absolutePath = parentAbsolutePath + name;
		}
		
		_name = name;
	}
	
	// an input stream that wraps another input stream and closes the wrappered input stream in a runnable that is always run with the user interface thread
	private class RSEFileStoreInputStream extends BufferedInputStream {
		
		/**
		 * Creates a BufferedInputStream  and saves its argument, the input stream, for later use. An internal buffer array is created.
		 * @param in the underlying input stream.
		 */
		public RSEFileStoreInputStream(InputStream in) {
			super(in);
		}

		/**
		 * Creates a BufferedInputStream  and saves its argument, the input stream, for later use. An internal buffer array of the given size is created.
		 * @param in the underlying input stream.
		 * @param size the buffer size.
		 */
		public RSEFileStoreInputStream(InputStream in, int size) {
			super(in, size);
		}

		/**
		 * @see java.io.BufferedInputStream#close()
		 */
		public void close() throws IOException {
			
			Display current = Display.getCurrent();
			
			if (current != null) {
				super.close();
			}
		}
	}
	
	private class RSEFileStoreOutputStream extends BufferedOutputStream {
		
		/**
		 * Creates a new buffered output stream to write data to the specified underlying output stream with a default 512-byte buffer size.
		 * @param out the underlying output stream.
		 */
		public RSEFileStoreOutputStream(OutputStream out) {
			super(out);
		}

		/**
		 * Creates a new buffered output stream to write data to the specified underlying output stream with the specified buffer size.
		 * @param out the underlying output stream.
		 * @param size the buffer size.
		 */
		public RSEFileStoreOutputStream(OutputStream out, int size) {
			super(out, size);
		}

		/**
		 * @see java.io.BufferedOutputStream#close()
		 */
		public void close() throws IOException {
			
			Display current = Display.getCurrent();
			
			if (current != null) {
				super.close();
			}
		}
	}
	
	public String[] childNames(int options, IProgressMonitor monitor) throws CoreException {
		
		String[] names;
		
		if (!_subSystem.isConnected()) {
			
			try {
				_subSystem.connect(monitor);
			}
			catch (Exception e) {
				throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Could not connect to subsystem", e)); //$NON-NLS-1$
			}
		}
		
		// at this point get the live remote file because we want to fetch the info about this file
		try {
			_remoteFile = _subSystem.getRemoteFileObject(_absolutePath);
		}
		catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Could not get remote file", e)); //$NON-NLS-1$
		}
		
		if (_remoteFile == null || !_remoteFile.exists()) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "The file store does not exist")); //$NON-NLS-1$
		}
		
		if (!_remoteFile.isStale() && _remoteFile.hasContents(RemoteChildrenContentsType.getInstance()) && !(_subSystem instanceof IFileServiceSubSystem))
		{
			Object[] children = _remoteFile.getContents(RemoteChildrenContentsType.getInstance());
			names = new String[children.length];
			                
			for (int i = 0; i < children.length; i++)
			{
				names[i] = ((IRemoteFile)children[i]).getName();
			}
		}
		else
		{
			try {
				
				IRemoteFile[] children = null;
				
				if (_subSystem instanceof FileServiceSubSystem) {
					FileServiceSubSystem fileServiceSubSystem = ((FileServiceSubSystem)_subSystem);
					IHostFile[] results = fileServiceSubSystem.getFileService().getFilesAndFolders(monitor, _remoteFile.getAbsolutePath(), "*"); //$NON-NLS-1$
					IRemoteFileSubSystemConfiguration config = _subSystem.getParentRemoteFileSubSystemConfiguration();
					RemoteFileFilterString filterString = new RemoteFileFilterString(config, _remoteFile.getAbsolutePath(), "*"); //$NON-NLS-1$
					filterString.setShowFiles(true);
					filterString.setShowSubDirs(true);
					RemoteFileContext context = new RemoteFileContext(_subSystem, _remoteFile, filterString);
					children = fileServiceSubSystem.getHostFileToRemoteFileAdapter().convertToRemoteFiles(fileServiceSubSystem, context, _remoteFile, results);
				}
				else {
					children = _subSystem.listFoldersAndFiles(_remoteFile, "*", monitor); //$NON-NLS-1$
				}
				
				names = new String[children.length];
				
				for (int i = 0; i < children.length; i++) {
					names[i] = (children[i]).getName();
				}		
			}
			catch (SystemMessageException e) {
				names = new String[0];
			}
		}
		
		return names;
	}
	
	public IFileInfo fetchInfo(int options, IProgressMonitor monitor) throws CoreException {
		
		// connect if needed
		if (!_subSystem.isConnected()) {
			
			try {
				_subSystem.connect(monitor);
			}
			catch (Exception e) {
				// throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Could not connect to subsystem", e)); //$NON-NLS-1$
				FileInfo info = new FileInfo(getName());
				info.setExists(false);
				return info;
			}
		}
		
		// at this point get the live remote file because we want to fetch the info about this file
		try {
			_remoteFile = _subSystem.getRemoteFileObject(_absolutePath);
		}
		catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Could not get remote file", e)); //$NON-NLS-1$
		}
		
		FileInfo info = new FileInfo(getName());
		
		if (_remoteFile == null || !_remoteFile.exists()) {
			info.setExists(false);
			return info;
		}
		
		info.setExists(true);
		info.setLastModified(_remoteFile.getLastModified());
		boolean isDir = _remoteFile.isDirectory();
		info.setDirectory(isDir);
		info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, !_remoteFile.canWrite());
		info.setAttribute(EFS.ATTRIBUTE_EXECUTABLE, _remoteFile.isExecutable());
		info.setAttribute(EFS.ATTRIBUTE_ARCHIVE, _remoteFile.isArchive());
		info.setAttribute(EFS.ATTRIBUTE_HIDDEN, _remoteFile.isHidden());

		if (!isDir) {
			info.setLength(_remoteFile.getLength());
		}
		
		return info;
	}
	
	public String getName() {
		return _name;
	}
	
	public IFileStore getParent() {
		return _parent;
	}
	
	public synchronized InputStream openInputStream(int options, IProgressMonitor monitor) throws CoreException 
	{
		if (!_subSystem.isConnected()) {
			
			try {
				_subSystem.connect(monitor);
			}
			catch (Exception e) {
				throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Could not connect to subsystem", e)); //$NON-NLS-1$
			}
		}
		
		// at this point get the live remote file
		try {
			_remoteFile = _subSystem.getRemoteFileObject(_absolutePath);
		}
		catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Could not get remote file", e)); //$NON-NLS-1$
		}
		
		if (_remoteFile == null || !_remoteFile.exists()) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "The file store does not exist")); //$NON-NLS-1$
		}
		
		if (_remoteFile.isDirectory()) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "The file store represents a directory")); //$NON-NLS-1$
		}
		
		if (_remoteFile.isFile()) {
				
			try {
				return new RSEFileStoreInputStream(_subSystem.getInputStream(_remoteFile.getParentPath(), _remoteFile.getName(), true, monitor));
			}
			catch (SystemMessageException e) {
				throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Could not get input stream", e)); //$NON-NLS-1$
			}
		}
		
		return null;
	}
	
	public URI toURI() {
		
		try {
			String path = _absolutePath;
			
			if (path.charAt(0) != '/') {
				path = "/" + path.replace('\\', '/'); //$NON-NLS-1$
			}
			
			return new URI("rse", _subSystem.getHost().getHostName(), path, null); //$NON-NLS-1$
		} 
		catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}	
	}

	public IFileStore mkdir(int options, IProgressMonitor monitor) throws CoreException 
	{
		if (!_subSystem.isConnected()) {
			
			try {
				_subSystem.connect(monitor);
			}
			catch (Exception e) {
				throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Could not connect to subsystem", e)); //$NON-NLS-1$
			}
		}
		
		// at this point get the live remote file
		try {
			_remoteFile = _subSystem.getRemoteFileObject(_absolutePath);
		}
		catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Could not get remote file", e)); //$NON-NLS-1$
		}
		
		if (!_remoteFile.exists()) {
			try {
				_remoteFile = _subSystem.createFolder(_remoteFile);
			}
			catch (Exception e) {
				throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "The directory could not be created", e)); //$NON-NLS-1$
			}
			
			return this;
		}
		else {
			
			if (_remoteFile.isFile()) {
				throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "A file of that name already exists")); //$NON-NLS-1$
			}
			else {
				return this;
			}
		}
	}

	public OutputStream openOutputStream(int options, IProgressMonitor monitor) throws CoreException {
		
		if (!_subSystem.isConnected()) {
			
			try {
				_subSystem.connect(monitor);
			}
			catch (Exception e) {
				throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Could not connect to subsystem", e)); //$NON-NLS-1$
			}
		}
		
		// at this point get the live remote file
		try {
			_remoteFile = _subSystem.getRemoteFileObject(_absolutePath);
		}
		catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Could not get remote file", e)); //$NON-NLS-1$
		}
		
		if (!_remoteFile.exists()) {
			
			try {
				_remoteFile = _subSystem.createFile(_remoteFile);
			}
			catch (Exception e) {
				throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Could not create file", e)); //$NON-NLS-1$
			} 
		}
			
		if (_remoteFile.isFile()) {
			
			try {
				return new RSEFileStoreOutputStream(_subSystem.getOutputStream(_remoteFile.getParentPath(), _remoteFile.getName(), true, monitor));
			}
			catch (SystemMessageException e) {
				throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Could not get output stream", e)); //$NON-NLS-1$
			}
		}
		else {
			
			if (_remoteFile.isDirectory()) {
				throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "This is a directory")); //$NON-NLS-1$
			}
			else {
				return null;
			}
		}
	}
	
	public IFileStore getChild(String name) {
		return new RSEFileStore(this, _subSystem, _absolutePath, name);
	}

	public File toLocalFile(int options, IProgressMonitor monitor) throws CoreException 
	{
		if (options == EFS.CACHE) {
			return super.toLocalFile(options, monitor);
		}
		else {
			
			if (!_subSystem.isConnected()) {
				
				try {
					_subSystem.connect(monitor);
				}
				catch (Exception e) {
					throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Could not connect to subsystem", e)); //$NON-NLS-1$
				}
			}
			
			// at this point get the live remote file
			try {
				_remoteFile = _subSystem.getRemoteFileObject(_absolutePath);
			}
			catch (Exception e) {
				throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Could not get remote file", e)); //$NON-NLS-1$
			}
			
			if (_remoteFile.exists()) {
				
				RSEFileCache cache = RSEFileCache.getInstance();
				InputStream inputStream = null;
				
				try {
					
					if (_remoteFile.isFile()) {
						inputStream = new RSEFileStoreInputStream(_subSystem.getInputStream(_remoteFile.getParentRemoteFileSubSystem().getHost().getHostName(), _remoteFile.getName(), true, monitor));
					}
					
					return cache.writeToCache(_remoteFile, inputStream);
				}
				catch (SystemMessageException e) {
					throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Could not get input stream", e)); //$NON-NLS-1$
				}
			}
			else {
				return null;
			}
		}
	}

	public void delete(int options, IProgressMonitor monitor) throws CoreException 
	{
		if (!_subSystem.isConnected()) {
			
			try {
				_subSystem.connect(monitor);
			}
			catch (Exception e) {
				throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Could not connect to subsystem", e)); //$NON-NLS-1$
			}
		}
		
		// at this point get the live remote file
		try {
			_remoteFile = _subSystem.getRemoteFileObject(_absolutePath);
		}
		catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Could not get remote file", e)); //$NON-NLS-1$
		}
		
		if (!_remoteFile.exists()) {
			return;
		}
		else {
			
			try {
				boolean success = _subSystem.delete(_remoteFile, monitor);
				
				if (!success) {
					throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Could not delete file")); //$NON-NLS-1$
				}
			}
			catch (Exception e) {
				throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Could not delete file", e)); //$NON-NLS-1$
			}
		}
	}
}