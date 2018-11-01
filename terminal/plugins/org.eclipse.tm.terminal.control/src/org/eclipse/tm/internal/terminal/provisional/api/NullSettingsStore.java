/*******************************************************************************
 * Copyright (c) 2015, 2018 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.provisional.api;

/**
 * A settings store implementation doing nothing.
 */
public class NullSettingsStore implements ISettingsStore {

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore#get(java.lang.String)
	 */
	@Override
	public String get(String key) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore#get(java.lang.String, java.lang.String)
	 */
	@Override
	public String get(String key, String defaultValue) {
		return defaultValue;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore#put(java.lang.String, java.lang.String)
	 */
	@Override
	public void put(String key, String value) {
	}
}
