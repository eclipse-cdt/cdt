/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.tests.util;

import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.extension.CBuildData;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyValue;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObjectProperties;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IFileInfo;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.macros.IConfigurationBuildMacroSupplier;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.PluginVersionIdentifier;

public class TestConfiguration implements IConfiguration {

	IToolChain toolchain;
	
	public TestConfiguration(IToolChain tc) {
		toolchain = tc;
	}
	
	public boolean buildsFileType(String srcExt) {
		// TODO Auto-generated method stub
		return false;
	}

	public ITool calculateTargetTool() {
		// TODO Auto-generated method stub
		return null;
	}

	public void changeBuilder(IBuilder newBuilder, String id, String name) {
		// TODO Auto-generated method stub

	}

	public IFileInfo createFileInfo(IPath path) {
		// TODO Auto-generated method stub
		return null;
	}

	public IFileInfo createFileInfo(IPath path, String id, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public IFileInfo createFileInfo(IPath path, IFolderInfo base,
			ITool baseTool, String id, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public IFileInfo createFileInfo(IPath path, IFileInfo base, String id,
			String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public IFolderInfo createFolderInfo(IPath path) {
		// TODO Auto-generated method stub
		return null;
	}

	public IFolderInfo createFolderInfo(IPath path, String id, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public IFolderInfo createFolderInfo(IPath path, IFolderInfo base,
			String id, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public IToolChain createToolChain(IToolChain superClass, String Id,
			String name, boolean isExtensionElement) {
		// TODO Auto-generated method stub
		return null;
	}

	public IManagedCommandLineInfo generateToolCommandLineInfo(
			String sourceExtension, String[] flags, String outputFlag,
			String outputPrefix, String outputName, String[] inputResources,
			IPath inputLocation, IPath outputLocation) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getArtifactExtension() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getArtifactName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getBuildArguments() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getBuildCommand() {
		// TODO Auto-generated method stub
		return null;
	}

	public CBuildData getBuildData() {
		// TODO Auto-generated method stub
		return null;
	}

	public IConfigurationBuildMacroSupplier getBuildMacroSupplier() {
		// TODO Auto-generated method stub
		return null;
	}

	public IBuilder getBuilder() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getCleanCommand() {
		// TODO Auto-generated method stub
		return null;
	}

	public CConfigurationData getConfigurationData() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	public IBuilder getEditableBuilder() {
		// TODO Auto-generated method stub
		return null;
	}

	public IConfigurationEnvironmentVariableSupplier getEnvironmentVariableSupplier() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getErrorParserIds() {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getErrorParserList() {
		// TODO Auto-generated method stub
		return null;
	}

	public ITool[] getFilteredTools() {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getLibs(String extension) {
		// TODO Auto-generated method stub
		return null;
	}

	public IManagedProject getManagedProject() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getOutputExtension(String resourceExtension) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getOutputFlag(String outputExt) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getOutputPrefix(String outputExtension) {
		// TODO Auto-generated method stub
		return null;
	}

	public IConfiguration getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPostannouncebuildStep() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPostbuildStep() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPreannouncebuildStep() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPrebuildStep() {
		// TODO Auto-generated method stub
		return null;
	}

	public IProjectType getProjectType() {
		// TODO Auto-generated method stub
		return null;
	}

	public IResourceConfiguration getResourceConfiguration(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	public IResourceConfiguration[] getResourceConfigurations() {
		// TODO Auto-generated method stub
		return null;
	}

	public IResourceInfo getResourceInfo(IPath path, boolean exactPath) {
		// TODO Auto-generated method stub
		return null;
	}

	public IResourceInfo getResourceInfoById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	public IResourceInfo[] getResourceInfos() {
		// TODO Auto-generated method stub
		return null;
	}

	public IFolderInfo getRootFolderInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	public IPath[] getSourcePaths() {
		// TODO Auto-generated method stub
		return null;
	}

	public ITool getTargetTool() {
		// TODO Auto-generated method stub
		return null;
	}

	public ITool getTool(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	public IToolChain getToolChain() {
		return toolchain;
	}

	public String getToolCommand(ITool tool) {
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

	public ITool[] getTools() {
		// TODO Auto-generated method stub
		return null;
	}

	public ITool[] getToolsBySuperClassId(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getUserObjects(String extension) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasOverriddenBuildCommand() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isBuilderCompatible(IBuilder builder) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isExtensionElement() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isHeaderFile(String ext) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isManagedBuildOn() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isSystemObject() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isTemporary() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean needsFullRebuild() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean needsRebuild() {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeResourceConfiguration(IResourceInfo resConfig) {
		// TODO Auto-generated method stub

	}

	public void removeResourceInfo(IPath path) {
		// TODO Auto-generated method stub

	}

	public void setArtifactExtension(String extension) {
		// TODO Auto-generated method stub

	}

	public void setArtifactName(String name) {
		// TODO Auto-generated method stub

	}

	public void setBuildArguments(String makeArgs) {
		// TODO Auto-generated method stub

	}

	public void setBuildCommand(String command) {
		// TODO Auto-generated method stub

	}

	public void setCleanCommand(String command) {
		// TODO Auto-generated method stub

	}

	public void setDescription(String description) {
		// TODO Auto-generated method stub

	}

	public void setDirty(boolean isDirty) {
		// TODO Auto-generated method stub

	}

	public void setErrorParserIds(String ids) {
		// TODO Auto-generated method stub

	}

	public void setErrorParserList(String[] ids) {
		// TODO Auto-generated method stub

	}

	public void setManagedBuildOn(boolean on) throws BuildException {
		// TODO Auto-generated method stub

	}

	public void setName(String name) {
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

	public void setPostannouncebuildStep(String announceStep) {
		// TODO Auto-generated method stub

	}

	public void setPostbuildStep(String step) {
		// TODO Auto-generated method stub

	}

	public void setPreannouncebuildStep(String announceStep) {
		// TODO Auto-generated method stub

	}

	public void setPrebuildStep(String step) {
		// TODO Auto-generated method stub

	}

	public void setRebuildState(boolean rebuild) {
		// TODO Auto-generated method stub

	}

	public void setSourcePaths(IPath[] paths) {
		// TODO Auto-generated method stub

	}

	public void setToolCommand(ITool tool, String command) {
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

	public PluginVersionIdentifier getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setVersion(PluginVersionIdentifier version) {
		// TODO Auto-generated method stub

	}

	public IBuildObjectProperties getBuildProperties() {
		// TODO Auto-generated method stub
		return null;
	}
	public IResource getOwner() { return null; }

	public IResourceConfiguration createResourceConfiguration(IFile file) {
		// TODO Auto-generated method stub
		return null;
	}

	public IBuildPropertyValue getBuildArtefactType() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setBuildArtefactType(String id) throws BuildException {
		// TODO Auto-generated method stub
		
	}

	public ICSourceEntry[] getSourceEntries() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setSourceEntries(ICSourceEntry[] entries) {
		// TODO Auto-generated method stub
		
	}

}
