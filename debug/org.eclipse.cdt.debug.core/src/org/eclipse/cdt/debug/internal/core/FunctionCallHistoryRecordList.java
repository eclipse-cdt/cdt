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
 *     Trande - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.model.IFunctionCallHistoryRecord;
import org.eclipse.cdt.debug.core.model.IFunctionCallHistoryRecordList;

public class FunctionCallHistoryRecordList implements IFunctionCallHistoryRecordList {

	private List<IFunctionCallHistoryRecord> records = new ArrayList<>();

	@Override
	public void clear() {
		records.clear();
	}

	@Override
	public List<IFunctionCallHistoryRecord> getFunctionCallHistoryRecordList() {
		return records;
	}

	@Override
	public int addFunctionCallHistoryRecord(IFunctionCallHistoryRecord record) {
		if (records == null)
			records = new ArrayList<>();
		records.add(record);
		return records.size();
	}

	@Override
	public int addFunctionCallHistoryRecordList(List<IFunctionCallHistoryRecord> records) {
		if (records == null)
			records = new ArrayList<>();
		this.records.addAll(records);
		return this.records.size();
	}

}
