/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems, Inc. - initial implementation
 *     
 *******************************************************************************/
package org.eclipse.tm.terminal.internal.view;

import org.eclipse.tm.terminal.ISettingsStore;

public class SettingStorePrefixDecorator implements ISettingsStore {
	private final String fPrefix;
	private final ISettingsStore fStore;
	SettingStorePrefixDecorator(ISettingsStore store,String prefix) {
		fPrefix=prefix;
		fStore=store;
	}
	public String get(String key) {
		return fStore.get(fPrefix+key);
	}

	public String get(String key, String defaultValue) {
		return fStore.get(fPrefix+key,defaultValue);
	}

	public void put(String key, String value) {
		fStore.put(fPrefix+key,value);
	}

}
