/*******************************************************************************
 * Copyright (c) 2011 Jens Elmenthaler and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jens Elmenthaler - http://bugs.eclipse.org/173458 (camel case completion)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.util;

/**
 * A matcher for content assist-like application to determine whether names
 * match the user provided text.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 5.3
 */
public interface IContentAssistMatcher {

	/**
	 * The user provided text normally represents some kind of pattern. This pattern
	 * may not be suitable for binary searches (e.g. within the index).
	 * For each content assist pattern, however, there is a string that can be
	 * calculated and used for binary searches.
	 * In the compare method used by your binary search, return 0 for any string
	 * that starts with the returned string.
	 *
	 * @return Such a string.
	 */
	char[] getPrefixForBinarySearch();

	/**
	 * @return If false, calling @{@link #match(char[])} can be skipped if a
	 *         name survived a binary search using the prefix returned by
	 *         @{@link #getPrefixForBinarySearch()} as key.
	 */
	boolean matchRequiredAfterBinarySearch();

	/**
	 * Matches the given name following the rules of content assist.
	 *
	 * @param name
	 *
	 * @return True if the name matches.
	 */
	boolean match(char[] name);
}
