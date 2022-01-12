/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import java.util.ArrayList;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescriptionManager;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 */
public class CContainerInfo extends OpenableInfo {

	Object[] nonCResources = null;

	/**
	 * Constructs a new C Model Info
	 */
	protected CContainerInfo(CElement element) {
		super(element);
	}

	public Object[] getNonCResources(IResource res) {
		if (nonCResources != null)
			return nonCResources;

		ArrayList<IResource> notChildren = new ArrayList<>();
		ICElement celement = getElement();
		ICProject cproject = celement.getCProject();
		// move back to the sourceroot.
		while (!(celement instanceof ISourceRoot) && celement != null) {
			celement = celement.getParent();
		}
		ISourceRoot root = null;
		if (celement instanceof ISourceRoot) {
			root = (ISourceRoot) celement;
		}

		try {
			IResource[] resources = null;
			if (res instanceof IContainer) {
				IContainer container = (IContainer) res;
				resources = container.members(false);
			}

			ICSourceEntry[] entries = null;
			ICProjectDescription des = CProjectDescriptionManager.getInstance()
					.getProjectDescription(cproject.getProject(), false);
			if (des != null) {
				ICConfigurationDescription cfg = des.getDefaultSettingConfiguration();
				if (cfg != null) {
					entries = cfg.getResolvedSourceEntries();
				}
			}

			if (resources != null) {
				for (IResource member : resources) {
					switch (member.getType()) {
					case IResource.FOLDER: {
						// Check if the folder is not itself a sourceEntry.
						IPath resourcePath = member.getFullPath();
						if (cproject.isOnSourceRoot(member) || isSourceEntry(resourcePath, entries)
								|| (root == null && cproject.isOnOutputEntry(member))) {
							continue;
						}
						break;
					}
					case IResource.FILE: {
						String filename = member.getName();
						if (root != null && CoreModel.isValidTranslationUnitName(cproject.getProject(), filename)
								&& root.isOnSourceEntry(member)) {
							continue;
						}
						if (cproject.isOnOutputEntry(member)
								&& CModelManager.getDefault().createBinaryFile((IFile) member) != null) {
							continue;
						}
						break;
					}
					}
					notChildren.add(member);
				}
			}
		} catch (CoreException e) {
			//System.out.println (e);
			//CPlugin.log (e);
			//e.printStackTrace();
		}
		setNonCResources(notChildren.toArray());
		return nonCResources;
	}

	public void setNonCResources(Object[] resources) {
		nonCResources = resources;
	}

	private static boolean isSourceEntry(IPath resourcePath, ICSourceEntry[] entries) {
		if (entries == null)
			return false;

		for (ICSourceEntry entry : entries) {
			//			if (entry.getEntryKind() == IPathEntry.CDT_SOURCE) {
			IPath sourcePath = entry.getFullPath();
			if (resourcePath.equals(sourcePath)) {
				return true;
			}
			//			}
		}
		return false;
	}
}
