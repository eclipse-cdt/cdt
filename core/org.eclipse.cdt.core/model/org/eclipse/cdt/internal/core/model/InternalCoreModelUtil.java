/*******************************************************************************
 * Copyright (c) 2010, 2024 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Sergey Prigogin (Google) - initial API and implementation
 *     John Dallaway - Support build CWD lookup (#652)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import static org.eclipse.cdt.core.model.CoreModelUtil.isExcludedPath;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariableManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.ISourceEntry;
import org.eclipse.cdt.core.settings.model.CSourceEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICOutputEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.WriteAccessException;
import org.eclipse.cdt.core.settings.model.extension.CBuildData;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * Non-API methods for manipulating C/C++ projects.
 */
public class InternalCoreModelUtil {
	public static void addSourceEntry(IProject project, IFolder folder, boolean removeProject, IProgressMonitor monitor)
			throws CoreException {
		ICSourceEntry newEntry = new CSourceEntry(folder, null, 0);
		ICProjectDescription des = CCorePlugin.getDefault().getProjectDescription(project, true);
		addEntryToAllCfgs(des, newEntry, removeProject);
		CCorePlugin.getDefault().setProjectDescription(project, des, false, monitor);
	}

	private static void addEntryToAllCfgs(ICProjectDescription des, ICSourceEntry entry, boolean removeProject)
			throws WriteAccessException, CoreException {
		ICConfigurationDescription cfgs[] = des.getConfigurations();
		for (ICConfigurationDescription cfg : cfgs) {
			ICSourceEntry[] entries = cfg.getSourceEntries();
			entries = addEntry(entries, entry, removeProject);
			cfg.setSourceEntries(entries);
		}
	}

	private static ICSourceEntry[] addEntry(ICSourceEntry[] entries, ICSourceEntry sourceEntry, boolean removeProject) {
		Set<ICSourceEntry> set = new HashSet<>();
		for (ICSourceEntry entry : entries) {
			if (removeProject && new Path(entry.getValue()).segmentCount() == 1)
				continue;
			set.add(entry);
		}
		set.add(sourceEntry);
		return set.toArray(new ICSourceEntry[set.size()]);
	}

	public static void addExclusionPatterns(IPathEntry newEntry, List<IPathEntry> existing,
			Set<IPathEntry> modifiedEntries) {
		IPath entryPath = newEntry.getPath();
		for (int i = 0; i < existing.size(); i++) {
			IPathEntry curr = existing.get(i);
			IPath currPath = curr.getPath();
			if (curr.getEntryKind() == IPathEntry.CDT_SOURCE && currPath.isPrefixOf(entryPath)) {
				IPath[] exclusionFilters = ((ISourceEntry) curr).getExclusionPatterns();
				if (!isExcludedPath(entryPath, exclusionFilters)) {
					IPath pathToExclude = entryPath.removeFirstSegments(currPath.segmentCount()).addTrailingSeparator();
					IPath[] newExclusionFilters = new IPath[exclusionFilters.length + 1];
					System.arraycopy(exclusionFilters, 0, newExclusionFilters, 0, exclusionFilters.length);
					newExclusionFilters[exclusionFilters.length] = pathToExclude;

					IPathEntry updated = CoreModel.newSourceEntry(currPath, newExclusionFilters);
					existing.set(i, updated);
					modifiedEntries.add(updated);
				}
			}
		}
	}

	public static ICConfigurationDescription findBuildConfiguration(IResource resource) {
		IPath location = resource.getLocation();
		IProject project = resource.getProject();
		ICProjectDescription projectDesc = CoreModel.getDefault().getProjectDescription(project, false);
		if (projectDesc == null) {
			return null; // not a CDT project
		}
		// for each build configuration of the project
		for (ICConfigurationDescription configDesc : projectDesc.getConfigurations()) {
			CConfigurationData configData = configDesc.getConfigurationData();
			if (configData == null) {
				continue; // no configuration data
			}
			CBuildData buildData = configData.getBuildData();
			if (buildData == null) {
				continue; // no build data
			}
			// for each build output directory of the build configuration
			for (ICOutputEntry dir : buildData.getOutputDirectories()) {
				IPath dirLocation = CDataUtil.makeAbsolute(project, dir).getLocation();
				// if the build output directory is an ancestor of the resource
				if ((dirLocation != null) && dirLocation.isPrefixOf(location)) {
					return configDesc; // build configuration found
				}
			}
		}
		return null;
	}

	public static IPath getBuildCWD(ICConfigurationDescription configDesc) {
		IPath builderCWD = configDesc.getBuildSetting().getBuilderCWD();
		if (builderCWD != null) {
			ICdtVariableManager manager = CCorePlugin.getDefault().getCdtVariableManager();
			try {
				String cwd = builderCWD.toString();
				cwd = manager.resolveValue(cwd, "", null, configDesc); //$NON-NLS-1$
				if (!cwd.isEmpty()) {
					return new Path(cwd);
				}
			} catch (CdtVariableException e) {
				CCorePlugin.log(e);
			}
		}
		return null;
	}

}
