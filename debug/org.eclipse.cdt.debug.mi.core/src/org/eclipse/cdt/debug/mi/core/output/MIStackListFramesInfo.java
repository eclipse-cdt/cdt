/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.output;

import java.util.ArrayList;
import java.util.List;


/**
 * GDB/MI stack list frames info.
 */
public class MIStackListFramesInfo extends MIInfo {

	MIFrame[] frames;

	public MIStackListFramesInfo(MIOutput out) {
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
					if (var.equals("stack")) {
						MIValue val = results[i].getMIValue();
						if (val instanceof MIList) {
							parseStack((MIList)val, aList);
						} else if (val instanceof MITuple) {
							parseStack((MITuple)val, aList);
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
			if (var.equals("frame")) {
				MIValue value = results[i].getMIValue();
				if (value instanceof MITuple) {
					aList.add (new MIFrame((MITuple)value));
				}
			}
		}
	}

	// Old gdb use tuple instead of a list.
	void parseStack(MITuple tuple, List aList) {
		MIResult[] results = tuple.getMIResults();
		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			if (var.equals("frame")) {
				MIValue value = results[i].getMIValue();
				if (value instanceof MITuple) {
					aList.add (new MIFrame((MITuple)value));
				}
			}
		}
	}
}
