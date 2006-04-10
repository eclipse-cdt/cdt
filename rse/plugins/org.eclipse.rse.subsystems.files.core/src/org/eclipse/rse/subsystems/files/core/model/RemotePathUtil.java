/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.subsystems.files.core.model;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Singleton utility class for remote paths.
 */
public class RemotePathUtil {
	
	private static RemotePathUtil instance;

	/**
	 * Constructor.
	 */
	private RemotePathUtil() {
		super();
	}
	
	/**
	 * Returns the singleton instance.
	 * @return the singleton instance.
	 */
	public static RemotePathUtil getInstance() {
		
		if (instance == null) {
			instance = new RemotePathUtil();
		}
		
		return instance;
	}
	
	/**
	 * Returns the path of the temp location corresponding to the remote path. The remote
	 * path must represent a file (i.e. can not be a directory).
	 * The temp path is simply the default temp location on the client system (given by system property
	 * "java.io.tmpdir"), with the name of the remote file appended (not the path of the
	 * remote file).
	 * @param remotePath the remote path.
	 * @return the path of the temporary file, or <code>null</code> if the default temporary location
	 * is not available.
	 */
	public IPath getClientTempLocationForFile(IRemotePath remotePath) {
		String tempDirString = System.getProperty("java.io.tmpdir");
		
		IPath tempPath = new Path(tempDirString);
		
		if (tempDirString == null) {
			return null;
		}
		else {
			
			String absolutePath = remotePath.getAbsolutePath();
			
			// try '/' first
			String sep = "/";
			
			boolean isVirtual = remotePath.isVirtual();
			
			IPath newPath = new Path(tempPath.toOSString());
			
			newPath = appendRemoteFileNameToPath(newPath, absolutePath, sep);
			
			if (!newPath.equals(tempPath)) {
				return newPath;
			}
			else {
				
				// for virtual separator is '/'
				if (isVirtual) {
					return null;
				}
				
				sep = "\\";
				
				newPath = appendRemoteFileNameToPath(newPath, absolutePath, sep);
				
				if (!newPath.equals(tempPath)) {
					return newPath;
				}
				else {
					return null;
				}
			}
		}
	}
	
	/**
	 * Apnnds a file name to a path given the absolute path of a file.
	 * The absolute path name of the file must not end with the given separator (we assume
	 * that a directory will end with a separator).
	 * @param path the path to which to append.
	 * @param absolutePath the absolute path from which the name will be obtained.
	 * @param sep the separator to parse the absolute path, and get the name.
	 * @return the new path, or the same path if the separator was not found, or
	 * was found to be the last character in the absolute path.
	 */
	private IPath appendRemoteFileNameToPath(IPath path, String absolutePath, String sep) {

		int sepIndex = absolutePath.lastIndexOf(sep);
		
		// found separator
		if (sepIndex != -1) {
			
			// if not the last character in the path, then strip out the last segment
			if (sepIndex != (absolutePath.length()-1)) {
				return path.append(absolutePath.substring(sepIndex+1));
			}
			// separator is last character in path, so it must be a directory
			else {
				return path;
			}
		}
		// no separator found so simply append the absolute path
		else {
			return path.append(absolutePath);
		}
	}
}