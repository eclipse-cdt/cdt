/*******************************************************************************
 * Copyright (c) 2009, 2011 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.language.settings.providers;

// TODO: move ILanguageSettingsEditableProvider here 
import org.eclipse.cdt.core.settings.model.ILanguageSettingsEditableProvider;

public class LanguageSettingsSerializableEditable extends LanguageSettingsSerializable implements ILanguageSettingsEditableProvider {
	@Override
	public LanguageSettingsSerializableEditable clone() throws CloneNotSupportedException {
		return (LanguageSettingsSerializableEditable) super.clone();
	}

	@Override
	public LanguageSettingsSerializableEditable cloneShallow() throws CloneNotSupportedException {
		return (LanguageSettingsSerializableEditable) super.cloneShallow();
	}

}
