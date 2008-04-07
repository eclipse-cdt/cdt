package org.eclipse.rse.internal.importexport.files;

/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *  David McKnight   (IBM)        - [219792] use background query when doing import
 *  David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 *******************************************************************************/
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
		return super.getFiles();
	}

	/**
	 * Returns a list of the folders that are immediate children. Use the supplied provider
	 * if it needs to be populated.
	 * of this folder.
	 */
	public AdaptableList getFolders(IImportStructureProvider provider) {
		return super.getFolders();
	}

	public void setPopulated(boolean populated) {
		this.populated = populated;
	}
	
	public boolean isPopulated() {
		return populated;
	}
}
