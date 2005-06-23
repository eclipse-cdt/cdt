/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.output;

import java.util.ArrayList;
import java.util.List;


/**
 * GDB/MI stack list arguments parsing.
 */
public class MIStackListArgumentsInfo extends MIInfo {

	MIFrame[] frames;

	public MIStackListArgumentsInfo(MIOutput out) {
		super(out);
	}

	public MIFrame[] getMIFrames() {
		if (frames == null) {
			parse();
		}
		return frames;
	}

	void parse() {
		List aList = new ArrayList(1);
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results =  rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("stack-args")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MIList) {
							parseStack((MIList)val, aList);
						}
					}
				}
			}
		}
		frames = (MIFrame[])aList.toArray(new MIFrame[aList.size()]);
	}

	void parseStack(MIList miList, List aList) {
		MIResult[] results = miList.getMIResults();
		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			if (var.equals("frame")) { //$NON-NLS-1$
				MIValue value = results[i].getMIValue();
				if (value instanceof MITuple) {
					aList.add (new MIFrame((MITuple)value));
				}
			}
		}
	}
}
