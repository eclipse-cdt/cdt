/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

import java.util.ArrayList;
import java.util.List;


/**
 * GDB/MI stack list arguments parsing.
 */
public class MIStackListArgumentsInfo extends MIInfo {

    MIFrame[] frames;

    public MIStackListArgumentsInfo(MIOutput out) {
        super(out);
        frames = null;
        List<MIFrame> aList = new ArrayList<MIFrame>(1);
        if (isDone()) {
            MIResultRecord rr = out.getMIResultRecord();
            if (rr != null) {
                MIResult[] results =  rr.getMIResults();
                for (int i = 0; i < results.length; i++) {
                    String var = results[i].getVariable();
                    if (var.equals("stack-args")) { //$NON-NLS-1$
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
        frames = aList.toArray(new MIFrame[aList.size()]);
    }

    public MIFrame[] getMIFrames() {
        return frames;
    }

    private void parseStack(MIList miList, List<MIFrame> aList) {
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
    
    private void parseStack(MITuple miTuple, List<MIFrame> aList) {
        MIResult[] results = miTuple.getMIResults();
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
