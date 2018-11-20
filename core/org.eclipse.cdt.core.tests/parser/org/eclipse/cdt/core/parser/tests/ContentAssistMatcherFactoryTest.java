/*******************************************************************************
 * Copyright (c) 2011 Tomasz Wesolowski and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jens Elmenthaler - initial implementation
 *                        http://bugs.eclipse.org/173458 (camel case completion)
 *******************************************************************************/

package org.eclipse.cdt.core.parser.tests;

import org.eclipse.cdt.core.parser.util.IContentAssistMatcher;
import org.eclipse.cdt.internal.core.parser.util.ContentAssistMatcherFactory;

import junit.framework.TestCase;

public class ContentAssistMatcherFactoryTest extends TestCase {

	@Override
	protected void tearDown() throws Exception {
		ContentAssistMatcherFactory.getInstance().setShowCamelCaseMatches(true);
		super.tearDown();
	}

	public void testConfiguration() {
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
		IContentAssistMatcher matcher = ContentAssistMatcherFactory.getInstance().createMatcher("fB");

		assertEquals("f", String.valueOf(matcher.getPrefixForBinarySearch()));
		assertTrue(matcher.matchRequiredAfterBinarySearch());
	}

	public void testPrefixMatcher() {
		setShowCamelCaseMatches(true);
		IContentAssistMatcher matcher = ContentAssistMatcherFactory.getInstance().createMatcher("foo");

		assertEquals("foo", String.valueOf(matcher.getPrefixForBinarySearch()));
		assertFalse(matcher.matchRequiredAfterBinarySearch());
	}

	private void setShowCamelCaseMatches(boolean enabled) {
		ContentAssistMatcherFactory.getInstance().setShowCamelCaseMatches(enabled);
	}

	private boolean match(String pattern, String name) {
		return ContentAssistMatcherFactory.getInstance().match(pattern.toCharArray(), name.toCharArray());
	}
}
