/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Freescale Semiconductor - refactoring
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.disassembly.dsf;

import java.math.BigInteger;

import org.eclipse.ui.IWorkbenchPartSite;

/**
 * Implementations of {@link IDisassemblyBackend} are given this access back
 * into the part (editor/view) that created them. Except where noted, methods
 * must be invoked on the GUI thread. A disassembly backend has to call back
 * into the editor/view to carry out its duties.
 *
 * <p>
 * This interface was born of the refactoring which allowed the DSF Disassembly
 * view to work with both DSF and CDI. Before that, the functionality of
 * IDisasssemblyBackend was built into DisassemblyPart and thus there was no
 * need for this interface. This interface merely exposes model (DSF/CDI)
 * agnostic access to DisassemblyPart. It exposes methods that have been in
 * DisassemblyPart all along. Documentation for those methods were sparse, and
 * thus this interface is likewise. See the DisassemblyPart for any available
 * documentation.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IDisassemblyPartCallback {
	void gotoFrame(int frame);

	void gotoFrameIfActive(int frame);

	void updateVisibleArea();

	void updateInvalidSource();

	void retrieveDisassembly(final BigInteger startAddress, BigInteger endAddress, final int linesHint, boolean mixed,
			boolean ignoreFile);

	void insertError(BigInteger address, String message);

	int getAddressSize();

	void addressSizeChanged(int addressSize);

	AddressRangePosition getPositionOfAddress(BigInteger address);

	void gotoFrame(int frame, BigInteger address);

	void updatePC(BigInteger pc);

	void doPending();

	void doScrollLocked(final Runnable doit);

	void lockScroller();

	void unlockScroller();

	void insertSource(AddressRangePosition pos);

	AddressRangePosition insertSource(AddressRangePosition pos, BigInteger address, final String file, int lineNumber);

	AddressRangePosition insertSource(AddressRangePosition pos, BigInteger address, final String file, int firstLine,
			int lastLine);

	void setUpdatePending(boolean pending);

	boolean getUpdatePending();

	void setGotoAddressPending(BigInteger address);

	BigInteger getGotoAddressPending();

	IDisassemblyDocument getDocument();

	Object getStorageForFile(String file);

	void gotoAddress(BigInteger address);

	IWorkbenchPartSite getSite();

	boolean hasViewer();

	/** Thread-safe */
	void handleTargetSuspended();

	/** Thread-safe */
	void handleTargetResumed();

	/** Thread-safe */
	void handleTargetEnded();

	/** Thread-safe */
	void asyncExec(Runnable runnable);
}
