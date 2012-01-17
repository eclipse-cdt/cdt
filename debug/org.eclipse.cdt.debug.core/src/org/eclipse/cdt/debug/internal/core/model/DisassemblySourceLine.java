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

import java.io.File;
import java.math.BigInteger;

import org.eclipse.cdt.debug.core.cdi.model.ICDIMixedInstruction;
import org.eclipse.cdt.debug.core.model.IAsmInstruction;
import org.eclipse.cdt.debug.core.model.IDisassemblySourceLine;

public class DisassemblySourceLine extends CDebugElement implements IDisassemblySourceLine {

    private BigInteger fBaseElement;
    private ICDIMixedInstruction fCDIMixedInstruction;

    public DisassemblySourceLine( CDebugTarget target, BigInteger baseElement, ICDIMixedInstruction mixedInstruction ) {
        super( target );
        fBaseElement = baseElement;
        fCDIMixedInstruction = mixedInstruction;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.model.IDisassemblySourceLine#getFile()
     */
    @Override
	public File getFile() {
        String name = fCDIMixedInstruction.getFileName();
        if ( name != null && name.length() > 0 ) {
            return new File( name );
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.model.IAsmSourceLine#getInstructions()
     */
    @Override
	public IAsmInstruction[] getInstructions() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.model.IAsmSourceLine#getLineNumber()
     */
    @Override
	public int getLineNumber() {
        return fCDIMixedInstruction.getLineNumber();
    }

    public BigInteger getBaseAddress() {
        return fBaseElement;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return fCDIMixedInstruction.getFileName() + ' ' + getLineNumber();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals( Object obj ) {
        if ( !(obj instanceof IDisassemblySourceLine) )
            return false;
        IDisassemblySourceLine other = (IDisassemblySourceLine)obj;
        if ( !getFile().equals( other.getFile() ) )
            return false;
        if ( getLineNumber() != other.getLineNumber() )
            return false;
        return true;
    }
}
