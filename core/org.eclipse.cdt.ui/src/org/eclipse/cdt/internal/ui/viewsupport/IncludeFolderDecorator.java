/*******************************************************************************
 * Copyright (c) 2010, 2012 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.viewsupport;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

import org.eclipse.cdt.core.model.IIncludeReference;
import org.eclipse.cdt.ui.CDTSharedImages;

import org.eclipse.cdt.internal.ui.cview.IncludeReferenceProxy;

/**
 * Determines whether an include folder under "Includes" node does exist
 * and if not decorates the file's icon with warning overlay and
 * renders the label using the qualifier (gray) color.
 */
public class IncludeFolderDecorator implements ILightweightLabelDecorator {
	@Override
	public void decorate(Object element, IDecoration decoration) {
		boolean isAccesible = true;

		if (element instanceof IncludeReferenceProxy) {
			IIncludeReference reference = ((IncludeReferenceProxy)element).getReference();
			IPath path = reference.getPath();
			IContainer container = ResourcesPlugin.getWorkspace().getRoot().getContainerForLocation(path);
			if (container != null) {
				isAccesible = container.isAccessible();
			} else {
				isAccesible = path.toFile().exists();
			}
		} else if (element instanceof IIncludeReference) {
			IPath path = ((IIncludeReference) element).getPath();
			isAccesible = path.toFile().exists();
		}

		if (!isAccesible) {
			decoration.addOverlay(CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OVR_WARNING));
			// JFacePreferences.QUALIFIER_COLOR colors label in gray
			decoration.setForegroundColor(JFaceResources.getColorRegistry().get(JFacePreferences.QUALIFIER_COLOR));
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