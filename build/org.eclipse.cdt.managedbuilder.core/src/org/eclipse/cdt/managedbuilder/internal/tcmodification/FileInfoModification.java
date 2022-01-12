/*******************************************************************************
 * Copyright (c) 2007, 2011 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.tcmodification;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.ResourceConfiguration;
import org.eclipse.cdt.managedbuilder.internal.core.Tool;
import org.eclipse.cdt.managedbuilder.tcmodification.IFileInfoModification;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

public class FileInfoModification extends ToolListModification implements IFileInfoModification {
	private String fFileExt;
	private Set<IPath> fApplPathSet;
	private IProject fProject;

	public FileInfoModification(ResourceConfiguration rcInfo) {
		super(rcInfo, rcInfo.getTools());
	}

	public FileInfoModification(ResourceConfiguration rcInfo, FileInfoModification base) {
		super(rcInfo, base);
	}

	@Override
	protected boolean canRemove(ITool realTool) {
		return true;
	}

	@Override
	protected boolean canAdd(Tool tool) {
		String ext = getFileExtension();
		return tool.buildsFileType(ext, getProject());
	}

	@Override
	protected boolean canReplace(Tool fromTool, Tool toTool) {
		String ext = getFileExtension();
		return toTool.buildsFileType(ext, getProject());
	}

	@Override
	protected Set<IPath> getToolApplicabilityPathSet(Tool realTool, boolean isProject) {
		if (fApplPathSet == null) {
			Set<IPath> s = new HashSet<>(1);
			s.add(getResourceInfo().getPath());
			fApplPathSet = Collections.unmodifiableSet(s);
		}
		return fApplPathSet;
	}

	@Override
	protected Set<Tool> getExtensionConflictToolSet(Tool tool, Tool[] toos) {
		return Collections.emptySet();
	}

	@Override
	protected Tool[] filterTools(Tool[] tools) {
		return tools;
	}

	private String getFileExtension() {
		if (fFileExt == null) {
			fFileExt = getResourceInfo().getPath().getFileExtension();
			if (fFileExt == null)
				fFileExt = ""; //$NON-NLS-1$
		}
		return fFileExt;
	}

	private IProject getProject() {
		if (fProject == null) {
			fProject = getResourceInfo().getParent().getOwner().getProject();
		}
		return fProject;
	}

	@Override
	public void restoreDefaults() {
		//		3.per-file : change to the tool from the parent folder's tool-chain suitable
		//		for the given file. NOTE: the custom build step tool should be preserved!
		ResourceConfiguration rcInfo = (ResourceConfiguration) getResourceInfo();
		IFolderInfo parentFo = rcInfo.getParentFolderInfo();
		String ext = rcInfo.getPath().getFileExtension();
		if (ext == null)
			ext = ""; //$NON-NLS-1$

		ITool tool = parentFo.getToolFromInputExtension(ext);
		ITool realTool = ManagedBuildManager.getRealTool(tool);
		boolean add = true;

		ITool[] curTools = getProjectTools();
		for (int i = 0; i < curTools.length; i++) {
			ITool cur = curTools[i];
			if (ManagedBuildManager.getRealTool(cur) == realTool) {
				add = false;
			} else if (!cur.getCustomBuildStep()) {
				changeProjectTools(cur, null);
			}
		}

		if (add && tool != null)
			changeProjectTools(null, tool);
	}
}
