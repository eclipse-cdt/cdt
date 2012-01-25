/*******************************************************************************
 * Copyright (c) 2009, 2012 Mentor Graphics Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Nikita Shulga                     - initial API and implementation 
 * Anna Dushistova (Mentor Graphics) - [331213][scp] Provide UI-less scp IFileService in org.eclipse.rse.services.ssh
 * Anna Dushistova (Mentor Graphics) - [331249][scp] incorrect home while logging in as root
 * Simon Bernard   (Sierra Wireless) - [349947][scp] with scp IRemoteFile.exist() returns always true
 * Anna Dushistova (MontaVista)      - [331213] [scp] Provide UI-less scp IFileService in org.eclipse.rse.services.ssh
 *******************************************************************************/
package org.eclipse.rse.internal.services.ssh.files.scp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;

import org.eclipse.rse.internal.services.ssh.Activator;
import org.eclipse.rse.internal.services.ssh.ISshSessionProvider;
import org.eclipse.rse.internal.services.ssh.SshServiceResources;
import org.eclipse.rse.internal.services.ssh.files.SshFileUtils;
import org.eclipse.rse.internal.services.ssh.files.SftpHostFile;
import org.eclipse.rse.services.clientserver.FileTypeMatcher;
import org.eclipse.rse.services.clientserver.IMatcher;
import org.eclipse.rse.services.clientserver.NamePatternMatcher;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.AbstractFileService;
import org.eclipse.rse.services.files.HostFilePermissions;
import org.eclipse.rse.services.files.IFilePermissionsService;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.services.files.IHostFilePermissions;
import org.eclipse.rse.services.files.IHostFilePermissionsContainer;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;

