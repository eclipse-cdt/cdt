/*******************************************************************************
 * Copyright (c) 2008 ARM Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Limited - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.core.model;

import java.math.BigInteger;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction;
import org.eclipse.cdt.debug.core.model.IDisassemblyInstruction;

public class DisassemblyInstruction extends CDebugElement implements IDisassemblyInstruction {

    private ICDIInstruction fCDIInstruction;
    private IAddress fAddress;

    public DisassemblyInstruction( CDebugTarget target, BigInteger baseElement, ICDIInstruction instruction ) {
        super( target );
        fCDIInstruction = instruction;
        fAddress = target.getAddressFactory().createAddress( fCDIInstruction.getAdress() );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.model.IAsmInstruction#getAdress()
     */
    @Override
	public IAddress getAdress() {
        return fAddress;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.model.IAsmInstruction#getArguments()
     */
    @Override
	public String getArguments() {
        return fCDIInstruction.getArgs();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.model.IAsmInstruction#getFunctionName()
     */
    @Override
	public String getFunctionName() {
        return fCDIInstruction.getFuntionName();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.model.IAsmInstruction#getInstructionText()
     */
    @Override
	public String getInstructionText() {
        return fCDIInstruction.getInstruction();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.model.IAsmInstruction#getOffset()
     */
    @Override
	public long getOffset() {
        return fCDIInstruction.getOffset();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.model.IAsmInstruction#getOpcode()
     */
    @Override
	public String getOpcode() {
        return fCDIInstruction.getOpcode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals( Object obj ) {
        if ( obj == this )
            return true;
        if ( !(obj instanceof IDisassemblyInstruction) )
            return false;
        IDisassemblyInstruction instr = (IDisassemblyInstruction)obj;
        if ( !instr.getAdress().equals( getAdress() ) )
            return false;
        if ( instr.getOffset() != getOffset() )
            return false;
        if ( instr.getFunctionName().compareTo( getFunctionName() ) != 0 )
            return false;
        if ( instr.getOpcode().compareTo( getOpcode() ) != 0 )
            return false;
        if ( instr.getArguments().compareTo( getArguments() ) != 0 )
            return false;
        if ( instr.getInstructionText().compareTo( getInstructionText() ) != 0 )
            return false;
        return true;
    }
}
