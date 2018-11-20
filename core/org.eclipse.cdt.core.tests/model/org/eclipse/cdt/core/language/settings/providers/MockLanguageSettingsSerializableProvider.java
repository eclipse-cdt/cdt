/*******************************************************************************
 * Copyright (c) 2009, 2012 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
