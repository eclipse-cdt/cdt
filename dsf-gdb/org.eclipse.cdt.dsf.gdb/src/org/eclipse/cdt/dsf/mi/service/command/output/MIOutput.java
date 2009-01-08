/*******************************************************************************
 * Copyright (c) 2000, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *     Ericsson				- Modified for additional features in DSF Reference Implementation 
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

/**
 * GDB/MI response.
 */
public class MIOutput {

    private final MIResultRecord rr;
    private final MIOOBRecord[] oobs;

    public MIOutput() {
        this(null, null);
    }

    public MIOutput(MIOOBRecord oob) {
        this(null, new MIOOBRecord[] { oob });
    }
    
    

    public MIOutput(MIResultRecord rr, MIOOBRecord[] oobs) {
        this.rr = rr;
        this.oobs = oobs;
    }
    
    public MIResultRecord getMIResultRecord() {
        return rr;
    }

    public MIOOBRecord[] getMIOOBRecords() {
        return oobs;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < oobs.length; i++) {
            buffer.append(oobs[i].toString());
        }
        if (rr != null) {
            buffer.append(rr.toString());
        }
        return buffer.toString();
    }
}
