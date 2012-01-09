/*******************************************************************************
 * Copyright (c) 2011 Jens Elmenthaler and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Elmenthaler - http://bugs.eclipse.org/173458 (camel case completion)
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.util;

import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.IContentAssistMatcher;
import org.eclipse.cdt.core.parser.util.SegmentMatcher;

/**
 * The facade to the pattern matching algorithms of content assist.
 *  
 * @author Jens Elmenthaler
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @since 5.3
 */
public class ContentAssistMatcherFactory {

	private static ContentAssistMatcherFactory instance = null;
	
	private boolean showCamelCaseMatches = true;
		
	private static class CamelCaseMatcher implements IContentAssistMatcher {

		private final SegmentMatcher matcher;
		
		public CamelCaseMatcher(char[] pattern) {
			matcher = new SegmentMatcher(pattern);
		}
		
		@Override
		public char[] getPrefixForBinarySearch() {
			return matcher.getPrefixForBinarySearch();
		}
		
		@Override
		public boolean matchRequiredAfterBinarySearch() {
			return matcher.matchRequiredAfterBinarySearch();
		}

		@Override
		public boolean match(char[] name) {
			return matcher.match(name);
		}
	}
	
	private static class PrefixMatcher implements IContentAssistMatcher {

		private final char[] prefix;
		
		public PrefixMatcher(char[] prefix) {
			this.prefix = prefix;
		}
		
		@Override
		public char[] getPrefixForBinarySearch() {
			return prefix;
		}

		@Override
		public boolean matchRequiredAfterBinarySearch() {
			return false;
		}

		@Override
		public boolean match(char[] name) {
			return CharArrayUtils.equals(name, 0, prefix.length, prefix, true);
		}
		
	}
	
	private ContentAssistMatcherFactory() {
		
	}

	public static synchronized ContentAssistMatcherFactory getInstance() {
		if (instance == null) {
			instance = new ContentAssistMatcherFactory();
		}
		
		return instance;
	}
	
	/**
     * This function is not supposed to be called from any functions except
     * for ContentAssistMatcherPreference.updateOnPreferences.
     *  
     * @param showCamelCaseMatches
     */
	public synchronized  void setShowCamelCaseMatches(boolean showCamelCaseMatches) {
		this.showCamelCaseMatches = showCamelCaseMatches;
	}
	
	/**
	 * 
	 * @return <code>true</code> if showCamelCaseMatches is set from the content assist preference page.
	 */
	public boolean getShowCamelCaseMatches() {
		return showCamelCaseMatches;
	}
	
	/**
	 * @param pattern The pattern for which to create a matcher.
	 * @return A suitable matcher.
	 */
	public synchronized IContentAssistMatcher createMatcher(char[] pattern) {
		
		return showCamelCaseMatches ? new CamelCaseMatcher(pattern) : new PrefixMatcher(pattern);
	}

	/**
	 * @param pattern The pattern for which to create a matcher.
	 * @return A suitable matcher.
	 */
	public IContentAssistMatcher createMatcher(String pattern) {
		return createMatcher(pattern.toCharArray());
	}

	/**
	 * A helper method to match a name against the pattern typed by the user.
	 * If you need to match many names at once against the same pattern, use
	 * {@link #createMatcher(char[])} and re-use the returned matcher instead.  
	 * 
	 * @param pattern The user provided pattern.
	 * @param name The name to match against the pattern.
	 * 
	 * @return <code>true</code> if the name matches the given pattern.
	 */
	public boolean match(char[] pattern, char[] name) {
		return createMatcher(pattern).match(name);
	}
}
