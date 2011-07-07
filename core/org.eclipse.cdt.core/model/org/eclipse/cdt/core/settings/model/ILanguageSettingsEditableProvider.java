/*******************************************************************************
 * Copyright (c) 2009, 2009 Andrew Gvozdev (Quoin Inc.) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev (Quoin Inc.) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.settings.model;

import java.util.List;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsSerializable;
import org.eclipse.core.resources.IResource;

/**
 * This interface is used in UI to identify classes allowing user to modify settings externally
 * contrary to some subclasses of {@link LanguageSettingsSerializable} managing
 * their settings themselves and not providing such option to the user.
 *
 */
public interface ILanguageSettingsEditableProvider extends ILanguageSettingsProvider, Cloneable {

	public void setSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId, List<ICLanguageSettingEntry> entries);
	public boolean isEmpty();
	public void clear();
	
	public ILanguageSettingsEditableProvider cloneShallow() throws CloneNotSupportedException;
	public ILanguageSettingsEditableProvider clone() throws CloneNotSupportedException;
}
