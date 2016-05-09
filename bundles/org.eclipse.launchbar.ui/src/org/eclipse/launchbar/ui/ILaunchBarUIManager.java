/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.launchbar.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.launchbar.core.ILaunchDescriptor;

public interface ILaunchBarUIManager {

	ILabelProvider getLabelProvider(ILaunchDescriptor descriptor) throws CoreException;

	IStatus openConfigurationEditor(ILaunchDescriptor descriptor);

}
