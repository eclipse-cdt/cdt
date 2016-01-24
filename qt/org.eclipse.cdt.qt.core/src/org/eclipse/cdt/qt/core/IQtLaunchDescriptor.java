/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.qt.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.launchbar.core.ILaunchDescriptor;

public interface IQtLaunchDescriptor extends ILaunchDescriptor {

	IProject getProject();

}
