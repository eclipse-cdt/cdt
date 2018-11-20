/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

/**
 * GDB/MI stack list locals parsing.
 * -stack-list-locals 1
 * ^done,locals=[{name="p",value="0x8048600 \"ghislaine\""},{name="buf",value="\"'\", 'x' <repeats 24 times>, \"i,xxxxxxxxx\", 'a' <repeats 24 times>"},{name="buf2",value="\"\\\"?'\\\\()~\""},{name="buf3",value="\"alain\""},{name="buf4",value="\"\\t\\t\\n\\f\\r\""},{name="i",value="0"}]
 *
 * On MacOS X 10.4 this returns a tuple:
 * ^done,locals={{name="p",value="0x8048600 \"ghislaine\""},{name="buf",value="\"'\", 'x' <repeats 24 times>, \"i,xxxxxxxxx\", 'a' <repeats 24 times>"},{name="buf2",value="\"\\\"?'\\\\()~\""},{name="buf3",value="\"alain\""},{name="buf4",value="\"\\t\\t\\n\\f\\r\""},{name="i",value="0"}}
 */
public class MIStackListLocalsInfo extends MIInfo {

	MIArg[] locals;

	public MIStackListLocalsInfo(MIOutput out) {
		super(out);
		locals = null;
		if (isDone()) {
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results = rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("locals")) { //$NON-NLS-1$
						MIValue value = results[i].getMIValue();
						if (value instanceof MIList) {
							locals = MIArg.getMIArgs((MIList) value);
						} else if (value instanceof MITuple) {
							locals = MIArg.getMIArgs((MITuple) value);
						}
					}
				}
			}
		}
		if (locals == null) {
			locals = new MIArg[0];
		}
	}

	public MIArg[] getLocals() {
		return locals;
	}
}
