/*******************************************************************************
	* Copyright (c) 2021 Trande UG.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Trande UG - Added function Call history support
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model;

/**
* Interface for function history record entry
* @since 8.7
*/
public interface IFunctionCallHistoryRecord {

	public int getTimestamp();

	public int getId();

	public int getLevel();

	public String getFunctionName();

	public int getStartInstructionId();

	public int getEndInsctructionId();

	public String getSource();

}
