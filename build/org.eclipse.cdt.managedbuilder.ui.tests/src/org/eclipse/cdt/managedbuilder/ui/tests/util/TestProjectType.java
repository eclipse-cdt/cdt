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

	@Override
	public boolean checkForMigrationSupport() {	return false; }

	@Override
	public IConfiguration createConfiguration(IConfiguration parent, String id,
			String name) { return null; }
	@Override
	public IProjectBuildMacroSupplier getBuildMacroSupplier() { return null; }
	@Override
	public IConfiguration getConfiguration(String id) {	return null; }
	@Override
	public IConfigurationNameProvider getConfigurationNameProvider() { return null; }
	@Override
	public IConfiguration[] getConfigurations() { return cfgs; }
	@Override
	public String getConvertToId() { return null; }
	@Override
	public IProjectEnvironmentVariableSupplier getEnvironmentVariableSupplier() { return null; }
	@Override
	public String getNameAttribute() { return null; }
	@Override
	public IProjectType getSuperClass() { return null; }
	@Override
	public String getUnusedChildren() { return null; }
	@Override
	public boolean isAbstract() { return false; }
	@Override
	public boolean isSupported() { return false; }
	@Override
	public boolean isTestProjectType() { return false; }
	@Override
	public void removeConfiguration(String id) {}
	@Override
	public void setConvertToId(String convertToId) {}
	@Override
	public void setIsAbstract(boolean b) {}
	@Override
	public String getBaseId() {	return null; }
	@Override
	public String getId() { return null; }
	@Override
	public String getManagedBuildRevision() { return null; }
	@Override
	public String getName() { return null; }
	@Override
	public Version getVersion() { return null; }
	@Override
	public void setVersion(Version version) {}
	@Override
	public IBuildObjectProperties getBuildProperties() { return null; }

	@Override
	public IBuildPropertyValue getBuildArtefactType() {
		return null;
	}

	@Override
	public boolean isSystemObject() {
		return isTestProjectType();
	}
}
