/*******************************************************************************
 * Copyright (c) 2013 Serge Beauchamp and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Serge Beauchamp (Freescale Semiconductor.) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.ui.tests.misc;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.cdt.core.language.WorkspaceLanguageConfiguration;
import org.eclipse.cdt.core.model.ILanguage;

import org.eclipse.cdt.internal.ui.language.LanguageVerifier;

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
		
		Map<String, ILanguage> availableLanguages = new HashMap<String, ILanguage>();
		
		availableLanguages.put("foo", null);
		availableLanguages.put("foo3", null);
		
		LanguageVerifier.removeMissingLanguages(config, availableLanguages);
	}
}
