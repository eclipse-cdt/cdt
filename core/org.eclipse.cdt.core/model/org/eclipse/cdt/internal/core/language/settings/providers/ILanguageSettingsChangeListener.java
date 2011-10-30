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
package org.eclipse.cdt.internal.core.language.settings.providers;

/**
 * Listens to changes in language settings.
 */
public interface ILanguageSettingsChangeListener {
	/**
	 * Indicates that language settings have been changed.
	 * @param event
	 */
	public void handleEvent(ILanguageSettingsChangeEvent event);
}