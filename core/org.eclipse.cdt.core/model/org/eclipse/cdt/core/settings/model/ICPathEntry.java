/*******************************************************************************
 * Copyright (c) 2007, 2014 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import org.eclipse.core.runtime.IPath;

/**
 * Helper interface capturing 'path' characteristic for {@link ICSettingEntry}
 * and {@link ICLanguageSettingEntry} interfaces.
 */
public interface ICPathEntry extends ICSettingEntry {
	IPath getFullPath();

	IPath getLocation();

	boolean isValueWorkspacePath();
}
