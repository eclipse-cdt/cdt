/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   IBM Corporation - Initial API and implementation
 *   Roland Schulz, University of Tennessee
 *******************************************************************************/
package org.eclipse.internal.remote.jsch.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.filesystem.provider.FileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

public class JschFileStore extends FileStore {
	private abstract class SftpCallable<T> implements Callable<T> {
		private ChannelSftp fSftpChannel = null;
		private IProgressMonitor fProgressMonitor = null;

		private Future<T> asyncCmdInThread(String jobName) throws CoreException {
			return fPool.submit(this);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.concurrent.Callable#call()
		 */
		public abstract T call() throws SftpException, IOException;

		private void finalizeCmdInThread() {
			setChannel(null);
		}

		public ChannelSftp getChannel() {
			return fSftpChannel;
		}

		public IProgressMonitor getProgressMonitor() {
			return fProgressMonitor;
		}

		public void setChannel(ChannelSftp channel) {
			fSftpChannel = channel;
		}

		/**
		 * Function opens sftp channel and then executes the sftp operation. If
		 * run on the main thread it executes it on a separate thread
		 */
		public T syncCmdInThread(String jobName, IProgressMonitor monitor) throws CoreException {
			Future<T> future = null;
			fProgressMonitor = SubMonitor.convert(monitor, 10);
			try {
				future = asyncCmdInThread(jobName);
				return waitCmdInThread(future);
			} finally {
				finalizeCmdInThread();
				if (monitor != null) {
					monitor.done();
				}
			}
		}

		private T waitCmdInThread(Future<T> future) throws CoreException {
			T ret = null;
			boolean bInterrupted = Thread.interrupted();
			while (ret == null) {
				if (getProgressMonitor().isCanceled()) {
					future.cancel(true);
					getChannel().quit();
					throw new CoreException(new Status(IStatus.CANCEL, Activator.getUniqueIdentifier(),
							"Operation cancelled by user"));
				}
				try {
					ret = future.get(100, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					bInterrupted = true;
				} catch (TimeoutException e) {
					// ignore
				} catch (ExecutionException e) {
					/*
					 * close sftp channel (gets
					 * automatically reopened) to make
					 * sure the channel is not in
					 * undefined state because of
					 * exception
					 */
					getChannel().quit();
					throw new CoreException(new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), "Execution exception",
							e.getCause()));
				}
				getProgressMonitor().worked(1);
			}
			if (bInterrupted) {
				Thread.currentThread().interrupt(); // set current thread flag
			}
			return ret;
		}
	}

	private static Map<String, JschFileStore> instanceMap = new HashMap<String, JschFileStore>();

	private static ExecutorService fPool = Executors.newSingleThreadExecutor();

	/**
	 * Public factory method for obtaining JschFileStore instances.
	 * 
	 * @param uri
	 *            URI to get a fileStore for
	 * @return an JschFileStore instance for the URI.
	 */
	public static JschFileStore getInstance(URI uri) {
		synchronized (instanceMap) {
			JschFileStore store = instanceMap.get(uri.toString());
			if (store == null) {
				String name = JSchFileSystem.getConnectionNameFor(uri);
				if (name != null) {
					String path = uri.getPath();
					store = new JschFileStore(name, path);
					instanceMap.put(uri.toString(), store);
				}
			}
			return store;
		}
	}

	private final String fConnectionName;
	private final IPath fRemotePath;

	public JschFileStore(String connName, String path) {
		fConnectionName = connName;
		fRemotePath = new Path(path);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileStore#childInfos(int,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IFileInfo[] childInfos(int options, IProgressMonitor monitor) throws CoreException {
		final SubMonitor subMon = SubMonitor.convert(monitor, 10);
		Vector<LsEntry> files = getChildren(fRemotePath.toString(), subMon.newChild(10));
		List<IFileInfo> result = new ArrayList<IFileInfo>();

		if (files != null && !subMon.isCanceled()) {
			Enumeration<LsEntry> enumeration = files.elements();
			while (enumeration.hasMoreElements() && !subMon.isCanceled()) {
				LsEntry entry = enumeration.nextElement();
				final String fileName = entry.getFilename();
				if (fileName.equals(".") || fileName.equals("..")) { //$NON-NLS-1$ //$NON-NLS-2$
					// Ignore parent and current dir entry.
					continue;
				}
				result.add(convertToFileInfo(fileName, entry.getAttrs(), subMon.newChild(10)));
			}
		}

		return result.toArray(new IFileInfo[0]);
	}

