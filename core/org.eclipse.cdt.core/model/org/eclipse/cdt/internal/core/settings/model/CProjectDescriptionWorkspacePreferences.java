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
package org.eclipse.cdt.internal.core.settings.model;

import org.eclipse.cdt.core.settings.model.ICProjectDescriptionWorkspacePreferences;
import org.eclipse.cdt.core.settings.model.ICStorageElement;

public class CProjectDescriptionWorkspacePreferences extends CProjectDescriptionPreferences
		implements ICProjectDescriptionWorkspacePreferences {

	public CProjectDescriptionWorkspacePreferences(CProjectDescriptionPreferences base, boolean isReadOnly) {
		super(base, isReadOnly);
	}

	public CProjectDescriptionWorkspacePreferences(CProjectDescriptionPreferences base,
			CProjectDescriptionPreferences superPreference, boolean isReadOnly) {
		super(base, superPreference, isReadOnly);
	}

	public CProjectDescriptionWorkspacePreferences(ICStorageElement el, CProjectDescriptionPreferences superPreference,
			boolean isReadOnly) {
		super(el, superPreference, isReadOnly);
	}
}
