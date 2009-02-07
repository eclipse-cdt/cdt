/*******************************************************************************
 * Copyright (c) 2008 Takuya Miyamoto and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Takuya Miyamoto - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.internal.synchronize;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.rse.internal.importexport.files.RemoteFileExportData;
import org.eclipse.rse.internal.importexport.files.RemoteFileImportData;
import org.eclipse.rse.internal.importexport.files.UniFilePlus;
import org.eclipse.rse.internal.synchronize.provisional.ISynchronizeOperation;
import org.eclipse.rse.internal.synchronize.provisional.SynchronizeFilter;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.ui.SystemBasePlugin;

public class SynchronizeData implements ISynchronizeData {
	private String descriptionFilePath;
	private String destination;
	private List<IResource> elements;
	private int synchronizeType;
	private boolean saveSettings;
	private boolean createSelectionOnly;
	private boolean createDirectoryStructure;
	private boolean overWriteExistingFiles;
	private boolean reviewSynchronzie;

	public SynchronizeData() {
		setDescriptionFilePath(null);
		setDestination(null);
		setElements(null);
		setSynchronizeType(0);
		setSaveSettings(false);
		setCreateDirectoryStructure(false);
		setCreateSelectionOnly(false);
		setOverWriteExistingFiles(false);
	}

	public SynchronizeData(RemoteFileExportData data) {
		setDescriptionFilePath(data.getDescriptionFilePath());
		setDestination(data.getDestination());
		setElements(data.getElements());
		setSynchronizeType(ISynchronizeOperation.SYNC_MODE_OVERRIDE_DEST);
		setSaveSettings(data.isSaveSettings());
		setCreateDirectoryStructure(data.isCreateDirectoryStructure());
		setCreateSelectionOnly(data.isCreateSelectionOnly());
		setOverWriteExistingFiles(data.isOverWriteExistingFiles());
	}
	
	public SynchronizeData(RemoteFileImportData data) throws SystemMessageException, CoreException{
		IRemoteFile remoteRoot = ((UniFilePlus)data.getSource()).getRemoteFile();
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IContainer localRoot = (IContainer)workspace.getRoot().findMember(data.getContainerPath());
        
		ArrayList<IPath> paths = new ArrayList<IPath>();
		List<UniFilePlus> resources = data.getElements();
		for (UniFilePlus uniFilePlus : resources) {
			paths.add(new Path(uniFilePlus.getRemoteFile().getAbsolutePathPlusConnection()));
		}
		SynchronizeFilter filter = new SynchronizeFilter(paths);
		ArrayList<IResource> localResource = new ArrayList<IResource>();
		
		RSESyncUtils.getSynchronizeResources(localRoot, remoteRoot, remoteRoot, filter, localResource);
		
		setDescriptionFilePath(data.getDescriptionFilePath());
		setDestination(((UniFilePlus)data.getSource()).getRemoteFile().getAbsolutePathPlusConnection());
		setElements(localResource);
		setSynchronizeType(ISynchronizeOperation.SYNC_MODE_OVERRIDE_SOURCE);
		setSaveSettings(data.isSaveSettings());
		setCreateDirectoryStructure(data.isCreateDirectoryStructure());
		setCreateSelectionOnly(data.isCreateSelectionOnly());
		setOverWriteExistingFiles(data.isOverWriteExistingFiles());
	}

	public IFile getDescriptionFile() {
		String pathString = getDescriptionFilePath();
		if (pathString == null) {
			return null;
		}
		IPath path = new Path(pathString);
		if (path.isValidPath(path.toString()) && path.segmentCount() >= 2) {
			return SystemBasePlugin.getWorkspace().getRoot().getFile(path);
		} else {
			return null;
		}
	}

	public String getDescriptionFilePath() {
		return descriptionFilePath;
	}

	public void setDescriptionFilePath(String descriptionFilePath) {
		this.descriptionFilePath = descriptionFilePath;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public List<IResource> getElements() {
		return elements;
	}

	public void setElements(List<IResource> elements) {
		this.elements = elements;
	}

	public int getSynchronizeType() {
		return synchronizeType;
	}

	public void setSynchronizeType(int synchronizeType) {
		this.synchronizeType = synchronizeType;
	}

	public boolean isSaveSettings() {
		return saveSettings;
	}

	public void setSaveSettings(boolean saveSettings) {
		this.saveSettings = saveSettings;
	}

	public boolean isCreateSelectionOnly() {
		return createSelectionOnly;
	}

	public void setCreateSelectionOnly(boolean createSelectionOnly) {
		this.createSelectionOnly = createSelectionOnly;
	}

	public boolean isOverWriteExistingFiles() {
		return overWriteExistingFiles;
	}

	public void setOverWriteExistingFiles(boolean overWriteExistingFiles) {
		this.overWriteExistingFiles = overWriteExistingFiles;
	}

	public boolean isCreateDirectoryStructure() {
		return createDirectoryStructure;
	}

	public void setCreateDirectoryStructure(boolean createDirectoryStructure) {
		this.createDirectoryStructure = createDirectoryStructure;
	}

	public boolean isReviewSynchronzie() {
		return reviewSynchronzie;
	}

	public void setReviewSynchronzie(boolean reviewSynchronzie) {
		this.reviewSynchronzie = reviewSynchronzie;
	}

	public RemoteFileExportData getExportData() {
		RemoteFileExportData data = new RemoteFileExportData();
		data.setElements(getElements());
		data.setCreateDirectoryStructure(isCreateDirectoryStructure());
		data.setCreateSelectionOnly(isCreateSelectionOnly());
		data.setOverWriteExistingFiles(isOverWriteExistingFiles());
		data.setSaveSettings(isSaveSettings());
		data.setDescriptionFilePath(getDescriptionFilePath());
		data.setDestination(getDestination());

		return data;
	}

}
