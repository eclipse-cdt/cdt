/**********************************************************************
 * Copyright (c) 2002,2003 Timesys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Timesys - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.core.builder.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.eclipse.core.internal.runtime.Assert;

/**
 * Singleton that wraps the concept of the current OS's
 * filesystem in a way that allows us to work properly
 * under Cygwin.
 */
public class Filesystem {
	
	private static IFilesystem fInstance;

	/**
	 * Create the IFilesystem instance appropriate for the current OS.
	 * 
	 * Right now, this is based off of the speratorChar reported by
	 * java.io.File; there is probably a better way to deal with this.
	 */
	static {
		// init to null, update with class reference if we can
		// otherwise leave null to signal that we don't have a valid file system.			
		if (File.separatorChar == IFilesystem.FILESYSTEM_ROOT.charAt(0)) {
			fInstance = new CUnixFilesystem();
		} else {
			fInstance = new CCygwinFilesystem();
		}
	}

	private static IFilesystem getInstance() {
		if (fInstance == null) {
			throw new FileSystemException ("Problems encountered while searching for your file system.");
		}
		return fInstance;
	}

	public static boolean isValid() {
		return (fInstance != null);
	}
		
	public static class FileSystemException extends Error {
		FileSystemException (String s) {
			super(s);
		}
	}

	/**
	 * Private constructor to prevent instatiation.
	 * 
	 * All members of this class are static, and intended to be accessed
	 * via "Filesystem.[method_name]".
	 */
	private Filesystem() {
	}

	/**
	 * @see org.eclipse.cdt.core.builder.util.IFilesystem#getRoot()
	 */
	public static String getRoot() {
		return getInstance().getRoot();
	}

	/**
	 * @see org.eclipse.cdt.core.builder.util.IFilesystem#getNativePath(String)
	 */
	public static String getNativePath(String path)  {
		return getInstance().getNativePath(path);
	}

	/**
	 * @see org.eclipse.cdt.core.builder.util.IFilesystem#getNativePath(File)
	 */
	public static String getNativePath(File path)  {
		return getInstance().getNativePath(path);
	}

	/**
	 * @see org.eclipse.cdt.core.builder.util.IFilesystem#getUnixPath(String)
	 */
	public static String getUnixPath(String path)  {
		return getInstance().getUnixPath(path);
	}

	/**
	 * @see org.eclipse.cdt.core.builder.util.IFilesystem#getUnixPath(File)
	 */
	public static String getUnixPath(File path)  {
		return getInstance().getUnixPath(path);
	}

	/**
	 * Copy a file from sourceFile to destFile.  Performs a binary file copy,
	 * reading data from sourceFile as a byte stream, and writing it to destFile
	 * as a byte stream.
	 * 
	 * @param sourceFile File to copy.
	 * @param destFile Where to copy the file to.
	 * @param replaceIfExists If true, if destFile exists, it is replaced.
	 * @return True if the file was copied; false otherwise.
	 */
	public static boolean copyFile(File sourceFile, File destFile, boolean replaceIfExists) {
		Assert.isNotNull(sourceFile);
		Assert.isNotNull(destFile);

		if (!sourceFile.exists()) {
			return false;
		}

		if (sourceFile.equals(destFile)) {
			return false;
		}

		if (replaceIfExists && destFile.exists()) {
			destFile.delete();
		}
		
		if (destFile.exists()) {
			return false;
		}

	    FileInputStream 	fis = null;
	    FileOutputStream	fos = null;
	    byte[]				buf = new byte[1024];
	    int 				i 	= 0;
	    
		try {
			fis = new FileInputStream(sourceFile);
	    	fos = new FileOutputStream(destFile);
		    
		    while(-1 != (i = fis.read(buf))) {
		      fos.write(buf, 0, i);
			}
			
		    fos.close();
		    fis.close();
		} catch (Exception e) {
			e.printStackTrace();
			if (destFile.exists()) {
				destFile.delete();
			}
			return false;
		}
		
		return true;
	}
}
