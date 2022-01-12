/*******************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.correction.proposals;

import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.swt.graphics.Image;

/**
 * A proposal for quick fixes and quick assists that works on a AST rewriter and enters the
 * linked mode when the proposal is set up.
 * Either a rewriter is directly passed in the constructor or method {@link #getRewrite()} is
 * overridden to provide the AST rewriter that is evaluated to the document when the proposal
 * is applied.
 * @since 5.1
 */
public class LinkedCorrectionProposal extends ASTRewriteCorrectionProposal {
	/**
	 * Constructs a linked correction proposal.
	 * @param name The display name of the proposal.
	 * @param tu The translation unit that is modified.
	 * @param rewrite The AST rewrite that is invoked when the proposal is applied
	 *     {@code null} can be passed if {@link #getRewrite()} is overridden.
	 * @param relevance The relevance of this proposal.
	 * @param image The image that is displayed for this proposal or {@code null} if no
	 * image is desired.
	 */
	public LinkedCorrectionProposal(String name, ITranslationUnit tu, ASTRewrite rewrite, int relevance, Image image) {
		super(name, tu, rewrite, relevance, image);
	}

	/**
	 * Adds a linked position to be shown when the proposal is applied. All position with the
	 * same group id are linked.
	 *
	 * @param position The position to add.
	 * @param isFirst If set, the proposal is jumped to first.
	 * @param groupID The id of the group the proposal belongs to. All proposals in the same group
	 *     are linked.
	 */
	public void addLinkedPosition(ITrackedNodePosition position, boolean isFirst, String groupID) {
		getLinkedProposalModel().getPositionGroup(groupID, true).addPosition(position, isFirst);
	}

	/**
	 * Sets the end position of the linked mode to the end of the passed range.
	 *
	 * @param position The position that describes the end position of the linked mode.
	 */
	public void setEndPosition(ITrackedNodePosition position) {
		getLinkedProposalModel().setEndPosition(position);
	}

	/**
	 * Adds a linked position proposal to the group with the given id.
	 *
	 * @param groupID The id of the group that should present the proposal
	 * @param proposal The string to propose.
	 * @param image The image to show for the position proposal or {@code null} if
	 *     no image is desired.
	 */
	public void addLinkedPositionProposal(String groupID, String proposal, Image image) {
		getLinkedProposalModel().getPositionGroup(groupID, true).addProposal(proposal, image, 10);
	}
}
