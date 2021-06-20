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

import java.util.List;

/**
 * Interface for a list of function call history records
 * @since 8.7
 */
public interface IFunctionCallHistoryRecordList {

	public void clear();

	public List<IFunctionCallHistoryRecord> getFunctionCallHistoryRecordList();

	public int addFunctionCallHistoryRecord(IFunctionCallHistoryRecord record);

	public int addFunctionCallHistoryRecordList(List<IFunctionCallHistoryRecord> records);
}
