/*******************************************************************************
 * Copyright (c) 2014, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.launchbar.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.internal.Activator;
import org.eclipse.swt.graphics.Image;

public class DefaultDescriptorLabelProvider extends LabelProvider {
	
	private Map<ImageDescriptor, Image> images = new HashMap<>();

	@Override
	public void dispose() {
		super.dispose();
		for (Image image : images.values()) {
			image.dispose();
		}
	}
	
	@Override
	public Image getImage(Object element) {
		if (element instanceof ILaunchDescriptor) {
			ILaunchConfiguration config = ((ILaunchDescriptor) element).getAdapter(ILaunchConfiguration.class);
			if (config != null) {
				try {
					ILaunchConfigurationType type = config.getType();
					ImageDescriptor imageDescriptor = DebugUITools.getDefaultImageDescriptor(type);
					if (imageDescriptor != null) {
						Image image = images.get(imageDescriptor);
						if (image == null) {
							image = imageDescriptor.createImage();
							images.put(imageDescriptor, image);
						}
						return image;
					}
				} catch (CoreException e) {
					Activator.log(e.getStatus());
				}
			}
		}
		return super.getImage(element);
	}

	@Override
	public String getText(Object element) {
		if (element instanceof ILaunchDescriptor) {
			return ((ILaunchDescriptor) element).getName();
		}
		return super.getText(element);
	}

}
