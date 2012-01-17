/*******************************************************************************
 * Copyright (c) 2008, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.executables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICOutputEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescriptionManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.DebugPlugin;

/**
 * @since 7.0
 */
public class StandardExecutableProvider implements IProjectExecutablesProvider {

	List<String> supportedNatureIds = new ArrayList<String>();
	
	public StandardExecutableProvider() {
		supportedNatureIds.add(CProjectNature.C_NATURE_ID);
		supportedNatureIds.add(CCProjectNature.CC_NATURE_ID);
	}
	
	@Override
	public List<String> getProjectNatures() {
		return supportedNatureIds;
	}

	@Override
	public List<Executable> getExecutables(IProject project, IProgressMonitor monitor) {
		List<Executable> executables = new ArrayList<Executable>();
		
		ICProject cproject = CModelManager.getDefault().create(project);
		try {
			// Start out by getting all binaries in all build configurations. If
			// we can't filter based on the active configuration, we'll use this
			// complete list
			IBinary[] allBinaries = cproject.getBinaryContainer().getBinaries();
			if (allBinaries.length == 0) {
				return executables; // save ourselves a lot of pointless busy work
			}
			
			// Get the output directories of the active build configuration then
			// go through the list of all binaries and pick only the ones that
			// are in these output directories
			List<IBinary> binaries = null;
			ICProjectDescription projDesc = CProjectDescriptionManager.getInstance().getProjectDescription(project, false);
			if (projDesc != null) {
				ICConfigurationDescription cfg = projDesc.getActiveConfiguration();
				if (cfg != null) {
					binaries = new ArrayList<IBinary>(allBinaries.length);
					ICOutputEntry[] cfgOutDirs = cfg.getBuildSetting().getOutputDirectories();
					for (IBinary allBinary : allBinaries) { 
						for (ICOutputEntry outdir : cfgOutDirs) {
							if (outdir.getFullPath().isPrefixOf(allBinary.getPath())) {
								binaries.add(allBinary);
								break;
							}
						}
					}
				}
			}

			// If we weren't able to filter on the active configuration,
			// consider binaries from all configurations
			if (binaries == null) {
				binaries = Arrays.asList(allBinaries);
			}

			SubMonitor progress = SubMonitor.convert(monitor, binaries.size());

			for (IBinary binary : binaries) {
				if (progress.isCanceled()) {
					break;
				}

				if (binary.isExecutable() || binary.isSharedLib()) {
					IPath exePath = binary.getResource().getLocation();
					if (exePath == null)
						exePath = binary.getPath();
					List<ISourceFileRemapping> srcRemappers = new ArrayList<ISourceFileRemapping>(2);
					ISourceFileRemappingFactory[] factories = ExecutablesManager.getExecutablesManager().getSourceFileRemappingFactories();
					for (ISourceFileRemappingFactory factory : factories) {
						ISourceFileRemapping remapper = factory.createRemapper(binary);
						if (remapper != null) {
							srcRemappers.add(remapper);
						}
					}
					executables.add(new Executable(exePath, project, binary.getResource(), srcRemappers.toArray(new ISourceFileRemapping[srcRemappers.size()])));
				}
				
				progress.worked(1);
			}
		} catch (CoreException e) {
			CDebugCorePlugin.log(e);
		}
		
		return executables;
	}

	@Override
	public IStatus removeExecutable(Executable executable, IProgressMonitor monitor) {
		IResource exeResource = executable.getResource();
		if (exeResource != null) {
			try {
				exeResource.delete(true, monitor);
			} catch (CoreException e) {
				DebugPlugin.log( e );
			}
			return Status.OK_STATUS;
		}
		return new Status(IStatus.WARNING, CDebugCorePlugin.PLUGIN_ID, "Can't remove " + executable.getName() + ": it is built by project \"" + executable.getProject().getName() + "\"");  //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
	}
}
