/*******************************************************************************
 * Copyright (c) 2008, 2016 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *     Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.utils;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansionLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.internal.ui.refactoring.Container;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.viewers.ISelection;

/**
 * Helper class to support operations concerning a selection.
 *
 * @author Mirko Stocker, Lukas Felber
 */
public class SelectionHelper {

	public static Region getRegion(ISelection selection) {
		if (selection instanceof ITextSelection) {
			final ITextSelection txtSelection = (ITextSelection) selection;
			return new Region(txtSelection.getOffset(), txtSelection.getLength());
		}
		return null;
	}

	public static IASTSimpleDeclaration findFirstSelectedDeclaration(final IRegion textSelection,
			IASTTranslationUnit translationUnit) {
		final Container<IASTSimpleDeclaration> container = new Container<>();

		translationUnit.accept(new ASTVisitor() {
			{
				shouldVisitDeclarations = true;
			}

			@Override
			public int visit(IASTDeclaration declaration) {
				if (declaration instanceof IASTSimpleDeclaration
						&& doesNodeOverlapWithRegion(declaration, textSelection)) {
					container.setObject((IASTSimpleDeclaration) declaration);
				}
				return super.visit(declaration);
			}
		});

		return container.getObject();
	}

	public static boolean doesNodeOverlapWithRegion(IASTNode node, IRegion region) {
		return doRegionsOverlap(getNodeSpan(node), region);
	}

	public static boolean isNodeInsideRegion(IASTNode node, IRegion region) {
		return isRegionInside(getNodeSpan(node), region);
	}

	/**
	 * Returns true if the first region is inside the second.
	 */
	private static boolean isRegionInside(IRegion region1, IRegion region2) {
		int offset1 = region1.getOffset();
		int offset2 = region2.getOffset();
		return offset1 >= offset2 && offset1 + region1.getLength() <= offset2 + region2.getLength();
	}

	/**
	 * Returns true if the two regions have at least one common point.
	 */
	private static boolean doRegionsOverlap(IRegion region1, IRegion region2) {
		int offset1 = region1.getOffset();
		int offset2 = region2.getOffset();
		return offset1 + region1.getLength() >= offset2 && offset1 <= offset2 + region2.getLength();
	}

	public static boolean isNodeInsideSelection(IASTNode node, IRegion selection) {
		return node.isPartOfTranslationUnitFile() && isNodeInsideRegion(node, selection);
	}

	public static boolean isSelectionInsideNode(IASTNode node, IRegion selection) {
		return node.isPartOfTranslationUnitFile() && isRegionInside(selection, getNodeSpan(node));
	}

	public static boolean nodeMatchesSelection(IASTNode node, IRegion region) {
		return getNodeSpan(node).equals(region);
	}

	protected static IRegion getNodeSpan(IASTNode region) {
		int start = Integer.MAX_VALUE;
		int nodeLength = 0;
		IASTNodeLocation[] nodeLocations = region.getNodeLocations();
		if (nodeLocations.length != 1) {
			for (IASTNodeLocation location : nodeLocations) {
				if (location instanceof IASTMacroExpansionLocation) {
					IASTMacroExpansionLocation macroLoc = (IASTMacroExpansionLocation) location;
					int nodeOffset = macroLoc.asFileLocation().getNodeOffset();
					if (nodeOffset < start) {
						start = nodeOffset;
					}
					nodeLength += macroLoc.asFileLocation().getNodeLength();
				} else {
					IASTFileLocation loc = region.getFileLocation();
					int nodeOffset = loc.getNodeOffset();
					if (nodeOffset < start) {
						start = nodeOffset;
					}
					nodeLength = loc.getNodeLength();
				}
			}
		} else {
			if (nodeLocations[0] instanceof IASTMacroExpansionLocation) {
				IASTMacroExpansionLocation macroLoc = (IASTMacroExpansionLocation) nodeLocations[0];
				start = macroLoc.asFileLocation().getNodeOffset();
				nodeLength = macroLoc.asFileLocation().getNodeLength();
			} else {
				IASTFileLocation loc = region.getFileLocation();
				start = loc.getNodeOffset();
				nodeLength = loc.getNodeLength();
			}
		}
		return new Region(start, nodeLength);
	}
}
