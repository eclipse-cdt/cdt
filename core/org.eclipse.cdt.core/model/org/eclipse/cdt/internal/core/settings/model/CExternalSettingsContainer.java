/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * James Blackburn (Broadcom Corp.)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import org.eclipse.cdt.core.settings.model.CExternalSetting;

/**
 * Container class which returns the {@link CExternalSetting}s
 * for a given context.
 */
public abstract class CExternalSettingsContainer {

	/** Empty array of external settings */
	public static final CExternalSetting[] EMPTY_EXT_SETTINGS_ARRAY = new CExternalSetting[0];

	/**
	 * @return CExternalSetting[] of settings in this container; never null
	 */
	public abstract CExternalSetting[] getExternalSettings();

}
