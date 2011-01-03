/*******************************************************************************
 * Copyright (c) 2008 Radoslav Gerganov
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Radoslav Gerganov - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.rapi.examples;

import org.eclipse.tm.rapi.IRapiDesktop;
import org.eclipse.tm.rapi.IRapiDevice;
import org.eclipse.tm.rapi.IRapiEnumDevices;
import org.eclipse.tm.rapi.IRapiSession;
import org.eclipse.tm.rapi.Rapi;
import org.eclipse.tm.rapi.ProcessInformation;
import org.eclipse.tm.rapi.RapiConnectionInfo;
import org.eclipse.tm.rapi.RapiDeviceInfo;
import org.eclipse.tm.rapi.RapiException;
import org.eclipse.tm.rapi.RapiFindData;

/**
 * This class demonstrates example usage of RAPI2.
 * 
 * @author Radoslav Gerganov
 */
public class RapiExamples {

	IRapiDesktop desktop = null;
	IRapiEnumDevices enumDevices = null;
	IRapiDevice device = null;
	IRapiSession session = null;

	/**
	 * Initialize the underlying natives.
	 */
	public void initRapi() {
	  Rapi.initialize(Rapi.COINIT_MULTITHREADED);
	}

	/**
	 * Uninitialize the underlying natives.
	 */
	public void uninitRapi() {
		if (desktop != null) {
			desktop.release();
		}
		if (enumDevices != null) {
			enumDevices.release();
		}
		if (device != null) {
			device.release();
		}
		if (session != null) {
			session.release();
		}
		Rapi.uninitialize();
	}

	/**
	 * Prints various information about the device.
	 */
	public void printDeviceInfo() throws RapiException {
		System.out.println(">>> printDeviceInfo()");
		RapiDeviceInfo deviceInfo = device.getDeviceInfo();
		System.out.println("Device id: " + deviceInfo.id);
		System.out.println("Device name: " + deviceInfo.name);
		System.out.println("Platform: " + deviceInfo.platform);
		System.out.println("Major version: " + deviceInfo.versionMajor);
		System.out.println("Minor version: " + deviceInfo.versionMinor);
	}

	/**
	 * Prints information about how the device is connected.
	 */
	public void printConnectionInfo() throws RapiException {
		System.out.println(">>> printConnectionInfo()");
		RapiConnectionInfo connectionInfo = device.getConnectionInfo();
		System.out.println("Connection type: " + connectionInfo);
		System.out.println("IsConnected: " + device.isConnected());
	}

	/**
	 * Creates file on the device with the specified filename.
	 */
	public void createFile(String fileName) throws RapiException {
		System.out.println(">>> createFile()");
		int handle = session.createFile(fileName, Rapi.GENERIC_WRITE,
		    Rapi.FILE_SHARE_READ, Rapi.CREATE_ALWAYS, Rapi.FILE_ATTRIBUTE_NORMAL);
		byte[] content = "Hello world!".getBytes();
		session.writeFile(handle, content);
		session.closeHandle(handle);
	}

	/**
	 * Reads the specified file on the device and prints its content.
	 */
	public void readFile(String fileName) throws RapiException {
		System.out.println(">>> readFile()");
		int handle = session.createFile(fileName, Rapi.GENERIC_READ,
		    Rapi.FILE_SHARE_READ, Rapi.OPEN_EXISTING, Rapi.FILE_ATTRIBUTE_NORMAL);
		byte[] buf = new byte[256];
		int br = session.readFile(handle, buf);
		System.out.println("readFile: " + new String(buf, 0, br));
		System.out.println("bytesRead: " + br);
		session.closeHandle(handle);
	}

	/**
	 * Utility method used to determine if the specified <code>RapiFindData</code>
	 * describes a directory.
	 */
	boolean isDirectory(RapiFindData findData) {
		return (findData.fileAttributes & Rapi.FILE_ATTRIBUTE_DIRECTORY) != 0;
	}

	/**
	 * Utility method used for printing <code>RapiFindData</code> on the console.
	 */
	void printFindData(RapiFindData findData, int indent) {
		for (int i = 0 ; i < indent ; i++) {
			System.out.print(" ");
		}
		String fileName = findData.fileName;
		if (isDirectory(findData)) {
			System.out.println("[" + fileName + "]");
		} else {
			System.out.println(fileName + " (" + findData.fileSize + ")");
		}
	}

