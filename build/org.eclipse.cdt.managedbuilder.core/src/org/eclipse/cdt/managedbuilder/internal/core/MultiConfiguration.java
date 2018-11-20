/*******************************************************************************
 * Copyright (c) 2007, 2012 Intel Corporation and others.
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
 * Dmitry Kozlov (CodeSourcery) - Save build output preferences (bug 294106)
 * Andrew Gvozdev (Quoin Inc)   - Saving build output implemented in different way (bug 306222)
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.MultiItemsHolder;
import org.eclipse.cdt.core.settings.model.extension.CBuildData;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildProperty;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyValue;
import org.eclipse.cdt.managedbuilder.buildproperties.IOptionalBuildProperties;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObjectProperties;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IFileInfo;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IMultiConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.macros.IConfigurationBuildMacroSupplier;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.osgi.framework.Version;

/**
 * This class represents a set of configurations
 * to be edited simultaneously on property pages.
 */
public class MultiConfiguration extends MultiItemsHolder implements IMultiConfiguration {
	private static final String[] EMPTY_STR_ARRAY = new String[0];

	protected IConfiguration[] fCfgs = null;
	private int curr = 0;

	public MultiConfiguration(IConfiguration[] cfs) {
		fCfgs = cfs;
		for (int i = 0; i < fCfgs.length; i++)
			if (((Configuration) fCfgs[i]).getConfigurationDescription().isActive()) {
				curr = i;
				break;
			}
	}

	public MultiConfiguration(ICConfigurationDescription[] cfds) {
		this(cfds2cfs(cfds));
	}

