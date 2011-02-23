/*******************************************************************************
 * Copyright (c) 2011 Jens Elmenthaler and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Elmenthaler - http://bugs.eclipse.org/173458 (camel case completion)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.util;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;


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
	
	private boolean showCamelCaseMatches;
	
	private final IPreferenceChangeListener preferencesListener = new IPreferenceChangeListener() {
		
		public void preferenceChange(PreferenceChangeEvent event) {
			String prop = event.getKey();
			if (prop.equals(CCorePreferenceConstants.SHOW_CAMEL_CASE_MATCHES)) {
				updateOnPreferences();
			}
		}
	};
	
	private static class CamelCaseMatcher implements IContentAssistMatcher {

		private final SegmentMatcher matcher;
		
		public CamelCaseMatcher(char[] pattern) {
			matcher = new SegmentMatcher(pattern);
		}
		
		public char[] getPrefixForBinarySearch() {
			return matcher.getPrefixForBinarySearch();
		}
		
		public boolean matchRequiredAfterBinarySearch() {
			return matcher.matchRequiredAfterBinarySearch();
		}

		public boolean match(char[] name) {
			return matcher.match(name);
		}
	}
	
	private static class PrefixMatcher implements IContentAssistMatcher {

		private final char[] prefix;
		
		public PrefixMatcher(char[] prefix) {
			this.prefix = prefix;
		}
		
		public char[] getPrefixForBinarySearch() {
			return prefix;
		}

		public boolean matchRequiredAfterBinarySearch() {
			return false;
		}

		public boolean match(char[] name) {
			return CharArrayUtils.equals(name, 0, prefix.length, prefix, true);
		}
		
	}
	
	private ContentAssistMatcherFactory() {
		getPreferences().addPreferenceChangeListener(
				preferencesListener);
		updateOnPreferences();
	}

	public static synchronized ContentAssistMatcherFactory getInstance() {
		if (instance == null) {
			instance = new ContentAssistMatcherFactory();
		}
		
		return instance;
	}
	
	private void shutdownInternal() {
		getPreferences().removePreferenceChangeListener(
				preferencesListener);
	}
	
	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static synchronized void shutdown() {
		if (instance != null) {
			instance.shutdownInternal();
		}
	}
	
	private static IEclipsePreferences getPreferences() {
		return InstanceScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID);
	}
	
	private synchronized void updateOnPreferences() {
		IPreferencesService prefs = Platform.getPreferencesService();
		showCamelCaseMatches = prefs.getBoolean(CCorePlugin.PLUGIN_ID,
				CCorePreferenceConstants.SHOW_CAMEL_CASE_MATCHES, true, null);
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
