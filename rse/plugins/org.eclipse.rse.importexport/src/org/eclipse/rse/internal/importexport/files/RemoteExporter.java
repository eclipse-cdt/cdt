package org.eclipse.rse.internal.importexport.files;

/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.services.clientserver.SystemEncodingUtil;
import org.eclipse.rse.services.files.RemoteFileException;
import org.eclipse.rse.services.files.RemoteFileSecurityException;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;

// Similar to org.eclipse.ui.wizards.datatransfer.FileSystemExporter
/**
 * Helper class for exporting resources to the file system.
 */
class RemoteExporter {
	private Object as400 = null;

	/**
	 *  Create an instance of this class.  Use this constructor if you wish to
	 *  use an AS400 object */
	public RemoteExporter(IHost s) {
		super();
		as400 = s;
	}

	/**
	 *  Create an instance of this class.
	 */
	public RemoteExporter() {
		super();
	}

	/**
	 *  Creates the specified file system directory at <code>destinationPath</code>.
	 *  This creates a new file system directory.
	 */
	public void createFolder(IPath destinationPath) {
		// IFS: use IFSJaveFile object if necessary
		if (as400 != null)
			new UniFilePlus(Utilities.getIRemoteFile((IHost) as400, destinationPath.toString())).mkdir();
		else
			new File(destinationPath.toOSString()).mkdir();
	}

	/**
	 *  Writes the passed resource to the specified location recursively
	 */
	public void write(IResource resource, IPath destinationPath) throws IOException, CoreException, RemoteFileSecurityException, RemoteFileException {
		if (resource.getType() == IResource.FILE)
			writeFile((IFile) resource, destinationPath);
		else
			writeChildren((IContainer) resource, destinationPath);
	}

	/**
	 *  Exports the passed container's children
	 */
	protected void writeChildren(IContainer folder, IPath destinationPath) throws IOException, CoreException, RemoteFileSecurityException, RemoteFileException {
		if (folder.isAccessible()) {
			IResource[] children = folder.members();
			for (int i = 0; i < children.length; i++) {
				IResource child = children[i];
				writeResource(child, destinationPath.append(child.getName()));
			}
		}
	}

	/**
	 *  Writes the passed file resource to the specified destination on the remote
	 *  file system
	 */
	protected void writeFile(IFile file, IPath destinationPath) throws IOException, CoreException, RemoteFileSecurityException, RemoteFileException {
		IRemoteFileSubSystem rfss = RemoteFileUtility.getFileSubSystem((IHost) as400);
		rfss.upload(file.getLocation().makeAbsolute().toOSString(), SystemEncodingUtil.ENCODING_UTF_8, destinationPath.toString(), System.getProperty("file.encoding"), null); //$NON-NLS-1$
	}

	/**
	 *  Writes the passed resource to the specified location recursively
	 */
	protected void writeResource(IResource resource, IPath destinationPath) throws IOException, CoreException, RemoteFileSecurityException, RemoteFileException {
		if (resource.getType() == IResource.FILE)
			writeFile((IFile) resource, destinationPath);
		else {
			createFolder(destinationPath);
			writeChildren((IContainer) resource, destinationPath);
		}
	}
}
