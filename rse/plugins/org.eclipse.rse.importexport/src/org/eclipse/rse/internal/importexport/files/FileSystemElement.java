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
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.AdaptableList;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchAdapter;

// Similar to org.eclipse.ui.dialogs.FileSystemElement
// Changes: added getName() method
/**
 *  Instances of this class represent files or file-like entities (eg.- zip
 *  file entries) on the local file system.  They do not represent resources
 *  within the workbench.  This distinction is made because the representation of
 *  a file system resource is significantly different from that of a workbench
 *  resource.
 *
 *  If self represents a collection (eg.- file system directory, zip directory)
 *  then its icon will be the folderIcon static field.  Otherwise (ie.- self
 *  represents a file system file) self's icon is stored in field "icon", and is
 *  determined by the extension of the file that self represents.
 *
 * This class is adaptable, and implements one adapter itself, namely the 
 * IWorkbenchAdapter adapter used for navigation and display in the workbench.
 */
public class FileSystemElement implements IAdaptable {
	private String name;
	private Object fileSystemObject;
	/* Wait until a child is added to initialize the receiver's lists.
	 * Doing so minimizes the amount of memory that is allocated when
	 * a large directory structure is being processed.
	 */
	private AdaptableList folders = null;
	private AdaptableList files = null;
	private boolean isDirectory = false;
	private FileSystemElement parent;
	private final static AdaptableList EMPTY_LIST = new AdaptableList(0);
	private WorkbenchAdapter workbenchAdapter = new WorkbenchAdapter() {
		/**
		 *	Answer the children property of this element
		 */
		public Object[] getChildren(Object o) {
			return getFolders().getChildren(o);
		}

		/**
		 * Returns the parent of this element
		 */
		public Object getParent(Object o) {
			return parent;
		}

		/**
		 * Returns an appropriate label for this file system element.
		 */
		public String getLabel(Object o) {
			return name;
		}

		/**
		 * Returns an image descriptor for this file system element
		 */
		public ImageDescriptor getImageDescriptor(Object object) {
			if (isDirectory()) {
				return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
			} else {
				return PlatformUI.getWorkbench().getEditorRegistry().getImageDescriptor(name);
			}
		}
	};

	/**
	 * Creates a new <code>FileSystemElement</code> and initializes it
	 * and its parent if applicable.
	 *
	 * @param name java.lang.String
	 */
	public FileSystemElement(String name, FileSystemElement parent, boolean isDirectory) {
		this.name = name;
		this.parent = parent;
		this.isDirectory = isDirectory;
		if (parent != null) parent.addChild(this);
	}

	/**
	 * Adds the passed child to this object's collection of children.
	 *
	 * @param child FileSystemElement
	 */
	public void addChild(FileSystemElement child) {
		if (child.isDirectory()) {
			if (folders == null) folders = new AdaptableList(1);
			folders.add(child);
		} else {
			if (files == null) files = new AdaptableList(1);
			files.add(child);
		}
	}

	/**
	 * Returns the adapter
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class) {
			return workbenchAdapter;
		}
		//defer to the platform
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

	/** 
	 * Returns the extension of this element's filename.  Returns
	 * The empty string if there is no extension.
	 */
	public String getFileNameExtension() {
		int lastDot = name.lastIndexOf('.');
		return lastDot < 0 ? "" : name.substring(lastDot + 1); //$NON-NLS-1$
	}

	/**
	 *	Answer the files property of this element.  Answer an empty list if the
	 *  files property is null. 
	 *  This method should not be used to add children
	 *  to the receiver. Use addChild(FileSystemElement) instead.
	 */
	public AdaptableList getFiles() {
		if (files == null) return EMPTY_LIST;
		return files;
	}

	/**
	 *	Returns the file system object property of this element
	 *
	 *	@return the file system object
	 */
	public Object getFileSystemObject() {
		return fileSystemObject;
	}

	/**
	 * Returns a list of the folders that are immediate children
	 * of this folder.  Answer an empty list if the folders property is null.
	 * This method should not be used to add children
	 * to the receiver. Use addChild(FileSystemElement) instead.
	 */
	public AdaptableList getFolders() {
		if (folders == null) return EMPTY_LIST;
		return folders;
	}

	/**
	 * Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Return the parent of this element.
	 *
	 * @return the parent file system element, or <code>null</code> if this is the root
	 */
	public FileSystemElement getParent() {
		return this.parent;
	}

	/**
	 * Returns true if this element represents a directory, and false
	 * otherwise.
	 */
	public boolean isDirectory() {
		return isDirectory;
	}

	/**
	 * Removes a sub-folder from this file system element.
	 */
	public void removeFolder(FileSystemElement child) {
		if (folders == null) return;
		folders.remove(child);
		child.setParent(null);
	}

	/**
	 *	Set the file system object property of this element
	 *
	 *	@param value the file system object
	 */
	public void setFileSystemObject(Object value) {
		fileSystemObject = value;
	}

	/**
	 * Sets the parent of this file system element.
	 */
	public void setParent(FileSystemElement element) {
		parent = element;
	}

	/**
	 * For debugging purposes only.
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer();
		if (isDirectory()) {
			buf.append("Folder(");//$NON-NLS-1$
		} else {
			buf.append("File(");//$NON-NLS-1$
		}
		buf.append(name).append(")");//$NON-NLS-1$
		if (!isDirectory()) {
			return buf.toString();
		}
		buf.append(" folders: ");//$NON-NLS-1$
		buf.append(folders);
		buf.append(" files: ");//$NON-NLS-1$
		buf.append(files);
		return buf.toString();
	}
}
