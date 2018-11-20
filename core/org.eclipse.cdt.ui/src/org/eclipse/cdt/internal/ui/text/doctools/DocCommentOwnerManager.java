/*******************************************************************************
 * Copyright (c) 2008, 2011 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *     IBM Corporation
 *     Johan Ekberg -  Bug 285932
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.doctools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.doctools.IDocCommentOwner;
import org.eclipse.cdt.ui.text.doctools.IDocCommentOwnershipListener;
import org.eclipse.cdt.ui.text.doctools.IDocCommentViewerConfiguration;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.Preferences;

import com.ibm.icu.text.MessageFormat;

/**
 * This class manages which IDocCommentOwner's are available in the run-time, and how they map to
 * resources in projects.
 * @since 5.0
 */
public class DocCommentOwnerManager {
	/** Constants for attributes/elements from the DocCommentOwner extension point */
	private static final String ELEMENT_OWNER = "owner"; //$NON-NLS-1$
	private static final String ATTRKEY_OWNER_ID = "id"; //$NON-NLS-1$
	private static final String ATTRKEY_OWNER_NAME = "name"; //$NON-NLS-1$
	private static final String ATTRKEY_OWNER_SINGLELINE = "singleline"; //$NON-NLS-1$
	private static final String ATTRKEY_OWNER_MULTILINE = "multiline"; //$NON-NLS-1$

	private static final String QUALIFIER = CCorePlugin.PLUGIN_ID;
	private static final String WORKSPACE_DOC_TOOL_NODE = "doctool"; //$NON-NLS-1$
	private static final String PREFKEY_WORKSPACE_DEFAULT = "workspace.default"; //$NON-NLS-1$

	private static DocCommentOwnerManager singleton;

	public static DocCommentOwnerManager getInstance() {
		return singleton == null ? singleton = new DocCommentOwnerManager() : singleton;
	}

	private Map<String, IDocCommentOwner> fOwners;
	private IDocCommentOwner fWorkspaceOwner;
	private Map<IProject, ProjectMap> prj2map = new HashMap<>();
	private static List<IDocCommentOwnershipListener> fListeners;

	private DocCommentOwnerManager() {
		fOwners = getCommentOwnerExtensions();
		fListeners = new ArrayList<>();

		Preferences defaultPrefs = DefaultScope.INSTANCE.getNode(QUALIFIER).node(WORKSPACE_DOC_TOOL_NODE);
		Preferences prefs = InstanceScope.INSTANCE.getNode(QUALIFIER).node(WORKSPACE_DOC_TOOL_NODE);
		String id = prefs.get(PREFKEY_WORKSPACE_DEFAULT,
				defaultPrefs.get(PREFKEY_WORKSPACE_DEFAULT, NullDocCommentOwner.INSTANCE.getID()));

		fWorkspaceOwner = getOwner(id);
		if (fWorkspaceOwner == null) {
			// this could occur if a plug-in is no longer available
			fWorkspaceOwner = NullDocCommentOwner.INSTANCE;
		}
	}

	/**
	 * @param project a non-null project
	 * @return whether the specified project defines any documentation owner association
	 */
	public boolean projectDefinesOwnership(IProject project) {
		return !getProjectMap(project).isEmpty();
	}

	/**
	 * @param newOwner the non-null doc-comment owner
	 */
	public void setWorkspaceCommentOwner(IDocCommentOwner newOwner) {
		if (newOwner == null)
			throw new IllegalArgumentException();

		if (!fWorkspaceOwner.getID().equals(newOwner.getID())) {
			IDocCommentOwner oldOwner = fWorkspaceOwner;
			fWorkspaceOwner = newOwner;

			Preferences prefs = InstanceScope.INSTANCE.getNode(QUALIFIER).node(WORKSPACE_DOC_TOOL_NODE);
			prefs.put(PREFKEY_WORKSPACE_DEFAULT, newOwner.getID());

			fireWorkspaceOwnershipChanged(oldOwner, fWorkspaceOwner);
		}
	}

	/**
	 * @return the doc comment owner associated with the workspace. If non
	 * is set, the {@link NullDocCommentOwner} is returned.
	 */
	public IDocCommentOwner getWorkspaceCommentOwner() {
		return fWorkspaceOwner;
	}

	/**
	 *
	 * @param resource May be null.
	 * @return a non-null IDocCommentOwner. If the resource was null, the {@link NullDocCommentOwner} is returned.
	 */
	public IDocCommentOwner getCommentOwner(IResource resource) {
		if (resource == null)
			return NullDocCommentOwner.INSTANCE;

		if (ResourcesPlugin.getWorkspace().getRoot().equals(resource))
			return getWorkspaceCommentOwner();

		ProjectMap pm = getProjectMap(resource);
		String ownerID = pm.getOwnerID(resource);
		IDocCommentOwner result = getOwner(ownerID);
		return result == null ? fWorkspaceOwner : result;
	}

