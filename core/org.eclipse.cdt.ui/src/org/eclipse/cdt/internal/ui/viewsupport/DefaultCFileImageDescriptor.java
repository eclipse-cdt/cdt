/*******************************************************************************
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   See git history
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.viewsupport;

import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.SharedImagesFactory;
import org.eclipse.cdt.ui.lsp.ICFileImageDescriptor;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;

public class DefaultCFileImageDescriptor implements ICFileImageDescriptor {
	private SharedImagesFactory imagesFactory = new SharedImagesFactory(CUIPlugin.getDefault());

	@Override
	public ImageDescriptor getCImageDescriptor() {
		return imagesFactory.getImageDescriptor(CDTSharedImages.IMG_OBJS_TUNIT);
	}

	@Override
	public ImageDescriptor getCXXImageDescriptor() {
		return imagesFactory.getImageDescriptor(CDTSharedImages.IMG_OBJS_TUNIT);
	}

	@Override
	public ImageDescriptor getHeaderImageDescriptor() {
		return imagesFactory.getImageDescriptor(CDTSharedImages.IMG_OBJS_TUNIT_HEADER);
	}

	@Override
	public boolean isEnabled(IProject project) {
		return true;
	}
}
