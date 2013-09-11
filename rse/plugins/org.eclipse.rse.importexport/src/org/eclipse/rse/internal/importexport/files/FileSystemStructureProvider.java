package org.eclipse.rse.internal.importexport.files;

/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     David McKnight   (IBM)        - [417033] [import/export] RSE import wizard won't let user to select new source
 *******************************************************************************/
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;

// Similar to org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider
// Changes marked with "IFS:" comments.
/**
 * This class provides information regarding the structure and
 * content of specified file system File objects.
 */
public class FileSystemStructureProvider implements IImportStructureProvider {
	/**
	 * Holds a singleton instance of this class.
	 */
	public final static FileSystemStructureProvider INSTANCE = new FileSystemStructureProvider();

	/**
	 * Creates an instance of <code>FileSystemStructureProvider</code>.
	 */
	public FileSystemStructureProvider() {
		super();
	}

	/* (non-Javadoc)
	 * Method declared on IImportStructureProvider
	 */
	public List getChildren(Object element) {
		List result = new ArrayList(0);
		try {
			String[] children = ((File) element).list();
			int childrenLength = children == null ? 0 : children.length;
			result = new ArrayList(childrenLength);
			//long start = System.currentTimeMillis();
			//			String p=((UniFilePlus)element).getAbsolutePath()+"/"; //$NON-NLS-1$
			//			IHost sysC=((UniFilePlus) element).remoteFile.getSystemConnection();
			IRemoteFile[] childIRemoteFiles = ((UniFilePlus) element).listIRemoteFiles();
			if (childIRemoteFiles != null){
				for (int i = 0; i < childrenLength; i++)
					result.add(new UniFilePlus(childIRemoteFiles[i]));
			}
			//Debug.out("Expanding [" + ((File) element).getPath() + "] took in (ms): " + (System.currentTimeMillis() - start)); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (Exception e) {
			// Probably caused by IFS authority problem
			// ignore for now
		}
		return result;
	}

	/* (non-Javadoc)
	 * Method declared on IImportStructureProvider
	 */
	public InputStream getContents(Object element) {
		try {
			return new FileInputStream((File) element);
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	/* (non-Javadoc)
	 * Method declared on IImportStructureProvider
	 */
	public String getFullPath(Object element) {
		return ((File) element).getPath();
	}

	/* (non-Javadoc)
	 * Method declared on IImportStructureProvider
	 */
	public String getLabel(Object element) {
		//Get the name - if it is empty then return the path as it is a file root
		File file = (File) element;
		String name = file.getName();
		if (name == null || name.length() == 0)
			return file.getPath();
		else
			return name;
	}

	/* (non-Javadoc)
	 * Method declared on IImportStructureProvider
	 */
	public boolean isFolder(Object element) {
		return ((File) element).isDirectory();
	}
}
