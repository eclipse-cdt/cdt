/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.launchbar.core;

import org.eclipse.core.resources.IProject;

/**
 * Project Based launch descriptor knows about project it is associated with
 */
public interface ILaunchDescriptorProjectBased extends ILaunchDescriptor {
	/**
	 * Get associate project
	 */
	public abstract IProject getProject();
}