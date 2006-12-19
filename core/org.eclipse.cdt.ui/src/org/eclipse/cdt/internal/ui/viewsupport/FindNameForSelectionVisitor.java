/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.ui.viewsupport;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * Searches for a name related to the given selection. The first choice will be the 
 * largest name inside the selection. If it does not exist the smallest name 
 * surounding the selection is taken. 
 * @see IASTNode#accept(ASTVisitor)
 * @since 4.0
 */
public class FindNameForSelectionVisitor extends ASTVisitor {

	private String fFilePath;
	private int fOffset;
	private int fEndOffset;
	private IASTName fSelectedName;

	public FindNameForSelectionVisitor(String filePath, int selectionStart, int selectionLength) {
		fFilePath= filePath;
		fOffset= selectionStart;
		fEndOffset= selectionStart+selectionLength;
		
		shouldVisitDeclarations= true;
		shouldVisitNames= true;
	}

	/**
	 * After the visitor was accepted by an ast-node you can query the 
	 * selected name.
	 * @return the name found for the selection, or <code>null</code>.
	 * @since 4.0
	 */
	public IASTName getSelectedName() {
		return fSelectedName;
	}

	public int visit(IASTDeclaration declaration) {
		IASTFileLocation loc= declaration.getFileLocation();
		if (loc == null || !loc.getFileName().equals(fFilePath)) {
			return PROCESS_SKIP;
		}
		int offset= loc.getNodeOffset();
		int endoffset= offset + loc.getNodeLength();
		if (endoffset < fOffset || fEndOffset < offset) {
			return PROCESS_SKIP;
		}
		
		return PROCESS_CONTINUE;
	}

	public int visit(IASTName name) {
		IASTFileLocation loc= name.getFileLocation();
		if (loc == null) {
			return PROCESS_CONTINUE;
		}
		
		if (!loc.getFileName().equals(fFilePath)) {
			return PROCESS_SKIP;
		}
		int offset= loc.getNodeOffset();
		int endoffset= offset + loc.getNodeLength();

		// check if name is inside of selection 
		if (fOffset <= offset && endoffset <= fEndOffset) {
			fSelectedName= name;
			return PROCESS_ABORT;
		}
		
		// check if name surrounds selection
		if (offset <= fOffset && fEndOffset <= endoffset) {
			fSelectedName= name;
			// continue as we might find a name inside of the selection, 
			// which is preferred.
			return PROCESS_CONTINUE;
		}
		
		return PROCESS_CONTINUE;
	}
}
