/*******************************************************************************
 * Copyright (c) 2021 Intel Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.service;

/**
 * Extension interface for instructions with raw Opcodes
 * <p>
 * Implementers must extend {@link AbstractInstruction} instead of implementing
 * this interface directly.
 * </p>
 *
 * @since 2.11
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IInstructionWithRawOpcode extends IInstruction {

	/**
	 * @return The raw opcode of the instruction as <code>String</code>, the instruction
	 * bytes are separated by space. If there is no opcode, <code>null</code> is returned.
	 */
	String getRawOpcode();
}