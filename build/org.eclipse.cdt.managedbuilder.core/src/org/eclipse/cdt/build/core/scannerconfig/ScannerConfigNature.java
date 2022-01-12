/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.build.core.scannerconfig;

import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;

/**
 * @see IProjectNature
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class ScannerConfigNature implements IProjectNature {

	public final static String NATURE_ID = ManagedBuilderCorePlugin.getUniqueIdentifier() + ".ScannerConfigNature"; //$NON-NLS-1$
	private IProject fProject;

	/**
	 * @see IProjectNature#configure
	 */
	@Override
	public void configure() throws CoreException {
		IProjectDescription description = getProject().getDescription();
		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(ScannerConfigBuilder.BUILDER_ID)) {
				return;
			}
		}
		ICommand command = description.newCommand();
		command.setBuilderName(ScannerConfigBuilder.BUILDER_ID);
		command.setBuilding(IncrementalProjectBuilder.AUTO_BUILD, false);
		command.setBuilding(IncrementalProjectBuilder.CLEAN_BUILD, false);
		command.setBuilding(IncrementalProjectBuilder.FULL_BUILD, true);
		command.setBuilding(IncrementalProjectBuilder.INCREMENTAL_BUILD, true);

		ICommand[] newCommands = new ICommand[commands.length + 1];
		System.arraycopy(commands, 0, newCommands, 0, commands.length);
		newCommands[commands.length] = command;
		description.setBuildSpec(newCommands);
		getProject().setDescription(description, null);

		// set default project scanner config settings
	}

	/**
	 * @see IProjectNature#deconfigure
	 */
	@Override
	public void deconfigure() throws CoreException {
		IProjectDescription description = getProject().getDescription();
		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(ScannerConfigBuilder.BUILDER_ID)) {
				ICommand[] newCommands = new ICommand[commands.length - 1];
				System.arraycopy(commands, 0, newCommands, 0, i);
				System.arraycopy(commands, i + 1, newCommands, i, commands.length - i - 1);
				description.setBuildSpec(newCommands);
				break;
			}
		}
		getProject().setDescription(description, null);
	}

	/**
	 * @see IProjectNature#getProject
	 */
	@Override
	public IProject getProject() {
		return fProject;
	}

	/**
	 * @see IProjectNature#setProject
	 */
	@Override
	public void setProject(IProject project) {
		fProject = project;
	}

	public static void addScannerConfigNature(IProject project) throws CoreException {
		IProjectDescription description = project.getDescription();
		if (description.hasNature(NATURE_ID))
			return;
		String[] ids = description.getNatureIds();
		String[] newIds = new String[ids.length + 1];
		System.arraycopy(ids, 0, newIds, 0, ids.length);
		newIds[ids.length] = NATURE_ID;
		description.setNatureIds(newIds);
		project.setDescription(description, null);

	}

	public static void removeScannerConfigNature(IProject project) throws CoreException {
		IProjectDescription description = project.getDescription();
		if (!description.hasNature(NATURE_ID))
			return;
		String[] ids = description.getNatureIds();
		for (int i = 0; i < ids.length; ++i) {
			if (ids[i].equals(NATURE_ID)) {
				String[] newIds = new String[ids.length - 1];
				System.arraycopy(ids, 0, newIds, 0, i);
				System.arraycopy(ids, i + 1, newIds, i, ids.length - i - 1);
				description.setNatureIds(newIds);
				project.setDescription(description, null);
			}
		}

	}

	/**
	 * Returns build command as stored in .project file
	 */
	public static ICommand getBuildSpec(IProjectDescription description, String builderID) {
		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(builderID)) {
				return commands[i];
			}
		}
		return null;
	}

	/**
	 * Stores a build command in .project file
	 */
	public static IProjectDescription setBuildSpec(IProjectDescription description, ICommand newCommand) {
		ICommand[] oldCommands = description.getBuildSpec();
		ICommand oldCommand = getBuildSpec(description, newCommand.getBuilderName());
		ICommand[] newCommands;

		if (oldCommand == null) {
			// Add the build spec at the end
			newCommands = new ICommand[oldCommands.length + 1];
			System.arraycopy(oldCommands, 0, newCommands, 0, oldCommands.length);
			newCommands[oldCommands.length] = newCommand;
		} else {
			for (int i = 0; i < oldCommands.length; i++) {
				if (oldCommands[i] == oldCommand) {
					oldCommands[i] = newCommand;
					break;
				}
			}
			newCommands = oldCommands;
		}

		// Commit the spec change into the project
		description.setBuildSpec(newCommands);
		return description;
	}

	/**
	 * @param project
	 */
	//FIXME	public static void initializeDiscoveryOptions(IProject project) {
	//		try {
	//			IScannerConfigBuilderInfo2 scPrefInfo = ScannerConfigProfileManager.createScannerConfigBuildInfo2(
	//					ManagedBuilderCorePlugin.getDefault().getPluginPreferences(), false);
	//			String selectedProfile = scPrefInfo.getSelectedProfileId();
	//			IScannerConfigBuilderInfo2 scProjInfo = ScannerConfigProfileManager.createScannerConfigBuildInfo2(
	//					project, selectedProfile);
	//
	//			scProjInfo.setAutoDiscoveryEnabled(scPrefInfo.isAutoDiscoveryEnabled());
	//			scProjInfo.setProblemReportingEnabled(scPrefInfo.isProblemReportingEnabled());
	//
	//			scProjInfo.setBuildOutputParserEnabled(scPrefInfo.isBuildOutputParserEnabled());
	//			scProjInfo.setBuildOutputFileActionEnabled(scPrefInfo.isBuildOutputFileActionEnabled());
	//			scProjInfo.setBuildOutputFilePath(scPrefInfo.getBuildOutputFilePath());
	//
	//			ScannerConfigProfile profile = ScannerConfigProfileManager.getInstance().getSCProfileConfiguration(selectedProfile);
	//			List providerIdList = scPrefInfo.getProviderIdList();
	//			for (Iterator i = providerIdList.iterator(); i.hasNext();) {
	//				String providerId = (String) i.next();
	//
	//				scProjInfo.setProviderOutputParserEnabled(providerId, scPrefInfo.isProviderOutputParserEnabled(providerId));
	//				if (profile.getScannerInfoProviderElement(providerId).getProviderKind().equals(
	//						ScannerConfigProfile.ScannerInfoProvider.RUN)) {
	//					scProjInfo.setProviderRunCommand(providerId, scPrefInfo.getProviderRunCommand(providerId));
	//					scProjInfo.setProviderRunArguments(providerId, scPrefInfo.getProviderRunArguments(providerId));
	//				}
	//				else {
	//					scProjInfo.setProviderOpenFilePath(providerId, scPrefInfo.getProviderOpenFilePath(providerId));
	//				}
	//			}
	//			scProjInfo.save();
	//
	//			// the last step is to add discovered paths container
	//			ICProject cProject = CoreModel.getDefault().create(project);
	//			IPathEntry[] rawPathEntries = CoreModel.getRawPathEntries(cProject);
	//			boolean found = false;
	//			for (int i = 0; i < rawPathEntries.length; i++) {
	//				if (rawPathEntries[i].getEntryKind() == IPathEntry.CDT_CONTAINER) {
	//					IContainerEntry container = (IContainerEntry) rawPathEntries[i];
	//					if (container.getPath().equals(DiscoveredPathContainer.CONTAINER_ID)) {
	//						found = true;
	//						break;
	//					}
	//				}
	//			}
	//			if (!found) {
	//				IPathEntry[] newRawPathEntries = new IPathEntry[rawPathEntries.length + 1];
	//				System.arraycopy(rawPathEntries, 0, newRawPathEntries, 0, rawPathEntries.length);
	//				newRawPathEntries[rawPathEntries.length] = CoreModel.newContainerEntry(DiscoveredPathContainer.CONTAINER_ID);
	//				CoreModel.setRawPathEntries(cProject, newRawPathEntries, null);
	//			}
	////			if (profile.getProfileScope().equals(ScannerConfigScope.PROJECT_SCOPE)) {
	////				CoreModel.setPathEntryContainer(new ICProject[]{cProject},
	////						new DiscoveredPathContainer(project), null);
	////			}
	////			else {	// file scope
	////				CoreModel.setPathEntryContainer(new ICProject[]{cProject},
	////						new PerFileDiscoveredPathContainer(project), null);
	////			}
	//		}
	//		catch (CoreException e) {
	//			ManagedBuilderCorePlugin.log(e);
	//		}
	//	}

}
