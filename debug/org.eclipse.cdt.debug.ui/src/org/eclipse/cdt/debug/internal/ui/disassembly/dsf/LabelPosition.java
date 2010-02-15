/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.disassembly.dsf;

import java.math.BigInteger;

/**
 * LabelPosition
 */
public class LabelPosition extends AddressRangePosition {

	public String fLabel;

	/**
	 * @param offset
	 * @param length
	 * @param addressOffset
	 */
	public LabelPosition(int offset, int length, BigInteger addressOffset, String label) {
		super(offset, length, addressOffset, BigInteger.ZERO);
		fLabel = label;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model.AddressRangePosition#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + "->[" + fLabel + ']'; //$NON-NLS-1$
	}
}
