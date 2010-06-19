/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Freescale Semiconductor - refactoring
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.disassembly.dsf;

import java.math.BigInteger;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMixedInstruction;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.cdt.debug.core.model.IDisassemblyBlock;
import org.eclipse.cdt.debug.internal.core.model.DisassemblyBlock;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.swt.widgets.Display;


/**
 */
public class CDIDisassemblyRetrieval implements IDisassemblyRetrieval {

	private ICDebugTarget fDebugTarget;
	
	/**
	 * Constructor
	 */
	public CDIDisassemblyRetrieval(ICDebugTarget debugTarget) {
		fDebugTarget= debugTarget;
	}

	/*
	 * @see org.eclipse.cdt.debug.ui.infinitedisassembly.views.IDisassemblyRetrieval#asyncGetDisassembly(java.math.BigInteger, java.math.BigInteger, java.lang.String, int, org.eclipse.cdt.debug.ui.infinitedisassembly.views.IDisassemblyRetrieval.DisassemblyRequest)
	 */
	public void asyncGetDisassembly(final BigInteger startAddress, final BigInteger endAddress, final String file, final int lineNumber, final int lines, final boolean mixed, final DisassemblyRequest disassemblyRequest) {
		Runnable op= new Runnable() {
			public void run() {
				ICDITarget cdiTarget= (ICDITarget) fDebugTarget.getAdapter(ICDITarget.class);
				try {
					ICDIMixedInstruction[] mixedInstructions= null;
					ICDIInstruction[] asmInstructions= null;
					if (file != null) {
						if (mixed) {
							mixedInstructions= cdiTarget.getMixedInstructions(file, lineNumber, lines);
						} else {
							asmInstructions= cdiTarget.getInstructions(file, lineNumber, lines);
						}
					}
					else if (startAddress != null) {
						if (mixed) {
							mixedInstructions= cdiTarget.getMixedInstructions(startAddress, endAddress);
						}
						if (mixedInstructions == null || mixedInstructions.length == 0) {
							mixedInstructions= null;
							asmInstructions= cdiTarget.getInstructions(startAddress, endAddress);
						} else if (mixedInstructions.length == 1 && mixedInstructions[0].getInstructions().length == 0) {
							mixedInstructions= null;
							asmInstructions= cdiTarget.getInstructions(startAddress, endAddress);
						}
					}
					if (mixedInstructions != null) {
						IDisassemblyBlock block= DisassemblyBlock.create(fDebugTarget.getDisassembly(), mixedInstructions);
						disassemblyRequest.setDisassemblyBlock(block);
					} else if (asmInstructions != null) {
						IDisassemblyBlock block= DisassemblyBlock.create(fDebugTarget.getDisassembly(), asmInstructions);
						disassemblyRequest.setDisassemblyBlock(block);
					}
				} catch (CDIException exc) {
					disassemblyRequest.setStatus(new Status(IStatus.ERROR, CDebugUIPlugin.getUniqueIdentifier(), exc.getDetailMessage(), exc));
				} catch (DebugException exc) {
					disassemblyRequest.setStatus(new Status(IStatus.ERROR, CDebugUIPlugin.getUniqueIdentifier(), exc.getMessage(), exc));
				} finally {
					disassemblyRequest.done();
				}

			}
		};
		Display.getDefault().asyncExec(op);
	}

	/*
	 * @see org.eclipse.cdt.debug.ui.infinitedisassembly.views.IDisassemblyRetrieval#asyncGetFrameAddress(org.eclipse.debug.core.model.IStackFrame, org.eclipse.cdt.debug.ui.infinitedisassembly.views.IDisassemblyRetrieval.AddressRequest)
	 */
	public void asyncGetFrameAddress(final IStackFrame stackFrame, final AddressRequest addressRequest) {
		Runnable op= new Runnable() {
			public void run() {
				if (stackFrame instanceof ICStackFrame) {
					IAddress address = ((ICStackFrame)stackFrame).getAddress();
					if (address != null ) {
						addressRequest.setAddress(address.getValue());
					} else {
						addressRequest.setStatus(new Status(IStatus.ERROR, CDebugUIPlugin.getUniqueIdentifier(), "Internal error: Cannot retrieve frame address")); //$NON-NLS-1$
					}
				} else {
					addressRequest.cancel();
				}
				addressRequest.done();
			}
		};
		Display.getDefault().asyncExec(op);
	}
}
