/*******************************************************************************
 * Copyright (c) 2011, 2016 Tomasz Wesolowski and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Tomasz Wesolowski - initial API and implementation
 *    Jens Elmenthaler - further tweaking
 *******************************************************************************/
package org.eclipse.cdt.core.parser.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A matcher for camel case matching supporting both the camel case as well as
 *  he underscore notation.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @since 5.3
 */
public class SegmentMatcher {

	private final char[] prefixForBinarySearch;

	/** The string the any prefix match has to start with. */
	private final char[] prefixForMatching;

	/** The regular expression for a segment match. */
	private final Pattern regexp;

	/** The minimum length any name must have in order to match. */
	private final int minNameLength;

	private final boolean singleSegment;

	/**
	 * @param pattern
	 *            The camel case or underscore pattern.
	 */
	public SegmentMatcher(char[] pattern) {

		if (pattern == null || pattern.length == 0) {
			prefixForMatching = CharArrayUtils.EMPTY;
			prefixForBinarySearch = CharArrayUtils.EMPTY;
			regexp = null;
			minNameLength = 0;
			singleSegment = true;
		} else {

			StringBuilder regexpBuffer = new StringBuilder("^"); //$NON-NLS-1$
			int i = 0;
			int lengthOfFirstSegment = 0;
			char currentChar;
			int segmentCount = 0;

			// Translate each segment
			while (i < pattern.length) {

				boolean separatorSpecified = false;

				// Handle prefix, i.e. anything before the first letter or digit
				for (; i < pattern.length; ++i) {
					currentChar = pattern[i];
					if (Character.isLetterOrDigit(currentChar)) {
						break;
					} else {
						// Quote those characters.
						regexpBuffer.append(Pattern.quote(String.valueOf(currentChar)));
						separatorSpecified = true;
					}
				}

				if (i < pattern.length) {
					// The character here is always a letter or digit.
					currentChar = pattern[i];

					if (Character.isDigit(currentChar)) {

						// Handle number segment
						regexpBuffer.append(currentChar);
						for (++i; i < pattern.length; ++i) {
							currentChar = pattern[i];
							if (Character.isDigit(currentChar)) {
								regexpBuffer.append(currentChar);
							} else {
								break;
							}
						}

					} else {

						// Handle text segment
						char lower = Character.toLowerCase(currentChar);
						char upper = Character.toUpperCase(currentChar);

						if ((segmentCount == 0) || separatorSpecified) {
							regexpBuffer.append(currentChar);
						} else {
							regexpBuffer.append("(_["); //$NON-NLS-1$
							regexpBuffer.append(lower);
							regexpBuffer.append(upper);
							regexpBuffer.append("]|"); //$NON-NLS-1$
							regexpBuffer.append(upper);
							regexpBuffer.append(')');
						}

						// Remaining letters of the segment
						for (++i; i < pattern.length; ++i) {

							currentChar = pattern[i];
							if (Character.isLetter(currentChar)) {
								if (Character.isUpperCase(currentChar)) {
									break;
								} else {
									lower = currentChar;
									upper = Character.toUpperCase(currentChar);
									regexpBuffer.append('[');
									regexpBuffer.append(lower);
									regexpBuffer.append(upper);
									regexpBuffer.append(']');
								}
							} else {
								break;
							}
						}
					}
				}
				regexpBuffer.append(".*"); //$NON-NLS-1$

				if (segmentCount == 0) {
					lengthOfFirstSegment = i;
				}

				++segmentCount;
			}

			regexp = Pattern.compile(regexpBuffer.toString());
			singleSegment = (segmentCount == 1);
			prefixForMatching = pattern;

			// The first segment is also the binary search prefix
			prefixForBinarySearch = CharArrayUtils.extract(pattern, 0, lengthOfFirstSegment);

			minNameLength = pattern.length;
		}
	}

	/**
	 * Matches the given name by prefix and segment matching.
	 *
	 * @return true if the associated pattern is a prefix-based or segment-based abbreviation of name.
	 */
	public boolean match(char[] name) {
		if (matchPrefix(name)) {
			return true;
		}

		// If there is only a single segment given and prefix match failed,
		// the segment match cannot pass either. So skip it.
		if (singleSegment) {
			return false;
		}

		return matchSegments(name);
	}

	/**
	 * Matches the given name by prefix matching.
	 *
	 * @return true if the associated pattern is a prefix-based abbreviation of name.
	 */
	public boolean matchPrefix(char[] name) {
		return (CharArrayUtils.equals(name, 0, prefixForMatching.length, prefixForMatching, true));
	}

	/**
	 * Matches the given name by segment matching.
	 *
	 * @return true if the associated pattern is a segment-based abbreviation of name.
	 */
	public boolean matchSegments(char[] name) {

		if (name == null) {
			return false;
		}

		if (name.length < minNameLength) {
			return false;
		}

		if (regexp == null) {
			return true;
		}

		Matcher matcher = regexp.matcher(String.valueOf(name));

		return matcher.find();
	}

	/**
	 * Matches pattern to name by prefix and segment matching. If you have to match
	 * against the same pattern repeatedly, create a {@link SegmentMatcher} instead
	 * and re-use it all the time, because this is much faster.
	 *
	 * @return true if pattern is a prefix-based or segment-based abbreviation of name
	 */
	public static boolean match(char[] pattern, char[] name) {
		return (new SegmentMatcher(pattern)).match(name);
	}

	/**
	 * The pattern used by this matcher is not suitable for binary searches
	 * (e.g. within the index).
	 * However, there can be calculated a string that can be used in the
	 * context of binary searches.
	 * In the compare method used by your binary search, return 0 for any string
	 * that starts with the returned string.
	 *
	 * @return Such a string.
	 */
	public char[] getPrefixForBinarySearch() {
		return prefixForBinarySearch;
	}

	/**
	 * @return If false, calling @{@link #match(char[])} can be skipped if a
	 *         name survived a binary search using the prefix returned by
	 *         @{@link #getPrefixForBinarySearch()} as key.
	 */
	public boolean matchRequiredAfterBinarySearch() {
		return !singleSegment;
	}
}
