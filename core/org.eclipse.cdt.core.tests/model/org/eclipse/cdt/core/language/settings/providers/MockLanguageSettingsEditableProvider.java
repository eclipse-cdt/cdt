/*******************************************************************************
 * Copyright (c) 2009, 2012 Andrew Gvozdev and others.
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
 * Mock of {@link ILanguageSettingsEditableProvider} for testing.
 */
public class MockLanguageSettingsEditableProvider extends LanguageSettingsSerializableProvider implements ILanguageSettingsEditableProvider {
	public MockLanguageSettingsEditableProvider() {
		super();
	}

	public MockLanguageSettingsEditableProvider(String id, String name) {
		super(id, name);
	}

	@Override
	public MockLanguageSettingsEditableProvider cloneShallow() throws CloneNotSupportedException {
		return (MockLanguageSettingsEditableProvider) super.cloneShallow();
	}

	@Override
	public MockLanguageSettingsEditableProvider clone() throws CloneNotSupportedException {
		return (MockLanguageSettingsEditableProvider) super.clone();
	}
}
