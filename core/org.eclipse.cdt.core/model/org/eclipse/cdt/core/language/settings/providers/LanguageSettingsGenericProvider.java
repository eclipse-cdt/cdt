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

/**
 * TODO
 */
final public class LanguageSettingsGenericProvider extends LanguageSettingsSerializableProvider implements ILanguageSettingsEditableProvider {
	@Override
	public LanguageSettingsGenericProvider clone() throws CloneNotSupportedException {
		return (LanguageSettingsGenericProvider) super.clone();
	}

	@Override
	public LanguageSettingsGenericProvider cloneShallow() throws CloneNotSupportedException {
		return (LanguageSettingsGenericProvider) super.cloneShallow();
	}

}
