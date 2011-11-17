/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import org.eclipse.cdt.core.settings.model.extension.CFileData;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IFileInfo;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.core.resources.IResource;

/**
 *
 */
public class MultiFileInfo extends MultiResourceInfo implements IFileInfo {

	public MultiFileInfo(IResourceInfo[] ris, IConfiguration p) {
		super(ris, p);
		fRis = ris;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IFileInfo#getFileData()
	 */
	@Override
	public CFileData getFileData() {
		return ((IFileInfo)fRis[curr]).getFileData();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IResourceConfiguration#createTool(org.eclipse.cdt.managedbuilder.core.ITool, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public ITool createTool(ITool superClass, String Id, String name,
			boolean isExtensionElement) {
		ITool t = null;
		for (IResourceInfo ri : fRis) {
			if (ri instanceof IFileInfo) {
				IFileInfo fi = (IFileInfo)ri;
				t = fi.createTool(superClass, Id, name, isExtensionElement);
			}
		}
		return t;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IResourceConfiguration#getOwner()
	 */
	@Override
	public IResource getOwner() {
		return ((IFileInfo)fRis[curr]).getOwner();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IResourceConfiguration#getRcbsApplicability()
	 */
	@Override
	public int getRcbsApplicability() {
		return ((IFileInfo)fRis[curr]).getRcbsApplicability();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IResourceConfiguration#getResourcePath()
	 */
	@Override
	public String getResourcePath() {
		return ((IFileInfo)fRis[curr]).getResourcePath();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IResourceConfiguration#getTool(java.lang.String)
	 */
	@Override
	public ITool getTool(String id) {
		return ((IFileInfo)fRis[curr]).getTool(id);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IResourceConfiguration#getToolsToInvoke()
	 */
	@Override
	public ITool[] getToolsToInvoke() {
		return ((IFileInfo)fRis[curr]).getToolsToInvoke();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IResourceConfiguration#removeTool(org.eclipse.cdt.managedbuilder.core.ITool)
	 */
	@Override
	public void removeTool(ITool tool) {
		System.out.println("MultiFileInfo.removeTool() does not work OK !"); //$NON-NLS-1$
		for (IResourceInfo ri : fRis) {
			if (ri instanceof IFileInfo) {
				IFileInfo fi = (IFileInfo)ri;
				fi.removeTool(tool);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IResourceConfiguration#setRcbsApplicability(int)
	 */
	@Override
	public void setRcbsApplicability(int value) {
		for (IResourceInfo ri : fRis) {
			if (ri instanceof IFileInfo) {
				IFileInfo fi = (IFileInfo)ri;
				fi.setRcbsApplicability(value);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IResourceConfiguration#setResourcePath(java.lang.String)
	 */
	@Override
	public void setResourcePath(String path) {
		for (IResourceInfo ri : fRis) {
			if (ri instanceof IFileInfo) {
				IFileInfo fi = (IFileInfo)ri;
				fi.setResourcePath(path);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IResourceConfiguration#setToolCommand(org.eclipse.cdt.managedbuilder.core.ITool, java.lang.String)
	 */
	@Override
	public void setToolCommand(ITool tool, String command) {
		System.out.println("MultiFileInfo.setToolCommand() does not work OK !"); //$NON-NLS-1$
		for (IResourceInfo ri : fRis) {
			if (ri instanceof IFileInfo) {
				IFileInfo fi = (IFileInfo)ri;
				fi.setToolCommand(tool, command);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IResourceConfiguration#setTools(org.eclipse.cdt.managedbuilder.core.ITool[])
	 */
	@Override
	public void setTools(ITool[] tools) {
		for (IResourceInfo ri : fRis) {
			if (ri instanceof IFileInfo) {
				IFileInfo fi = (IFileInfo)ri;
				fi.setTools(tools);
			}
		}
	}

}
