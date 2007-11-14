/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.tcmodification;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.internal.core.ResourceConfiguration;
import org.eclipse.cdt.managedbuilder.internal.core.Tool;
import org.eclipse.cdt.managedbuilder.tcmodification.IFileInfoModification;
import org.eclipse.core.resources.IProject;

public class FileInfoModification extends
		ToolListModification implements IFileInfoModification {
	private String fFileExt;
	private Set fApplPathSet;
	private IProject fProject;
	
	public FileInfoModification(ResourceConfiguration rcInfo) {
		super(rcInfo, rcInfo.getTools());
	}

	public FileInfoModification(ResourceConfiguration rcInfo, FileInfoModification base) {
		super(rcInfo, base);
	}

	protected boolean canRemove(ITool realTool) {
		return true;
	}

	protected boolean canAdd(Tool tool) {
		String ext = getFileExtension();
		return tool.buildsFileType(ext, getProject());
	}

	protected boolean canReplace(Tool fromTool, Tool toTool) {
		String ext = getFileExtension();
		return toTool.buildsFileType(ext, getProject());
	}
	
	protected Set getToolApplicabilityPathSet(Tool realTool, boolean isProject) {
		if(fApplPathSet == null){
			Set s = new HashSet(1);
			s.add(getResourceInfo().getPath());
			fApplPathSet = Collections.unmodifiableSet(s);
		}
		return fApplPathSet;
	}

	protected Set getExtensionConflictToolSet(Tool tool, Tool[] toos) {
		return Collections.EMPTY_SET;
	}

	protected Tool[] filterTools(Tool[] tools) {
		return tools;
	}

	private String getFileExtension(){
		if(fFileExt == null){
			fFileExt = getResourceInfo().getPath().getFileExtension();
			if(fFileExt == null)
				fFileExt = ""; //$NON-NLS-1$
		}
		return fFileExt;
	}
	
	private IProject getProject(){
		if(fProject == null){
			fProject = getResourceInfo().getParent().getOwner().getProject();
		}
		return fProject;
	}

}
