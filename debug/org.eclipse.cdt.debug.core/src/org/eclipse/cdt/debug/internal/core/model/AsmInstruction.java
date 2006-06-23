/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.model;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IAddressFactory;
import org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction;
import org.eclipse.cdt.debug.core.model.IAsmInstruction;

/**
 * Adapter for ICDIInstruction.
 */
public class AsmInstruction implements IAsmInstruction {

	private ICDIInstruction fCDIInstruction;

	private IAddress fAddress;

	/**
	 * Constructor for AsmInstruction.
	 */
	public AsmInstruction( IAddressFactory factory, ICDIInstruction cdiInstruction ) {
		fCDIInstruction = cdiInstruction;
		fAddress = factory.createAddress( cdiInstruction.getAdress() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IAsmInstruction#getAdress()
	 */
	public IAddress getAdress() {
		return fAddress;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IAsmInstruction#getFunctionName()
	 */
	public String getFunctionName() {
		return fCDIInstruction.getFuntionName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IAsmInstruction#getInstructionText()
	 */
	public String getInstructionText() {
		return fCDIInstruction.getInstruction();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IAsmInstruction#getOpcode()
	 */
	public String getOpcode() {
		return fCDIInstruction.getOpcode();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IAsmInstruction#getArguments()
	 */
	public String getArguments() {
		return fCDIInstruction.getArgs();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IAsmInstruction#getOffset()
	 */
	public long getOffset() {
		return fCDIInstruction.getOffset();
	}
}
