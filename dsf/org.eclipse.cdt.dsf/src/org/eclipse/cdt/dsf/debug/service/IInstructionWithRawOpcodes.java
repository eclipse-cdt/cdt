/*******************************************************************************
 * Copyright (c) 2014 Renesas Electronics and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     William Riley (Renesas) - Bug 357270
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.service;

import java.math.BigInteger;

/**
 * Extension interface for instructions with raw Opcodes
 * <p>
 * Implementers must extend {@link AbstractInstruction} instead of implementing
 * this interface directly.
 * </p>
 *
 * @since 2.5
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IInstructionWithRawOpcodes extends IInstruction {

	/**
	 * @return The raw Opcodes of the Instruction or <code>null</code> if
	 *         unknown
	 */
	BigInteger getRawOpcodes();
}
