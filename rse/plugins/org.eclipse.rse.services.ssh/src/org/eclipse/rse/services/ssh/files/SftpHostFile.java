/********************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation. All rights reserved.
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
 * Martin Oberhuber (Wind River) - Adapted from FTPHostFile.
 ********************************************************************************/

package org.eclipse.rse.services.ssh.files;

import java.io.File;

import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.files.IHostFile;

public class SftpHostFile implements IHostFile {

	private String fName;
	private String fParentPath;
	private boolean fIsDirectory = false;
	private boolean fIsRoot = false;
	private boolean fIsArchive = false;
	private boolean fExists = true;
	private long fLastModified = 0;
	private long fSize = 0;
	private boolean fIsLink = false;
	private String[] fExtended = null;
	
	//TODO just re-use or extend FTPHostFile instead of copying here?
	public SftpHostFile(String parentPath, String name, boolean isDirectory, boolean isRoot, boolean isLink, long lastModified, long size) {
		fParentPath = parentPath;
		fName = name;
		fIsDirectory = isDirectory;
		fIsRoot = isRoot;
		fLastModified = lastModified;
		fSize = size;
		fIsLink = isLink;
		fIsArchive = internalIsArchive();
	}

	public String getName() {
		return fName;			
	}
	
	public boolean isHidden() {
		String name = getName();
		return name.charAt(0) == '.';
	}
	
	public String getParentPath() {
		return fParentPath;
	}
	
	public boolean isDirectory() {
		return fIsDirectory;
	}
	
	public boolean isFile() {
		return !(fIsDirectory || fIsRoot || fIsLink);
	}
	
	public boolean isRoot() {
		return fIsRoot;
	}
	
	public void setExists(boolean b) {
		fExists = b;
	}
	
	public boolean exists() {
		return fExists;
	}

	public String getAbsolutePath() {
		if (isRoot()) {
			return getName();
		} else {
			StringBuffer path = new StringBuffer(getParentPath());
			if (!fParentPath.endsWith("/"))
			{
				path.append('/');
			}
			path.append(getName());
			return path.toString();
		}
	}

	public long getSize() {
		return fSize;
	}

	public long getModifiedDate() {
		return fLastModified;
	}

	public void renameTo(String newAbsolutePath) {
		int i = newAbsolutePath.lastIndexOf("/");
		if (i == -1) {
			fName = newAbsolutePath;
		}
		else {
			fParentPath = newAbsolutePath.substring(0, i);
			fName = newAbsolutePath.substring(i+1);
		}
		fIsArchive = internalIsArchive();
	}

	protected boolean internalIsArchive() {
		return ArchiveHandlerManager.getInstance().isArchive(new File(getAbsolutePath())) 
		&& !ArchiveHandlerManager.isVirtual(getAbsolutePath());
	}
	
	public boolean isArchive() {
		return fIsArchive;
	}
	
	public boolean isLink() {
		return fIsLink;
	}
	
	/** Extended data: name:value pairs */
	public void setExtendedData(String[] extended) {
		fExtended = extended;
	}
	
	public String[] getExtendedData() {
		return fExtended;
	}

	public String getClassification() {
		//TODO: isExecutable(), shellscript vs. binary
		String result;
		if (isLink()) {
			//TODO: read symbolic link target and its type to provide e.g. "symbolic link(directory):/export4/opt
			result = "symbolic link"; //$NON-NLS-1$
		} else if (isFile()) {
			result = "file"; //$NON-NLS-1$
		} else if (isDirectory()) {
			result = "directory"; //$NON-NLS-1$
		} else {
			result = "unknown"; //default-fallback //$NON-NLS-1$
		}
		return result;
	}
}
