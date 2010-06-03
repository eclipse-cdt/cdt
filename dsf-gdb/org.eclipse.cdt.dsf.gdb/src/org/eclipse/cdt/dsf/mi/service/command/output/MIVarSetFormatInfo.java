/*******************************************************************************
 * Copyright (c) 2008, 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson               - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;

/**
 * GDB/MI var-set-format
 */
public class MIVarSetFormatInfo extends MIInfo {

    String value = null;

    public MIVarSetFormatInfo(MIOutput record) {
    	super(record);
    	if (isDone()) {
    		MIOutput out = getMIOutput();
    		MIResultRecord rr = out.getMIResultRecord();
    		if (rr != null) {
    			MIResult[] results =  rr.getMIResults();
    			for (int i = 0; i < results.length; i++) {
    				String var = results[i].getVariable();
    				if (var.equals("value")) { //$NON-NLS-1$
    					MIValue val = results[i].getMIValue();
    					if (val instanceof MIConst) {
    						value = ((MIConst)val).getCString();
    					}
    				}
    			}
    		}
    	}
    }

    public String getValue () {
    	return value;
    }
}
