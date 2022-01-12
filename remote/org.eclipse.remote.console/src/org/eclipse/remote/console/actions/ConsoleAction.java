/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.console.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.remote.internal.console.Activator;

/**
 * An abstract class to implement basic functionality common to terminal console actions
 * @since 1.1
 */
public abstract class ConsoleAction extends Action {

	/**
	 * @param id
	 * 			The action id
	 */
	public ConsoleAction(String id) {
		this(id, 0);
	}

	/**
	 * @param id
	 * 			The action id
	 * @param style
	 * 			one of AS_PUSH_BUTTON, AS_CHECK_BOX, AS_DROP_DOWN_MENU, AS_RADIO_BUTTON,
	 *  and AS_UNSPECIFIED
	 */
	public ConsoleAction(String id, int style) {
		super("", style); //$NON-NLS-1$
		setId(id);
	}

	/**
	 * @param text
	 * 			the text for this action
	 * @param tooltip
	 * 			the tooltip for this action
	 * @param image
	 * 			the image key for this action
	 * @param enabledImage
	 * 			the enabled image key for this action
	 * @param disabledImage
	 * 			the disabled image key for this action
	 * @param enabled
	 * 			the enabled state for this action
	 */
	protected void setupAction(String text, String tooltip, String image, String enabledImage, String disabledImage,
			boolean enabled) {
		ImageRegistry imageRegistry = Activator.getDefault().getImageRegistry();
		setupAction(text, tooltip, image, enabledImage, disabledImage, enabled, imageRegistry);
	}

	/**
	 * @param text
	 * 			the text for this action
	 * @param tooltip
	 * 			the tooltip for this action
	 * @param hoverImage
	 * 			the hover image key for this action
	 * @param enabledImage
	 * 			the enabled image key for this action
	 * @param disabledImage
	 * 			the disabled image key for this action
	 * @param enabled
	 * 			the enabled state for this action
	 * @param imageRegistry
	 * 			the ImageRegistry to retrieve ImageDescriptor for the keys provided
	 */
	protected void setupAction(String text, String tooltip, String hoverImage, String enabledImage,
			String disabledImage, boolean enabled, ImageRegistry imageRegistry) {
		setupAction(text, tooltip, imageRegistry.getDescriptor(hoverImage), imageRegistry.getDescriptor(enabledImage),
				imageRegistry.getDescriptor(disabledImage), enabled);
	}

	/**
	 * @param text
	 * 			the text for this action
	 * @param tooltip
	 * 			the tooltip for this action
	 * @param hoverImage
	 * 			the hover image for this action
	 * @param enabledImage
	 * 			the enabled image for this action
	 * @param disabledImage
	 * 			the disabled image for this action
	 * @param enabled
	 * 			the enabled state for this action
	 */
	protected void setupAction(String text, String tooltip, ImageDescriptor hoverImage, ImageDescriptor enabledImage,
			ImageDescriptor disabledImage, boolean enabled) {
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
