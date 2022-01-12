/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.fix;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

public class LinkedProposalPositionGroup {
	/**
	 * {@link LinkedProposalPositionGroup.PositionInformation} describes a position
	 * inside a position group. The information provided must be accurate
	 * after the document change to the proposal has been performed, but doesn't
	 * need to reflect the changed done by the linking mode.
	 */
	public static abstract class PositionInformation {
		public abstract int getOffset();

		public abstract int getLength();

		public abstract int getSequenceRank();
	}

	public static class Proposal {

		private String fDisplayString;
		private Image fImage;
		private int fRelevance;

		public Proposal(String displayString, Image image, int relevance) {
			fDisplayString = displayString;
			fImage = image;
			fRelevance = relevance;
		}

		public String getDisplayString() {
			return fDisplayString;
		}

		public Image getImage() {
			return fImage;
		}

		public int getRelevance() {
			return fRelevance;
		}

		public void setImage(Image image) {
			fImage = image;
		}

		public String getAdditionalProposalInfo() {
			return null;
		}

		public TextEdit computeEdits(int offset, LinkedPosition position, char trigger, int stateMask,
				LinkedModeModel model) throws CoreException {
			return new ReplaceEdit(position.getOffset(), position.getLength(), fDisplayString);
		}
	}

	public static PositionInformation createPositionInformation(ITrackedNodePosition pos, boolean isFirst) {
		return new TrackedNodePosition(pos, isFirst);
	}

	private static class TrackedNodePosition extends PositionInformation {
		private final ITrackedNodePosition fPos;
		private final boolean fIsFirst;

		public TrackedNodePosition(ITrackedNodePosition pos, boolean isFirst) {
			fPos = pos;
			fIsFirst = isFirst;
		}

		@Override
		public int getOffset() {
			return fPos.getStartPosition();
		}

		@Override
		public int getLength() {
			return fPos.getLength();
		}

		@Override
		public int getSequenceRank() {
			return fIsFirst ? 0 : 1;
		}
	}

	private final String fGroupId;
	private final List<PositionInformation> fPositions;
	private final List<Proposal> fProposals;

	public LinkedProposalPositionGroup(String groupID) {
		fGroupId = groupID;
		fPositions = new ArrayList<>();
		fProposals = new ArrayList<>();
	}

	public void addPosition(PositionInformation position) {
		fPositions.add(position);
	}

	public void addProposal(Proposal proposal) {
		fProposals.add(proposal);
	}

	public void addPosition(ITrackedNodePosition position, boolean isFirst) {
		addPosition(createPositionInformation(position, isFirst));
	}

	public void addProposal(String displayString, Image image, int relevance) {
		addProposal(new Proposal(displayString, image, relevance));
	}

	public String getGroupId() {
		return fGroupId;
	}

	public PositionInformation[] getPositions() {
		return fPositions.toArray(new PositionInformation[fPositions.size()]);
	}

	public Proposal[] getProposals() {
		return fProposals.toArray(new Proposal[fProposals.size()]);
	}
}
