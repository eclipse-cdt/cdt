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
		reset();
	}

	public void reset() {
		fCurrentBranchPoint= -1;
		fTemplateNames= IASTName.EMPTY_NAME_ARRAY;
		if (fSimpleIDs != null) {
			fSimpleIDs.clear();
		}
	}

	public boolean ignoreTemplateID() {
		fCurrentBranchPoint++;
		return fSimpleIDs == null ? false : fSimpleIDs.get(fCurrentBranchPoint);
	}

	public void addTemplateName(IASTName name) {
		fTemplateNames= ArrayUtil.append(fTemplateNames, name);
	}

	public boolean setNextAlternative() {
		final int bp = fCurrentBranchPoint;
		if (bp < 0)
			return false;
		
		fCurrentBranchPoint= -1;
		fTemplateNames= IASTName.EMPTY_NAME_ARRAY;
		if (fSimpleIDs == null) {
			fSimpleIDs= new BitSet();
		}

		// Set a new branch as far right as possible.
		final int len = fSimpleIDs.length();
		if (len <= bp) {
			fSimpleIDs.set(bp);
			return true;
		}
			
		for (int branch= Math.min(bp, len-2); branch>=0; branch--) {
			if (!fSimpleIDs.get(branch)) {
				fSimpleIDs.clear(branch+1, len);
				fSimpleIDs.set(branch);
				return true;
			}
		}
		return false;
	}

	public IASTName[] getTemplateNames() {
		return ArrayUtil.trim(fTemplateNames);
	}		
}