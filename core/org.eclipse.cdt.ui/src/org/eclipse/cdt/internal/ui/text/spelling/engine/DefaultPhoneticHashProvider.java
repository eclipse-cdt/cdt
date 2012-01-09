/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.spelling.engine;

/**
 * Default phonetic hash provider for english languages.
 * <p>
 * This algorithm uses an adapted version double metaphone algorithm by
 * Lawrence Philips.
 * <p>
 */
public final class DefaultPhoneticHashProvider implements IPhoneticHashProvider {
	private static final String[] meta01= { "ACH", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] meta02= { "BACHER", "MACHER", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String[] meta03= { "CAESAR", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] meta04= { "CHIA", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] meta05= { "CH", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] meta06= { "CHAE", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] meta07= { "HARAC", "HARIS", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String[] meta08= { "HOR", "HYM", "HIA", "HEM", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	private static final String[] meta09= { "CHORE", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] meta10= { "VAN ", "VON ", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String[] meta11= { "SCH", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] meta12= { "ORCHES", "ARCHIT", "ORCHID", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private static final String[] meta13= { "T", "S", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String[] meta14= { "A", "O", "U", "E", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	private static final String[] meta15= { "L", "R", "N", "M", "B", "H", "F", "V", "W", " ", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$
	private static final String[] meta16= { "MC", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] meta17= { "CZ", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] meta18= { "WICZ", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] meta19= { "CIA", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] meta20= { "CC", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] meta21= { "I", "E", "H", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private static final String[] meta22= { "HU", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] meta23= { "UCCEE", "UCCES", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String[] meta24= { "CK", "CG", "CQ", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private static final String[] meta25= { "CI", "CE", "CY", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private static final String[] meta26= { "GN", "KN", "PN", "WR", "PS", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
	private static final String[] meta27= { " C", " Q", " G", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private static final String[] meta28= { "C", "K", "Q", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private static final String[] meta29= { "CE", "CI", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String[] meta30= { "DG", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] meta31= { "I", "E", "Y", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private static final String[] meta32= { "DT", "DD", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String[] meta33= { "B", "H", "D", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private static final String[] meta34= { "B", "H", "D", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private static final String[] meta35= { "B", "H", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String[] meta36= { "C", "G", "L", "R", "T", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
	private static final String[] meta37= { "EY", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] meta38= { "LI", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] meta39= { "ES", "EP", "EB", "EL", "EY", "IB", "IL", "IN", "IE", "EI", "ER", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$
	private static final String[] meta40= { "ER", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] meta41= { "DANGER", "RANGER", "MANGER", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private static final String[] meta42= { "E", "I", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String[] meta43= { "RGY", "OGY", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String[] meta44= { "E", "I", "Y", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private static final String[] meta45= { "AGGI", "OGGI", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String[] meta46= { "VAN ", "VON ", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String[] meta47= { "SCH", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] meta48= { "ET", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] meta49= { "C", "X", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String[] meta50= { "JOSE", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] meta51= { "SAN ", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] meta52= { "SAN ", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] meta53= { "JOSE", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] meta54= { "L", "T", "K", "S", "N", "M", "B", "Z", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
	private static final String[] meta55= { "S", "K", "L", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private static final String[] meta56= { "ILLO", "ILLA", "ALLE", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private static final String[] meta57= { "AS", "OS", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String[] meta58= { "A", "O", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String[] meta59= { "ALLE", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] meta60= { "UMB", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] meta61= { "ER", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] meta62= { "P", "B", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String[] meta63= { "IE", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] meta64= { "ME", "MA", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String[] meta65= { "ISL", "YSL", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String[] meta66= { "SUGAR", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] meta67= { "SH", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] meta68= { "HEIM", "HOEK", "HOLM", "HOLZ", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	private static final String[] meta69= { "SIO", "SIA", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String[] meta70= { "SIAN", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] meta71= { "M", "N", "L", "W", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	private static final String[] meta72= { "Z", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] meta73= { "Z", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] meta74= { "SC", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] meta75= { "OO", "ER", "EN", "UY", "ED", "EM", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
	private static final String[] meta76= { "ER", "EN", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String[] meta77= { "I", "E", "Y", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private static final String[] meta78= { "AI", "OI", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String[] meta79= { "S", "Z", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String[] meta80= { "TION", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] meta81= { "TIA", "TCH", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String[] meta82= { "TH", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] meta83= { "TTH", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] meta84= { "OM", "AM", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String[] meta85= { "VAN ", "VON ", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String[] meta86= { "SCH", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] meta87= { "T", "D", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String[] meta88= { "WR", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] meta89= { "WH", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] meta90= { "EWSKI", "EWSKY", "OWSKI", "OWSKY", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	private static final String[] meta91= { "SCH", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] meta92= { "WICZ", "WITZ", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String[] meta93= { "IAU", "EAU", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String[] meta94= { "AU", "OU", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String[] meta95= { "W", "K", "CZ", "WITZ" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	/** The mutator characters */
	private static final char[] MUTATOR_CHARACTERS= { 'A', 'B', 'X', 'S', 'K', 'J', 'T', 'F', 'H', 'L', 'M', 'N', 'P', 'R', '0' };

	/** The vowel characters */
	private static final char[] VOWEL_CHARACTERS= new char[] { 'A', 'E', 'I', 'O', 'U', 'Y' };

	/**
	 * Test whether the specified string contains one of the candidates in the
	 * list.
	 *
	 * @param candidates
	 *                   Array of candidates to check
	 * @param token
	 *                   The token to check for occurrences of the candidates
	 * @param offset
	 *                   The offset where to begin checking in the string
	 * @param length
	 *                   The length of the range in the string to check
	 * @return <code>true</code> iff the string contains one of the
	 *               candidates, <code>false</code> otherwise.
	 */
	protected static final boolean hasOneOf(final String[] candidates, final char[] token, final int offset, final int length) {
		if (offset < 0 || offset >= token.length || candidates.length == 0)
			return false;

		final String checkable= new String(token, offset, length);
		for (String candidate : candidates) {

			if (candidate.equals(checkable))
				return true;
		}
		return false;
	}

	/**
	 * Test whether the specified token contains one of the candidates in the
	 * list.
	 *
	 * @param candidates
	 *                   Array of candidates to check
	 * @param token
	 *                   The token to check for occurrences of the candidates
	 * @return <code>true</code> iff the string contains one of the
	 *               candidates, <code>false</code> otherwise.
	 */
	protected static final boolean hasOneOf(final String[] candidates, final String token) {
		for (String candidate : candidates) {

			if (token.indexOf(candidate) >= 0)
				return true;
		}
		return false;
	}

	/**
	 * Tests whether the specified token contains a vowel at the specified
	 * offset.
	 *
	 * @param token
	 *                   The token to check for a vowel
	 * @param offset
	 *                   The offset where to begin checking in the token
	 * @param length
	 *                   The length of the range in the token to check
	 * @return <code>true</code> iff the token contains a vowel, <code>false</code>
	 *               otherwise.
	 */
	protected static final boolean hasVowel(final char[] token, final int offset, final int length) {
		if (offset >= 0 && offset < length) {
			final char character= token[offset];
			for (char element : VOWEL_CHARACTERS) {
				if (element == character)
					return true;
			}
		}
		return false;
	}

	/*
	 * @see org.eclipse.spelling.done.IPhoneticHasher#getHash(java.lang.String)
	 */
	@Override
	public final String getHash(final String word) {
		final String input= word.toUpperCase() + "     "; //$NON-NLS-1$
		final char[] hashable= input.toCharArray();

		final boolean has95= hasOneOf(meta95, input);
		final StringBuffer buffer= new StringBuffer(hashable.length);

		int offset= 0;
		if (hasOneOf(meta26, hashable, 0, 2))
			offset += 1;

		if (hashable[0] == 'X') {
			buffer.append('S');
			offset += 1;
		}

		while (offset < hashable.length) {
			switch (hashable[offset]) {
				case 'A' :
				case 'E' :
				case 'I' :
				case 'O' :
				case 'U' :
				case 'Y' :
					if (offset == 0)
						buffer.append('A');
					offset += 1;
					break;
				case 'B' :
					buffer.append('P');
					if (hashable[offset + 1] == 'B')
						offset += 2;
					else
						offset += 1;
					break;
				case 'C' :
					if ((offset > 1) && !hasVowel(hashable, offset - 2, hashable.length) && hasOneOf(meta01, hashable, (offset - 1), 3) && (hashable[offset + 2] != 'I') && (hashable[offset + 2] != 'E') || hasOneOf(meta02, hashable, (offset - 2), 6)) {
						buffer.append('K');
						offset += 2;
						break;
					}
					if ((offset == 0) && hasOneOf(meta03, hashable, offset, 6)) {
						buffer.append('S');
						offset += 2;
						break;
					}
					if (hasOneOf(meta04, hashable, offset, 4)) {
						buffer.append('K');
						offset += 2;
						break;
					}
					if (hasOneOf(meta05, hashable, offset, 2)) {
						if ((offset > 0) && hasOneOf(meta06, hashable, offset, 4)) {
							buffer.append('K');
							offset += 2;
							break;
						}
						if ((offset == 0) && hasOneOf(meta07, hashable, (offset + 1), 5) || hasOneOf(meta08, hashable, offset + 1, 3) && !hasOneOf(meta09, hashable, 0, 5)) {
							buffer.append('K');
							offset += 2;
							break;
						}
						if (hasOneOf(meta10, hashable, 0, 4) || hasOneOf(meta11, hashable, 0, 3) || hasOneOf(meta12, hashable, offset - 2, 6) || hasOneOf(meta13, hashable, offset + 2, 1) || (hasOneOf(meta14, hashable, offset - 1, 1) || (offset == 0)) && hasOneOf(meta15, hashable, offset + 2, 1)) {
							buffer.append('K');
						} else {
							if (offset > 0) {
								if (hasOneOf(meta16, hashable, 0, 2))
									buffer.append('K');
								else
									buffer.append('X');
							} else {
								buffer.append('X');
							}
						}
						offset += 2;
						break;
					}
					if (hasOneOf(meta17, hashable, offset, 2) && !hasOneOf(meta18, hashable, offset, 4)) {
						buffer.append('S');
						offset += 2;
						break;
					}
					if (hasOneOf(meta19, hashable, offset, 2)) {
						buffer.append('X');
						offset += 2;
						break;
					}
					if (hasOneOf(meta20, hashable, offset, 2) && !((offset == 1) && hashable[0] == 'M')) {
						if (hasOneOf(meta21, hashable, offset + 2, 1) && !hasOneOf(meta22, hashable, offset + 2, 2)) {
							if (((offset == 1) && (hashable[offset - 1] == 'A')) || hasOneOf(meta23, hashable, (offset - 1), 5))
								buffer.append("KS"); //$NON-NLS-1$
							else
								buffer.append('X');
							offset += 3;
						} else {
							buffer.append('K');
							offset += 2;
						}
						break;
					}
					if (hasOneOf(meta24, hashable, offset, 2)) {
						buffer.append('K');
						offset += 2;
						break;
					} else if (hasOneOf(meta25, hashable, offset, 2)) {
						buffer.append('S');
						offset += 2;
						break;
					}
					buffer.append('K');
					if (hasOneOf(meta27, hashable, offset + 1, 2))
						offset += 3;
					else if (hasOneOf(meta28, hashable, offset + 1, 1) && !hasOneOf(meta29, hashable, offset + 1, 2))
						offset += 2;
					else
						offset += 1;
					break;
				case '\u00C7' :
					buffer.append('S');
					offset += 1;
					break;
				case 'D' :
					if (hasOneOf(meta30, hashable, offset, 2)) {
						if (hasOneOf(meta31, hashable, offset + 2, 1)) {
							buffer.append('J');
							offset += 3;
						} else {
							buffer.append("TK"); //$NON-NLS-1$
							offset += 2;
						}
						break;
					}
					buffer.append('T');
					if (hasOneOf(meta32, hashable, offset, 2)) {
						offset += 2;
					} else {
						offset += 1;
					}
					break;
				case 'F' :
					if (hashable[offset + 1] == 'F')
						offset += 2;
					else
						offset += 1;
					buffer.append('F');
					break;
				case 'G' :
					if (hashable[offset + 1] == 'H') {
						if ((offset > 0) && !hasVowel(hashable, offset - 1, hashable.length)) {
							buffer.append('K');
							offset += 2;
							break;
						}
						if (offset < 3) {
							if (offset == 0) {
								if (hashable[offset + 2] == 'I')
									buffer.append('J');
								else
									buffer.append('K');
								offset += 2;
								break;
							}
						}
						if ((offset > 1) && hasOneOf(meta33, hashable, offset - 2, 1) || ((offset > 2) && hasOneOf(meta34, hashable, offset - 3, 1)) || ((offset > 3) && hasOneOf(meta35, hashable, offset - 4, 1))) {
							offset += 2;
						} else {
							if ((offset > 2) && (hashable[offset - 1] == 'U') && hasOneOf(meta36, hashable, offset - 3, 1)) {
								buffer.append('F');
							} else {
								if ((offset > 0) && (hashable[offset - 1] != 'I'))
									buffer.append('K');
							}
							offset += 2;
						}
						break;
					}
					if (hashable[offset + 1] == 'N') {
						if ((offset == 1) && hasVowel(hashable, 0, hashable.length) && !has95) {
							buffer.append("KN"); //$NON-NLS-1$
						} else {
							if (!hasOneOf(meta37, hashable, offset + 2, 2) && (hashable[offset + 1] != 'Y') && !has95) {
								buffer.append("N"); //$NON-NLS-1$
							} else {
								buffer.append("KN"); //$NON-NLS-1$
							}
						}
						offset += 2;
						break;
					}
					if (hasOneOf(meta38, hashable, offset + 1, 2) && !has95) {
						buffer.append("KL"); //$NON-NLS-1$
						offset += 2;
						break;
					}
					if ((offset == 0) && ((hashable[offset + 1] == 'Y') || hasOneOf(meta39, hashable, offset + 1, 2))) {
						buffer.append('K');
						offset += 2;
						break;
					}
					if ((hasOneOf(meta40, hashable, offset + 1, 2) || (hashable[offset + 1] == 'Y')) && !hasOneOf(meta41, hashable, 0, 6) && !hasOneOf(meta42, hashable, offset - 1, 1) && !hasOneOf(meta43, hashable, offset - 1, 3)) {
						buffer.append('K');
						offset += 2;
						break;
					}
					if (hasOneOf(meta44, hashable, offset + 1, 1) || hasOneOf(meta45, hashable, offset - 1, 4)) {
						if (hasOneOf(meta46, hashable, 0, 4) || hasOneOf(meta47, hashable, 0, 3) || hasOneOf(meta48, hashable, offset + 1, 2)) {
							buffer.append('K');
						} else {
							buffer.append('J');
						}
						offset += 2;
						break;
					}
					if (hashable[offset + 1] == 'G')
						offset += 2;
					else
						offset += 1;
					buffer.append('K');
					break;
				case 'H' :
					if (((offset == 0) || hasVowel(hashable, offset - 1, hashable.length)) && hasVowel(hashable, offset + 1, hashable.length)) {
						buffer.append('H');
						offset += 2;
					} else {
						offset += 1;
					}
					break;
				case 'J' :
					if (hasOneOf(meta50, hashable, offset, 4) || hasOneOf(meta51, hashable, 0, 4)) {
						if ((offset == 0) && (hashable[offset + 4] == ' ') || hasOneOf(meta52, hashable, 0, 4)) {
							buffer.append('H');
						} else {
							buffer.append('J');
						}
						offset += 1;
						break;
					}
					if ((offset == 0) && !hasOneOf(meta53, hashable, offset, 4)) {
						buffer.append('J');
					} else {
						if (hasVowel(hashable, offset - 1, hashable.length) && !has95 && ((hashable[offset + 1] == 'A') || hashable[offset + 1] == 'O')) {
							buffer.append('J');
						} else {
							if (offset == (hashable.length - 1)) {
								buffer.append('J');
							} else {
								if (!hasOneOf(meta54, hashable, offset + 1, 1) && !hasOneOf(meta55, hashable, offset - 1, 1)) {
									buffer.append('J');
								}
							}
						}
					}
					if (hashable[offset + 1] == 'J')
						offset += 2;
					else
						offset += 1;
					break;
				case 'K' :
					if (hashable[offset + 1] == 'K')
						offset += 2;
					else
						offset += 1;
					buffer.append('K');
					break;
				case 'L' :
					if (hashable[offset + 1] == 'L') {
						if (((offset == (hashable.length - 3)) && hasOneOf(meta56, hashable, offset - 1, 4)) || ((hasOneOf(meta57, hashable, (hashable.length - 1) - 1, 2) || hasOneOf(meta58, hashable, hashable.length - 1, 1)) && hasOneOf(meta59, hashable, offset - 1, 4))) {
							buffer.append('L');
							offset += 2;
							break;
						}
						offset += 2;
					} else
						offset += 1;
					buffer.append('L');
					break;
				case 'M' :
					if ((hasOneOf(meta60, hashable, offset - 1, 3) && (((offset + 1) == (hashable.length - 1)) || hasOneOf(meta61, hashable, offset + 2, 2))) || (hashable[offset + 1] == 'M'))
						offset += 2;
					else
						offset += 1;
					buffer.append('M');
					break;
				case 'N' :
					if (hashable[offset + 1] == 'N')
						offset += 2;
					else
						offset += 1;
					buffer.append('N');
					break;
				case '\u00D1' :
					offset += 1;
					buffer.append('N');
					break;
				case 'P' :
					if (hashable[offset + 1] == 'N') {
						buffer.append('F');
						offset += 2;
						break;
					}
					if (hasOneOf(meta62, hashable, offset + 1, 1))
						offset += 2;
					else
						offset += 1;
					buffer.append('P');
					break;
				case 'Q' :
					if (hashable[offset + 1] == 'Q')
						offset += 2;
					else
						offset += 1;
					buffer.append('K');
					break;
				case 'R' :
					if (!((offset == (hashable.length - 1)) && !has95 && hasOneOf(meta63, hashable, offset - 2, 2) && !hasOneOf(meta64, hashable, offset - 4, 2)))
						buffer.append('R');
					if (hashable[offset + 1] == 'R')
						offset += 2;
					else
						offset += 1;
					break;
				case 'S' :
					if (hasOneOf(meta65, hashable, offset - 1, 3)) {
						offset += 1;
						break;
					}
					if ((offset == 0) && hasOneOf(meta66, hashable, offset, 5)) {
						buffer.append('X');
						offset += 1;
						break;
					}
					if (hasOneOf(meta67, hashable, offset, 2)) {
						if (hasOneOf(meta68, hashable, offset + 1, 4))
							buffer.append('S');
						else
							buffer.append('X');
						offset += 2;
						break;
					}
					if (hasOneOf(meta69, hashable, offset, 3) || hasOneOf(meta70, hashable, offset, 4)) {
						buffer.append('S');
						offset += 3;
						break;
					}
					if (((offset == 0) && hasOneOf(meta71, hashable, offset + 1, 1)) || hasOneOf(meta72, hashable, offset + 1, 1)) {
						buffer.append('S');
						if (hasOneOf(meta73, hashable, offset + 1, 1))
							offset += 2;
						else
							offset += 1;
						break;
					}
					if (hasOneOf(meta74, hashable, offset, 2)) {
						if (hashable[offset + 2] == 'H') {
							if (hasOneOf(meta75, hashable, offset + 3, 2)) {
								if (hasOneOf(meta76, hashable, offset + 3, 2)) {
									buffer.append("X"); //$NON-NLS-1$
								} else {
									buffer.append("SK"); //$NON-NLS-1$
								}
								offset += 3;
							} else {
								buffer.append('X');
								offset += 3;
							}
							break;
						}
						if (hasOneOf(meta77, hashable, offset + 2, 1)) {
							buffer.append('S');
							offset += 3;
							break;
						}
						buffer.append("SK"); //$NON-NLS-1$
						offset += 3;
						break;
					}
					if (!((offset == (hashable.length - 1)) && hasOneOf(meta78, hashable, offset - 2, 2)))
						buffer.append('S');
					if (hasOneOf(meta79, hashable, offset + 1, 1))
						offset += 2;
					else
						offset += 1;
					break;
				case 'T' :
					if (hasOneOf(meta80, hashable, offset, 4)) {
						buffer.append('X');
						offset += 3;
						break;
					}
					if (hasOneOf(meta81, hashable, offset, 3)) {
						buffer.append('X');
						offset += 3;
						break;
					}
					if (hasOneOf(meta82, hashable, offset, 2) || hasOneOf(meta83, hashable, offset, 3)) {
						if (hasOneOf(meta84, hashable, (offset + 2), 2) || hasOneOf(meta85, hashable, 0, 4) || hasOneOf(meta86, hashable, 0, 3)) {
							buffer.append('T');
						} else {
							buffer.append('0');
						}
						offset += 2;
						break;
					}
					if (hasOneOf(meta87, hashable, offset + 1, 1)) {
						offset += 2;
					} else
						offset += 1;
					buffer.append('T');
					break;
				case 'V' :
					if (hashable[offset + 1] == 'V')
						offset += 2;
					else
						offset += 1;
					buffer.append('F');
					break;
				case 'W' :
					if (hasOneOf(meta88, hashable, offset, 2)) {
						buffer.append('R');
						offset += 2;
						break;
					}
					if ((offset == 0) && (hasVowel(hashable, offset + 1, hashable.length) || hasOneOf(meta89, hashable, offset, 2))) {
						buffer.append('A');
					}
					if (((offset == (hashable.length - 1)) && hasVowel(hashable, offset - 1, hashable.length)) || hasOneOf(meta90, hashable, offset - 1, 5) || hasOneOf(meta91, hashable, 0, 3)) {
						buffer.append('F');
						offset += 1;
						break;
					}
					if (hasOneOf(meta92, hashable, offset, 4)) {
						buffer.append("TS"); //$NON-NLS-1$
						offset += 4;
						break;
					}
					offset += 1;
					break;
				case 'X' :
					if (!((offset == (hashable.length - 1)) && (hasOneOf(meta93, hashable, offset - 3, 3) || hasOneOf(meta94, hashable, offset - 2, 2))))
						buffer.append("KS"); //$NON-NLS-1$
					if (hasOneOf(meta49, hashable, offset + 1, 1))
						offset += 2;
					else
						offset += 1;
					break;
				case 'Z' :
					if (hashable[offset + 1] == 'H') {
						buffer.append('J');
						offset += 2;
						break;
					}
					buffer.append('S');
					if (hashable[offset + 1] == 'Z')
						offset += 2;
					else
						offset += 1;
					break;
				default :
					offset += 1;
			}
		}
		return buffer.toString();
	}

	/*
	 * @see org.eclipse.spelling.done.IPhoneticHasher#getMutators()
	 */
	@Override
	public final char[] getMutators() {
		return MUTATOR_CHARACTERS;
	}
}
