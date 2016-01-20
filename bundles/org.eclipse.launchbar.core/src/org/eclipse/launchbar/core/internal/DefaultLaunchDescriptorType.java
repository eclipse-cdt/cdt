/*******************************************************************************
 * Copyright (c) 2014, 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.launchbar.core.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.ILaunchDescriptorType;

/**
 * A special descriptor type that managed configurations that aren't owned by
 * other descriptor types.
 */
public class DefaultLaunchDescriptorType implements ILaunchDescriptorType {

	public static final String ID = Activator.PLUGIN_ID + ".descriptorType.default"; //$NON-NLS-1$

	private Map<ILaunchConfiguration, DefaultLaunchDescriptor> descriptors = new HashMap<>();

	@Override
	public ILaunchDescriptor getDescriptor(Object launchObject) {
		if (launchObject instanceof ILaunchConfiguration) {
			ILaunchConfiguration config = (ILaunchConfiguration) launchObject;
			try {
				ILaunchConfigurationType type = config.getType();
				if (type == null) {
					return null;
				}

				// Filter out private and external tools builders
				String category = type.getCategory();
				if (type.isPublic() && !(config.getAttribute(ILaunchManager.ATTR_PRIVATE, false))
						&& !("org.eclipse.ui.externaltools.builder".equals(category))) { //$NON-NLS-1$

					DefaultLaunchDescriptor descriptor = descriptors.get(config);
					if (descriptor == null) {
						descriptor = new DefaultLaunchDescriptor(this, config);
						descriptors.put(config, descriptor);
					}
					return descriptor;
				}
			} catch (CoreException ce) {
				Activator.log(ce.getStatus());
			}
		}

		return null;
	}

}
