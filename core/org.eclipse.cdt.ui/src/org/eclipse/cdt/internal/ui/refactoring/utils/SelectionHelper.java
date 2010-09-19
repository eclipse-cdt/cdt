/*******************************************************************************
 * Copyright (c) 2008, 2009 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.utils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansionLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

import org.eclipse.cdt.internal.ui.refactoring.Container;

/**
 * Helper class to support operations concerning a selection.
 * 
 * @author Mirko Stocker, Lukas Felber
 *
 */
public class SelectionHelper {

	public static Region getRegion(ISelection selection) {
		if (selection instanceof ITextSelection) {
			final ITextSelection txtSelection= (ITextSelection) selection;
			return new Region(txtSelection.getOffset(), txtSelection.getLength());
		}
		return null;
	}
	
	public static IASTSimpleDeclaration findFirstSelectedDeclaration(final Region textSelection, IASTTranslationUnit translationUnit) {

		final Container<IASTSimpleDeclaration> container = new Container<IASTSimpleDeclaration>();

		translationUnit.accept(new ASTVisitor() {
			{
				shouldVisitDeclarations = true;
			}
			@Override
			public int visit(IASTDeclaration declaration) {
				if (declaration instanceof IASTSimpleDeclaration && isSelectionOnExpression(textSelection, declaration)) {
					container.setObject((IASTSimpleDeclaration) declaration);
				}
				return super.visit(declaration);
			}
		});

		return container.getObject();
	}
	
	public static boolean isSelectionOnExpression(Region textSelection, IASTNode expression) {
		Region exprPos = createExpressionPosition(expression);
		int selStart = textSelection.getOffset();
		int selEnd = textSelection.getLength() + selStart;
		return exprPos.getOffset()+exprPos.getLength() >= selStart && exprPos.getOffset() <= selEnd;
	}
	
	public static boolean isExpressionWhollyInSelection(Region textSelection, IASTNode expression) {
		Region exprPos = createExpressionPosition(expression);

		int selStart = textSelection.getOffset();
		int selEnd = textSelection.getLength() + selStart;

		return exprPos.getOffset() >= selStart && exprPos.getOffset()+exprPos.getLength() <= selEnd;
	}
	
	public static boolean isInSameFile(IASTNode node, IFile file) {
		IPath path = new Path(node.getContainingFilename());
		IFile locFile = ResourcesPlugin.getWorkspace().getRoot().getFile(file.getLocation());
		IFile tmpFile = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		return locFile.equals(tmpFile);
	}
	
	public static boolean isInSameFileSelection(Region textSelection, IASTNode node, IFile file) {
		if( isInSameFile(node, file) ) {
			return SelectionHelper.isSelectionOnExpression(textSelection, node);
		}
		return false;
	}
	
	public static boolean isSelectedFile(Region textSelection, IASTNode node, IFile file) {
		if( isInSameFile(node, file) ) {
			return isExpressionWhollyInSelection(textSelection, node);
		}
		return false;
	}
	
	protected static Region createExpressionPosition(IASTNode expression) {

		int start = Integer.MAX_VALUE;
		int nodeLength = 0;
		IASTNodeLocation[] nodeLocations = expression.getNodeLocations();
		if (nodeLocations.length != 1) {
			for (IASTNodeLocation location : nodeLocations) {
				if (location instanceof IASTMacroExpansionLocation) {
					IASTMacroExpansionLocation macroLoc = (IASTMacroExpansionLocation) location;
					int nodeOffset = macroLoc.asFileLocation().getNodeOffset();
					if(nodeOffset < start) {
						start = nodeOffset;
					}
					nodeLength += macroLoc.asFileLocation().getNodeLength();
				}else {
					IASTFileLocation loc = expression.getFileLocation();
					int nodeOffset = loc.getNodeOffset();
					if(nodeOffset < start) {
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
				IASTFileLocation loc = expression.getFileLocation();
				start = loc.getNodeOffset();
				nodeLength = loc.getNodeLength();
			}
		}
		return new Region(start, nodeLength);
	}
}
