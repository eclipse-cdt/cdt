/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.core.build;

import org.osgi.service.prefs.Preferences;

/**
 * @since 5.12
 */
public interface IToolChainFactory {

	CToolChain createToolChain(String id, Preferences settings);

	default void discover() {
	}

}
