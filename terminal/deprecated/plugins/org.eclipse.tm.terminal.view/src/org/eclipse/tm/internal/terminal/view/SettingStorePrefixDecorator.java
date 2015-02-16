/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation 
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.view;

import org.eclipse.tm.internal.terminal.provisional.api.ISettings;

public class SettingStorePrefixDecorator extends org.eclipse.tm.internal.terminal.provisional.api.Settings {
	private final String fPrefix;
	private final ISettings fStore;
	SettingStorePrefixDecorator(ISettings store,String prefix) {
		fPrefix=prefix;
		fStore=store;
	}

	public Object get(String key) {
		return fStore.get(fPrefix+key);
	}

	public boolean set(String key, Object value) {
		return super.set(fPrefix+key,value);
	}
}
