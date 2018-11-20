/*******************************************************************************
 * Copyright (c) 2013 Serge Beauchamp and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Serge Beauchamp (Freescale Semiconductor.) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.ui.tests.misc;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.language.WorkspaceLanguageConfiguration;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.internal.ui.language.LanguageVerifier;

import junit.framework.TestCase;

/**
 * Tests for CDT Language Verifier.
 */
public class LanguageVerifierTests extends TestCase {

	/**
	 * This API call was throwing a java.utilConcurrentModificationException.
	 * see Bug 417852
	 */
	public void testConcurrentExceptionInLanguageVerifier() throws Exception {

		WorkspaceLanguageConfiguration config = new WorkspaceLanguageConfiguration();

		config.addWorkspaceMapping("foo", "bar");
		config.addWorkspaceMapping("foo2", "bar2");

		Map<String, ILanguage> availableLanguages = new HashMap<>();

		availableLanguages.put("foo", null);
		availableLanguages.put("foo3", null);

		LanguageVerifier.removeMissingLanguages(config, availableLanguages);
	}
}
