/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.debug.internal.core.model;

import org.eclipse.cdt.core.IAddressFactory;
import org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction;
import org.eclipse.cdt.debug.core.model.IAsmInstruction;
import org.eclipse.cdt.debug.core.model.IAsmSourceLine;

/**
 * Adapter for ICDIMixedInstruction.
 */
public class AsmSourceLine implements IAsmSourceLine {

	private String fText;

	private IAsmInstruction[] fInstructions = null;

	/**
	 * Constructor for AsmSourceLine.
	 */
	public AsmSourceLine( IAddressFactory factory, String text, ICDIInstruction[] cdiInstructions ) {
		fText = text;
		fInstructions = new IAsmInstruction[cdiInstructions.length];
		for ( int i = 0; i < fInstructions.length; ++i ) {
			fInstructions[i] = new AsmInstruction( factory, cdiInstructions[i] );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IAsmSourceLine#getInstructions()
	 */
	public IAsmInstruction[] getInstructions() {
		return fInstructions;
	}

	public String toString() {
		return fText;
	}
}
