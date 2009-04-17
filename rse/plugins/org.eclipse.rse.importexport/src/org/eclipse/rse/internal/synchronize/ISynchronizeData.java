/*******************************************************************************
 * Copyright (c) 2008, 2009 Takuya Miyamoto and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Takuya Miyamoto - initial API and implementation
 * David McKnight   (IBM)        - [272708] [import/export] fix various bugs with the synchronization support
 *******************************************************************************/
package org.eclipse.rse.internal.synchronize;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

/**
 * Store synchronize information. This class is needed per each synchronize
 * operation.
 * 
 */
public interface ISynchronizeData {
	/**
	 * Return the descriptionFile path that store synchronize information.
	 * 
	 * @return
	 */
	public IFile getDescriptionFile();

	/**
	 * Return the remote path.
	 * 
	 * @return
	 */
	public String getRemoteLocation();

	/**
	 * Set the remote path
	 * 
	 * @param location
	 */
	public void setRemoteLocation(String location);

	/**
	 * Return the local path.
	 * 
	 * @return
	 */
	public IPath getLocalLocation();

	/**
	 * Set the local path
	 * 
	 * @param location
	 */
	public void setLocalLocation(IPath location);
	
	/**
	 * Return the synchronize type of this operation.
	 * 
	 * @return
	 */
	public int getSynchronizeType();

	public void setSynchronizeType(int synchronizeType);

	/**
	 * Return the element list that are synchronized in this operation.
	 * 
	 * @return
	 */
	public List<IResource> getElements();

	public void setElements(List<IResource> elements);

	/**
	 * Return if synchronize information is saved or not.
	 * 
	 * @return
	 */
	public boolean isSaveSettings();

	public void setSaveSettings(boolean saveSettings);

	/**
	 * Return if overwrite existing files or not.
	 * 
	 * @return
	 */
	public boolean isOverWriteExistingFiles();

	public void setOverWriteExistingFiles(boolean overWriteExistingFiles);

	/**
	 * Return if create directory structure or not.
	 * 
	 * @return
	 */
	public boolean isCreateDirectoryStructure();

	public void setCreateDirectoryStructure(boolean createDirectoryStructure);

	/**
	 * Return the description file path.
	 * 
	 * @return
	 */
	public String getDescriptionFilePath();

	public void setDescriptionFilePath(String descriptionFilePath);

	/**
	 * Return if create selection only or not.
	 * 
	 * @return
	 */
	public boolean isCreateSelectionOnly();

	public void setCreateSelectionOnly(boolean createSelectionOnly);

}
