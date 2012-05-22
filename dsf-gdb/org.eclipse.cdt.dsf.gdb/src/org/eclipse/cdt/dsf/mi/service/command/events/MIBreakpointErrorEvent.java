/*******************************************************************************
 * Copyright (c) 2012 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.events;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResult;
import org.eclipse.cdt.dsf.mi.service.command.output.MIStreamRecord;


/**
 * Gdb breakpoint warning with no error result. This can occur when
 * gdb hits an error whilst stepping temporary breakpoints.
 * @since 4.1
 *
 */
public class MIBreakpointErrorEvent extends MIErrorEvent {

	private String details;
	private int bpNum;
	private static Pattern errorPattern = Pattern.compile(".*Cannot insert breakpoint ([0-9]+).*"); //$NON-NLS-1$
	private static final int GROUP_BP_NUM = 1;

	protected MIBreakpointErrorEvent(IExecutionDMContext ctx, int token,
			MIResult[] results, String details, int bpNum) {
		super(ctx, token, results, null, details, null);
		this.details = details;
		this.bpNum = bpNum;
	}

	public String getDetails() {
		return details;
	}
	
	@Override
	public String getMessage() {
		return "Cannot insert breakpoint" + ((bpNum > 0) ? " " + bpNum : ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public static MIBreakpointErrorEvent parse(
			IExecutionDMContext containerDmc, int token, MIResult[] results) {
		String details = ""; //$NON-NLS-1$
		int bpNum = 0;
		for (MIResult r : results) {
			String resultStr = r.getMIValue().toString();
			if (bpNum == 0) {
				int bp = parseBreakpointNumber(resultStr);
				if (bp > 0) {
					bpNum = bp;
				}
			}
			details += resultStr;
		}
		return new MIBreakpointErrorEvent(containerDmc, token, results, details, bpNum);
	}
	
	private static int parseBreakpointNumber(String line) {
		line = line.replace("\n", ""); //$NON-NLS-1$ //$NON-NLS-2$
		Matcher matcher = errorPattern.matcher(line);
		if (matcher.matches()) {
			return Integer.parseInt(matcher.group(GROUP_BP_NUM));
		}
		
		return 0;
	}
	
	public static MIBreakpointErrorEvent parse (
			IExecutionDMContext containerDmc, int token, MIResult[] results,
			MIStreamRecord[] records) {
		String details = ""; //$NON-NLS-1$
		int bpNum = 0;
		
		for (MIStreamRecord streamRecord : records) {
			String recordString = streamRecord.getString();
			if (bpNum == 0) {
				int bp = parseBreakpointNumber(recordString);
				if (bp > 0) {
					bpNum = bp;
				}				
			}
			details += streamRecord.getString();
		}
		return new MIBreakpointErrorEvent(containerDmc, token, results, details, bpNum);
	}

}