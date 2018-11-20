/*******************************************************************************
 * Copyright (c) 2010, 2011 Meisam Fathi and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Meisam Fathi  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.fs;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class parses the format string argument and extracts all %s tokens.
 *
 * @version 0.2, June 04, 2010
 * @author Meisam Fathi
 */
public class CFormatStringParser {
	/**
	 * At least one digit should be present
	 */
	private static final String DIGIT_PATTERN = "[0-9][0-9]*";//$NON-NLS-1$
	/**
	 * The general format for a <strong>format string</strong> argument is
	 * "%[*][size][modifier]type", in which type is one of the following items:
	 * <ul>
	 * <li>c: Single character.
	 * <li>d: Decimal integer.
	 * <li>e,E,f,g,G: Floating point.
	 * <li>o Octal: integer.
	 * <li>s: String of characters.
	 * <li>u: Unsigned decimal integer.
	 * <li>x,X: Hexadecimal integer.
	 * </ul>
	 *
	 * @see {@link http://www.cplusplus.com/reference/clibrary/cstdio/scanf/}
	 *      for more information.
	 */
	private static final String STRING_FORMAT_PATTERN = "%[\\*]?[0-9]*[hlL]?[cdeEfgGsuxX]";//$NON-NLS-1$
	/**
	 * If there is an asterisk in the format argument, then it cannot be
	 * vulnerable. If there is a [modifier] (i.e. hlL), then compiler warns.
	 * Hence, the only vulnerable arguments are arguments in which either there
	 * is no specified size, or there is a size greater than the size of the
	 * string.
	 *
	 * @see #FORMAT_STRING_PATTERN
	 */
	private static final String VULNERABLE_PATTERN = "%[0-9]*s";//$NON-NLS-1$
	/**
	 * The pattern which represents a string format.
	 */
	private final Pattern argumentPattern;
	/**
	 * The matcher which matches string format arguments.
	 */
	private final Matcher argumentMatcher;
	/**
	 * The pattern which may lead to vulnerability in <code>scanf</code>
	 * function calls.
	 */
	private final Pattern vulnerablePattern;
	/**
	 * I guess, this must be a concurrent Collection, but I'm not sure. --
	 * Meisam
	 */
	private final Collection<VulnerableFormatStringArgument> vulnerableArguments;
	public final static int ARGUMENT_SIZE_NOT_SPECIFIED = -1;

	/**
	 * Constructs an argument parser for the given argument.
	 *
	 * @param argument
	 */
	protected CFormatStringParser(final String argument) {
		this.argumentPattern = Pattern.compile(STRING_FORMAT_PATTERN);
		this.argumentMatcher = this.argumentPattern.matcher(argument);
		this.vulnerablePattern = Pattern.compile(VULNERABLE_PATTERN);
		this.vulnerableArguments = new ConcurrentLinkedQueue<>();
		extractVulnerableArguments();
	}

	/**
	 * If the given argument to this class is vulnerable, it returns true, else
	 * it return false.
	 *
	 * @return true if the format string argument is vulnerable.
	 */
	public boolean isVulnerable() {
		return !this.vulnerableArguments.isEmpty();
	}

	public Iterator<VulnerableFormatStringArgument> getVulnerableArgumentsIterator() {
		return this.vulnerableArguments.iterator();
	}

	/**
	 * This method is guaranteed to be invoked in the constructor of the class.
	 * DON'T invoke it yourself. It should be invoke only once.
	 */
	private void extractVulnerableArguments() {
		/*
		 * I'm not sure if clearing the collection is necessary. -- Meisam Fathi
		 */
		this.vulnerableArguments.clear();
		boolean hasMore = this.argumentMatcher.find();
		int indexOfCurrentArgument = 0;
		while (hasMore) {
			final String formatString = this.argumentMatcher.group();
			final String matchedArgument = formatString;
			final Matcher vulnerabilityMatcher = this.vulnerablePattern.matcher(matchedArgument);
			final boolean isVulnerable = vulnerabilityMatcher.find();
			if (isVulnerable) {
				final int argumentSize = parseArgumentSize(formatString);
				final VulnerableFormatStringArgument vulnerableArgument = new VulnerableFormatStringArgument(
						indexOfCurrentArgument, formatString, argumentSize);
				this.vulnerableArguments.add(vulnerableArgument);
			}
			hasMore = this.argumentMatcher.find();
			indexOfCurrentArgument++;
		}
	}

	/**
	 * This method takes a string as input. The format of the input string is
	 * %[0-9]*s. If there is no digit present in the given string it returns
	 * <code>ARGUMENT_SIZE_NOT_SPECIFIED</code>, otherwise it returns the number
	 * specified after "%". For example:
	 * <ul>
	 * <li>%s ==> -1</li>
	 * <li>%123s ==> 123</li>
	 * <li>%1s ==> 1</li>
	 * <li>%015s ==> 15</li>
	 * <li>%0s ==> 0</li>
	 * </ul>
	 *
	 * @param formatString
	 *        The given format string.
	 * @return Either ARGUMENT_SIZE_NOT_SPECIFIED or the number embedded in the
	 *         input string.
	 */
	private int parseArgumentSize(final String formatString) {
		// The minimum possible size for a string of format %[0-9]*s
		final int MINIMUM_POSSIBLE_SIZE = 2;
		int argumentSize = ARGUMENT_SIZE_NOT_SPECIFIED;
		if (formatString.length() > MINIMUM_POSSIBLE_SIZE) {
			final Pattern numberPattern = Pattern.compile(DIGIT_PATTERN);
			final Matcher numberMatcher = numberPattern.matcher(formatString);
			if (numberMatcher.find()) {
				final String sizeModifierString = numberMatcher.group();
				argumentSize = Integer.parseInt(sizeModifierString);
			}
		}
		return argumentSize;
	}
}
