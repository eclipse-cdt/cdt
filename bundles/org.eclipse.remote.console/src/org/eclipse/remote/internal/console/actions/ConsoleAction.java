/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.internal.console.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.remote.internal.console.Activator;

public abstract class ConsoleAction extends Action {
	public ConsoleAction(String id) {
		this(id, 0);
	}

	public ConsoleAction(String id, int style) {
		super("", style); //$NON-NLS-1$
		setId(id);
	}

	protected void setupAction(String text, String tooltip,
			String image, String enabledImage, String disabledImage,
			boolean enabled) {
		ImageRegistry imageRegistry = Activator.getDefault().getImageRegistry();
		setupAction(text, tooltip, image, enabledImage, disabledImage, enabled, imageRegistry);
	}

	protected void setupAction(String text, String tooltip,
			String hoverImage, String enabledImage, String disabledImage,
			boolean enabled, ImageRegistry imageRegistry) {
		setupAction(text,
				tooltip,
				imageRegistry.getDescriptor(hoverImage),
				imageRegistry.getDescriptor(enabledImage),
				imageRegistry.getDescriptor(disabledImage),
				enabled);
	}

	protected void setupAction(String text, String tooltip,
			ImageDescriptor hoverImage, ImageDescriptor enabledImage, ImageDescriptor disabledImage,
			boolean enabled) {
		setText(text);
		setToolTipText(tooltip);
		setEnabled(enabled);
		if (enabledImage != null) {
			setImageDescriptor(enabledImage);
		}
		if (disabledImage != null) {
			setDisabledImageDescriptor(disabledImage);
		}
		if (hoverImage != null) {
			setHoverImageDescriptor(hoverImage);
		}
	}
}
