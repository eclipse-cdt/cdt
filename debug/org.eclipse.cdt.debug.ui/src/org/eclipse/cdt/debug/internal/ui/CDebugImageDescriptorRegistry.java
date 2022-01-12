/*******************************************************************************
 * Copyright (c) 2004, 2012 QNX Software Systems and others.
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
package org.eclipse.cdt.debug.internal.ui;

import java.util.HashMap;

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * A registry that maps <code>ImageDescriptors</code> to <code>Image</code>.
 */
public class CDebugImageDescriptorRegistry {
	private HashMap<ImageDescriptor, Image> fRegistry = new HashMap<>(10);
	private Display fDisplay;

	/**
	 * Creates a new image descriptor registry for the current or default display, respectively.
	 */
	public CDebugImageDescriptorRegistry() {
		this(CDebugUIPlugin.getStandardDisplay());
	}

	/**
	 * Creates a new image descriptor registry for the given display. All images managed by this registry will be disposed when the display gets disposed.
	 *
	 * @param diaplay
	 *            the display the images managed by this registry are allocated for
	 */
	public CDebugImageDescriptorRegistry(Display display) {
		fDisplay = display;
		Assert.isNotNull(fDisplay);
		hookDisplay();
	}

	/**
	 * Returns the image associated with the given image descriptor.
	 *
	 * @param descriptor
	 *            the image descriptor for which the registry manages an image
	 * @return the image associated with the image descriptor or <code>null</code> if the image
	 * 		descriptor can't create the requested image.
	 */
	public Image get(ImageDescriptor descriptor) {
		if (descriptor == null)
			descriptor = ImageDescriptor.getMissingImageDescriptor();
		Image result = fRegistry.get(descriptor);
		if (result != null)
			return result;
		Assert.isTrue(fDisplay == CDebugUIPlugin.getStandardDisplay(),
				CDebugUIMessages.getString("CDebugImageDescriptorRegistry.0")); //$NON-NLS-1$
		result = descriptor.createImage();
		if (result != null)
			fRegistry.put(descriptor, result);
		return result;
	}

	/**
	 * Disposes all images managed by this registry.
	 */
	public void dispose() {
		for (Image image : fRegistry.values()) {
			image.dispose();
		}
		fRegistry.clear();
	}

	private void hookDisplay() {
		fDisplay.asyncExec(() -> getDisplay().disposeExec(() -> dispose()));
	}

	protected Display getDisplay() {
		return fDisplay;
	}
}
