/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.tests.util;

import org.eclipse.cdt.core.settings.model.extension.CFolderData;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.cdt.core.settings.model.extension.CResourceData;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IModificationStatus;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.OptionStringValue;
import org.eclipse.core.runtime.IPath;
import org.osgi.framework.Version;

public class TestFolderInfo implements IFolderInfo {

	IConfiguration cfg;
	public TestFolderInfo(IConfiguration parent) {
		cfg = parent;
	}
	
	public boolean buildsFileType(String srcExt) {
		// TODO Auto-generated method stub
		return false;
	}

	public IToolChain changeToolChain(IToolChain newSuperClass, String Id,
			String name) throws BuildException {
		// TODO Auto-generated method stub
		return null;
	}

	public ITool[] getFilteredTools() {
		// TODO Auto-generated method stub
		return null;
	}

	public CFolderData getFolderData() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getOutputExtension(String resourceExtension) {
		// TODO Auto-generated method stub
		return null;
	}

	public ITool getTool(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	public IToolChain getToolChain() {
		// TODO Auto-generated method stub
		return null;
	}

	public IModificationStatus getToolChainModificationStatus(ITool[] removed,
			ITool[] added) {
		// TODO Auto-generated method stub
		return null;
	}

	public ITool getToolFromInputExtension(String sourceExtension) {
		// TODO Auto-generated method stub
		return null;
	}

	public ITool getToolFromOutputExtension(String extension) {
		// TODO Auto-generated method stub
		return null;
	}

	public ITool[] getToolsBySuperClassId(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isHeaderFile(String ext) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isToolChainCompatible(IToolChain ch) {
		// TODO Auto-generated method stub
		return false;
	}

	public void modifyToolChain(ITool[] removed, ITool[] added)
			throws BuildException {
		// TODO Auto-generated method stub

	}

	public CLanguageData[] getCLanguageDatas() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getKind() {
		// TODO Auto-generated method stub
		return 0;
	}

	public IConfiguration getParent() {
		return cfg;
	}

	public IPath getPath() {
		// TODO Auto-generated method stub
		return null;
	}

	public CResourceData getResourceData() {
		// TODO Auto-generated method stub
		return null;
	}

	public ITool[] getTools() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isExcluded() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isExtensionElement() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean needsRebuild() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setDirty(boolean dirty) {
		// TODO Auto-generated method stub

	}

	public void setExclude(boolean excluded) {
		// TODO Auto-generated method stub

	}

	public IOption setOption(IHoldsOptions parent, IOption option, boolean value)
			throws BuildException {
		// TODO Auto-generated method stub
		return null;
	}

	public IOption setOption(IHoldsOptions parent, IOption option, String value)
			throws BuildException {
		// TODO Auto-generated method stub
		return null;
	}

	public IOption setOption(IHoldsOptions parent, IOption option,
			String[] value) throws BuildException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setPath(IPath path) {
		// TODO Auto-generated method stub

	}

	public void setRebuildState(boolean rebuild) {
		// TODO Auto-generated method stub

	}

	public boolean supportsBuild(boolean managed) {
		// TODO Auto-generated method stub
		return false;
	}

	public String getBaseId() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getManagedBuildRevision() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	public Version getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setVersion(Version version) {
		// TODO Auto-generated method stub

	}

	public boolean canExclude(boolean exclude) {
		// TODO Auto-generated method stub
		return false;
	}

	public IOption setOption(IHoldsOptions parent, IOption option,
			OptionStringValue[] value) throws BuildException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isSupported() {
		// TODO Auto-generated method stub
		return false;
	}

}
