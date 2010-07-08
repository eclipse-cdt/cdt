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

import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyValue;
import org.eclipse.cdt.managedbuilder.core.IBuildObjectProperties;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IConfigurationNameProvider;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.envvar.IProjectEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.macros.IProjectBuildMacroSupplier;
import org.osgi.framework.Version;

public class TestProjectType implements IProjectType {
	private IConfiguration[] cfgs = new IConfiguration[1];
	
	public TestProjectType() {
		cfgs[0] = new TestConfiguration(new TestToolchain());
	}
	
	public boolean checkForMigrationSupport() {	return false; }

	public IConfiguration createConfiguration(IConfiguration parent, String id,
			String name) { return null; }
	public IProjectBuildMacroSupplier getBuildMacroSupplier() { return null; }
	public IConfiguration getConfiguration(String id) {	return null; }
	public IConfigurationNameProvider getConfigurationNameProvider() { return null; }
	public IConfiguration[] getConfigurations() { return cfgs; }
	public String getConvertToId() { return null; }
	public IProjectEnvironmentVariableSupplier getEnvironmentVariableSupplier() { return null; }
	public String getNameAttribute() { return null; }
	public IProjectType getSuperClass() { return null; }
	public String getUnusedChildren() { return null; }
	public boolean isAbstract() { return false; }
	public boolean isSupported() { return false; }
	public boolean isTestProjectType() { return false; }
	public void removeConfiguration(String id) {}
	public void setConvertToId(String convertToId) {}
	public void setIsAbstract(boolean b) {}
	public String getBaseId() {	return null; }
	public String getId() { return null; }
	public String getManagedBuildRevision() { return null; }
	public String getName() { return null; }
	public Version getVersion() { return null; }
	public void setVersion(Version version) {}
	public IBuildObjectProperties getBuildProperties() { return null; }

	public IBuildPropertyValue getBuildArtefactType() {
		return null;
	}

	public boolean isSystemObject() {
		return isTestProjectType();
	}
}
