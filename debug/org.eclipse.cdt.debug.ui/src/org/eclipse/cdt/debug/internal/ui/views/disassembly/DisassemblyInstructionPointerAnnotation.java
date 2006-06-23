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
package org.eclipse.cdt.debug.internal.ui.views.disassembly;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.cdt.debug.core.model.IDisassembly;
import org.eclipse.cdt.debug.internal.ui.IInternalCDebugUIConstants;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.text.source.Annotation;

/**
 * An annotation for the vertical ruler in the disassembly view that shows one 
 * of two images for the current instruction pointer when debugging (one for 
 * the top stack frame, one for all others).
 */
public class DisassemblyInstructionPointerAnnotation extends Annotation {

	private int fHashCode = 0;

	/**
	 * Construct an instruction pointer annotation for the given stack frame.
	 * 
	 * @param stackFrame frame to create an instruction pointer annotation for
	 * @param isTopFrame whether the given frame is the top stack frame in its thread 
	 */
	public DisassemblyInstructionPointerAnnotation( ICStackFrame stackFrame, boolean isTopFrame ) {
		super( isTopFrame ? IInternalCDebugUIConstants.ANN_DISASM_INSTR_POINTER_CURRENT : IInternalCDebugUIConstants.ANN_DISASM_INSTR_POINTER_SECONDARY, 
			   false, 
			   isTopFrame ? DisassemblyMessages.getString( "DisassemblyInstructionPointerAnnotation.Current_Pointer_1" ) : DisassemblyMessages.getString( "DisassemblyInstructionPointerAnnotation.Secondary_Pointer_1" ) ); //$NON-NLS-1$ //$NON-NLS-2$
		fHashCode = getHashCode( stackFrame );
	}

	private IDisassembly getDisassembly( ICStackFrame frame ) {
		if ( frame != null ) {
			ICDebugTarget target = (ICDebugTarget)frame.getDebugTarget();
			try {
				return target.getDisassembly();
			}
			catch( DebugException e ) {
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return fHashCode;
	}

	private int getHashCode( ICStackFrame frame ) {
		int hashCode = 17;
		IDisassembly disassembly = getDisassembly( frame );
		hashCode = 37*hashCode + (( disassembly != null ) ? disassembly.hashCode() : 0);
		if ( frame != null ) {
			IAddress address = frame.getAddress();
			hashCode = 37*hashCode + address.hashCode();
		}
		return hashCode;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals( Object obj ) {
		return ( obj != null ? obj.hashCode() == hashCode() : false );
	}
}
