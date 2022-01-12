/*******************************************************************************
 * Copyright (c) 2010, 2013 Atmel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Torkild U. Resheim (Atmel Corporation) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.viewsupport;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

/**
 * Determines if a file is excluded from a CDT build and if that is the case decorates the file's icon and
 * renders the label using the qualifier (gray) color.
 */
public class ExcludedFileDecorator implements ILightweightLabelDecorator {
	@Override
	public void decorate(Object element, IDecoration decoration) {
		if (element instanceof IFile || element instanceof IFolder) {
			IResource resource = (IResource) element;
			ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
			ICProjectDescription desc = mngr.getProjectDescription(resource.getProject(),
					ICProjectDescriptionManager.GET_IF_LOADDED);
			if (desc == null)
				return;
			ICConfigurationDescription conf = desc.getDefaultSettingConfiguration();
			ICSourceEntry[] entries = conf != null ? conf.getSourceEntries() : new ICSourceEntry[0];
			boolean isUnderSourceRoot = false;
			IPath fullPath = resource.getFullPath();
			for (ICSourceEntry icSourceEntry : entries) {
				if (icSourceEntry.getFullPath().isPrefixOf(fullPath)) {
					isUnderSourceRoot = true;
					break;
				}
			}
			// Only bother to mark items that would be included otherwise
			if (isUnderSourceRoot && CDataUtil.isExcluded(fullPath, entries)) {
				decoration.addOverlay(CPluginImages.DESC_OVR_INACTIVE);
				decoration.setForegroundColor(JFaceResources.getColorRegistry().get(JFacePreferences.QUALIFIER_COLOR));
			}
		}
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
		// We don't track state changes
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// We don't track state changes
	}
}
