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
import java.util.List;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.internal.core.model.CModelManager;
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
	
	public List<String> getProjectNatures() {
		return supportedNatureIds;
	}

	public List<Executable> getExecutables(IProject project, IProgressMonitor monitor) {
		List<Executable> executables = new ArrayList<Executable>();
		
		ICProject cproject = CModelManager.getDefault().create(project);
		try {
			IBinary[] binaries = cproject.getBinaryContainer().getBinaries();

			SubMonitor progress = SubMonitor.convert(monitor, binaries.length);

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
		} catch (CModelException e) {
		}
		
		return executables;
	}

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
