/*******************************************************************************
 * Copyright (c) 2010 Atmel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Torkild U. Resheim (Atmel Corporation) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.viewsupport;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;

import org.eclipse.cdt.internal.ui.CPluginImages;

/**
 * Determines if a file is excluded from a CDT build and if that is the case decorates the file's icon and
 * renders the label using the qualifier (gray) color.
 */
public class ExcludedFileDecorator implements ILightweightLabelDecorator {

	public void decorate(Object element, IDecoration decoration) {
		if (element instanceof IFile) {
			IFile resource = (IFile) element;
			ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
			ICProjectDescription desc = mngr.getProjectDescription(resource.getProject(), false);
			if (desc == null)
				return;
			ICConfigurationDescription conf = desc.getDefaultSettingConfiguration();
			ICSourceEntry[] entries = conf.getSourceEntries();
			boolean isSource = false;
			for (ICSourceEntry icSourceEntry : entries) {
				if (icSourceEntry.getFullPath().isPrefixOf(resource.getFullPath())) {
					isSource = true;
					break;
				}
			}
			// Only bother to mark items that would be included otherwise
			if (isSource && CDataUtil.isExcluded(resource.getFullPath(), entries)) {
				decoration.addOverlay(CPluginImages.DESC_OVR_INACTIVE);
				decoration.setForegroundColor(JFaceResources.getColorRegistry().get(
						JFacePreferences.QUALIFIER_COLOR));
			}
		}
	}

	public void addListener(ILabelProviderListener listener) {
		// We don't track state changes
	}

	public void dispose() {
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		// We don't track state changes
	}
}