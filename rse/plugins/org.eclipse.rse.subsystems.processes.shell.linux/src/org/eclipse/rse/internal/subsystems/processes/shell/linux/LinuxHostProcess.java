/********************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others. All rights reserved.
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
 * Yu-Fen Kuo (MontaVista) - adapted from RSE AbstractHostProcess
 * Martin Oberhuber (Wind River) - [refactor] "shell" instead of "ssh" everywhere
 *******************************************************************************/

package org.eclipse.rse.internal.subsystems.processes.shell.linux;

import java.util.StringTokenizer;

import org.eclipse.rse.services.processes.AbstractHostProcess;

/**
 * This class parses one line of the process record at a time and saves property
 * value into it's member fields.
 * 
 */
public class LinuxHostProcess extends AbstractHostProcess {

	private static String NAME = "Name"; //$NON-NLS-1$

	private static String STATE = "State"; //$NON-NLS-1$

	private static String TGID = "Tgid"; //$NON-NLS-1$

	private static String PID = "Pid"; //$NON-NLS-1$

	private static String PPID = "PPid"; //$NON-NLS-1$

	private static String TRACERPID = "TracerPid"; //$NON-NLS-1$

	private static String UID = "Uid"; //$NON-NLS-1$

	private static String GID = "Gid"; //$NON-NLS-1$

	private static String VMSIZE = "VmSize"; //$NON-NLS-1$

	private static String VMRSS = "VmRSS"; //$NON-NLS-1$

	private static String STATUS_DELIMITER = "|"; //$NON-NLS-1$

	private LinuxProcessHelper linuxProcessHelper;

	/**
	 * constructor
	 * 
	 * @param linuxProcessHelper
	 */
	public LinuxHostProcess(LinuxProcessHelper linuxProcessHelper) {
		super();
		this.linuxProcessHelper = linuxProcessHelper;
	}

	/**
	 * process one line of process record and based on the property name, put
	 * the corresponding value in the right member field(s). The state value
	 * would get converted into state code before the value is saved.
	 * 
	 * @param line
	 *            one line of the process record
	 */
	public void processLine(String line) {
		String[] result = line.split(":"); //$NON-NLS-1$
		if (result.length < 2)
			return;
		String propertyName = result[0];
		String propertyValue = result[1].trim();
		if (NAME.equals(propertyName)) {
			this.setName(propertyValue);
			this.setLabel(propertyValue);
			// initialize some of the properties
			this.setVmSizeInKB("0"); //$NON-NLS-1$
			this.setVmRSSInKB("0"); //$NON-NLS-1$
		} else if (STATE.equals(propertyName)) {
			String firstValue = getFirstValue(propertyValue);
			if (firstValue != null)
				this.setState(linuxProcessHelper
						.convertToStateCode(propertyValue));
		} else if (TGID.equals(propertyName)) {
			this.setTgid(propertyValue);
		} else if (PID.equals(propertyName)) {
			this.setPid(propertyValue);
		} else if (PPID.equals(propertyName)) {
			this.setPPid(propertyValue);
		} else if (TRACERPID.equals(propertyName)) {
			this.setTracerPid(propertyValue);
		} else if (TRACERPID.equals(propertyName)) {
			this.setTracerPid(propertyValue);
		} else if (UID.equals(propertyName)) {
			String firstValue = getFirstValue(propertyValue);
			if (firstValue != null) {
				this.setUid(firstValue);
				this.setUsername(linuxProcessHelper.getUsername(firstValue));
			}
		} else if (GID.equals(propertyName)) {
			String firstValue = getFirstValue(propertyValue);
			if (firstValue != null)
				this.setGid(firstValue);
		} else if (VMSIZE.equals(propertyName)) {
			String firstValue = getFirstValue(propertyValue);
			if (firstValue != null)
				this.setVmSizeInKB(firstValue);
		} else if (VMRSS.equals(propertyName)) {
			String firstValue = getFirstValue(propertyValue);
			if (firstValue != null)
				this.setVmRSSInKB(firstValue);
		}
	}

	/**
	 * checks if the line is a starting of a new process record New process
	 * record should start with Name: string
	 * 
	 * @param line
	 *            one line of process record
	 * @return true if is a new process record
	 */
	public static boolean isNewRecord(String line) {
		String[] result = line.split(":"); //$NON-NLS-1$

		if (result.length >= 2 && NAME.equals(result[0])) {
			return true;
		}
		return false;
	}

	private String getFirstValue(String propertyValue) {
		StringTokenizer st = new StringTokenizer(propertyValue);
		if (st.hasMoreTokens()) {
			return st.nextToken();
		}
		return null;
	}

	// "pid|name|status|tgid|ppid|tracerpid|uid|username|gid|vmSize|vmRSS"
	/**
	 * format the property values in the format that IHostProcessFilter.allows()
	 * can use it to determine if this process record is allowed by the filter
	 * to be displayed to the user.
	 * 
	 * @return formatted string in the following format:
	 *         "pid|name|status|tgid|ppid|tracerpid|uid|username|gid|vmSize|vmRSS"
	 */
	public String getStatusLine() {
		StringBuffer statusLine = new StringBuffer();
		statusLine = statusLine.append(getPid()).append(STATUS_DELIMITER);
		statusLine = statusLine.append(getName()).append(STATUS_DELIMITER);
		statusLine = statusLine.append(getState()).append(STATUS_DELIMITER);
		statusLine = statusLine.append(getTgid()).append(STATUS_DELIMITER);
		statusLine = statusLine.append(getPPid()).append(STATUS_DELIMITER);
		statusLine = statusLine.append(getTracerPid()).append(STATUS_DELIMITER);
		statusLine = statusLine.append(getUid()).append(STATUS_DELIMITER);
		statusLine = statusLine.append(getUsername()).append(STATUS_DELIMITER);
		statusLine = statusLine.append(getGid()).append(STATUS_DELIMITER);
		statusLine = statusLine.append(getVmSizeInKB())
				.append(STATUS_DELIMITER);
		statusLine = statusLine.append(getVmRSSInKB()).append(STATUS_DELIMITER);

		return statusLine.toString();
	}
}
