package org.eclipse.rse.files.importexport.files;

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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.model.AdaptableList;

// Similar to org.eclipse.ui.wizards.datatransfer.MinimizedFileSystemElement
// Changes marked with "IFS:" comments.
/**
 * The <code>MinimizedFileSystemElement</code> is a <code>FileSystemElement</code> that knows
 * if it has been populated or not.
 */
// IFS: made class public
public class MinimizedFileSystemElement extends FileSystemElement {
	private boolean populated = false;

	/**
	 * Create a <code>MinimizedFileSystemElement</code> with the supplied name and parent.
	 * @param name the name of the file element this represents
	 * @param parent the containing parent
	 * @param isDirectory indicated if this could have children or not
	 */
	public MinimizedFileSystemElement(String name, FileSystemElement parent, boolean isDirectory) {
		super(name, parent, isDirectory);
	}

	/**
	 * Returns a list of the files that are immediate children. Use the supplied provider
	 * if it needs to be populated.
	 * of this folder.
	 */
	public AdaptableList getFiles(IImportStructureProvider provider) {
		if (!populated) populate(provider);
		return super.getFiles();
	}

	/**
	 * Returns a list of the folders that are immediate children. Use the supplied provider
	 * if it needs to be populated.
	 * of this folder.
	 */
	public AdaptableList getFolders(IImportStructureProvider provider) {
		if (!populated) populate(provider);
		return super.getFolders();
	}

	/**
	 * Return whether or not population has happened for the receiver.
	 */
	// IFS: made method public
	public boolean isPopulated() {
		return this.populated;
	}

	/**
	 * Return whether or not population has not happened for the receiver.
	 */
	boolean notPopulated() {
		return !this.populated;
	}

	/**
	 * Populate the files and folders of the receiver using the suppliec structure provider.
	 * @param provider org.eclipse.ui.wizards.datatransfer.IImportStructureProvider
	 */
	private void populate(IImportStructureProvider provider) {
		Object fileSystemObject = getFileSystemObject();
		List children = provider.getChildren(fileSystemObject);
		if (children == null) children = new ArrayList(1);
		Iterator childrenEnum = children.iterator();
		while (childrenEnum.hasNext()) {
			Object child = childrenEnum.next();
			String elementLabel = provider.getLabel(child);
			//Create one level below
			MinimizedFileSystemElement result = new MinimizedFileSystemElement(elementLabel, this, provider.isFolder(child));
			result.setFileSystemObject(child);
		}
		setPopulated();
	}

	/**
	 * Set whether or not population has happened for the receiver to true.
	 */
	public void setPopulated() {
		this.populated = true;
	}
}
