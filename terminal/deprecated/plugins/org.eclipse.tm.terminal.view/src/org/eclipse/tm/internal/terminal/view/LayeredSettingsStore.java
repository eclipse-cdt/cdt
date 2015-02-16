/*******************************************************************************
 * Copyright (c) 2008, 2011 Wind River Systems, Inc. and others.
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
import org.eclipse.tm.internal.terminal.provisional.api.Settings;

/**
 * Uses an array of {@link ISettings} to find a value.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same. Please do not use this API without consulting with
 * the <a href="http://www.eclipse.org/tm/">Target Management</a> team.
 * </p>
 */
public class LayeredSettingsStore extends Settings {

	private final ISettings[] fStores;

	/**
	 * @param stores the stores used to search the values.
	 * {@link #setProperty(String, Object)} will put the value in the
	 * first store in the list.
	 */
	public LayeredSettingsStore(ISettings[] stores) {
		fStores=stores;
	}
	/**
	 * Convince constructor for two stores
	 * @param s1 first store
	 * @param s2 second store
	 */
	public LayeredSettingsStore(ISettings s1, ISettings s2) {
		this(new ISettings[]{s1,s2});
	}
	
	public Object getProperty(String key) {
		for (int i = 0; i < fStores.length; i++) {
			Object value=fStores[i].getProperty(key);
			if (value!=null)
				return value;
		}
		return null;
	}

	public boolean setProperty(String key, Object value) {
		return fStores[0].setProperty(key,value);
	}

}
