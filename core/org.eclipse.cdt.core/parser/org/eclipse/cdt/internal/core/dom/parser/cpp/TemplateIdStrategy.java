/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.BitSet;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.AbstractGNUSourceCodeParser.ITemplateIdStrategy;

/**
 * Governs backtracking through multiple variants due to the ambiguous meaning of '<'.
 * @see NameOrTemplateIDVariants
 */
final class TemplateIdStrategy implements ITemplateIdStrategy {
	private int fCurrentBranchPoint;
	private BitSet fSimpleIDs;
	private IASTName[] fTemplateNames;
	
	public TemplateIdStrategy() {
		fCurrentBranchPoint= -1;
		fTemplateNames= IASTName.EMPTY_NAME_ARRAY;
	}

	@Override
	public boolean shallParseAsTemplateID(IASTName name) {
		fCurrentBranchPoint++;
		
		boolean templateID= fSimpleIDs == null || !fSimpleIDs.get(fCurrentBranchPoint);
		if (templateID) {
			fTemplateNames= ArrayUtil.append(fTemplateNames, name);
		}
		return templateID;
	}

	public boolean setNextAlternative() {
		int bp = fCurrentBranchPoint;
		if (bp < 0)
			return false;
		
		fCurrentBranchPoint= -1;
		IASTName[] names = getTemplateNames();
		int nameLen= names.length;
		fTemplateNames= IASTName.EMPTY_NAME_ARRAY;
		if (fSimpleIDs == null) {
			fSimpleIDs= new BitSet();
		}

		// Set a new branch as far right as possible.
		while (bp >= 0) {
			if (!fSimpleIDs.get(bp)) {
				if (nameLen == 0 || !hasMultipleArgs(names[--nameLen])) {
					fSimpleIDs.clear(bp+1, Integer.MAX_VALUE);
					fSimpleIDs.set(bp);
					return true;
				}
			}
			bp--;
		}
		return false;
	}

	private boolean hasMultipleArgs(IASTName templateName) {
		IASTNode parent= templateName.getParent();
		if (parent instanceof ICPPASTTemplateId) {
			return ((ICPPASTTemplateId) parent).getTemplateArguments().length > 1;
		}
		return false;
	}

	public IASTName[] getTemplateNames() {
		return ArrayUtil.trim(fTemplateNames);
	}		
}