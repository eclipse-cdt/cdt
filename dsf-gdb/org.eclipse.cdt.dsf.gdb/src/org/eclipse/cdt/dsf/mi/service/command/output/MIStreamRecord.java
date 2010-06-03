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

/**
 * GDB/MI stream record response.
 */
public abstract class MIStreamRecord extends MIOOBRecord {

    String cstring = ""; //$NON-NLS-1$

    public String getCString() {
        return cstring;
    } 

    public void setCString(String str) {
        cstring = str;
    } 

    public String getString () {
        return MIConst.getString(getCString());
    }

    @Override
    public String toString() {
             if (this instanceof MIConsoleStreamOutput) { return "~\"" + cstring + "\"\n"; } //$NON-NLS-1$ //$NON-NLS-2$
        else if (this instanceof MITargetStreamOutput)  { return "@\"" + cstring + "\"\n"; } //$NON-NLS-1$ //$NON-NLS-2$
        else if (this instanceof MILogStreamOutput)     { return "&\"" + cstring + "\"\n"; } //$NON-NLS-1$ //$NON-NLS-2$
        else                                               { return  "\"" + cstring + "\"\n"; } //$NON-NLS-1$ //$NON-NLS-2$
    }
}
