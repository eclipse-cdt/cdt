package org.eclipse.cdt.core.resources;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;

/**
 *
 * @see IStorage
 */
public class FileStorage extends PlatformObject implements IStorage {
	IPath path;
	InputStream in = null;
        
	public InputStream getContents() throws CoreException {
		if (in == null) {	
			try {
				return new FileInputStream(path.toFile());
			} catch (FileNotFoundException e) {
				throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID,
						IStatus.ERROR, e.toString(), e));
			}
		} else {
			return in;
		}
	}

	/**
	 * @see IStorage#getFullPath
	 */
	public IPath getFullPath() {
		return this.path;
	}

	/**
	 * @see IStorage#getName
	 */
	public String getName() {
		return this.path.lastSegment();
	}

	/**
	 * @see IStorage#isReadOnly()
	 */
	public boolean isReadOnly() {
		return true;
	}

	public FileStorage(IPath path){
		this.path = path;
	}

	public FileStorage(InputStream in, IPath path){
		this.path = path;
		this.in = in;
	}

	/**
	 * @see IStorage#isReadOnly()
	 */
	public String toString() {
		return path.toOSString();
	}
}
