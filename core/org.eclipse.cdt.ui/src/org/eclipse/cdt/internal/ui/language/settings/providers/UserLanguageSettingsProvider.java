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

package org.eclipse.cdt.internal.ui.language.settings.providers;

import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsSerializable;
import org.eclipse.cdt.core.settings.model.ILanguageSettingsEditableProvider;

/**
 * Provider to support user interface for language settings. The important difference with
 * {@link LanguageSettingsSerializable} is that it implements {@link ILanguageSettingsEditableProvider}.
 *
 */
public class UserLanguageSettingsProvider extends LanguageSettingsSerializable implements ILanguageSettingsEditableProvider {
	
	@Override
	public int hashCode() {
		return super.hashCode()*13 + 1;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof UserLanguageSettingsProvider) {
			return super.equals(o);
		}
		return false;
	}

	@Override
	public UserLanguageSettingsProvider cloneShallow() throws CloneNotSupportedException {
		return (UserLanguageSettingsProvider)super.cloneShallow();
	}

	@Override
	public UserLanguageSettingsProvider clone() throws CloneNotSupportedException {
		return (UserLanguageSettingsProvider)super.clone();
	}
	
}
