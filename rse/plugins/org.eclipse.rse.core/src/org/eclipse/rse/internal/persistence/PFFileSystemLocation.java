/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.rse.internal.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.rse.core.RSECorePlugin;

class PFFileSystemLocation implements PFPersistenceLocation {
	
	private File _baseFolder;
	
	public PFFileSystemLocation(File baseFolder) {
		_baseFolder = baseFolder;
	}

	public void ensure() {
		if (!exists()) {
			_baseFolder.mkdirs();
		}
	}
	public boolean exists() {
		return _baseFolder.exists();
	}
	
	public PFPersistenceLocation getChild(String childName) {
		File childFolder = new File(_baseFolder, childName);
		return new PFFileSystemLocation(childFolder);
	}
	
	public PFPersistenceLocation[] getChildren() {
		File[] members = _baseFolder.listFiles();
		if (members == null)  members = new File[0];
		List children = new ArrayList(members.length);
		for (int i = 0; i < members.length; i++) {
			File member = members[i];
			if (member.isDirectory()) {
				PFPersistenceLocation child = new PFFileSystemLocation(member);
				children.add(child);
			}
		}
		PFPersistenceLocation[] result = new PFPersistenceLocation[children.size()];
		children.toArray(result);
		return result;
	}
	
	public InputStream getContents() {
		InputStream stream = null;
		File contentsFile = getContentsFile();
		try {
			stream = new FileInputStream(contentsFile);
		} catch (FileNotFoundException e) {
			logException(e);
		}
		return stream;
	}

	public URI getLocator() {
		return _baseFolder.toURI();
	}
	
	public String getName() {
		return _baseFolder.getName();
	}
	
	public boolean hasContents() {
		return getContentsFile().exists();
	}
	
	public void keepChildren(Set keepSet) {
		File[] children = _baseFolder.listFiles();
		if (children == null) children = new File[0];
		for (int i = 0; i < children.length; i++) {
			File child = children[i];
			if (!keepSet.contains(child.getName())) {
				deleteFile(child);
			}
		}
	}
	
	public void setContents(InputStream stream) {
		ensure();
		OutputStream out = null;
		try {
			out = new FileOutputStream(getContentsFile());
			byte[] buffer = new byte[1000];
			int n = stream.read(buffer);
			while(n > 0) {
				out.write(buffer, 0, n);
				n = stream.read(buffer);
			}
		} catch (FileNotFoundException e) {
			logException(e);
		} catch (IOException e) {
			logException(e);
		}
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
				logException(e);
			}
		}
		try {
			stream.close();
		} catch (IOException e) {
			logException(e);
		}
	}
	
	/**
	 * Force the deletion of the named "file" (which may be a folder).
	 * The file's children must be deleted first.
	 * @param file the file to delete.
	 */
	private void deleteFile(File file) {
		if (file.exists()) {
			if (file.isDirectory()) {
				File[] children = file.listFiles();
				for (int i = 0; i < children.length; i++) {
					File child = children[i];
					deleteFile(child);
				}
			}
			file.delete();
		}
	}
	
	private File getContentsFile() {
		File contentsFile = new File(_baseFolder, PFConstants.PROPERTIES_FILE_NAME);
		return contentsFile;
	}

	private void logException(Exception e) {
		RSECorePlugin.getDefault().getLogger().logError("unexpected exception", e); //$NON-NLS-1$
	}

}
