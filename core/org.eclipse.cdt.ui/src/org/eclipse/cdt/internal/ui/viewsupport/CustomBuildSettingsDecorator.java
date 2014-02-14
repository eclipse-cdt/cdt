/*******************************************************************************
 * Copyright (c) 2013, 2014 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.viewsupport;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.util.ResourceConfigurationUtil;

/**
 * Determines if a file or folder got customized build settings and if so decorates with the "wrench" overlay.
 */
public class CustomBuildSettingsDecorator implements ILightweightLabelDecorator {
	@Override
	public void decorate(Object element, IDecoration decoration) {
		if (element instanceof IFile || element instanceof IFolder) {
			IResource rc = (IResource) element;
			ICProjectDescriptionManager projectDescriptionManager = CoreModel.getDefault().getProjectDescriptionManager();
			ICProjectDescription prjDescription = projectDescriptionManager.getProjectDescription(rc.getProject(), ICProjectDescriptionManager.GET_IF_LOADDED);
			if (prjDescription != null) {
				ICConfigurationDescription cfgDescription = prjDescription.getDefaultSettingConfiguration();
				if (cfgDescription != null) {
					if (ResourceConfigurationUtil.isCustomizedResource(cfgDescription, rc))
						decoration.addOverlay(CPluginImages.DESC_OVR_SETTING);
				}
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
