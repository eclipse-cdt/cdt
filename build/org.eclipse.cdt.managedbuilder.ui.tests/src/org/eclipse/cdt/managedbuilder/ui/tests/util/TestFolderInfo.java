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

	@Override
	public boolean buildsFileType(String srcExt) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IToolChain changeToolChain(IToolChain newSuperClass, String Id,
			String name) throws BuildException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITool[] getFilteredTools() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CFolderData getFolderData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getOutputExtension(String resourceExtension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITool getTool(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IToolChain getToolChain() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IModificationStatus getToolChainModificationStatus(ITool[] removed,
			ITool[] added) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITool getToolFromInputExtension(String sourceExtension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITool getToolFromOutputExtension(String extension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITool[] getToolsBySuperClassId(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isHeaderFile(String ext) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isToolChainCompatible(IToolChain ch) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void modifyToolChain(ITool[] removed, ITool[] added)
			throws BuildException {
		// TODO Auto-generated method stub

	}

	@Override
	public CLanguageData[] getCLanguageDatas() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getKind() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IConfiguration getParent() {
		return cfg;
	}

	@Override
	public IPath getPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CResourceData getResourceData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITool[] getTools() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isExcluded() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isExtensionElement() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean needsRebuild() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setDirty(boolean dirty) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setExclude(boolean excluded) {
		// TODO Auto-generated method stub

	}

	@Override
	public IOption setOption(IHoldsOptions parent, IOption option, boolean value)
			throws BuildException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IOption setOption(IHoldsOptions parent, IOption option, String value)
			throws BuildException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IOption setOption(IHoldsOptions parent, IOption option,
			String[] value) throws BuildException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPath(IPath path) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRebuildState(boolean rebuild) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean supportsBuild(boolean managed) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getBaseId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getManagedBuildRevision() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Version getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setVersion(Version version) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canExclude(boolean exclude) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IOption setOption(IHoldsOptions parent, IOption option,
			OptionStringValue[] value) throws BuildException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSupported() {
		// TODO Auto-generated method stub
		return false;
	}

}
