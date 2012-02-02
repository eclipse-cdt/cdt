/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *     Mathias Kunter       - Modified for implementation of Bug 307311. Please
 *                            find further implementation details within the
 *                            MIStringHandler class.
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

/**
 * GDB/MI const value represents a ios-c string.
 */
public class MIConst extends MIValue {
    
    private String cstring = ""; //$NON-NLS-1$
    
    public String getCString() {
        return cstring;
    }
    
    public void setCString(String str) {
        cstring = str;
    }
    
    /**
     * Translates the C string value into a string which is suitable for display to a human.
     * @return The translated string.
     */
    public String getString() {
        return MIStringHandler.translateCString(cstring, true);
    }
    
    public static String getString(String str) {
    	return MIStringHandler.translateCString(str, true);
    }
    
    @Override
    public String toString() {
        return getCString();
    }
}
