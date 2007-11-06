/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.parser.scanner;

import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;

/**
 * A location context representing macro expansions.
 * @since 5.0
 */
class LocationCtxMacroExpansion extends LocationCtx {
	private final LocationMap fLocationMap;
	private final int fLength;
	private ASTMacroReferenceName fName;

	public LocationCtxMacroExpansion(LocationMap map, LocationCtxContainer parent, int parentOffset, int parentEndOffset,
			int sequenceNumber, int length, ImageLocationInfo[] imageLocations,	ASTMacroReferenceName expansion) {
		super(parent, parentOffset, parentEndOffset, sequenceNumber);
		fLocationMap= map;
		fLength= length;
		fName= expansion;
	}

	public int getSequenceLength() {
		return fLength;
	}
	
	public boolean collectLocations(int start, int length, ArrayList locations) {
		final int offset= start-fSequenceNumber;
		assert offset >= 0 && length >= 0;
		
		if (offset+length <= fLength) {
			locations.add(new ASTMacroExpansionLocation(this, offset, length));
			return true;
		}

		locations.add(new ASTMacroExpansionLocation(this, offset, fLength-offset));
		return false;
	}	
	
	public IASTPreprocessorMacroDefinition getMacroDefinition() {
		return fLocationMap.getMacroDefinition((IMacroBinding) fName.getBinding());
	}
}


