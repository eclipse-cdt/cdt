/*******************************************************************************
 * Copyright (c) 2009, 2010 Mentor Graphics Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Nikita Shulga - initial API and implementation 
 *******************************************************************************/

package org.eclipse.rse.internal.subsystems.files.scp;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.rse.services.clientserver.messages.SystemMessageException;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;

public class ScpFileUtils {
	public static final String EOL_STRING = "\n"; //$NON-NLS-1$
	public static final String TARGET_SEPARATOR = "/"; //$NON-NLS-1$
	public static final String EXEC_CHANNEL = "exec"; //$NON-NLS-1$
	public static final String QUOTATION_MARK = "\""; //$NON-NLS-1$

	public static char EOL_CHAR = EOL_STRING.charAt(0);
	public static char TARGET_SEPARATOR_CHAR = TARGET_SEPARATOR.charAt(0);

	/**
	 * Concatenate a parent directory with a file name to form a new proper path
	 * name.
	 * 
	 * This method was cloned from
	 * org.eclipse.rse.services.ssh / SftpFileService#concat()
	 */
	protected static String concat(String parentDir, String fileName) {
		// See also {@link SftpHostFile#getAbsolutePath()}
		if (parentDir == null || parentDir.length() == 0) {
			// Looking at a Root
			return fileName;
		}

		StringBuffer path = new StringBuffer(parentDir);
		if (!parentDir.endsWith(TARGET_SEPARATOR))
			path.append(TARGET_SEPARATOR_CHAR);

		path.append(fileName);
		return path.toString();
	}

	public static ChannelExec openExecChannel(Session sess) throws Exception {
		return (ChannelExec) sess.openChannel(EXEC_CHANNEL);
	}

	public static ChannelExec openExecChannel(Session sess, String cmd)
			throws Exception {
		ChannelExec ch = openExecChannel(sess);
		if (ch != null)
			ch.setCommand(cmd);
		return ch;
	}

	public static String execCommand(Session sess, String command)
			throws Exception {
		ChannelExec ch = openExecChannel(sess, command);
		ch.setInputStream(null);
		ch.setErrStream(System.err, true);
		InputStream is = ch.getInputStream();
		ch.connect();

		String str = readStream(ch);
		is.close();
		ch.disconnect();
		return str;
	}

	public static String execCommandSafe(Session session, String command)
			throws SystemMessageException {
		String rc = null;
		try {
			rc = execCommand(session, command);
		} catch (Exception e) {
			ScpFileService.throwSystemException(e);
		}
		return rc;
	}

	public static String escapePath(String path) {
		if (path.indexOf(' ') < 0)
			return path;
		return QUOTATION_MARK + path + QUOTATION_MARK;
	}

	/**
	 * Concatenates parent directory with file name and escape the result if
	 * necessary
	 * 
	 * @param parentDir
	 *            parent directory
	 * @param fileName
	 *            file name
	 * @return escaped concatenated path to the file
	 */
	protected static String concatEscape(String parentDir, String fileName) {
		return escapePath(concat(parentDir, fileName));
	}

	public static String readString(InputStream is) throws IOException {
		StringBuffer buf = new StringBuffer();
		int rc = 0;
		while (true) {
			rc = is.read();
			if (rc == EOL_CHAR)
				break;
			buf.append((char) rc);
		}
		return buf.toString();
	}

	public static String readStream(ChannelExec ch) throws IOException {
		InputStream is = ch.getInputStream();
		byte[] buf = new byte[1024];
		String rc = ""; //$NON-NLS-1$
		while (!ch.isClosed() || is.available() > 0) {
			int cnt = is.read(buf, 0, buf.length);
			if (cnt < 0)
				break;
			rc += new String(buf, 0, cnt);
		}
		return rc;
	}

}
