/*******************************************************************************
 * Copyright (c) 2011 Tomasz Wesolowski and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Elmenthaler - initial implementation
 *                        http://bugs.eclipse.org/173458 (camel case completion)
 *******************************************************************************/

package org.eclipse.cdt.core.parser.tests;

import junit.framework.TestCase;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.core.parser.util.ContentAssistMatcherFactory;
import org.eclipse.cdt.core.parser.util.IContentAssistMatcher;
import org.eclipse.core.runtime.preferences.InstanceScope;

public class ContentAssistMatcherFactoryTest extends TestCase {

	@Override
	protected void tearDown() throws Exception {
		InstanceScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID).remove(
				CCorePreferenceConstants.SHOW_CAMEL_CASE_MATCHES);
		super.tearDown();
	}

	public void testCamelCasePreference() {
		// Default is show camel case matches on
		assertTrue(match("foo", "fooBar"));
		assertTrue(match("fB", "fooBar"));

		setShowCamelCaseMatches(false);

		assertTrue(match("foo", "fooBar"));
		assertFalse(match("fB", "fooBar"));

		setShowCamelCaseMatches(true);

		assertTrue(match("foo", "fooBar"));
		assertTrue(match("fB", "fooBar"));
	}

	public void testCamelCaseMatcher() {
		setShowCamelCaseMatches(true);
		IContentAssistMatcher matcher = ContentAssistMatcherFactory
				.getInstance().createMatcher("fB");

		assertEquals("f", String.valueOf(matcher.getPrefixForBinarySearch()));
		assertTrue(matcher.matchRequiredAfterBinarySearch());
	}

	public void testPrefixMatcher() {
		setShowCamelCaseMatches(true);
		IContentAssistMatcher matcher = ContentAssistMatcherFactory
				.getInstance().createMatcher("foo");

		assertEquals("foo", String.valueOf(matcher.getPrefixForBinarySearch()));
		assertFalse(matcher.matchRequiredAfterBinarySearch());
	}

	private void setShowCamelCaseMatches(boolean enabled) {
		InstanceScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID).putBoolean(
				CCorePreferenceConstants.SHOW_CAMEL_CASE_MATCHES, enabled);
	}

	private boolean match(String pattern, String name) {
		return ContentAssistMatcherFactory.getInstance().match(
				pattern.toCharArray(), name.toCharArray());
	}
}