	private Vector<LsEntry> getChildren(final String path, IProgressMonitor monitor) throws CoreException {
		SftpCallable<Vector<LsEntry>> c = new SftpCallable<Vector<LsEntry>>() {
			@SuppressWarnings("unchecked")
			@Override
			public Vector<LsEntry> call() throws SftpException {
				return getChannel().ls(path);
			}
		};
		return c.syncCmdInThread("Get file attributes", monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileStore#childNames(int,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public String[] childNames(int options, IProgressMonitor monitor) throws CoreException {
		String[] names = new String[0];
		return names;
	}

	private IFileInfo convertToFileInfo(final String name, SftpATTRS attrs, IProgressMonitor monitor) throws CoreException {
		FileInfo fileInfo = new FileInfo(name);
		fileInfo.setExists(true);
		fileInfo.setDirectory(attrs.isDir());
		fileInfo.setAttribute(EFS.ATTRIBUTE_OWNER_EXECUTE, (attrs.getPermissions() & 0100) != 0);
		fileInfo.setAttribute(EFS.ATTRIBUTE_OWNER_WRITE, (attrs.getPermissions() & 0200) != 0);
		fileInfo.setAttribute(EFS.ATTRIBUTE_OWNER_READ, (attrs.getPermissions() & 0400) != 0);
		fileInfo.setAttribute(EFS.ATTRIBUTE_GROUP_EXECUTE, (attrs.getPermissions() & 0010) != 0);
		fileInfo.setAttribute(EFS.ATTRIBUTE_GROUP_WRITE, (attrs.getPermissions() & 0020) != 0);
		fileInfo.setAttribute(EFS.ATTRIBUTE_GROUP_READ, (attrs.getPermissions() & 0040) != 0);
		fileInfo.setAttribute(EFS.ATTRIBUTE_OTHER_EXECUTE, (attrs.getPermissions() & 0001) != 0);
		fileInfo.setAttribute(EFS.ATTRIBUTE_OTHER_WRITE, (attrs.getPermissions() & 0002) != 0);
		fileInfo.setAttribute(EFS.ATTRIBUTE_OTHER_READ, (attrs.getPermissions() & 0004) != 0);
		fileInfo.setAttribute(EFS.ATTRIBUTE_SYMLINK, attrs.isLink());
		if (attrs.isLink()) {
			SftpCallable<String> c2 = new SftpCallable<String>() {
				@Override
				public String call() throws SftpException {
					String pathName = fRemotePath.append(name).toString();
					return getChannel().readlink(pathName);
				}
			};
			String target = c2.syncCmdInThread("Get symlink target", monitor);
			fileInfo.setStringAttribute(EFS.ATTRIBUTE_LINK_TARGET, target);
		}
		fileInfo.setLastModified(attrs.getMTime());
		fileInfo.setLength(attrs.getSize());
		return fileInfo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileStore#delete(int,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void delete(int options, IProgressMonitor monitor) throws CoreException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileStore#fetchInfo(int,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IFileInfo fetchInfo(int options, IProgressMonitor monitor) throws CoreException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.filesystem.provider.FileStore#getChild(java.lang.String)
	 */
	@Override
	public IFileStore getChild(String name) {
		//		System.out.println("GETCHILD: " + name); //$NON-NLS-1$
		URI uri = JSchFileSystem.getURIFor(fConnectionName, fRemotePath.append(name).toString());
		return JschFileStore.getInstance(uri);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileStore#getName()
	 */
	@Override
	public String getName() {
		return getNameFromPath(fRemotePath);
	}

	/**
	 * Utility routing to get the file name from an absolute path.
	 * 
	 * @param path
	 *            path to extract file name from
	 * @return last segment of path, or the full path if it is root
	 */
	private String getNameFromPath(IPath path) {
		if (path.isRoot()) {
			return path.toString();
		}
		return path.lastSegment();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileStore#getParent()
	 */
	@Override
	public IFileStore getParent() {
		//		System.out.println("GETPARENT: " + fRemotePath.toString()); //$NON-NLS-1$
		if (fRemotePath.isRoot()) {
			return null;
		}
		String parentPath = fRemotePath.toString();
		if (fRemotePath.segmentCount() > 0) {
			parentPath = fRemotePath.removeLastSegments(1).toString();
		}
		return JschFileStore.getInstance(JSchFileSystem.getURIFor(fConnectionName, parentPath));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileStore#mkdir(int,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IFileStore mkdir(int options, IProgressMonitor monitor) throws CoreException {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileStore#openInputStream(int,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public InputStream openInputStream(int options, IProgressMonitor monitor) throws CoreException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileStore#openOutputStream(int,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public OutputStream openOutputStream(int options, IProgressMonitor monitor) throws CoreException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.filesystem.provider.FileStore#putInfo(org.eclipse.core
	 * .filesystem.IFileInfo, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void putInfo(IFileInfo info, int options, IProgressMonitor monitor) throws CoreException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileStore#toURI()
	 */
	@Override
	public URI toURI() {
		return JSchFileSystem.getURIFor(fConnectionName, fRemotePath.toString());
	}

}
