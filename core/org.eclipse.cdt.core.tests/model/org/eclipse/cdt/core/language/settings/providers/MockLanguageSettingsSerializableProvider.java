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
 * Mock of {@link LanguageSettingsSerializableProvider} for testing.
 */
public class MockLanguageSettingsSerializableProvider extends LanguageSettingsSerializableProvider {
	public MockLanguageSettingsSerializableProvider() {
		super();
	}

	public MockLanguageSettingsSerializableProvider(String id, String name) {
		super(id, name);
	}
}
