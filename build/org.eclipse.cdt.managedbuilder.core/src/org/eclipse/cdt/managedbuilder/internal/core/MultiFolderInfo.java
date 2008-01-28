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

import org.eclipse.cdt.core.settings.model.extension.CFolderData;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IModificationStatus;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;

/**
 * This class holds a number of IFolderInfo objects
 * delonging to different configurations when they 
 * are edited simultaneously. 
 */
public class MultiFolderInfo extends MultiResourceInfo implements IFolderInfo {

	public MultiFolderInfo(IFolderInfo[] ris, IConfiguration p) {
		super(ris, p);
		fRis = ris;
	}
	
	public boolean buildsFileType(String srcExt) {
		for (int i=0; i<fRis.length; i++)
			if (! ((IFolderInfo)fRis[i]).buildsFileType(srcExt))
				return false;
		return true;
	}

	public IToolChain changeToolChain(IToolChain newSuperClass, String Id,
			String name) throws BuildException {
		IToolChain t = null;
		for (int i=0; i<fRis.length; i++)
			t = ((IFolderInfo)fRis[i]).changeToolChain(newSuperClass, Id, name);
		return t;
	}

	public ITool[] getFilteredTools() {
		return ((IFolderInfo)fRis[0]).getFilteredTools();
	}

	public CFolderData getFolderData() {
		return ((IFolderInfo)fRis[0]).getFolderData();
	}

	public String getOutputExtension(String resourceExtension) {
		return ((IFolderInfo)fRis[0]).getOutputExtension(resourceExtension);
	}

	public ITool getTool(String id) {
		return ((IFolderInfo)fRis[0]).getTool(id);
	}

	public IToolChain getToolChain() {
		return ((IFolderInfo)fRis[0]).getToolChain();	
	}

	public IModificationStatus getToolChainModificationStatus(ITool[] removed,
			ITool[] added) {
		return ((IFolderInfo)fRis[0]).getToolChainModificationStatus(removed, added);		
	}

	public ITool getToolFromInputExtension(String sourceExtension) {
		return ((IFolderInfo)fRis[0]).getToolFromInputExtension(sourceExtension);
	}

	public ITool getToolFromOutputExtension(String extension) {
		return ((IFolderInfo)fRis[0]).getToolFromOutputExtension(extension);
	}

	public ITool[] getToolsBySuperClassId(String id) {
		return ((IFolderInfo)fRis[0]).getToolsBySuperClassId(id);
	}

	public boolean isHeaderFile(String ext) {
		return ((IFolderInfo)fRis[0]).isHeaderFile(ext);
	}

	public boolean isToolChainCompatible(IToolChain ch) {
		return ((IFolderInfo)fRis[0]).isToolChainCompatible(ch);
	}

	public void modifyToolChain(ITool[] removed, ITool[] added)
			throws BuildException {
		((IFolderInfo)fRis[0]).modifyToolChain(removed, added);
	}

}