public class ScpFileService extends AbstractFileService implements
		IFilePermissionsService {

	private final ISshSessionProvider fSessionProvider;
	private String fUserHome = null;

	public ISshSessionProvider getSessionProvider() {
		return fSessionProvider;
	}

	public Session getSession() {
		return getSessionProvider().getSession();
	}

	public ScpFileService(ISshSessionProvider provider) {
		fSessionProvider = provider;
	}

	protected static void throwSystemException(String msg)
			throws SystemMessageException {
		throw new SystemMessageException(new SimpleSystemMessage(
				Activator.PLUGIN_ID, IStatus.ERROR, msg));
	}

	protected IHostFile[] internalFetch(String parentPath, String fileFilter,
			int fileType, IProgressMonitor monitor)
			throws SystemMessageException {

		if (fileFilter == null)
			fileFilter = "*"; //$NON-NLS-1$
		IMatcher filematcher = null;
		if (fileFilter.endsWith(",")) { //$NON-NLS-1$
			String[] types = fileFilter.split(","); //$NON-NLS-1$
			filematcher = new FileTypeMatcher(types, true);
		} else {
			filematcher = new NamePatternMatcher(fileFilter, true, true);
		}

		List results = new ArrayList();
		Session sess = getSession();
		String cmd = "ls -lAn " + SshFileUtils.escapePath(parentPath); //$NON-NLS-1$

		String rc = SshFileUtils.execCommandSafe(sess, cmd);
		String[] lsStrings = rc.split(SshFileUtils.EOL_STRING);
		for (int i=0;i<lsStrings.length;i++){
			if (lsStrings[i].length() == 0 || lsStrings[i].startsWith("total")) //$NON-NLS-1$
				continue;
			ScpFileAttr attr = new ScpFileAttr(lsStrings[i]);
			if (attr.getName() == null) {
				Activator.warn("internalFetch(parentPath='" + parentPath //$NON-NLS-1$
						+ "'): Can't get name of " + lsStrings[i], null); //$NON-NLS-1$
				continue;
			}
			if (!filematcher.matches(attr.getName()))
				continue;
			IHostFile f = makeHostFile(parentPath, null, attr);
			if (isRightType(fileType, f))
				results.add(f);
		}
		return (IHostFile[]) results.toArray(new IHostFile[results.size()]);
	}

	public int getCapabilities(IHostFile host) {
		return IFilePermissionsService.FS_CAN_GET_ALL
				| IFilePermissionsService.FS_CAN_SET_ALL;
	}

	public IHostFilePermissions getFilePermissions(IHostFile file,
			IProgressMonitor monitor) throws SystemMessageException {
		if (file instanceof IHostFilePermissionsContainer) {
			return ((IHostFilePermissionsContainer) file).getPermissions();
		}
		return null;
	}

	public void setFilePermissions(IHostFile file,
			IHostFilePermissions permissions, IProgressMonitor monitor)
			throws SystemMessageException {
		Session session = getSession();
		String path = SshFileUtils.escapePath(file.getAbsolutePath());
		int permBits = permissions.getPermissionBits();
		String ownStr = permissions.getUserOwner() + ':'
				+ permissions.getGroupOwner();
		String cmd = "chmod " + Integer.toOctalString(permBits) + " " + path; //$NON-NLS-1$ //$NON-NLS-2$
		SshFileUtils.execCommandSafe(session, cmd);
		SshFileUtils.execCommandSafe(session, "chown " + ownStr + " " + path); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void copy(String arg0, String arg1, String arg2, String arg3,
			IProgressMonitor arg4) throws SystemMessageException {
		// TODO Auto-generated method stub

	}

	public void copyBatch(String[] arg0, String[] arg1, String arg2,
			IProgressMonitor arg3) throws SystemMessageException {
		// TODO Auto-generated method stub

	}

	public IHostFile createFile(String remotePath, String fileName,
			IProgressMonitor arg2) throws SystemMessageException {
		Session session = getSession();
		String fullPath = SshFileUtils.concat(remotePath, fileName);

		SshFileUtils.execCommandSafe(session,
				"touch " + SshFileUtils.escapePath(fullPath)); //$NON-NLS-1$
		ScpFileAttr attr = ScpFileAttr.getAttr(session, fullPath);

		return makeHostFile(remotePath, fileName, attr);
	}

	public IHostFile createFolder(String remotePath, String fileName,
			IProgressMonitor arg2) throws SystemMessageException {
		Session session = getSession();
		String fullPath = SshFileUtils.concat(remotePath, fileName);

		SshFileUtils.execCommandSafe(session,
				"mkdir " + SshFileUtils.escapePath(fullPath)); //$NON-NLS-1$
		ScpFileAttr attr = ScpFileAttr.getAttr(session, fullPath);

		return makeHostFile(remotePath, fileName, attr);
	}

	public void delete(String remotePath, String fileName, IProgressMonitor arg2)
			throws SystemMessageException {
		Session session = getSession();
		String fullPathEsc = SshFileUtils.concatEscape(remotePath, fileName);

		SshFileUtils.execCommandSafe(session, "rm -rf " + fullPathEsc); //$NON-NLS-1$

	}

	private static String lastErrorMessage = null;

	private static void setErrorMessage(String s) {
		lastErrorMessage = s;
	}

	public static String getLastError() {
		return lastErrorMessage;
	}

	private static int readAck(InputStream is) throws IOException {
		int rc = is.read();
		if (rc <= 0)
			return rc;

		/* In case of non fatal error had to read an error string */
		StringBuffer sb = new StringBuffer();
		int ch;
		do {
			ch = is.read();
			sb.append((char) ch);
		} while (ch != SshFileUtils.EOL_CHAR);
		setErrorMessage(sb.toString());
		return rc;
	}

	public void download(String remoteParent, String fileName, File localFile,
			boolean isBinary, String hostEncoding, IProgressMonitor monitor)
			throws SystemMessageException {

		try {
			if (!localFile.exists()) {
				File localParentFile = localFile.getParentFile();
				if (!localParentFile.exists()) {
					localParentFile.mkdirs();
				}
			}

			internalDownload(remoteParent, fileName, localFile, monitor);

		} catch (Exception e) {
			SshFileUtils.throwSystemException(e);
		}

	}

	public IHostFile getFile(String remotePath, String fileName,
			IProgressMonitor monitor) throws SystemMessageException {
		Session session = getSession();
		String fullPath = SshFileUtils.concat(remotePath, fileName);
		ScpFileAttr attr = ScpFileAttr.getAttr(session, fullPath);

		return makeHostFile(remotePath, fileName, attr);
	}

	public IHostFile[] getRoots(IProgressMonitor monitor)
			throws SystemMessageException {
		IHostFile root = null;
		try {
			root = getFile(null, SshFileUtils.TARGET_SEPARATOR, monitor);
		} catch (SystemMessageException e) {
			Activator.warn("Failed to get root file", e); //$NON-NLS-1$
		}
		if (root == null)
			root = new SftpHostFile(null, SshFileUtils.TARGET_SEPARATOR, true,
					true, false, 0, 0);

		return new IHostFile[] { root };

	}

	public IHostFile getUserHome() {
		if (fUserHome == null) {
			try {
				Session sess = getSession();
				fUserHome = SshFileUtils.execCommand(sess, "cd ;pwd").split( //$NON-NLS-1$
						SshFileUtils.EOL_STRING)[0];
			} catch (Exception e) {
				Activator.warn("Failed to execute pwd", e); //$NON-NLS-1$
				return null;
			}
		}
		if (fUserHome == null)
			return null;
		int lastSlash = fUserHome
				.lastIndexOf(SshFileUtils.TARGET_SEPARATOR_CHAR);
		String name = fUserHome.substring(lastSlash + 1);
		String parent = fUserHome.substring(0, lastSlash+1);
		IHostFile rc = null;
		try {
			rc = getFile(parent, name, null);
		} catch (SystemMessageException e) {
			Activator.warn("Failed to get user home file ", e); //$NON-NLS-1$
		}
		if (rc == null)
			rc = new SftpHostFile(null, fUserHome, true, true, false, 0, 0);
		return rc;
	}

	public boolean isCaseSensitive() {
		return true;
	}

	public void move(String oldPath, String oldName, String newPath,
			String newName, IProgressMonitor arg4)
			throws SystemMessageException {
		Session session = getSession();
		String oldFullPathEsc = SshFileUtils.concatEscape(newPath, oldName);
		String newFullPathEsc = SshFileUtils.concatEscape(newPath, newName);

		SshFileUtils.execCommandSafe(session, "mv " + oldFullPathEsc + " " //$NON-NLS-1$ //$NON-NLS-2$
				+ newFullPathEsc);
	}

	public void rename(String remotePath, String oldName, String newName,
			IProgressMonitor monitor) throws SystemMessageException {
		Session session = getSession();
		String oldFullPathEsc = SshFileUtils.concatEscape(remotePath, oldName);
		String newFullPathEsc = SshFileUtils.concatEscape(remotePath, newName);

		SshFileUtils.execCommandSafe(session, "mv " + oldFullPathEsc + " " //$NON-NLS-1$ //$NON-NLS-2$
				+ newFullPathEsc);

	}

	public void rename(String remotePath, String oldName, String newName,
			IHostFile hostFile, IProgressMonitor monitor)
			throws SystemMessageException {
		// hostFile remains unused
		rename(remotePath, oldName, newName, monitor);
	}

	public void setLastModified(String arg0, String arg1, long arg2,
			IProgressMonitor arg3) throws SystemMessageException {
		throwSystemException(SshServiceResources.ScpFileService_LastModifiedNotSupportedError);
	}

	public void setReadOnly(String remotePath, String fileName,
			boolean disableWrite, IProgressMonitor monitor)
			throws SystemMessageException {
		Session session = getSession();
		String fullPath = SshFileUtils.concat(remotePath, fileName);
		ScpFileAttr attr = ScpFileAttr.getAttr(session, fullPath);
		if (attr == null)
			throwSystemException(SshServiceResources.ScpFileService_AttributesError + fullPath);
		int perm = new HostFilePermissions(attr.getAttrs(), "", "") //$NON-NLS-1$ //$NON-NLS-2$
				.getPermissionBits();
		if (disableWrite)
			perm &= ~0222;
		else
			perm |= 0200;
		SshFileUtils.execCommandSafe(
				session,
				"chmod " + Integer.toOctalString(perm) + " "  //$NON-NLS-1$//$NON-NLS-2$
						+ SshFileUtils.escapePath(fullPath));
	}

	public void upload(InputStream stream, String remotePath,
			String remoteFile, boolean isBinary, String hostEncoding,
			IProgressMonitor monitor) throws SystemMessageException {

	}

	private void internalDownload(String remoteParent, String fileName,
			File localFile, IProgressMonitor monitor) throws Exception {
		String remotePath = SshFileUtils.concat(remoteParent, fileName);
		String cmd = "scp -f " + SshFileUtils.escapePath(remotePath); //$NON-NLS-1$

		ChannelExec ch = SshFileUtils.openExecChannel(getSession(), cmd);
		InputStream is = ch.getInputStream();
		OutputStream os = ch.getOutputStream();
		ch.connect();

		byte buf[] = new byte[1024];
		// send '0'
		buf[0] = 0;
		os.write(buf, 0, 1);
		os.flush();

		int c = is.read();
		if (c == 1) {
			String errmsg = SshFileUtils.readString(is);
			throw new Exception(NLS.bind(SshServiceResources.ScpFileService_DownloadException,remotePath,
					errmsg));
		}
		if (c != 'C')
			throw new Exception(NLS.bind(SshServiceResources.ScpFileService_DownloadException1,remotePath, ""+c)); //$NON-NLS-1$

		// read '0644'
		is.read(buf, 0, 5);

		// get file size
		long filesize = 0;
		while (true) {
			if (is.read(buf, 0, 1) < 0)
				break;
			if (buf[0] == ' ')
				break;
			filesize = filesize * 10 + (buf[0] - '0');
		}
		String fname = SshFileUtils.readString(is);

		Activator.log("filesize=" + filesize + " fname=" + fname);  //$NON-NLS-1$//$NON-NLS-2$

		// Confirm that file description is read by sending '0'
		buf[0] = 0;
		os.write(buf, 0, 1);
		os.flush();

		FileOutputStream fos = new FileOutputStream(localFile);
		monitor.beginTask(SshServiceResources.ScpFileService_DownloadFileTaskName, (int) filesize);
		long bytesDownloaded = 0;
		while (true) {
			int len = buf.length;
			if (filesize - bytesDownloaded < len)
				len = (int) (filesize - bytesDownloaded);
			int rc = is.read(buf, 0, len);
			if (rc < 0)
				break;
			monitor.worked(rc);
			fos.write(buf, 0, rc);
			bytesDownloaded += rc;
			if (bytesDownloaded >= filesize)
				break;
		}
		fos.close();
		fos = null;
		monitor.done();

		c = is.read();
		if (c == 0) {
			// Confirm that file was received by sending '0'
			buf[0] = 0;
			os.write(buf, 0, 1);
			os.flush();
		}

		os.close();
		is.close();

		ch.disconnect();
	}

	private void internalUpload(File localFile, String remotePath,
			String remoteFile, IProgressMonitor monitor) throws Exception {

		monitor.beginTask(SshServiceResources.ScpFileService_UploadFileTaskName, (int) localFile.length() + 10);

		Session session = getSession();
		String cmd = "scp -p -t " + SshFileUtils.escapePath(remotePath); //$NON-NLS-1$
		ChannelExec ch = SshFileUtils.openExecChannel(session, cmd);
		InputStream is = ch.getInputStream();
		OutputStream os = ch.getOutputStream();
		ch.connect();
		monitor.internalWorked(5);

		String fileHeader = "C0644" + " " + localFile.length() + " "  //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
				+ remoteFile + "\n"; //$NON-NLS-1$
		if (readAck(is) != 0)
			throw new Exception(SshServiceResources.ScpFileService_ReadAckError
					+ getLastError());

		os.write(fileHeader.getBytes());
		os.flush();

		monitor.internalWorked(4);

		FileInputStream fis = new FileInputStream(localFile);

		byte[] buf = new byte[1024];
		while (true) {
			int len = fis.read(buf, 0, buf.length);
			if (len <= 0)
				break;
			os.write(buf, 0, len);
			monitor.internalWorked(len);
		}
		fis.close();

		buf[0] = 0;
		os.write(buf, 0, 1);
		os.flush();
		if (readAck(is) != 0)
			throw new Exception(NLS.bind(SshServiceResources.ScpFileService_UploadException
					,localFile.getAbsolutePath(),getLastError()));
		monitor.internalWorked(1);

		os.close();
		is.close();
		monitor.done();
		ch.disconnect();
	}

	public void upload(File localFile, String remotePath, String remoteFile,
			boolean isBinary, String srcEncoding, String hostEncoding,
			IProgressMonitor monitor) throws SystemMessageException {

		try {
			internalUpload(localFile, remotePath, remoteFile, monitor);
		} catch (Exception e) {
			SshFileUtils.throwSystemException(e);
		}

	}

	private IHostFile makeHostFile(String remotePath, String fileName,
			ScpFileAttr attr) {
		boolean isRoot = (remotePath == null || remotePath.length() == 0);
		if (attr == null) {
			SftpHostFile sftpHostFile = new SftpHostFile(isRoot ? null : remotePath, fileName, false,
					isRoot, false, 0, 0);
			sftpHostFile.setExists(false);
			return sftpHostFile;
		}

		boolean isLink = attr.isLink();
		boolean isDir = attr.isDirectory();
		if (fileName == null)
			fileName = attr.getName();
		SftpHostFile node = new SftpHostFile(isRoot ? null : remotePath,
				fileName, isDir, isRoot, isLink, 1000L * attr.getMTime(),
				attr.getSize());
		if (isLink && attr.getLinkName() != null)
			node.setLinkTarget(attr.getLinkName());
		node.setPermissions(attr.getFilePermissions());
		return node;
	}

	public String getDescription() {
		return SshServiceResources.ScpFileService_Description;
	}

	public String getName() {
		return SshServiceResources.ScpFileService_Name;
	}

}
