/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model.util;

import org.eclipse.core.runtime.IPath;

public interface IPathSettingsContainerListener {
	void containerAdded(PathSettingsContainer container);

	void aboutToRemove(PathSettingsContainer container);

	void containerValueChanged(PathSettingsContainer container, Object oldValue);

	void containerPathChanged(PathSettingsContainer container, IPath oldPath, boolean childrenMoved);
}
