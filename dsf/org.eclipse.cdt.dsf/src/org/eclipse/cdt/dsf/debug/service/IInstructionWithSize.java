/*******************************************************************************
 * Copyright (c) 2010 Nokia, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nokia
 *     Wind River Systems
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.service;

/**
 * Extension interface for instructions knowing their size.
 * <p>
 * Implementers must extend {@link AbstractInstruction} instead of
 * implementing this interface directly.
 * </p>
 * @since 2.2
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IInstructionWithSize extends IInstruction {

	/**
	 * @return size of the instruction in bytes or <code>null</code> if unknown
	 */
	Integer getSize();

}
