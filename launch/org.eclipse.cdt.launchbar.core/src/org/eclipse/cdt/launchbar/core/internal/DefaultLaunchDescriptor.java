/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.cdt.launchbar.core.internal;

import org.eclipse.cdt.launchbar.core.ConfigBasedLaunchDescriptor;
import org.eclipse.cdt.launchbar.core.ILaunchDescriptorType;
import org.eclipse.debug.core.ILaunchConfiguration;


public class DefaultLaunchDescriptor extends ConfigBasedLaunchDescriptor {

	public DefaultLaunchDescriptor(ILaunchDescriptorType type, ILaunchConfiguration config) {
		super(type, config);
	}
}
