/*******************************************************************************
 * Copyright (c) 2005, 2007 Texas Instruments Incorporated and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Texas Instruments - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.ui.tests.util;

import org.eclipse.cdt.core.settings.model.extension.CTargetPlatformData;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IOptionPathConverter;
import org.eclipse.cdt.managedbuilder.core.IOutputType;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITargetPlatform;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.internal.core.HoldsOptions;
import org.eclipse.cdt.managedbuilder.macros.IConfigurationBuildMacroSupplier;
import org.osgi.framework.Version;

public class TestToolchain extends HoldsOptions implements IToolChain {

	public TestToolchain() {
		super(true /* resolved */);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getManagedBuildRevision() {
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
	public IConfigurationBuildMacroSupplier getBuildMacroSupplier() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IConfiguration getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITargetPlatform createTargetPlatform(ITargetPlatform superClass,
			String Id, String name, boolean isExtensionElement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITargetPlatform getTargetPlatform() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeLocalTargetPlatform() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getVersionsSupported() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getConvertToId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setVersionsSupported(String versionsSupported) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setConvertToId(String convertToId) {
		// TODO Auto-generated method stub

	}

	@Override
	public IBuilder createBuilder(IBuilder superClass, String Id, String name,
			boolean isExtensionElement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeLocalBuilder() {
		// TODO Auto-generated method stub

	}

	@Override
	public IBuilder getBuilder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITool createTool(ITool superClass, String Id, String name,
			boolean isExtensionElement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITool[] getTools() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITool getTool(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITool[] getToolsBySuperClassId(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IToolChain getSuperClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAbstract() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setIsAbstract(boolean b) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getUnusedChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getOSList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setOSList(String[] OSs) {
		// TODO Auto-generated method stub

	}

	@Override
	public String[] getArchList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setArchList(String[] archs) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getErrorParserIds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getErrorParserIds(IConfiguration config) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getErrorParserList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setErrorParserIds(String ids) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getScannerConfigDiscoveryProfileId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setScannerConfigDiscoveryProfileId(String profileId) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getTargetToolIds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTargetToolIds(String targetToolIds) {
		// TODO Auto-generated method stub

	}

	@Override
	public String[] getTargetToolList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IOutputType[] getSecondaryOutputs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSecondaryOutputs(String ids) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setDirty(boolean isDirty) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isExtensionElement() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IConfigurationEnvironmentVariableSupplier getEnvironmentVariableSupplier() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getId() {
		return id;

	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	private String id = null;

	public void setID(String id)
	{
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IToolChain#getPathConverter()
	 */
	@Override
	public IOptionPathConverter getOptionPathConverter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected IResourceInfo getParentResourceInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IFolderInfo getParentFolderInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CTargetPlatformData getTargetPlatformData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUniqueRealName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSystemObject() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean matches(IToolChain tc) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean supportsBuild(boolean managed) {
		// TODO Auto-generated method stub
		return false;
	}



}
