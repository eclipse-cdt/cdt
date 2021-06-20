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
 *     Trande UG - Added Functions Call History support
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;

import org.eclipse.cdt.debug.internal.core.FunctionCallHistoryRecord;
import org.eclipse.cdt.debug.internal.core.FunctionCallHistoryRecordList;

/**
 * 'info record' returns the selected reverse trace method.
 *
 * sample output:
 *
 * (gdb) record function-call-history /ilc
 * ~ "id"	"tab" "2x level spaces" "function name" "tab" "inst" "start,stop" "tab" "at" "source"
 *
 * @since 6.5
 */

public class CLIFunctionsCallHistoryInfo extends MIInfo {

	private FunctionCallHistoryRecordList fRecordList = new FunctionCallHistoryRecordList();

	public CLIFunctionsCallHistoryInfo(MIOutput record) {
		super(record);
		parse();
	}

	protected void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIOOBRecord[] records = out.getMIOOBRecords();
			for (MIOOBRecord rec : records) {
				if (rec instanceof MIConsoleStreamOutput) {
					MIStreamRecord o = (MIStreamRecord) rec;
					String str = o.getCString();
					FunctionCallHistoryRecord record = parseFunctionsCallHistoryRecord(str);
					fRecordList.addFunctionCallHistoryRecord(record);
				}
			}
		}
	}

	protected FunctionCallHistoryRecord parseFunctionsCallHistoryRecord(String output) {
		if (output == null)
			return null;
		String[] str = output.split("\\\\t");
		if (str.length < 2)
			return null;
		FunctionCallHistoryRecord record = new FunctionCallHistoryRecord();
		try {
			record.id = Integer.parseInt(str[0]);
			record.timestamp = record.id;
			int nspaces = 0;
			for (int i = 0; i < str[1].length(); ++i) {
				if (str[1].charAt(i) == ' ')
					nspaces++;
				else
					break;
			}
			if (nspaces % 2 == 1)
				return null;
			record.level = nspaces / 2;
			record.functionName = str[1].trim();
			if (str.length < 3)
				return record;
			if (!str[2].contains("inst"))
				return null;
			String[] instStr = str[2].split(" ");
			if (instStr.length < 2)
				return null;
			instStr = instStr[1].split(",");
			if (instStr.length != 2)
				return null;
			record.startInstructionId = Integer.parseInt(instStr[0]);
			record.endInstructionId = Integer.parseInt(instStr[1]);
			if (str.length < 4)
				return record;
			instStr = str[3].split(" ");
			if (!instStr[0].contains("at"))
				return null;
			if (instStr[1].length() > 0)
				record.source = instStr[1];
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return record;
	}

	public FunctionCallHistoryRecordList getFunctionCallHistoryRecordList() {
		return fRecordList;
	}
}