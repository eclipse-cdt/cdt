/*******************************************************************************
 * Copyright (c) 2008, 2009 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.debug.service;

/**
 * Represents the assembly instruction(s) corresponding to a source line
 *
 * @since 1.0
 */
public interface IMixedInstruction {

	/**
	 * @return the file name
	 */
	String getFileName();

	/**
	 * @return the line Number.
	 */
	int getLineNumber();

	/**
	 * @return the array of instruction.
	 */
	IInstruction[] getInstructions();

}
