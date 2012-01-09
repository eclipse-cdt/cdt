/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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
 * Default phonetic distance algorithm for English words.
 * <p>
 * This algorithm implements the Levenshtein text edit distance.
 * </p>
 */
public final class DefaultPhoneticDistanceAlgorithm implements IPhoneticDistanceAlgorithm {

	/** The change case cost */
	public static final int COST_CASE= 10;

	/** The insert character cost */
	public static final int COST_INSERT= 95;

	/** The remove character cost */
	public static final int COST_REMOVE= 95;

	/** The substitute characters cost */
	public static final int COST_SUBSTITUTE= 100;

	/** The swap characters cost */
	public static final int COST_SWAP= 90;

	/*
	 * @see org.eclipse.spelling.done.IPhoneticDistanceAlgorithm#getDistance(java.lang.String,java.lang.String)
	 */
	@Override
	public final int getDistance(final String from, final String to) {

		final char[] first= (" " + from).toCharArray(); //$NON-NLS-1$
		final char[] second= (" " + to).toCharArray(); //$NON-NLS-1$

		final int rows= first.length;
		final int columns= second.length;

		final int[][] metric= new int[rows][columns];
		for (int column= 1; column < columns; column++)
			metric[0][column]= metric[0][column - 1] + COST_REMOVE;

		for (int row= 1; row < rows; row++)
			metric[row][0]= metric[row - 1][0] + COST_INSERT;

		char source, target;

		int swap= Integer.MAX_VALUE;
		int change= Integer.MAX_VALUE;

		int minimum, diagonal, insert, remove;
		for (int row= 1; row < rows; row++) {

			source= first[row];
			for (int column= 1; column < columns; column++) {

				target= second[column];
				diagonal= metric[row - 1][column - 1];

				if (source == target) {
					metric[row][column]= diagonal;
					continue;
				}

				change= Integer.MAX_VALUE;
				if (Character.toLowerCase(source) == Character.toLowerCase(target))
					change= COST_CASE + diagonal;

				swap= Integer.MAX_VALUE;
				if (row != 1 && column != 1 && source == second[column - 1] && first[row - 1] == target)
					swap= COST_SWAP + metric[row - 2][column - 2];

				minimum= COST_SUBSTITUTE + diagonal;
				if (swap < minimum)
					minimum= swap;

				remove= metric[row][column - 1];
				if (COST_REMOVE + remove < minimum)
					minimum= COST_REMOVE + remove;

				insert= metric[row - 1][column];
				if (COST_INSERT + insert < minimum)
					minimum= COST_INSERT + insert;
				if (change < minimum)
					minimum= change;

				metric[row][column]= minimum;
			}
		}
		return metric[rows - 1][columns - 1];
	}
}
