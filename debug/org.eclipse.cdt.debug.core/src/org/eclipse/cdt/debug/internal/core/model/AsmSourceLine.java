/*******************************************************************************
 * Copyright (c) 2004, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
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

	private int fLineNumber;

	/**
	 * Constructor for AsmSourceLine.
	 */
	public AsmSourceLine( IAddressFactory factory, String text, ICDIInstruction[] cdiInstructions ) {
		this( factory, text, -1, cdiInstructions );
	}

	/**
	 * Constructor for AsmSourceLine.
	 */
	public AsmSourceLine( IAddressFactory factory, String text, int lineNumber, ICDIInstruction[] cdiInstructions ) {
		fText = text;
		fLineNumber = lineNumber;
		fInstructions = new IAsmInstruction[cdiInstructions.length];
		for ( int i = 0; i < fInstructions.length; ++i ) {
			fInstructions[i] = new AsmInstruction( factory, cdiInstructions[i] );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IAsmSourceLine#getLineNumber()
	 */
	@Override
	public int getLineNumber() {
		return fLineNumber;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IAsmSourceLine#getInstructions()
	 */
	@Override
	public IAsmInstruction[] getInstructions() {
		return fInstructions;
	}

	@Override
	public String toString() {
		return fText;
	}
}
