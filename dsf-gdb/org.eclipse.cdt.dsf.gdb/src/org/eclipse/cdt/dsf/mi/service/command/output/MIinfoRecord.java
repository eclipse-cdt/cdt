/*******************************************************************************
 * Copyright (c) 2015 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - Added Reverse Debugging BTrace support
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;

import org.eclipse.cdt.debug.core.model.IChangeReverseMethodHandler.ReverseTraceMethod;
import org.eclipse.cdt.dsf.mi.service.command.output.MIConsoleStreamOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOOBRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIStreamRecord;

/**
 * @since 5.0
 */
public class MIinfoRecord extends MIInfo {

	private ReverseTraceMethod fReverseMethod;
	
	public MIinfoRecord(MIOutput record) {
		super(record);
		parse();
	}

	protected void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIOOBRecord[] records = out.getMIOOBRecords();
			StringBuilder builder = new StringBuilder();
			for(MIOOBRecord rec : records) {
                if (rec instanceof MIConsoleStreamOutput) {
                    MIStreamRecord o = (MIStreamRecord)rec;
                    builder.append(o.getString());
                }
			}
			parseReverseMethod(builder.toString());
		}
	}

	protected void parseReverseMethod(String output) {
		if (output.toString().contains("Processor")) //$NON-NLS-1$
			fReverseMethod = ReverseTraceMethod.PROCESSOR_TRACE;
    	else if (output.toString().contains("Branch")) //$NON-NLS-1$
    		fReverseMethod = ReverseTraceMethod.BRANCH_TRACE;
    	else
    		fReverseMethod = ReverseTraceMethod.FULL_TRACE;
	}

	public ReverseTraceMethod getReverseMethod() {
		return fReverseMethod;
	}
}
