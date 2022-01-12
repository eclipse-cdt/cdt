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

import java.util.ArrayList;
import java.util.List;

/**
 * GDB/MI stack list frames info.
 */
public class MIStackListFramesInfo extends MIInfo {

	MIFrame[] frames;

	public MIStackListFramesInfo(MIOutput out) {
		super(out);
		frames = null;
		List<MIFrame> aList = new ArrayList<>(1);
		if (isDone()) {
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results = rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("stack")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MIList) {
							parseStack((MIList) val, aList);
						} else if (val instanceof MITuple) {
							parseStack((MITuple) val, aList);
						}
					}
				}
			}
		}
		frames = aList.toArray(new MIFrame[aList.size()]);
	}

	public MIFrame[] getMIFrames() {
		return frames;
	}

	void parseStack(MIList miList, List<MIFrame> aList) {
		MIResult[] results = miList.getMIResults();
		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			if (var.equals("frame")) { //$NON-NLS-1$
				MIValue value = results[i].getMIValue();
				if (value instanceof MITuple) {
					aList.add(new MIFrame((MITuple) value));
				}
			}
		}
	}

	// Old gdb use tuple instead of a list.
	void parseStack(MITuple tuple, List<MIFrame> aList) {
		MIResult[] results = tuple.getMIResults();
		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			if (var.equals("frame")) { //$NON-NLS-1$
				MIValue value = results[i].getMIValue();
				if (value instanceof MITuple) {
					aList.add(new MIFrame((MITuple) value));
				}
			}
		}
	}
}
