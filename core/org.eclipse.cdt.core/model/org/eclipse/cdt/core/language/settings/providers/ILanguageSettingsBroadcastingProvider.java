/*******************************************************************************
 * Copyright (c) 2011, 2012 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.language.settings.providers;

/**
 * This interface is to be implemented by providers which want to broadcast the changes in their
 * setting entries with {@link ILanguageSettingsChangeEvent}.
 *
 * @since 5.4
 */
public interface ILanguageSettingsBroadcastingProvider extends ILanguageSettingsProvider {

	/**
	 * Return a copy of internal storage. This should be a deep copy/clone of the storage. It is
	 * used to calculate the delta and being kept in the last state object of configuration
	 * description to compare to a new state later.
	 *
	 * @return a copy of internal storage.
	 */
	public LanguageSettingsStorage copyStorage();

}
