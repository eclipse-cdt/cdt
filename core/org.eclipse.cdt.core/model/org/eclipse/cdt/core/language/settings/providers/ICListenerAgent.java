/*******************************************************************************
 * Copyright (c) 2011, 2011 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.language.settings.providers;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;


/**
 * Helper class to allow listeners self-register/dispose. Called by cdt core.
 * TODO - expand in more detail.
 */
public interface ICListenerAgent {
	public void registerListener(ICConfigurationDescription cfgDescription);
	public void unregisterListener();
}
