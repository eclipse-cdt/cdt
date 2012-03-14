/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import org.eclipse.cdt.core.dom.ast.ASTGenericVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import org.eclipse.cdt.internal.corext.refactoring.code.flow.Selection;
import org.eclipse.cdt.internal.corext.util.ASTNodes;

/**
 * Maps a selection to a set of AST nodes.
 */
public class SelectionAnalyzer extends ASTGenericVisitor {
	private Selection fSelection;
	private final boolean fTraverseSelectedNode;
	private IASTNode fLastCoveringNode;

	// Selected nodes
	private List<IASTNode> fSelectedNodes;

	public SelectionAnalyzer(Selection selection, boolean traverseSelectedNode) {
		super(true);
		Assert.isNotNull(selection);
		fSelection= selection;
		fTraverseSelectedNode= traverseSelectedNode;
	}

	protected void setSelection(Selection selection) {
		fSelection= selection;
	}

	public boolean hasSelectedNodes() {
		return fSelectedNodes != null && !fSelectedNodes.isEmpty();
	}

	public IASTNode[] getSelectedNodes() {
		if (fSelectedNodes == null || fSelectedNodes.isEmpty())
			return new IASTNode[0];
		return fSelectedNodes.toArray(new IASTNode[fSelectedNodes.size()]);
	}

	public IASTNode getFirstSelectedNode() {
		if (fSelectedNodes == null || fSelectedNodes.isEmpty())
			return null;
		return fSelectedNodes.get(0);
	}

	public IASTNode getLastSelectedNode() {
		if (fSelectedNodes == null || fSelectedNodes.isEmpty())
			return null;
		return fSelectedNodes.get(fSelectedNodes.size() - 1);
	}

	public boolean isExpressionSelected() {
		if (!hasSelectedNodes())
			return false;
		return fSelectedNodes.get(0) instanceof IASTExpression;
	}

	public IRegion getSelectedNodeRange() {
		if (fSelectedNodes == null || fSelectedNodes.isEmpty())
			return null;
		IASTNode firstNode= fSelectedNodes.get(0);
		int start= firstNode.getFileLocation().getNodeOffset();
		IASTNode lastNode= fSelectedNodes.get(fSelectedNodes.size() - 1);
		return new Region(start, ASTNodes.endOffset(lastNode) - start);
	}

	public IASTNode getLastCoveringNode() {
		return fLastCoveringNode;
	}

	public Selection getSelection() {
		return fSelection;
	}

	//--- node management ---------------------------------------------------------

	@Override
	protected int genericVisit(IASTNode node) {
		// The selection lies behind the node.
		if (fSelection.liesOutside(node)) {
			return PROCESS_SKIP;
		} else if (fSelection.covers(node)) {
			if (isFirstNode()) {
				handleFirstSelectedNode(node);
			} else {
				handleNextSelectedNode(node);
			}
			return fTraverseSelectedNode ? PROCESS_CONTINUE : PROCESS_SKIP;
		} else if (fSelection.coveredBy(node)) {
			fLastCoveringNode= node;
			return PROCESS_CONTINUE;
		} else if (fSelection.endsIn(node)) {
			return handleSelectionEndsIn(node) ? PROCESS_CONTINUE : PROCESS_SKIP;
		}
		// There is a possibility that the user has selected trailing semicolons that don't belong
		// to the statement. So dive into it to check if sub nodes are fully covered.
		return PROCESS_CONTINUE;
	}

	protected void reset() {
		fSelectedNodes= null;
	}

	protected void handleFirstSelectedNode(IASTNode node) {
		fSelectedNodes= new ArrayList<IASTNode>(5);
		fSelectedNodes.add(node);
	}

	protected void handleNextSelectedNode(IASTNode node) {
		if (getFirstSelectedNode().getParent() == node.getParent()) {
			fSelectedNodes.add(node);
		}
	}

	protected boolean handleSelectionEndsIn(IASTNode node) {
		return false;
	}

	protected List<IASTNode> internalGetSelectedNodes() {
		return fSelectedNodes;
	}

	private boolean isFirstNode() {
		return fSelectedNodes == null;
	}
}
