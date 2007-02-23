/********************************************************************************
 * Copyright (c) 2007 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * {Kushal Munir} (IBM) - initial implementation.
 ********************************************************************************/

package org.eclipse.rse.eclipse.filesystem;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;

public class RSEFileCache {
	
	private static RSEFileCache instance;
	
	/**
	 * Constructor.
	 */
	private RSEFileCache() {
	}
	
	/**
	 * Gets the singleton instance.
	 * @return the singleton instance.
	 */
	public static RSEFileCache getInstance() {
		
		if (instance == null) {
			instance = new RSEFileCache();
		}
		
		return instance;
	}
	
	/**
	 * Returns whether the remote file exists in the cache. Returns <code>true</code> if the file exists in the cache, <code>false</code> otherwise.
	 * @param remoteFile the remote file.
	 * @return <code>true</code> if the file exists in the cache, <code>false</code> otherwise.
	 */
	public boolean isExistsInCache(IRemoteFile remoteFile) {
		File file = getFromCache(remoteFile);
		
		if (file != null) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Writes to the cache. If this is a directory, it simply creates the folder.
	 * @param remoteFile the remote file.
	 * @param inputStream the input stream with the contents of the remote file, or <code>null</code> if this is a directory.
	 * @return the file in the cache.
	 */
	public File writeToCache(IRemoteFile remoteFile, InputStream inputStream) {

		String path = getCachePath(remoteFile.getParentRemoteFileSubSystem().getHost().getHostName(), remoteFile.getAbsolutePath());
		File file = new File(path);
		
		if (remoteFile.isDirectory()) {
			
			if (!file.exists()) {
				boolean success = file.mkdirs();
			
				if (success) {
					return file;
				}
				else {
					return null;
				}
			}
			else {
				return file;
			}
		}
		else {
			File parent = file.getParentFile();
			
			if (!parent.exists()) {
				parent.mkdirs();
			}
		}
		
		FileOutputStream outputStream = null;
		BufferedOutputStream bufOutputStream = null;
		
		try {
			outputStream = new FileOutputStream(file);
			bufOutputStream = new BufferedOutputStream(outputStream);
			
			byte[] buffer = new byte[4096];
			
			int readCount;
			
			while((readCount = inputStream.read(buffer)) > 0) {
				bufOutputStream.write(buffer, 0, readCount);
			}
			
			bufOutputStream.flush();
			inputStream.close();
			bufOutputStream.close();
		}
		catch (Exception e) {
			return null;
		}
		
		return file;
	}
	
	/**
	 * Gets the cached file for the remote file from the cache if any.
	 * @param remoteFile the remote file.
	 * @return the cached file, or <code>null</code> if none.
	 */
	public File getFromCache(IRemoteFile remoteFile) {
		String path = getCachePath(remoteFile.getParentRemoteFileSubSystem().getHost().getHostName(), remoteFile.getAbsolutePath());
		File file = new File(path);
		
		if (file.exists()) {
			return file;
		}
		else {
			return null;
		}
	}
	
	/**
	 * Gets the cache path.
	 * @param hostname the hostname.
	 * @param remotePath the remote path.
	 * @return the cache path.
	 */
	private String getCachePath(String hostname, String remotePath) {
		IPath path = Platform.getStateLocation(Activator.getDefault().getBundle());
		path = path.makeAbsolute();
		path = path.addTrailingSeparator();
		path = path.append(hostname);
		path = path.addTrailingSeparator();
		path = path.append(remotePath);
		return path.toOSString();
	}
}