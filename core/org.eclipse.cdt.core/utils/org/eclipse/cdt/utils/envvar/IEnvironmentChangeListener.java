/*******************************************************************************
 * Copyright (c) 2013, 2013 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.envvar;


/**
 * Interface for listeners to changes in environment variables defined by user
 * on CDT Environment page in Preferences.
 * 
 * @since 5.5
 */
public interface IEnvironmentChangeListener {
	/**
	 * Indicates that environment variables have been changed.
	 *
	 * @param event - details of the event.
	 */
	public void handleEvent(IEnvironmentChangeEvent event);
}
