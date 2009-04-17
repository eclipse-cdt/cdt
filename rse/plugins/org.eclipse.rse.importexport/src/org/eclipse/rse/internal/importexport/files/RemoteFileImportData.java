/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [168870] refactor org.eclipse.rse.core package of the UI plugin
 * David McKnight   (IBM)        - [272708] [import/export] fix various bugs with the synchronization support
 *******************************************************************************/
package org.eclipse.rse.internal.importexport.files;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.rse.ui.SystemBasePlugin;

/**
 * Holds data of what to import.
 */
public class RemoteFileImportData {
	private IPath containerPath;
	private Object source;
	private boolean overWriteExistingFiles;
	private boolean createDirectoryStructure;
	private boolean createSelectionOnly;
	private boolean saveSettings;
	private String descriptionFilePath;
	// import elements
	private HashMap map;
	private List elements;

	private boolean reviewSynchronize;
	
	/**
	 * Constructor.
	 */
	public RemoteFileImportData() {
		setContainerPath(null);
		setSource(null);
		setOverWriteExistingFiles(false);
		setCreateDirectoryStructure(false);
		setCreateSelectionOnly(true);
		setSaveSettings(false);
		setDescriptionFilePath(null);
	}

	/**
	 * @return Returns the descriptionFilePath.
	 */
	public String getDescriptionFilePath() {
		return descriptionFilePath;
	}

	/**
	 * Gets the description file as a workspace resource.
	 * @return a file representing the description file.
	 */
	public IFile getDescriptionFile() {
		IPath path = new Path(getDescriptionFilePath());
		if (path.isValidPath(path.toString()) && path.segmentCount() >= 2) {
			return SystemBasePlugin.getWorkspace().getRoot().getFile(path);
		} else {
			return null;
		}
	}

	/**
	 * @param descriptionFilePath The descriptionFilePath to set.
	 */
	public void setDescriptionFilePath(String descriptionFilePath) {
		this.descriptionFilePath = descriptionFilePath;
	}

	/**
	 * @return returns container path.
	 */
	public IPath getContainerPath() {
		return containerPath;
	}

	/**
	 * @param containerPath the container path.
	 */
	public void setContainerPath(IPath containerPath) {
		this.containerPath = containerPath;
	}

	/**
	 * Gets the source.
	 * @return the source from which to import.
	 */
	public Object getSource() {
		return source;
	}

	/**
	 * Sets the source.
	 * @param source the source from which to import.
	 */
	public void setSource(Object source) {
		this.source = source;
	}

	/**
	 * Returns the elements to be imported.
	 * @return the elements.
	 */
	public List getElements() {
		return elements;
	}

	/**
	 * Sets the elements to imported.
	 * @param elements the elements.
	 */
	public void setElements(List elements) {
		this.elements = elements;
	}

	/**
	 * Add to the list of files to import.
	 * @param object the file object.
	 */
	public void addToList(UniFilePlus object) {
		if (map == null) {
			map = new HashMap();
		}
		// add to map
		map.put(object.getAbsolutePath(), object);
	}

	/**
	 * Does the file exist.
	 * @param object the file object.
	 * @return <code>true</code> of the 
	 */
	public boolean doesExist(UniFilePlus object) {
		if (map.get(object.getAbsolutePath()) == null) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * @return Returns the overWriteExistingFiles.
	 */
	public boolean isOverWriteExistingFiles() {
		return overWriteExistingFiles;
	}

	/**
	 * @param overWriteExistingFiles The overWriteExistingFiles to set.
	 */
	public void setOverWriteExistingFiles(boolean overWriteExistingFiles) {
		this.overWriteExistingFiles = overWriteExistingFiles;
	}
	
	public boolean isReviewSynchronize(){
		return reviewSynchronize;
	}
	
	public void setReviewSynchronize(boolean reviewSynchronize){
		this.reviewSynchronize = reviewSynchronize;
	}

	/**
	 * @return Returns the saveSettings.
	 */
	public boolean isSaveSettings() {
		return saveSettings;
	}

	/**
	 * @param saveSettings The saveSettings to set.
	 */
	public void setSaveSettings(boolean saveSettings) {
		this.saveSettings = saveSettings;
	}

	/**
	 * @return Returns the createDirectoryStructure.
	 */
	public boolean isCreateDirectoryStructure() {
		return createDirectoryStructure;
	}

	/**
	 * @param createDirectoryStructure The createDirectoryStructure to set.
	 */
	public void setCreateDirectoryStructure(boolean createDirectoryStructure) {
		this.createDirectoryStructure = createDirectoryStructure;
	}

	/**
	 * @return Returns the createSelectionOnly.
	 */
	public boolean isCreateSelectionOnly() {
		return createSelectionOnly;
	}

	/**
	 * @param createSelectionOnly The createSelectionOnly to set.
	 */
	public void setCreateSelectionOnly(boolean createSelectionOnly) {
		this.createSelectionOnly = createSelectionOnly;
	}

	/**
	 * Creates and returns an import description writer.
	 */
	public IRemoteFileImportDescriptionWriter createImportDescriptionWriter(OutputStream outputStream) {
		return new RemoteFileImportDescriptionWriter(outputStream);
	}

	/**
	 * Creates and returns an import description writer.
	 */
	public IRemoteFileImportDescriptionReader createImportDescriptionReader(InputStream inputStream) {
		return new RemoteFileImportDescriptionReader(inputStream);
	}
}