	/**
	 * List all files in the specified directory using
	 * <code>IRapiSession.findFirstFile</code> and
	 * <code>IRapiSession.findNextFile</code>
	 */
	public void listFiles(String dir) throws RapiException {
		System.out.println(">>> listFiles()");
		RapiFindData findData = new RapiFindData();
		int fh = session.findFirstFile(dir + "*", findData);
		while (findData != null) {
			printFindData(findData, 0);
			findData = session.findNextFile(fh);
		}
		session.findClose(fh);
	}

	/**
	 * List all files in the specified directory using
	 * <code>IRapiSession.findAllFiles</code>
	 */
	public void listFiles2(String dir) throws RapiException {
		System.out.println(">>> listFiles2()");
		RapiFindData[] fdArr = session.findAllFiles(dir + "*",
		    Rapi.FAF_ATTRIBUTES | Rapi.FAF_NAME | Rapi.FAF_SIZE_LOW);
		for (int i = 0 ; i < fdArr.length ; i++) {
			printFindData(fdArr[i], 0);
		}
	}

	/**
	 * Prints various information about the specified file.
	 */
	public void statFile(String fileName) throws RapiException {
		System.out.println(">>> statFile()");
		int handle = session.createFile(fileName, Rapi.GENERIC_READ,
		    Rapi.FILE_SHARE_READ, Rapi.OPEN_EXISTING, Rapi.FILE_ATTRIBUTE_NORMAL);
		int fileAttributes = session.getFileAttributes(fileName);
		System.out.println("fileAttributes: " + fileAttributes);
		long fileSize = session.getFileSize(handle);
		System.out.println("fileSize: " + fileSize);
		System.out.println("creationTime: " + session.getFileCreationTime(handle));
		System.out.println("lastAccessTime: " + session.getFileLastAccessTime(handle));
		System.out.println("lastWriteTime: " + session.getFileLastWriteTime(handle));
		session.closeHandle(handle);
	}

	/**
	 * Recursively print the whole device tree on the console.
	 */
	void printDeviceTree(String dir, int indent) throws RapiException {
		RapiFindData[] fdArr = session.findAllFiles(dir + "*",
		    Rapi.FAF_ATTRIBUTES | Rapi.FAF_NAME | Rapi.FAF_SIZE_LOW);
		if (fdArr == null) {
			return;
		}
		for (int i = 0 ; i < fdArr.length ; i++) {
			if (isDirectory(fdArr[i])) {
				printDeviceTree(dir + fdArr[i].fileName + "\\", indent + 1);
			} else {
				printFindData(fdArr[i], indent);
			}
		}
	}

	/**
	 * Opens the specified URL in a browser. The browser is started with
	 * {@link IRapiSession#createProcess(String, String, int)} and the URL
	 * is passed as command line argument.
	 */
	void openUrl(String url) throws RapiException {
		System.out.println(">>> openUrl() url: " + url);
		ProcessInformation pi = session.createProcess("\\Windows\\iexplore.exe", url, 0);
		System.out.println("hProcess: " + Integer.toHexString(pi.hProcess));
		System.out.println("hThread: " + Integer.toHexString(pi.hThread));
		System.out.println("dwProcessId: " + Integer.toHexString(pi.dwProcessId));
		System.out.println("dwThreadId: " + Integer.toHexString(pi.dwThreadId));
		session.closeHandle(pi.hProcess);
		session.closeHandle(pi.hThread);
	}

	void runExamples() {
		try {
			initRapi();
			desktop = IRapiDesktop.getInstance();
			enumDevices = desktop.enumDevices();
			// get the first device
			device = enumDevices.next();
			printDeviceInfo();
			printConnectionInfo();
			// create session
			session = device.createSession();
			session.init();
			createFile("\\foo.txt");
			readFile("\\foo.txt");
			session.copyFile("\\foo.txt", "\\bar.txt");
			listFiles("\\");
			session.moveFile("\\bar.txt", "\\spam.txt");
			listFiles2("\\");
			session.deleteFile("\\spam.txt");
			statFile("\\foo.txt");
			System.out.println(">>> printDeviceTree()");
			printDeviceTree("\\", 0);
			openUrl("www.eclipse.org/tm");
			session.uninit();
		} catch (RapiException e) {
			e.printStackTrace();
		} finally {
			uninitRapi();
		}
	}
}
