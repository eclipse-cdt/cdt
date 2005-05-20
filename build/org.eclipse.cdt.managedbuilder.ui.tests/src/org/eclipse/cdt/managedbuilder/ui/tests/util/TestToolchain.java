/*******************************************************************************
 * Copyright (c) 2005 Texas Instruments Incorporated and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Texas Instruments - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.ui.tests.util;

import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOutputType;
import org.eclipse.cdt.managedbuilder.core.ITargetPlatform;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.macros.IConfigurationBuildMacroSupplier;

public class TestToolchain implements IToolChain {

	public IConfigurationBuildMacroSupplier getBuildMacroSupplier() {
		// TODO Auto-generated method stub
		return null;
	}

	public IConfiguration getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	public ITargetPlatform createTargetPlatform(ITargetPlatform superClass,
			String Id, String name, boolean isExtensionElement) {
		// TODO Auto-generated method stub
		return null;
	}

	public ITargetPlatform getTargetPlatform() {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeLocalTargetPlatform() {
		// TODO Auto-generated method stub

	}

	public String getVersionsSupported() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getConvertToId() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setVersionsSupported(String versionsSupported) {
		// TODO Auto-generated method stub

	}

	public void setConvertToId(String convertToId) {
		// TODO Auto-generated method stub

	}

	public IBuilder createBuilder(IBuilder superClass, String Id, String name,
			boolean isExtensionElement) {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeLocalBuilder() {
		// TODO Auto-generated method stub

	}

	public IBuilder getBuilder() {
		// TODO Auto-generated method stub
		return null;
	}

	public ITool createTool(ITool superClass, String Id, String name,
			boolean isExtensionElement) {
		// TODO Auto-generated method stub
		return null;
	}

	public ITool[] getTools() {
		// TODO Auto-generated method stub
		return null;
	}

	public ITool getTool(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	public IToolChain getSuperClass() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isAbstract() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setIsAbstract(boolean b) {
		// TODO Auto-generated method stub

	}

	public String getUnusedChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getOSList() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setOSList(String[] OSs) {
		// TODO Auto-generated method stub

	}

	public String[] getArchList() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setArchList(String[] archs) {
		// TODO Auto-generated method stub

	}

	public String getErrorParserIds() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getErrorParserIds(IConfiguration config) {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getErrorParserList() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setErrorParserIds(String ids) {
		// TODO Auto-generated method stub

	}

	public String getScannerConfigDiscoveryProfileId() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setScannerConfigDiscoveryProfileId(String profileId) {
		// TODO Auto-generated method stub

	}

	public String getTargetToolIds() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setTargetToolIds(String targetToolIds) {
		// TODO Auto-generated method stub

	}

	public String[] getTargetToolList() {
		// TODO Auto-generated method stub
		return null;
	}

	public IOutputType[] getSecondaryOutputs() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setSecondaryOutputs(String ids) {
		// TODO Auto-generated method stub

	}

	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setDirty(boolean isDirty) {
		// TODO Auto-generated method stub

	}

	public boolean isExtensionElement() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	public IConfigurationEnvironmentVariableSupplier getEnvironmentVariableSupplier() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getId() {
		return id;
	
	}

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	private String id = null;
	
	public void setID(String id)
	{
		this.id = id;
	}
	
}
