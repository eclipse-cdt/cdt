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
 * Ranked word proposal for quick fix and content assist.
 */
public class RankedWordProposal implements Comparable<RankedWordProposal> {
	/** The word rank */
	private int fRank;

	/** The word text */
	private final String fText;

	/**
	 * Creates a new ranked word proposal.
	 *
	 * @param text       The text of this proposal
	 * @param rank       The rank of this proposal
	 */
	public RankedWordProposal(final String text, final int rank) {
		fText= text;
		fRank= rank;
	}

	/*
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public final int compareTo(RankedWordProposal word) {

		final int rank= word.getRank();

		if (fRank < rank)
			return -1;

		if (fRank > rank)
			return 1;

		return 0;
	}

	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public final boolean equals(Object object) {
		if (object instanceof RankedWordProposal)
			return object.hashCode() == hashCode();

		return false;
	}

	/**
	 * Returns the rank of the word
	 *
	 * @return The rank of the word
	 */
	public final int getRank() {
		return fRank;
	}

	/**
	 * Returns the text of this word.
	 *
	 * @return The text of this word
	 */
	public final String getText() {
		return fText;
	}

	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public final int hashCode() {
		return fText.hashCode();
	}

	/**
	 * Sets the rank of the word.
	 *
	 * @param rank       The rank to set
	 */
	public final void setRank(final int rank) {
		fRank= rank;
	}
}
