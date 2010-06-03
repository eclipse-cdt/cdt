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

package org.eclipse.cdt.dsf.mi.service.command.events;

import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIConst;
import org.eclipse.cdt.dsf.mi.service.command.output.MILogStreamOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOOBRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResult;
import org.eclipse.cdt.dsf.mi.service.command.output.MIStreamRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MIValue;

/**
 * (gdb)
 * &"warning: Cannot insert breakpoint 2:\n"
 * &"Cannot access memory at address 0x8020a3\n"
 * 30^error,msg=3D"Cannot access memory at address 0x8020a3"=20
 */
@Immutable
public class MIErrorEvent extends MIStoppedEvent {

    final private String msg;
    final private String log;
    final private MIOOBRecord[] oobs;

    protected MIErrorEvent(
        IExecutionDMContext ctx, int token, MIResult[] results, MIOOBRecord[] oobs, String msg, String log) 
    {
        super(ctx, token, results, null);
        this.msg = msg;
        this.log = log;
        this.oobs = oobs;
    }

    public String getMessage() {
        return msg;
    }

    public String getLogMessage() {
        return log;
    }

    public static MIErrorEvent parse(
        IContainerDMContext containerDmc, int token, MIResult[] results, MIOOBRecord[] oobs) 
    { 
        String msg = "", log = ""; //$NON-NLS-1$ //$NON-NLS-2$
            
        if (results != null) {
            for (int i = 0; i < results.length; i++) {
                String var = results[i].getVariable();
                MIValue value = results[i].getMIValue();
                String str = ""; //$NON-NLS-1$
                if (value instanceof MIConst) {
                    str = ((MIConst)value).getString();
                }

                if (var.equals("msg")) { //$NON-NLS-1$
                    msg = str;
                }
            }
        }
        if (oobs != null) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < oobs.length; i++) {
                if (oobs[i] instanceof MILogStreamOutput) {
                    MIStreamRecord o = (MIStreamRecord)oobs[i];
                    sb.append(o.getString());
                }
            }
            log = sb.toString();
        }
        return new MIErrorEvent(containerDmc, token, results, oobs, msg, log);
    }
    
    @Override
    public String toString() {
        if (oobs != null) {
            StringBuilder builder = new StringBuilder();
            for (MIOOBRecord oob : oobs) {
                builder.append(oob.toString());
            }
            builder.append(super.toString());
            return builder.toString();
        } else {
            return super.toString();
        }
    }
}

