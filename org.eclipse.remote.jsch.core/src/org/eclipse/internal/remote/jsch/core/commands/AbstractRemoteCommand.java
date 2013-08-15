/******************************************************************************
 * Copyright (c) 2013 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.internal.remote.jsch.core.commands;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.internal.remote.jsch.core.Activator;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

/**
 * @author greg
 * 
 */
public abstract class AbstractRemoteCommand<T> {
	private static ExecutorService fPool = Executors.newSingleThreadExecutor();

	protected abstract class SftpCallable<T1> implements Callable<T1> {
		private Future<T1> asyncCmdInThread(String jobName) throws CoreException {
			return fPool.submit(this);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.concurrent.Callable#call()
		 */
		public abstract T1 call() throws JSchException, SftpException, IOException;

		private void finalizeCmdInThread() {
		}

		public ChannelSftp getChannel() throws JSchException {
			return getSftpChannel();
		}

		/**
		 * Function opens sftp channel and then executes the sftp operation. If
		 * run on the main thread it executes it on a separate thread
		 */
		public T1 getResult(String jobName, IProgressMonitor monitor) throws SftpException, CoreException {
			Future<T1> future = null;
			SubMonitor progress = SubMonitor.convert(monitor, 10);
			try {
				future = asyncCmdInThread(jobName);
				return waitCmdInThread(future, progress.newChild(10));
			} finally {
				finalizeCmdInThread();
				if (monitor != null) {
					monitor.done();
				}
			}
		}

		private T1 waitCmdInThread(Future<T1> future, IProgressMonitor monitor) throws SftpException, CoreException {
			T1 ret = null;
			boolean bInterrupted = Thread.interrupted();
			while (ret == null) {
				try {
					if (monitor.isCanceled()) {
						future.cancel(true);
						try {
							getSftpChannel().quit();
						} catch (JSchException e) {
							// Ignore
						}
						throw new CoreException(new Status(IStatus.CANCEL, Activator.getUniqueIdentifier(),
								"Operation cancelled by user"));
					}
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
					try {
						getSftpChannel().quit();
					} catch (JSchException e1) {
						// Ignore
					}
					if (e.getCause() instanceof SftpException) {
						throw (SftpException) e.getCause();
					}
					throw new CoreException(new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), "Execution exception",
							e.getCause()));
				}
				monitor.worked(1);
			}
			if (bInterrupted) {
				Thread.currentThread().interrupt(); // set current thread flag
			}
			return ret;
		}
	}

	protected ChannelSftp getSftpChannel() throws JSchException {
		if (!fSftpChannel.isConnected()) {
			fSftpChannel.connect();
		}
		return fSftpChannel;
	}

	protected abstract T getResult(IProgressMonitor monitor) throws CoreException;

	private final ChannelSftp fSftpChannel;

	protected IFileInfo convertToFileInfo(final IPath path, SftpATTRS attrs, IProgressMonitor monitor) throws CoreException {
		return convertToFileInfo(path.lastSegment(), path.removeLastSegments(1), attrs, monitor);
	}

	protected IFileInfo convertToFileInfo(final String name, final IPath parentPath, SftpATTRS attrs, IProgressMonitor monitor)
			throws CoreException {
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
				public String call() throws JSchException, SftpException {
					return getChannel().readlink(parentPath.append(name).toString());
				}
			};
			String target;
			try {
				target = c2.getResult("Get symlink target", monitor);
				fileInfo.setStringAttribute(EFS.ATTRIBUTE_LINK_TARGET, target);
			} catch (SftpException e) {
				// Ignore
			}
		}
		fileInfo.setLastModified(attrs.getMTime());
		fileInfo.setLength(attrs.getSize());
		return fileInfo;
	}

	public static final int UNKNOWN = 0;
	public static final int SUCCESS_OK = 1;
	public static final int SUCCESS_ERROR = 2;
	public static final int ERROR_NOT_EXECUTABLE = 126;
	public static final int ERROR_NOT_FOUND = 127;
	public static final int INVALID_EXIT_CODE = 128;
	public static final int SIGHUP = 129;
	public static final int SIGINT = 130;
	public static final int SIGQUIT = 131;
	public static final int SIGILL = 132;
	public static final int SIGTRAP = 133;
	public static final int SIGIOT = 134;
	public static final int SIGBUS = 135;
	public static final int SIGFPE = 136;
	public static final int SIGKILL = 137;
	public static final int SIGUSR1 = 138;
	public static final int SIGSEGV = 139;
	public static final int SIGUSR2 = 140;
	public static final int SIGPIPE = 141;
	public static final int SIGALRM = 142;
	public static final int SIGTERM = 143;
	public static final int SIGSTKFLT = 144;
	public static final int SIGCHLD = 145;
	public static final int SIGCONT = 146;
	public static final int SIGSTOP = 147;
	public static final int SIGTSTP = 148;
	public static final int SIGTTIN = 149;
	public static final int SIGTTOU = 150;
	public static final int SIGURG = 151;
	public static final int SIGXCPU = 152;
	public static final int SIGXFSZ = 153;
	public static final int SIGVTALRM = 154;
	public static final int SIGPROF = 155;
	public static final int SIGWINCH = 156;
	public static final int SIGIO = 157;
	public static final int SIGPWR = 158;

	public AbstractRemoteCommand(ChannelSftp channel) {
		fSftpChannel = channel;
	}

	public int getFinishStatus() {
		int code = 0;

		if (code == 0) {
			return SUCCESS_OK;
		} else if (code <= 125) {
			return SUCCESS_ERROR;
		} else if (code == 126) {
			return ERROR_NOT_EXECUTABLE;
		} else if (code == 127) {
			return ERROR_NOT_FOUND;
		} else if (code == 128) {
			return UNKNOWN;
		} else if (code == 255) {
			return INVALID_EXIT_CODE;
		} else if (code == 128 + 1) {
			return SIGHUP;
		} else if (code == 128 + 2) {
			return SIGINT;
		} else if (code == 128 + 3) {
			return SIGQUIT;
		} else if (code == 128 + 4) {
			return SIGILL;
		} else if (code == 128 + 5) {
			return SIGTRAP;
		} else if (code == 128 + 6) {
			return SIGIOT;
		} else if (code == 128 + 7) {
			return SIGBUS;
		} else if (code == 128 + 8) {
			return SIGFPE;
		} else if (code == 128 + 9) {
			return SIGKILL;
		} else if (code == 128 + 10) {
			return SIGUSR1;
		} else if (code == 128 + 11) {
			return SIGSEGV;
		} else if (code == 128 + 12) {
			return SIGUSR2;
		} else if (code == 128 + 13) {
			return SIGPIPE;
		} else if (code == 128 + 14) {
			return SIGALRM;
		} else if (code == 128 + 15) {
			return SIGTERM;
		} else if (code == 128 + 16) {
			return SIGSTKFLT;
		} else if (code == 128 + 17) {
			return SIGCHLD;
		} else if (code == 128 + 18) {
			return SIGCONT;
		} else if (code == 128 + 19) {
			return SIGSTOP;
		} else if (code == 128 + 20) {
			return SIGTSTP;
		} else if (code == 128 + 21) {
			return SIGTTIN;
		} else if (code == 128 + 22) {
			return SIGTTOU;
		} else if (code == 128 + 23) {
			return SIGURG;
		} else if (code == 128 + 24) {
			return SIGXCPU;
		} else if (code == 128 + 25) {
			return SIGXFSZ;
		} else if (code == 128 + 26) {
			return SIGVTALRM;
		} else if (code == 128 + 27) {
			return SIGPROF;
		} else if (code == 128 + 28) {
			return SIGWINCH;
		} else if (code == 128 + 29) {
			return SIGIO;
		} else if (code == 128 + 30) {
			return SIGPWR;
		} else {
			return UNKNOWN;
		}
	}

}