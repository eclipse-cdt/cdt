/*******************************************************************************
 * Copyright (c) 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation9
 *******************************************************************************/

package org.eclipse.cdt.launch.internal.refactoring;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.internal.LaunchConfigAffinityExtensionPoint;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;

/**
 * A rename participant for resource refactorings, that updates affected CDT
 * launch configurations.
 * 
 * @author Christian W. Damus (cdamus)
 * 
 * @since 6.0
 */
public class ResourceRenameParticipant extends RenameParticipant implements
		IExecutableExtension {

	private String name;

	private IResource resourceBeingRenamed;

	/**
	 * Initializes me.
	 */
	public ResourceRenameParticipant() {
		super();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	protected boolean initialize(Object element) {
		if (element instanceof IResource) {
			resourceBeingRenamed = (IResource) element;
		} else if (element instanceof IAdaptable) {
			resourceBeingRenamed = (IResource) ((IAdaptable) element)
					.getAdapter(IResource.class);
		}

		return true;
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {

		Change result = null;

		if (resourceBeingRenamed instanceof IProject) {
			String oldName = resourceBeingRenamed.getName();
			String newName = getArguments().getNewName();
			Collection<ILaunchConfigurationType> launchTypes = getCLaunchConfigTypes();

			if (!launchTypes.isEmpty()) {
				ILaunchManager mgr = DebugPlugin.getDefault()
						.getLaunchManager();

				for (ILaunchConfigurationType type : launchTypes) {
					ILaunchConfiguration[] launches = mgr
							.getLaunchConfigurations(type);

					for (ILaunchConfiguration next : launches) {
						if (next
								.getAttribute(
										ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME,
										"").equals(oldName)) { //$NON-NLS-1$

							result = AbstractLaunchConfigChange.append(result,
									new ProjectRenameChange(next, oldName,
											newName));
						}
					}
				}
			}
		}

		return result;
	}

	static Collection<ILaunchConfigurationType> getCLaunchConfigTypes() {
		Set<ILaunchConfigurationType> result = new java.util.HashSet<ILaunchConfigurationType>();

		// Get launch config types registered by CDT adopters
		Set<String> thirdPartyConfgTypeIds = new HashSet<String>(5);
		LaunchConfigAffinityExtensionPoint.getLaunchConfigTypeIds(thirdPartyConfgTypeIds);
		
		ILaunchManager mgr = DebugPlugin.getDefault().getLaunchManager();
		for (ILaunchConfigurationType next : mgr.getLaunchConfigurationTypes()) {
			// is it a CDT launch type or a third party one that is CDT-ish?
			if (next.getPluginIdentifier().startsWith("org.eclipse.cdt.") || //$NON-NLS-1$ 
					thirdPartyConfgTypeIds.contains(next.getIdentifier())) { 
				result.add(next);
			}
		}

		return result;
	}

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm,
			CheckConditionsContext context) throws OperationCanceledException {

		// I have no conditions to check. Updating the launches is trivial
		return new RefactoringStatus();
	}

	@Override
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {

		this.name = config.getAttribute("name"); //$NON-NLS-1$
	}

}
