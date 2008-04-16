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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.IAddressFactory;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMixedInstruction;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.cdt.debug.core.model.IDisassemblyInstruction;
import org.eclipse.cdt.debug.core.model.IDisassemblyLine;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;

public class DisassemblyRetrieval extends CDebugElement implements ICDIEventListener {

    public static final int FLAGS_SHOW_INSTRUCTIONS = 0x1;
    public static final int FLAGS_SHOW_SOURCE = 0x2;

    private Object fInput = null;
    private BigInteger fBaseElement = null;
    private int fCurrentOffset = 0;
    private int fFlags = 0;
    private IDisassemblyLine[] fLines;

    public DisassemblyRetrieval( CDebugTarget target ) {
        super( target );
        fLines = new IDisassemblyLine[0];
        CDebugCorePlugin.getDefault().getDisassemblyContextService().register( this );
        getCDISession().getEventManager().addEventListener( this );
    }

    public void dispose() {
        getCDISession().getEventManager().removeEventListener( this );
        CDebugCorePlugin.getDefault().getDisassemblyContextService().unregister( this );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener#handleDebugEvents(org.eclipse.cdt.debug.core.cdi.event.ICDIEvent[])
     */
    public void handleDebugEvents( ICDIEvent[] event ) {
        // TODO Auto-generated method stub
    }

    public Object getInput() {
        return fInput;
    }

    public BigInteger getBaseElement() {
        return fBaseElement;
    }

    public int getCurrentOffset() {
        return fCurrentOffset;
    }

    public IDisassemblyLine[] getLines() {
        return fLines;
    }

    public void changeBase( Object input, int offset, int flags ) throws DebugException {
        if ( input instanceof ICStackFrame ) {
            fInput = input;
            ICStackFrame frame = (ICStackFrame)input;
            BigInteger address = frame.getAddress().getValue();
            if ( !containsAddress( address, fLines ) ) {
                fCurrentOffset = 0;
                reset();
            }
            else if ( flags != fFlags ) {
                reset();
            }
            else {
                fCurrentOffset += getDistance( fBaseElement, address );
            }
            fBaseElement = address;
        }
    }

    public void retrieveDisassembly( Object input, Object base, int offset, int lineCount, boolean reveal, int flags ) throws DebugException {
        boolean showInstructions = ( (flags & FLAGS_SHOW_INSTRUCTIONS) != 0 );
        boolean showSource = ( (flags & FLAGS_SHOW_SOURCE) != 0 );
        List<IDisassemblyLine> lines = new ArrayList<IDisassemblyLine>( lineCount );
        BigInteger startAddress = getCurrentStartAddress();
        BigInteger address = null;
        if ( startAddress != null ) { 
            if ( getCurrentOffset() > offset ) {
                address = startAddress.subtract( BigInteger.valueOf( getMinInstructionSize() * (getCurrentOffset() - offset) ) ); 
            }
            else if ( getCurrentOffset() < offset ) {
                IDisassemblyInstruction next = getNextInstruction( startAddress, fLines );
                if ( next != null )
                    address = next.getAdress().getValue();
            }
            else {
                address = startAddress;
            }
        }
        if ( address == null )
            address = fBaseElement;
        lines.addAll( Arrays.asList( disassembleDown( address, lineCount, showSource ) ) );
        fLines = lines.toArray( new IDisassemblyLine[lines.size()] );
        fCurrentOffset = offset;
    }

    private boolean containsAddress( BigInteger address, IDisassemblyLine[] lines ) {
        return ( getIndexForAddress( address, lines ) >= 0 );
    }

    public void reset() {
        fLines = new IDisassemblyLine[0];
    }

    private int getDistance( BigInteger address1, BigInteger address2 ) {
        int index1 = getIndexForAddress( address1, fLines );
        Assert.isTrue( index1 >=0 );
        int index2 = getIndexForAddress( address2, fLines );
        Assert.isTrue( index2 >=0 );
        return index2 - index1;
    }

    private int getIndexForAddress( BigInteger address, IDisassemblyLine[] lines ) {
        for ( int i = 0; i < lines.length; ++i ) {
            if ( lines[i] instanceof IDisassemblyInstruction && 
                 address.compareTo( ((IDisassemblyInstruction)lines[i]).getAdress().getValue() ) == 0 )
                return i;
        }
        return -1;
    }

    private BigInteger getCurrentStartAddress() {
        for ( IDisassemblyLine l : fLines ) {
            if ( l instanceof IDisassemblyInstruction )
                return ((IDisassemblyInstruction)l).getAdress().getValue();
        }
        return null;
    }
    
    private IDisassemblyLine[] disassembleDown( BigInteger address, int lineCount, boolean mixed ) throws DebugException {
        BigInteger endAddress = address.add( BigInteger.valueOf( lineCount * getMaxInstructionSize() ) );
        if ( endAddress.compareTo( getGlobalEndAddress() ) > 0 )
            endAddress = getGlobalEndAddress();
        IDisassemblyLine[] lines = disassemble( address, endAddress, mixed );
        int size = Math.min( lineCount, lines.length );
        IDisassemblyLine[] result = new IDisassemblyLine[size];
        int start = getIndexForAddress( address, lines );
        if ( start != -1 ) {
            System.arraycopy( lines, start, result, 0, size );
        }
        return result;
    }

    private IDisassemblyLine[] disassemble( BigInteger startAddress, BigInteger endAddress, boolean mixed ) throws DebugException {
        List<IDisassemblyLine> list = new ArrayList<IDisassemblyLine>();
        ICDITarget cdiTarget = (ICDITarget)getDebugTarget().getAdapter( ICDITarget.class );
        try {
            ICDIMixedInstruction[] mixedInstructions = null;
            ICDIInstruction[] asmInstructions = null;
            if ( mixed ) {
                mixedInstructions = cdiTarget.getMixedInstructions( startAddress, endAddress );
                if ( mixedInstructions.length == 0 
                     || mixedInstructions.length == 1 
                     && mixedInstructions[0].getInstructions().length == 0 ) {
                    mixedInstructions = null;
                }
            }
            if ( mixedInstructions == null ) {
                asmInstructions = cdiTarget.getInstructions( startAddress, endAddress );
            }
            if ( mixedInstructions != null ) {
                for ( ICDIMixedInstruction mi : mixedInstructions ) {
                    list.add( new DisassemblySourceLine( (CDebugTarget)getDebugTarget(), fBaseElement, mi ) );
                    ICDIInstruction[] instructions = mi.getInstructions();
                    for ( ICDIInstruction i : instructions ) {
                        list.add( new DisassemblyInstruction( (CDebugTarget)getDebugTarget(), fBaseElement, i ) );
                    }
                }
            }
            else if ( asmInstructions != null ) {
                for ( ICDIInstruction i : asmInstructions ) {
                    list.add( new DisassemblyInstruction( (CDebugTarget)getDebugTarget(), fBaseElement, i ) );
                }
            }
        }
        catch( CDIException exc ) {
            throw new DebugException( new Status( IStatus.ERROR, "dummy", exc.getDetailMessage(), exc ) ); //$NON-NLS-1$
        }
        return list.toArray( new IDisassemblyLine[list.size()] );
    }
    
    private int getMaxInstructionSize() {
        return 4;
    }

    private int getMinInstructionSize() {
        return 1;
    }

    private IDisassemblyInstruction getNextInstruction( BigInteger address, IDisassemblyLine[] lines ) {
        int index = getIndexForAddress( address, lines );
        if ( index == -1 || index == lines.length - 1 )
            return null;
        for ( int i = index + 1; i < lines.length; ++i ) {
            if ( lines[i] instanceof IDisassemblyInstruction )
                return (IDisassemblyInstruction)lines[i];
        }
        return null;
    }

    private IDisassemblyInstruction getPreviousInstruction( BigInteger baseAddress ) throws DebugException {
        BigInteger endAddress = baseAddress.add( BigInteger.valueOf( 1 ) );
        BigInteger startAddress = baseAddress;
        IDisassemblyLine[] lines = new IDisassemblyLine[0];
        int index = -1;
        while( index == -1 && startAddress.compareTo( getGlobalStartAddress() ) > 0 ) {
            startAddress = startAddress.subtract( BigInteger.valueOf( 1 ) );
            lines = disassemble( startAddress, endAddress, false );
            index = getIndexForAddress( baseAddress, lines );
        }
        return ( index > 0 ) ? (IDisassemblyInstruction)lines[index - 1] : null;
    }

    private BigInteger getGlobalStartAddress() {
        return getAddressFactory().getZero().getValue();
    }

    private BigInteger getGlobalEndAddress() {
        return getAddressFactory().getMax().getValue();
    }

    private IAddressFactory getAddressFactory() {
        return ((CDebugTarget)getDebugTarget()).getAddressFactory();
    }
}
