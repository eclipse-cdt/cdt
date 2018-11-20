/*******************************************************************************
 * Copyright (c) 2010, 2014 Wind River Systems, Inc. and others.
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
 *     William Riley (Renesas) - Add raw Opcodes parsing (Bug 357270)
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.service;

import java.math.BigInteger;

/**
 * Implementers of {@link IInstruction} should extend this abstract class
 * instead of implementing the interface directly.
 *
 * @since 2.2
 */
public abstract class AbstractInstruction implements IInstructionWithSize, IInstructionWithRawOpcodes {
	/*
	 * @see org.eclipse.cdt.dsf.debug.service.IInstructionWithSize#getSize()
	 */
	@Override
	public Integer getSize() {
		// unknown size
		return null;
	}

	/**
	 * @since 2.5
	 */
	@Override
	public BigInteger getRawOpcodes() {
		return null;
	}

}
