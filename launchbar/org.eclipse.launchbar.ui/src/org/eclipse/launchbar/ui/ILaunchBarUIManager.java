/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