	public static IConfiguration[] cfds2cfs(ICConfigurationDescription[] cfgds) {
		IConfiguration[] cfs = new IConfiguration[cfgds.length];
		for (int i = 0; i < cfgds.length; i++)
			cfs[i] = ManagedBuildManager.getConfigurationForDescription(cfgds[i]);
		return cfs;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.MultiItemsHolder#getItems()
	 */
	@Override
	public Object[] getItems() {
		return fCfgs;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#buildsFileType(java.lang.String)
	 */
	@Override
	public boolean buildsFileType(String srcExt) {
		return curr().buildsFileType(srcExt);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#calculateTargetTool()
	 */
	@Override
	public ITool calculateTargetTool() {
		return curr().calculateTargetTool();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#changeBuilder(org.eclipse.cdt.managedbuilder.core.IBuilder, java.lang.String, java.lang.String)
	 */
	@Override
	public void changeBuilder(IBuilder newBuilder, String id, String name) {
		for (int i = 0; i < fCfgs.length; i++)
			fCfgs[i].changeBuilder(newBuilder, id, name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#createFileInfo(org.eclipse.core.runtime.IPath)
	 */
	@Override
	public IFileInfo createFileInfo(IPath path) {
		if (DEBUG)
			System.out.println("Strange multi access: MultiConfiguration.createFileInfo(1)"); //$NON-NLS-1$
		return curr().createFileInfo(path);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#createFileInfo(org.eclipse.core.runtime.IPath, java.lang.String, java.lang.String)
	 */
	@Override
	public IFileInfo createFileInfo(IPath path, String id, String name) {
		if (DEBUG)
			System.out.println("Strange multi access: MultiConfiguration.createFileInfo(3)"); //$NON-NLS-1$
		return curr().createFileInfo(path, id, name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#createFileInfo(org.eclipse.core.runtime.IPath, org.eclipse.cdt.managedbuilder.core.IFolderInfo, org.eclipse.cdt.managedbuilder.core.ITool, java.lang.String, java.lang.String)
	 */
	@Override
	public IFileInfo createFileInfo(IPath path, IFolderInfo base, ITool baseTool, String id, String name) {
		if (DEBUG)
			System.out.println("Bad multi access: MultiConfiguration.createFileInfo(5)"); //$NON-NLS-1$
		return null; // curr().createFileInfo(path, base, baseTool, id, name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#createFileInfo(org.eclipse.core.runtime.IPath, org.eclipse.cdt.managedbuilder.core.IFileInfo, java.lang.String, java.lang.String)
	 */
	@Override
	public IFileInfo createFileInfo(IPath path, IFileInfo base, String id, String name) {
		if (DEBUG)
			System.out.println("Bad multi access: MultiConfiguration.createFileInfo(4)"); //$NON-NLS-1$
		return null; // curr().createFileInfo(path, base, id, name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#createFolderInfo(org.eclipse.core.runtime.IPath)
	 */
	@Override
	public IFolderInfo createFolderInfo(IPath path) {
		if (DEBUG)
			System.out.println("Bad multi access: MultiConfiguration.createFolderInfo()"); //$NON-NLS-1$
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#createFolderInfo(org.eclipse.core.runtime.IPath, java.lang.String, java.lang.String)
	 */
	@Override
	public IFolderInfo createFolderInfo(IPath path, String id, String name) {
		if (DEBUG)
			System.out.println("Bad multi access: MultiConfiguration.createFolderInfo(3)"); //$NON-NLS-1$
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#createFolderInfo(org.eclipse.core.runtime.IPath, org.eclipse.cdt.managedbuilder.core.IFolderInfo, java.lang.String, java.lang.String)
	 */
	@Override
	public IFolderInfo createFolderInfo(IPath path, IFolderInfo base, String id, String name) {
		if (DEBUG)
			System.out.println("Bad multi access: MultiConfiguration.createFolderInfo(4)"); //$NON-NLS-1$
		return null; // do nothing now
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#createResourceConfiguration(org.eclipse.core.resources.IFile)
	 */
	@Override
	public IResourceConfiguration createResourceConfiguration(IFile file) {
		if (DEBUG)
			System.out.println("Bad multi access: MultiConfiguration.createResourceConfiguration()"); //$NON-NLS-1$
		return null; // do nothing now
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#createToolChain(org.eclipse.cdt.managedbuilder.core.IToolChain, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public IToolChain createToolChain(IToolChain superClass, String Id, String name, boolean isExtensionElement) {
		if (DEBUG)
			System.out.println("Bad multi access: MultiConfiguration.createToolChain()"); //$NON-NLS-1$
		return null; // do nothing
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#generateToolCommandLineInfo(java.lang.String, java.lang.String[], java.lang.String, java.lang.String, java.lang.String, java.lang.String[], org.eclipse.core.runtime.IPath, org.eclipse.core.runtime.IPath)
	 */
	@Override
	public IManagedCommandLineInfo generateToolCommandLineInfo(String sourceExtension, String[] flags,
			String outputFlag, String outputPrefix, String outputName, String[] inputResources, IPath inputLocation,
			IPath outputLocation) {
		if (DEBUG)
			System.out.println("Strange multi access: MultiConfiguration.generateToolCommandLineInfo()"); //$NON-NLS-1$
		return curr().generateToolCommandLineInfo(sourceExtension, flags, outputFlag, outputPrefix, outputName,
				inputResources, inputLocation, outputLocation);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getArtifactExtension()
	 */
	@Override
	public String getArtifactExtension() {
		String s = fCfgs[0].getArtifactExtension();
		for (int i = 1; i < fCfgs.length; i++)
			if (!s.equals(fCfgs[i].getArtifactExtension()))
				return EMPTY_STR;
		return s;
	}

	public String[] getArtifactExtensions() {
		String[] s = new String[fCfgs.length];
		for (int i = 0; i < fCfgs.length; i++)
			s[i] = fCfgs[i].getArtifactExtension();
		return s;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getArtifactName()
	 */
	@Override
	public String getArtifactName() {
		String s = fCfgs[0].getArtifactName();
		for (int i = 1; i < fCfgs.length; i++)
			if (!s.equals(fCfgs[i].getArtifactName()))
				return EMPTY_STR;
		return s;
	}

	public String[] getArtifactNames() {
		String[] s = new String[fCfgs.length];
		for (int i = 0; i < fCfgs.length; i++)
			s[i] = fCfgs[i].getArtifactName();
		return s;
	}

	/**
	 * @return build arguments if the arguments for all configurations match
	 *    or {@code null} otherwise.
	 */
	@Override
	public String getBuildArguments() {
		String args0 = fCfgs[0].getBuildArguments();
		if (args0 == null)
			args0 = EMPTY_STR;

		for (IConfiguration cfg : fCfgs) {
			String args = cfg.getBuildArguments();
			if (args == null)
				args = EMPTY_STR;
			if (!args0.equals(args))
				return null;
		}
		return args0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getBuildArtefactType()
	 */
	@Override
	public IBuildPropertyValue getBuildArtefactType() {
		IBuildPropertyValue b = fCfgs[0].getBuildArtefactType();
		if (b == null)
			return null;
		for (int i = 1; i < fCfgs.length; i++)
			if (!b.equals(fCfgs[i].getBuildArtefactType()))
				return null;
		return b;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getBuildCommand()
	 */
	@Override
	public String getBuildCommand() {
		String s = fCfgs[0].getBuildCommand();
		if (s == null || s.length() == 0)
			return EMPTY_STR;
		for (int i = 1; i < fCfgs.length; i++)
			if (!s.equals(fCfgs[i].getBuildCommand()))
				return EMPTY_STR;
		return s;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getBuildData()
	 */
	@Override
	public CBuildData getBuildData() {
		if (DEBUG)
			System.out.println("Strange multi access: MultiConfiguration.getBuildData()"); //$NON-NLS-1$
		return curr().getBuildData();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getBuildMacroSupplier()
	 */
	@Override
	public IConfigurationBuildMacroSupplier getBuildMacroSupplier() {
		IConfigurationBuildMacroSupplier ms = fCfgs[0].getBuildMacroSupplier();
		if (ms == null)
			return null;
		for (int i = 1; i < fCfgs.length; i++)
			if (!ms.equals(fCfgs[i].getBuildMacroSupplier()))
				return null;
		return ms;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getBuilder()
	 */
	@Override
	public IBuilder getBuilder() {
		IBuilder b = fCfgs[0].getBuilder();
		if (b == null)
			return null;
		for (int i = 1; i < fCfgs.length; i++)
			if (!b.matches(fCfgs[i].getBuilder()))
				return null;
		return b;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getCleanCommand()
	 */
	@Override
	public String getCleanCommand() {
		String s = fCfgs[0].getCleanCommand();
		for (int i = 1; i < fCfgs.length; i++)
			if (!s.equals(fCfgs[i].getCleanCommand()))
				return EMPTY_STR;
		return s;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getConfigurationData()
	 */
	@Override
	public CConfigurationData getConfigurationData() {
		if (DEBUG)
			System.out.println("Strange multi access: MultiConfiguration.getConfigurationData()"); //$NON-NLS-1$
		return curr().getConfigurationData();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getDescription()
	 */
	@Override
	public String getDescription() {
		String s = fCfgs[0].getDescription();
		for (int i = 1; i < fCfgs.length; i++)
			if (!s.equals(fCfgs[i].getDescription()))
				return EMPTY_STR;
		return s;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getEditableBuilder()
	 */
	@Override
	public IBuilder getEditableBuilder() {
		return curr().getEditableBuilder();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getEnvironmentVariableSupplier()
	 */
	@Override
	public IConfigurationEnvironmentVariableSupplier getEnvironmentVariableSupplier() {
		IConfigurationEnvironmentVariableSupplier vs = fCfgs[0].getEnvironmentVariableSupplier();
		if (vs == null)
			return null;
		for (int i = 1; i < fCfgs.length; i++)
			if (!vs.equals(fCfgs[i].getEnvironmentVariableSupplier()))
				return null;
		return vs;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getErrorParserIds()
	 */

	@Override
	public String getErrorParserIds() {
		String s = fCfgs[0].getErrorParserIds();
		if (s == null || s.length() == 0)
			return EMPTY_STR;
		for (int i = 1; i < fCfgs.length; i++)
			if (!s.equals(fCfgs[i].getErrorParserIds()))
				return EMPTY_STR;
		return s;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getErrorParserList()
	 */
	@Override
	public String[] getErrorParserList() {
		String[] s = fCfgs[0].getErrorParserList();
		if (s == null || s.length == 0)
			return EMPTY_STR_ARRAY;
		for (int i = 1; i < fCfgs.length; i++)
			if (!Arrays.equals(s, fCfgs[i].getErrorParserList()))
				return EMPTY_STR_ARRAY;
		return s;
	}

	@Override
	public String[] getDefaultLanguageSettingsProviderIds() {
		ManagedBuilderCorePlugin
				.error("Default Language Settings Providers are not supported in multiconfiguration mode"); //$NON-NLS-1$
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getFilteredTools()
	 */
	@Override
	public ITool[] getFilteredTools() {
		ITool[] ts = curr().getFilteredTools();
		return ts;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getLibs(java.lang.String)
	 */
	@Override
	public String[] getLibs(String extension) {
		String[] s = fCfgs[0].getLibs(extension);
		if (s == null || s.length == 0)
			return EMPTY_STR_ARRAY;
		for (int i = 1; i < fCfgs.length; i++)
			if (!Arrays.equals(s, fCfgs[i].getLibs(extension)))
				return EMPTY_STR_ARRAY;
		return s;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getManagedProject()
	 */
	@Override
	public IManagedProject getManagedProject() {
		IManagedProject s = fCfgs[0].getManagedProject();
		if (s == null)
			return null;
		for (int i = 1; i < fCfgs.length; i++)
			if (!s.equals(fCfgs[i].getManagedProject()))
				return null;
		return s;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getOutputExtension(java.lang.String)
	 */
	@Override
	public String getOutputExtension(String resourceExtension) {
		String s = fCfgs[0].getOutputExtension(resourceExtension);
		if (s == null || s.length() == 0)
			return EMPTY_STR;
		for (int i = 1; i < fCfgs.length; i++)
			if (!s.equals(fCfgs[i].getOutputExtension(resourceExtension)))
				return EMPTY_STR;
		return s;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getOutputFlag(java.lang.String)
	 */
	@Override
	public String getOutputFlag(String outputExt) {
		String s = fCfgs[0].getOutputFlag(outputExt);
		if (s == null || s.length() == 0)
			return EMPTY_STR;
		for (int i = 1; i < fCfgs.length; i++)
			if (!s.equals(fCfgs[i].getOutputFlag(outputExt)))
				return EMPTY_STR;
		return s;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getOutputPrefix(java.lang.String)
	 */
	@Override
	public String getOutputPrefix(String outputExtension) {
		String s = fCfgs[0].getOutputPrefix(outputExtension);
		if (s == null || s.length() == 0)
			return EMPTY_STR;
		for (int i = 1; i < fCfgs.length; i++)
			if (!s.equals(fCfgs[i].getOutputPrefix(outputExtension)))
				return EMPTY_STR;
		return s;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getOwner()
	 */
	@Override
	public IResource getOwner() {
		IResource s = fCfgs[0].getOwner();
		if (s == null)
			return null;
		for (int i = 1; i < fCfgs.length; i++)
			if (!s.equals(fCfgs[i].getOwner()))
				return null;
		return s;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getParent()
	 */
	@Override
	public IConfiguration getParent() {
		if (DEBUG)
			System.out.println("Bad multi access: MultiConfiguration.getParent()"); //$NON-NLS-1$
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getPostannouncebuildStep()
	 */
	@Override
	public String getPostannouncebuildStep() {
		String s = fCfgs[0].getPostannouncebuildStep();
		if (s == null || s.length() == 0)
			return EMPTY_STR;
		for (int i = 1; i < fCfgs.length; i++)
			if (!s.equals(fCfgs[i].getPostannouncebuildStep()))
				return EMPTY_STR;
		return s;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getPostbuildStep()
	 */
	@Override
	public String getPostbuildStep() {
		String s = fCfgs[0].getPostbuildStep();
		if (s == null || s.length() == 0)
			return EMPTY_STR;
		for (int i = 1; i < fCfgs.length; i++)
			if (!s.equals(fCfgs[i].getPostbuildStep()))
				return EMPTY_STR;
		return s;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getPreannouncebuildStep()
	 */
	@Override
	public String getPreannouncebuildStep() {
		String s = fCfgs[0].getPreannouncebuildStep();
		if (s == null || s.length() == 0)
			return EMPTY_STR;
		for (int i = 1; i < fCfgs.length; i++)
			if (!s.equals(fCfgs[i].getPreannouncebuildStep()))
				return EMPTY_STR;
		return s;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getPrebuildStep()
	 */
	@Override
	public String getPrebuildStep() {
		String s = fCfgs[0].getPrebuildStep();
		if (s == null || s.length() == 0)
			return EMPTY_STR;
		for (int i = 1; i < fCfgs.length; i++)
			if (!s.equals(fCfgs[i].getPrebuildStep()))
				return EMPTY_STR;
		return s;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getProjectType()
	 */
	@Override
	public IProjectType getProjectType() {
		if (DEBUG)
			System.out.println("Strange multi access: MultiConfiguration.getProjectType()"); //$NON-NLS-1$
		return curr().getProjectType();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getResourceConfiguration(java.lang.String)
	 */
	@Override
	public IResourceConfiguration getResourceConfiguration(String path) {
		if (DEBUG)
			System.out.println("Bad multi access: MultiConfiguration.getResourceConfiguration()"); //$NON-NLS-1$
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getResourceConfigurations()
	 */
	@Override
	public IResourceConfiguration[] getResourceConfigurations() {
		if (DEBUG)
			System.out.println("Bad multi access: MultiConfiguration.getResourceConfigurations()"); //$NON-NLS-1$
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getResourceInfo(org.eclipse.core.runtime.IPath, boolean)
	 */
	@Override
	public IResourceInfo getResourceInfo(IPath path, boolean exactPath) {
		IResourceInfo ris[] = new IResourceInfo[fCfgs.length];
		boolean isFolder = true;
		for (int i = 0; i < fCfgs.length; i++) {
			ris[i] = fCfgs[i].getResourceInfo(path, exactPath);
			if (!(ris[i] instanceof IFolderInfo))
				isFolder = false;
		}
		if (isFolder) {
			IFolderInfo fis[] = new IFolderInfo[ris.length];
			System.arraycopy(ris, 0, fis, 0, ris.length);
			return new MultiFolderInfo(fis, this);
		}
		return new MultiFileInfo(ris, this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getResourceInfoById(java.lang.String)
	 */
	@Override
	public IResourceInfo getResourceInfoById(String id) {
		if (DEBUG)
			System.out.println("Bad multi access: MultiConfiguration.getResourceInfoById()"); //$NON-NLS-1$
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getResourceInfos()
	 */
	@Override
	public IResourceInfo[] getResourceInfos() {
		ArrayList<IResourceInfo> ri = new ArrayList<>();
		for (int i = 0; i < fCfgs.length; i++) {
			IResourceInfo[] ris = fCfgs[i].getResourceInfos();
			ri.addAll(Arrays.asList(ris));
		}
		return ri.toArray(new IResourceInfo[ri.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getRootFolderInfo()
	 */
	@Override
	public IFolderInfo getRootFolderInfo() {
		IFolderInfo ris[] = new IFolderInfo[fCfgs.length];
		for (int i = 0; i < fCfgs.length; i++)
			ris[i] = fCfgs[i].getRootFolderInfo();
		return new MultiFolderInfo(ris, this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getSourceEntries()
	 */
	@Override
	public ICSourceEntry[] getSourceEntries() {
		if (DEBUG)
			System.out.println("Strange multi access: MultiConfiguration.getSourceEntries()"); //$NON-NLS-1$
		return curr().getSourceEntries();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getTargetTool()
	 */
	@Override
	public ITool getTargetTool() {
		if (DEBUG)
			System.out.println("Strange multi access: MultiConfiguration.getTargetTool()"); //$NON-NLS-1$
		return curr().getTargetTool();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getTool(java.lang.String)
	 */
	@Override
	public ITool getTool(String id) {
		if (DEBUG)
			System.out.println("Strange multi access: MultiConfiguration.getTool()"); //$NON-NLS-1$
		return curr().getTool(id);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getToolChain()
	 */
	@Override
	public IToolChain getToolChain() {
		return curr().getToolChain();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getToolCommand(org.eclipse.cdt.managedbuilder.core.ITool)
	 */
	@Override
	public String getToolCommand(ITool tool) {
		return curr().getToolCommand(tool);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getToolFromInputExtension(java.lang.String)
	 */
	@Override
	public ITool getToolFromInputExtension(String sourceExtension) {
		if (DEBUG)
			System.out.println("Strange multi access: MultiConfiguration.getToolFromInputExtension()"); //$NON-NLS-1$
		return curr().getToolFromInputExtension(sourceExtension);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getToolFromOutputExtension(java.lang.String)
	 */
	@Override
	public ITool getToolFromOutputExtension(String extension) {
		if (DEBUG)
			System.out.println("Strange multi access: MultiConfiguration.getToolFromOutputExtension()"); //$NON-NLS-1$
		return curr().getToolFromOutputExtension(extension);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getTools()
	 */
	@Override
	public ITool[] getTools() {
		if (DEBUG)
			System.out.println("Strange multi access: MultiConfiguration.getTools()"); //$NON-NLS-1$
		return curr().getTools();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getToolsBySuperClassId(java.lang.String)
	 */
	@Override
	public ITool[] getToolsBySuperClassId(String id) {
		if (DEBUG)
			System.out.println("Strange multi access: MultiConfiguration.getToolsBySuperClassId()"); //$NON-NLS-1$
		return curr().getToolsBySuperClassId(id);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getUserObjects(java.lang.String)
	 */
	@Override
	public String[] getUserObjects(String extension) {
		if (DEBUG)
			System.out.println("Strange multi access: MultiConfiguration.getUserObjects()"); //$NON-NLS-1$
		return curr().getUserObjects(extension);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#hasOverriddenBuildCommand()
	 */
	@Override
	public boolean hasOverriddenBuildCommand() {
		for (int i = 0; i < fCfgs.length; i++)
			if (fCfgs[i].hasOverriddenBuildCommand())
				return true;
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#isBuilderCompatible(org.eclipse.cdt.managedbuilder.core.IBuilder)
	 */
	@Override
	public boolean isBuilderCompatible(IBuilder builder) {
		for (int i = 0; i < fCfgs.length; i++)
			if (!fCfgs[i].isBuilderCompatible(builder))
				return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#isDirty()
	 */
	@Override
	public boolean isDirty() {
		for (int i = 0; i < fCfgs.length; i++)
			if (fCfgs[i].isDirty())
				return true;
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#isExtensionElement()
	 */
	@Override
	public boolean isExtensionElement() {
		if (DEBUG)
			System.out.println("Strange multi access: MultiConfiguration.isExtensionElement()"); //$NON-NLS-1$
		return curr().isExtensionElement();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#isHeaderFile(java.lang.String)
	 */
	@Override
	public boolean isHeaderFile(String ext) {
		if (DEBUG)
			System.out.println("Strange multi access: MultiConfiguration.isHeaderFile()"); //$NON-NLS-1$
		return curr().isHeaderFile(ext);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#isManagedBuildOn()
	 */
	@Override
	public boolean isManagedBuildOn() {
		for (int i = 0; i < fCfgs.length; i++)
			if (!fCfgs[i].isManagedBuildOn())
				return false;
		return true;
	}

	public boolean[] isManagedBuildOnMulti() {
		boolean[] b = new boolean[fCfgs.length];
		for (int i = 0; i < fCfgs.length; i++)
			b[i] = fCfgs[i].isManagedBuildOn();
		return b;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#isSupported()
	 */
	@Override
	public boolean isSupported() {
		for (int i = 0; i < fCfgs.length; i++)
			if (fCfgs[i].isSupported())
				return true;
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#isSystemObject()
	 */
	@Override
	public boolean isSystemObject() {
		for (int i = 0; i < fCfgs.length; i++)
			if (!fCfgs[i].isSystemObject())
				return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#isTemporary()
	 */
	@Override
	public boolean isTemporary() {
		for (int i = 0; i < fCfgs.length; i++)
			if (!fCfgs[i].isTemporary())
				return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#needsFullRebuild()
	 */
	@Override
	public boolean needsFullRebuild() {
		for (int i = 0; i < fCfgs.length; i++)
			if (fCfgs[i].needsFullRebuild())
				return true;
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#needsRebuild()
	 */
	@Override
	public boolean needsRebuild() {
		for (int i = 0; i < fCfgs.length; i++)
			if (fCfgs[i].needsRebuild())
				return true;
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#removeResourceConfiguration(org.eclipse.cdt.managedbuilder.core.IResourceInfo)
	 */
	@Override
	public void removeResourceConfiguration(IResourceInfo resConfig) {
		for (int i = 0; i < fCfgs.length; i++)
			fCfgs[i].removeResourceConfiguration(resConfig);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#removeResourceInfo(org.eclipse.core.runtime.IPath)
	 */
	@Override
	public void removeResourceInfo(IPath path) {
		for (int i = 0; i < fCfgs.length; i++)
			fCfgs[i].removeResourceInfo(path);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#setArtifactExtension(java.lang.String)
	 */
	@Override
	public void setArtifactExtension(String extension) {
		for (int i = 0; i < fCfgs.length; i++)
			fCfgs[i].setArtifactExtension(extension);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#setArtifactName(java.lang.String)
	 */
	@Override
	public void setArtifactName(String name) {
		for (int i = 0; i < fCfgs.length; i++)
			fCfgs[i].setArtifactName(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#setBuildArguments(java.lang.String)
	 */
	@Override
	public void setBuildArguments(String makeArgs) {
		for (int i = 0; i < fCfgs.length; i++)
			fCfgs[i].setBuildArguments(makeArgs);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#setBuildArtefactType(java.lang.String)
	 */
	@Override
	public void setBuildArtefactType(String id) throws BuildException {
		for (int i = 0; i < fCfgs.length; i++)
			fCfgs[i].setBuildArtefactType(id);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#setBuildCommand(java.lang.String)
	 */
	@Override
	public void setBuildCommand(String command) {
		for (int i = 0; i < fCfgs.length; i++)
			fCfgs[i].setBuildCommand(command);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#setCleanCommand(java.lang.String)
	 */
	@Override
	public void setCleanCommand(String command) {
		for (int i = 0; i < fCfgs.length; i++)
			fCfgs[i].setCleanCommand(command);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#setDescription(java.lang.String)
	 */
	@Override
	public void setDescription(String description) {
		for (int i = 0; i < fCfgs.length; i++)
			fCfgs[i].setDescription(description);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#setDirty(boolean)
	 */
	@Override
	public void setDirty(boolean isDirty) {
		for (int i = 0; i < fCfgs.length; i++)
			fCfgs[i].setDirty(isDirty);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#setErrorParserIds(java.lang.String)
	 */
	@Override
	public void setErrorParserIds(String ids) {
		for (int i = 0; i < fCfgs.length; i++)
			fCfgs[i].setErrorParserIds(ids);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#setErrorParserList(java.lang.String[])
	 */
	@Override
	public void setErrorParserList(String[] ids) {
		for (int i = 0; i < fCfgs.length; i++)
			fCfgs[i].setErrorParserList(ids);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#setManagedBuildOn(boolean)
	 */
	@Override
	public void setManagedBuildOn(boolean on) throws BuildException {
		for (int i = 0; i < fCfgs.length; i++)
			fCfgs[i].setManagedBuildOn(on);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
	} // do nothing

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#setOption(org.eclipse.cdt.managedbuilder.core.IHoldsOptions, org.eclipse.cdt.managedbuilder.core.IOption, boolean)
	 */
	@Override
	public IOption setOption(IHoldsOptions parent, IOption option, boolean value) throws BuildException {
		IOption op = null;
		for (int i = 0; i < fCfgs.length; i++)
			op = fCfgs[i].setOption(parent, option, value);
		return op;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#setOption(org.eclipse.cdt.managedbuilder.core.IHoldsOptions, org.eclipse.cdt.managedbuilder.core.IOption, java.lang.String)
	 */
	@Override
	public IOption setOption(IHoldsOptions parent, IOption option, String value) throws BuildException {
		IOption op = null;
		for (int i = 0; i < fCfgs.length; i++)
			op = fCfgs[i].setOption(parent, option, value);
		return op;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#setOption(org.eclipse.cdt.managedbuilder.core.IHoldsOptions, org.eclipse.cdt.managedbuilder.core.IOption, java.lang.String[])
	 */
	@Override
	public IOption setOption(IHoldsOptions parent, IOption option, String[] value) throws BuildException {
		IOption op = null;
		for (int i = 0; i < fCfgs.length; i++)
			op = fCfgs[i].setOption(parent, option, value);
		return op;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#setPostannouncebuildStep(java.lang.String)
	 */
	@Override
	public void setPostannouncebuildStep(String announceStep) {
		for (int i = 0; i < fCfgs.length; i++)
			fCfgs[i].setPostannouncebuildStep(announceStep);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#setPostbuildStep(java.lang.String)
	 */
	@Override
	public void setPostbuildStep(String step) {
		for (int i = 0; i < fCfgs.length; i++)
			fCfgs[i].setPostbuildStep(step);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#setPreannouncebuildStep(java.lang.String)
	 */
	@Override
	public void setPreannouncebuildStep(String announceStep) {
		for (int i = 0; i < fCfgs.length; i++)
			fCfgs[i].setPreannouncebuildStep(announceStep);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#setPrebuildStep(java.lang.String)
	 */
	@Override
	public void setPrebuildStep(String step) {
		for (int i = 0; i < fCfgs.length; i++)
			fCfgs[i].setPrebuildStep(step);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#setRebuildState(boolean)
	 */
	@Override
	public void setRebuildState(boolean rebuild) {
		for (int i = 0; i < fCfgs.length; i++)
			fCfgs[i].setRebuildState(rebuild);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#setSourceEntries(org.eclipse.cdt.core.settings.model.ICSourceEntry[])
	 */
	@Override
	public void setSourceEntries(ICSourceEntry[] entries) {
		for (int i = 0; i < fCfgs.length; i++)
			fCfgs[i].setSourceEntries(entries);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#setToolCommand(org.eclipse.cdt.managedbuilder.core.ITool, java.lang.String)
	 */
	@Override
	public void setToolCommand(ITool tool, String command) {
		for (int i = 0; i < fCfgs.length; i++)
			fCfgs[i].setToolCommand(tool, command);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#supportsBuild(boolean)
	 */
	@Override
	public boolean supportsBuild(boolean managed) {
		for (int i = 0; i < fCfgs.length; i++)
			if (!fCfgs[i].supportsBuild(managed))
				return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuildObject#getBaseId()
	 */
	@Override
	public String getBaseId() {
		if (DEBUG)
			System.out.println("Strange multi access: MultiConfiguration.getBaseId()"); //$NON-NLS-1$
		return curr().getBaseId();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuildObject#getId()
	 */
	@Override
	public String getId() {
		return curr().getId() + "_etc"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuildObject#getManagedBuildRevision()
	 */
	@Override
	public String getManagedBuildRevision() {
		if (DEBUG)
			System.out.println("Strange multi access: MultiConfiguration.getMngBuildRevision()"); //$NON-NLS-1$
		return curr().getManagedBuildRevision();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuildObject#getName()
	 */
	@Override
	public String getName() {
		return "Multiple configurations"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuildObject#getVersion()
	 */
	@Override
	public Version getVersion() {
		if (DEBUG)
			System.out.println("Strange multi access: MultiConfiguration.getVersion()"); //$NON-NLS-1$
		return curr().getVersion();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuildObject#setVersion(org.eclipse.core.runtime.PluginVersionIdentifier)
	 */
	@Override
	public void setVersion(Version version) {
	} // do nothing

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuildObjectPropertiesContainer#getBuildProperties()
	 */
	@Override
	public IBuildObjectProperties getBuildProperties() {
		return curr().getBuildProperties();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuildObjectPropertiesContainer#getOptionalBuildProperties()
	 */
	@Override
	public IOptionalBuildProperties getOptionalBuildProperties() {
		return curr().getOptionalBuildProperties();
	}

	@Override
	public boolean getParallelDef() {
		for (IConfiguration cfg : fCfgs) {
			if (cfg instanceof Configuration) {
				if (!((Configuration) cfg).getParallelDef())
					return false;
			} else
				return false;
		}
		return true; // all cfgs report true
	}

	@Override
	public void setParallelDef(boolean parallel) {
		for (IConfiguration cfg : fCfgs) {
			if (cfg instanceof Configuration)
				((Configuration) cfg).setParallelDef(parallel);
		}
	}

	@Override
	public int getParallelNumber() {
		int res = 0;
		for (IConfiguration cfg : fCfgs) {
			if (cfg instanceof Configuration) {
				int num = ((Configuration) cfg).getParallelNumber();
				Assert.isTrue(num != 0); // can't be 0, see IMakeCommonBuildInfo.getParallelizationNum()

				if (res == 0)
					res = num;
				else if (res != num)
					return 0; // values are different !
			} else
				return 0;
		}
		return res; // all cfgs report same value
	}

	@Override
	public void setParallelNumber(int num) {
		for (IConfiguration cfg : fCfgs) {
			if (cfg instanceof Configuration)
				((Configuration) cfg).setParallelNumber(num);
		}
	}

	@Override
	public boolean getInternalBuilderParallel() {
		return getParallelDef();
	}

	@Override
	public boolean isInternalBuilderEnabled() {
		for (int i = 0; i < fCfgs.length; i++)
			if (fCfgs[i] instanceof Configuration) {
				if (!((Configuration) fCfgs[i]).isInternalBuilderEnabled())
					return false;
			} else
				return false;
		return true; // all cfgs report true
	}

	@Override
	public boolean canEnableInternalBuilder(boolean v) {
		for (int i = 0; i < fCfgs.length; i++)
			if (fCfgs[i] instanceof Configuration) {
				if (!((Configuration) fCfgs[i]).canEnableInternalBuilder(v))
					return false;
			} else
				return false;
		return true; // all cfgs report true
	}

	@Override
	public void enableInternalBuilder(boolean v) {
		for (int i = 0; i < fCfgs.length; i++)
			if (fCfgs[i] instanceof Configuration)
				((Configuration) fCfgs[i]).enableInternalBuilder(v);
	}

	/**
	 * Returns "default" configuration.
	 */
	private IConfiguration curr() {
		return fCfgs[curr];
	}

	@Override
	public String getToolOutputPrefix() {
		String s = fCfgs[0].calculateTargetTool().getOutputPrefix();
		if (s == null || s.length() == 0)
			return EMPTY_STR;
		for (int i = 1; i < fCfgs.length; i++)
			if (!s.equals(fCfgs[i].calculateTargetTool().getOutputPrefix()))
				return EMPTY_STR;
		return s;
	}

	@Override
	public void setOutputPrefixForPrimaryOutput(String pref) {
		for (int i = 0; i < fCfgs.length; i++)
			fCfgs[i].calculateTargetTool().setOutputPrefixForPrimaryOutput(pref);
	}

	@Override
	public IBuildProperty getBuildProperty(String id) {
		IBuildProperty b = fCfgs[0].getBuildProperties().getProperty(id);
		if (b == null)
			return null;
		for (int i = 1; i < fCfgs.length; i++)
			if (!b.getValue().getId().equals(fCfgs[i].getBuildProperties().getProperty(id).getValue().getId()))
				return null;
		return b;
	}

	// Performing conjunction of supported values for each cfg
	@Override
	public IBuildPropertyValue[] getSupportedValues(String id) {
		IBuildPropertyValue[] a = fCfgs[0].getBuildProperties().getSupportedValues(id);
		if (a == null || a.length == 0)
			return new IBuildPropertyValue[0];
		int cnt = a.length;
		for (int i = 1; i < fCfgs.length; i++) {
			IBuildPropertyValue[] b = fCfgs[i].getBuildProperties().getSupportedValues(id);
			for (int x = 0; x < a.length; x++) {
				if (a[x] == null)
					continue;
				boolean found = false;
				for (int y = 0; y < b.length; y++) {
					if (a[x].equals(b[y])) {
						found = true;
						break;
					}
				}
				if (!found) {
					a[x] = null;
					cnt--;
				}
			}
		}
		if (cnt > 0) {
			IBuildPropertyValue[] b = new IBuildPropertyValue[cnt];
			int pos = 0;
			for (int x = 0; x < a.length && pos < cnt; x++) {
				if (a[x] != null)
					b[pos++] = a[x];
			}
			return b;
		} else {
			return new IBuildPropertyValue[0];
		}
	}

	@Override
	public void setBuildProperty(String id, String val) {
		try {
			for (int i = 0; i < fCfgs.length; i++)
				fCfgs[i].getBuildProperties().setProperty(id, val);
		} catch (CoreException e) {
			ManagedBuilderCorePlugin.log(e);
		}
	}

	public String getBuildAttribute(String name, String defValue) {
		String res = defValue;
		IBuilder b = fCfgs[0].getBuilder();
		if (b != null)
			res = b.getBuildAttribute(name, defValue);
		for (int i = 1; i < fCfgs.length; i++) {
			b = fCfgs[i].getBuilder();
			if (b != null)
				if (!res.equals(b.getBuildAttribute(name, defValue)))
					return defValue;
		}
		return res;
	}
}
