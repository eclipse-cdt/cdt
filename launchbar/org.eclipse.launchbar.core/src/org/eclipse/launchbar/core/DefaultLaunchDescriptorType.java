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
package org.eclipse.launchbar.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.launchbar.core.internal.Activator;

/**
 * A special descriptor type that managed configurations that aren't owned by
 * other descriptor types.
 *
 * @since 2.3
 */
public class DefaultLaunchDescriptorType implements ILaunchDescriptorType {

	public static final String ID = Activator.PLUGIN_ID + ".descriptorType.default"; //$NON-NLS-1$

	private Map<ILaunchConfiguration, DefaultLaunchDescriptor> descriptors = new HashMap<>();

	@Override
	public boolean supportsTargets() throws CoreException {
		// Old style launch configs do not support targets.
		// Though if yours does, you can always subclass and override this.
		return false;
	}

	/**
	 * Used to filter out private and external tools builders
	 *
	 * @param config
	 * @return
	 * @throws CoreException
	 */
	public static boolean isPublic(ILaunchConfiguration config) throws CoreException {
		ILaunchConfigurationType type = config.getType();
		if (type == null) {
			return false;
		}

		String category = type.getCategory();

		return type.isPublic() && !(config.getAttribute(ILaunchManager.ATTR_PRIVATE, false))
				&& !("org.eclipse.ui.externaltools.builder".equals(category)); // $NON-NLS-1$
	}

	@Override
	public ILaunchDescriptor getDescriptor(Object launchObject) {
		if (launchObject instanceof ILaunchConfiguration) {
			ILaunchConfiguration config = (ILaunchConfiguration) launchObject;
			try {
				if (isPublic(config)) {
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
