/*******************************************************************************
 * Copyright (c) 2009, 2010 Mentor Graphics Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Nikita Shulga - initial API and implementation 
 * Nikita Shulga   (Mentor Graphics) - [331109] Added long-iso time format support
 * Anna Dushistova (Mentor Graphics) - [331213][scp] Provide UI-less scp IFileService in org.eclipse.rse.services.ssh
 *******************************************************************************/

package org.eclipse.rse.internal.services.ssh.files.scp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Stack;
import java.util.regex.Pattern;

import org.eclipse.rse.internal.services.ssh.Activator;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.HostFilePermissions;
import org.eclipse.rse.services.files.IHostFilePermissions;

import com.jcraft.jsch.Session;

public class ScpFileAttr {
	private String lsString;
	private String attrString = null;
	private int linkNo = 0;
	private long mTime = 0;
	private String user = null;
	private String group = null;
	private long size = -1;
	private String name = null;
	private String linkName = null;

	public IHostFilePermissions getFilePermissions() {
		return new HostFilePermissions(getAttrs(), getUser(), getGroup());
	}

	public long getMTime() {
		if (!initialized)
			doSplit();
		return mTime;
	}

	public long getSize() {
		if (!initialized)
			doSplit();
		return size;
	}

	public String getUser() {
		if (!initialized)
			doSplit();
		return user;
	}

	public String getGroup() {
		if (!initialized)
			doSplit();
		return group;
	}

	public int getLinkNo() {
		if (!initialized)
			doSplit();
		return linkNo;
	}

	public String getName() {
		if (!initialized)
			doSplit();
		return name;
	}

	public String getLinkName() {
		if (!initialized)
			doSplit();
		return linkName;
	}

	public String getAttrs() {
		if (!initialized)
			doSplit();

		if (attrString == null || attrString.length() < 9)
			return "---------"; //$NON-NLS-1$

		return attrString;
	}

	private char getFileType() {
		String attrs = getAttrs();
		if (attrs == null || attrs.length() == 0)
			return 0;
		return attrs.charAt(0);
	}

	public boolean isBlockDevice() {
		return getFileType() == 'b';
	}

	public boolean isCharDevice() {
		return getFileType() == 'c';
	}

	public boolean isLink() {
		return getFileType() == 'l';
	}

	public boolean isDirectory() {
		return getFileType() == 'd';
	}

	ScpFileAttr(String str) {
		lsString = str;
		if (lsString.endsWith("\n")) //$NON-NLS-1$
			lsString = lsString.substring(0, lsString.length() - 1);
	}

	private boolean initialized = false;

	private void doSplit() {
		if (initialized)
			return;
		initialized = true;
		try {
			SplitAux();
		} catch (Exception e) {
			Activator.warn(
					"ScpFileAttr:Exception occured while splitting  string "
							+ lsString, e);
		}
	}

	/*
	 * ls command uses two short date formats: - for files modified more than 6
	 * month ago - for files modified less than 6 month ago
	 */
	final static DateFormat lessThanSixMonthOldFormat = new SimpleDateFormat(
			"MMM dd HH:mm"); //$NON-NLS-1$
	final static DateFormat moreThanSixMonthOldFormat = new SimpleDateFormat(
			"MMM dd yyyy"); //$NON-NLS-1$

	/*
	 * ls ISO date format 
	 */
	final static DateFormat isoDateFormat = new SimpleDateFormat(
	"yyyy-MM-dd HH:mm"); //$NON-NLS-1$

	
	/**
	 * Parses date time string returned by ls command
	 * 
	 * @param date
	 *            - file modification date as string
	 * @return time in seconds since start of epoch until mTime
	 */
	static long parseDateTime(String date) {
		// TODO: Add support for ls -le date format
		try {
			Date d = lessThanSixMonthOldFormat.parse(date);
			Calendar c = Calendar.getInstance();
			c.setTime(d);
			c.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
			return c.getTimeInMillis() / 1000;
		} catch (Exception e) {
		}
		try {
			Date d = moreThanSixMonthOldFormat.parse(date);
			return d.getTime() / 1000;
		} catch (Exception e) {
		}
		try {
			Date d = isoDateFormat.parse(date);
			return d.getTime() / 1000;
		} catch (Exception e) {
		}

		return 0;
	}

	private static Pattern lsPattern = Pattern.compile("\\s+"); //$NON-NLS-1$

	public void SplitAux() throws Exception {
		Stack fields = new Stack();
		String[] lsPatterns = lsPattern.split(lsString);
		for (int i=0;i<lsPatterns.length;i++)
			fields.insertElementAt(lsPatterns[i], 0);

		/* store file attributes */
		if (fields.empty())
			return;
		attrString = (String) fields.pop();

		/* store link number */
		if (fields.empty())
			return;
		linkNo = Integer.parseInt((String) fields.pop());

		/* store uid and gid */
		if (fields.empty())
			return;
		user = (String) fields.pop();
		if (fields.empty())
			return;
		group = (String) fields.pop();

		/* store file size */
		if (fields.empty())
			return;
		if (!isCharDevice() && !isBlockDevice())
			size = Long.parseLong((String) fields.pop());
		else {
			/* Size is undefined for character and block devices */
			size = 0;
			/* And we don't know what to do with devs major/minor */
			fields.pop();
			if (fields.empty())
				return;
			fields.pop();
		}

		/* Short date formats always take three fields, long(iso) date format takes only two */
		if (fields.empty())
			return;
		String dateField = (String) fields.pop();
		if (fields.empty())
			return;
		dateField = dateField + " " + fields.pop(); //$NON-NLS-1$
		
		/*Long date format contains two dashes and colon*/
		/*If that's not the case - parse last chunk of short date format*/
		if (dateField.lastIndexOf('-')==dateField.indexOf('-') || dateField.indexOf(':') == -1) { 
			if (fields.empty())
				return;
			dateField = dateField + " " + fields.pop(); //$NON-NLS-1$
		}
		mTime = parseDateTime(dateField);
		/* The rest of the entry is name ( and may be symlink ) */
		String[] namesplit = Pattern.compile(
				dateField.replaceAll(" ", "\\s+") + "\\s").split(lsString);  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		if (namesplit.length != 2)
			return;
		name = namesplit[1];
		if (isLink()) {
			namesplit = name.split(" -> "); //$NON-NLS-1$
			if (namesplit.length != 2)
				return;
			name = namesplit[0];
			linkName = namesplit[1];
		}
	}

	public static ScpFileAttr getAttr(Session sess, String path)
			throws SystemMessageException {
		String attr = ScpFileUtils.execCommandSafe(sess, "ls -land " //$NON-NLS-1$
				+ ScpFileUtils.escapePath(path));
		if (attr == null || attr.length() < 9)
			return null;
		return new ScpFileAttr(attr);
	}

}
