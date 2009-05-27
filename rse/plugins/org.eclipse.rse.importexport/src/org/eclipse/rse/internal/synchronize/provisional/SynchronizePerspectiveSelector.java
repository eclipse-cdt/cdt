/*******************************************************************************
 * Copyright (c) 2008, 2009 Takuya Miyamoto and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Takuya Miyamoto - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.internal.synchronize.provisional;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.internal.synchronize.RSESyncUtils;
import org.eclipse.rse.internal.synchronize.filesystem.subscriber.FileSystemMergeContext;
import org.eclipse.rse.internal.synchronize.filesystem.subscriber.FileSystemSubscriber;
import org.eclipse.rse.internal.synchronize.filesystem.ui.FileSystemOperation;
import org.eclipse.rse.internal.synchronize.filesystem.ui.FileSystemSynchronizeParticipant;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.subscribers.SubscriberScopeManager;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;
import org.eclipse.ui.IContributorResourceAdapter;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.ide.IContributorResourceAdapter2;

public class SynchronizePerspectiveSelector implements ISynchronizePerspectiveSelector {
	private IWorkbenchPart targetPart;

	/**
	 * Open Synchronize Perspective
	 */
	// <Copied copied from
	// org.eclipse.team.examples.filesystem.ui.SynchronizeAction>
	public void openSynchronizePerspective(List<IResource> synchronizeElement) {
		// Get resource mapping which is prepared the previous step.
		ResourceMapping[] mappings = getSelectedResourceMappings(RSESyncUtils.PROVIDER_ID, synchronizeElement);
		if (mappings.length == 0) {
			return;
		}
		SubscriberScopeManager manager = FileSystemOperation.createScopeManager(FileSystemSubscriber.getInstance().getName(), mappings);
		FileSystemMergeContext context = new FileSystemMergeContext(manager);
		FileSystemSynchronizeParticipant participant = new FileSystemSynchronizeParticipant(context);
		TeamUI.getSynchronizeManager().addSynchronizeParticipants(new ISynchronizeParticipant[] { participant });
		participant.run(getTargetPart());
	}

	// </Copied copied from
	// org.eclipse.team.examples.filesystem.ui.SynchronizeAction>

	// <Copied copied from org.eclipse.team.internal.ui.actions.TeamAction>
	/**
	 * Return the selected resource mappins that contain resources in projects
	 * that are associated with a repository of the given id.
	 *
	 * @param providerId
	 * 		the repository provider id
	 * @return the resource mappings that contain resources associated with the
	 * 	given provider
	 */
	protected ResourceMapping[] getSelectedResourceMappings(String providerId, List synchronizeResources) {
		Object[] elements = synchronizeResources.toArray();
		ArrayList providerMappings = new ArrayList();
		for (int i = 0; i < elements.length; i++) {
			Object object = elements[i];
			Object adapted = getResourceMapping(object);
			if (adapted instanceof ResourceMapping) {
				ResourceMapping mapping = (ResourceMapping) adapted;
				if (providerId == null || isMappedToProvider(mapping, providerId)) {
					providerMappings.add(mapping);
				}
			}
		}
		return (ResourceMapping[]) providerMappings.toArray(new ResourceMapping[providerMappings.size()]);
	}

	// </Copied copied from org.eclipse.team.internal.ui.actions.TeamAction>

	/**
	 * return if the element is mapped to providerId in the RepositoryProvider
	 *
	 * @param element
	 * @param providerId
	 * @return
	 */
	// <Copied copied from org.eclipse.team.internal.ui.actions.TeamAction>
	private boolean isMappedToProvider(ResourceMapping element, String providerId) {
		IProject[] projects = element.getProjects();
		for (int k = 0; k < projects.length; k++) {
			IProject project = projects[k];
			RepositoryProvider provider = RepositoryProvider.getProvider(project);
			if (provider != null && provider.getID().equals(providerId)) {
				return true;
			}
		}
		return false;
	}

	// </Copied copied from org.eclipse.team.internal.ui.actions.TeamAction>

	/**
	 *
	 * @param object
	 * @return
	 */
	// <Copied copied from org.eclipse.team.internal.ui.actions.TeamAction>
	private Object getResourceMapping(Object object) {
		// if object is already ResourceMapping
		if (object instanceof ResourceMapping) {
			return object;
		}

		// <Copied copied from org.eclipse.team.internal.ui.Utils>
		if (object instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) object;
			Object adapted = adaptable.getAdapter(ResourceMapping.class);
			if (adapted instanceof ResourceMapping) {
				return adapted;
			}
			adapted = adaptable.getAdapter(IContributorResourceAdapter.class);
			if (adapted instanceof IContributorResourceAdapter2) {
				IContributorResourceAdapter2 cra = (IContributorResourceAdapter2) adapted;
				return cra.getAdaptedResourceMapping(adaptable);
			}
		} else {
			Object adapted = Platform.getAdapterManager().getAdapter(object, ResourceMapping.class);
			if (adapted instanceof ResourceMapping) {
				return adapted;
			}
		}
		return null;
		// </Copied copied from org.eclipse.team.internal.ui.Utils>
	}

	// </Copied copied from org.eclipse.team.internal.ui.actions.TeamAction>

	/**
	 *
	 * @return
	 */
	// <Copied copied from org.eclipse.team.internal.ui.actions.TeamAction>
	protected IWorkbenchPart getTargetPart() {
		if (targetPart == null) {
			IWorkbenchPage page = RSESyncUtils.getActivePage();
			if (page != null) {
				targetPart = page.getActivePart();
			}
		}
		return targetPart;

	}
	// </Copied copied from org.eclipse.team.internal.ui.actions.TeamAction>
}