	/**
	 * @param id
	 * @return the {@link IDocCommentOwner} with the specified id, or null
	 */
	public IDocCommentOwner getOwner(String id) {
		if (NullDocCommentOwner.INSTANCE.getID().equals(id)) {
			return NullDocCommentOwner.INSTANCE;
		}
		return fOwners.get(id);
	}

	/**
	 * @param resource a non-null resource to map a comment owner to
	 * @param newOwner the new owner to assign, or null to inherit the parent's mapping
	 * @param removeSubMappings if the resource is an {@link IContainer}, then remove any mappings
	 * children have. <em>This is currently unimplemented.</em>
	 */
	/*
	 * Note - this implementation currently ignores removeSubMappings.
	 */
	public void setCommentOwner(IResource resource, IDocCommentOwner newOwner, boolean removeSubMappings) {
		Assert.isNotNull(resource);

		if (ResourcesPlugin.getWorkspace().getRoot().equals(resource)) {
			setWorkspaceCommentOwner(newOwner);
			return;
		}

		ProjectMap pm = getProjectMap(resource);
		IDocCommentOwner oldOwner = getCommentOwner(resource);
		pm.setCommentOwner(resource, newOwner);

		IDocCommentOwner newLogicalOwner = getCommentOwner(resource);
		if (!newLogicalOwner.getID().equals(oldOwner.getID())) {
			fireOwnershipChanged(resource, removeSubMappings, oldOwner, newLogicalOwner);
		}
	}

	/**
	 * @return any comment owners registered against the extension point. This does not include
	 * the null comment processor.
	 */
	public IDocCommentOwner[] getRegisteredOwners() {
		return fOwners.values().toArray(new IDocCommentOwner[fOwners.values().size()]);
	}

	/**
	 * @param listener registers a listener for doc-comment ownership events
	 */
	public void addListener(IDocCommentOwnershipListener listener) {
		if (!fListeners.contains(listener)) {
			fListeners.add(listener);
		}
	}

	/**
	 * @param listener removes a listener from those registered for doc-comment ownership events
	 */
	public void removeListener(IDocCommentOwnershipListener listener) {
		fListeners.remove(listener);
	}

	/*
	 * Utilities
	 */

	/**
	 * @param resource a non-null resource
	 * @return the cached (or newly obtained) ProjectMap for the resources
	 * associated project.
	 */
	private ProjectMap getProjectMap(IResource resource) {
		Assert.isNotNull(resource);
		IProject project = resource.getProject();

		if (!prj2map.containsKey(project)) {
			prj2map.put(project, new ProjectMap(project));
		}

		return prj2map.get(project);
	}

	/**
	 * @return a map of ID to {@link IDocCommentOwner} for comment owners registered
	 * via the DocCommentOwner extension point
	 */
	private static Map<String, IDocCommentOwner> getCommentOwnerExtensions() {
		Map<String, IDocCommentOwner> result = new HashMap<>();

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint indexProviders = registry.getExtensionPoint(CUIPlugin.ID_COMMENT_OWNER);
		IExtension[] extensions = indexProviders.getExtensions();
		for (IExtension extension : extensions) {
			try {
				IConfigurationElement[] ce = extension.getConfigurationElements();
				for (IConfigurationElement element : ce) {
					if (element.getName().equals(ELEMENT_OWNER)) {
						IDocCommentViewerConfiguration multi = (IDocCommentViewerConfiguration) element
								.createExecutableExtension(ATTRKEY_OWNER_MULTILINE);
						IDocCommentViewerConfiguration single = (IDocCommentViewerConfiguration) element
								.createExecutableExtension(ATTRKEY_OWNER_SINGLELINE);
						String id = element.getAttribute(ATTRKEY_OWNER_ID);
						String name = element.getAttribute(ATTRKEY_OWNER_NAME);
						if (result.put(id, new DocCommentOwner(id, name, multi, single)) != null) {
							String msg = MessageFormat.format(Messages.DocCommentOwnerManager_DuplicateMapping0,
									new Object[] { id });
							CUIPlugin.log(new Status(IStatus.WARNING, CUIPlugin.PLUGIN_ID, msg));
						}
					}
				}
			} catch (CoreException ce) {
				CUIPlugin.log(ce);
			}
		}

		return result;
	}

	private void fireOwnershipChanged(IResource resource, boolean submappingsRemoved, IDocCommentOwner oldOwner,
			IDocCommentOwner newOwner) {
		for (IDocCommentOwnershipListener docCommentOwnershipListener : fListeners) {
			docCommentOwnershipListener.ownershipChanged(resource, submappingsRemoved, oldOwner, newOwner);
		}
	}

	private void fireWorkspaceOwnershipChanged(IDocCommentOwner oldOwner, IDocCommentOwner newOwner) {
		for (IDocCommentOwnershipListener element : fListeners) {
			element.workspaceOwnershipChanged(oldOwner, newOwner);
		}
	}
}
