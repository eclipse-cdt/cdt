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

import org.eclipse.cdt.debug.core.model.IFunctionCallHistoryRecord;

public class FunctionCallHistoryRecord implements IFunctionCallHistoryRecord {

	public int timestamp;
	public int id;
	public int level;
	public String functionName;
	public int startInstructionId;
	public int endInstructionId;
	public String source;

	@Override
	public int getTimestamp() {
		return timestamp;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public int getLevel() {
		return level;
	}

	@Override
	public String getFunctionName() {
		return functionName;
	}

	@Override
	public int getStartInstructionId() {
		return startInstructionId;
	}

	@Override
	public int getEndInsctructionId() {
		return endInstructionId;
	}

	@Override
	public String getSource() {
		return source;
	}

	@Override
	public String toString() {
		return "FunctionCallHistoryRecord [timestamp=" + timestamp + ", id=" + id + ", level=" + level
				+ ", functionName=" + functionName + ", startInstructionId=" + startInstructionId
				+ ", endInstructionId=" + endInstructionId + ", source=" + source + "]";
	}

}
