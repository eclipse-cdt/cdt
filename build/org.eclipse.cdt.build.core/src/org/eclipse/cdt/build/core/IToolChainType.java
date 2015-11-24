/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.build.core;

import org.osgi.service.prefs.Preferences;

/**
 * A type of toolchain.
 */
public interface IToolChainType {

	String getId();

	/**
	 * Called by the toolchain to inflate the toolchain from the user preference
	 * store.
	 * 
	 * @param name
	 *            the name of the toolchain
	 * @param properties
	 *            the persisted settings for the toolchain
	 * @return the toolchain initialized with the settings.
	 */
	IToolChain getToolChain(String name, Preferences properties);

}
