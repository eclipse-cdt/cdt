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
 * GDB/MI var-create.
 * -var-create "-" * a
 * ^done,name="var1",numchild="0",value="11",type="int"
 * -var-create "-" * buf
 * ^done,name="var1",numchild="6",value=[6]",type="char [6]"
 * 
 * Note that the value is returned in the output, as of GDB6.7
 */
public class MIVarCreateInfo extends MIInfo {

    String name = ""; //$NON-NLS-1$
    int numChild;
    String type = ""; //$NON-NLS-1$
    MIVar child;
    String value = null;

    public MIVarCreateInfo(MIOutput record) {
        super(record);
        if (isDone()) {
            MIOutput out = getMIOutput();
            MIResultRecord rr = out.getMIResultRecord();
            if (rr != null) {
                MIResult[] results =  rr.getMIResults();
                for (int i = 0; i < results.length; i++) {
                    String var = results[i].getVariable();
                    MIValue resultVal = results[i].getMIValue();
                    String str = ""; //$NON-NLS-1$
                    if (resultVal instanceof MIConst) {
                        str = ((MIConst)resultVal).getString();
                    }

                    if (var.equals("name")) { //$NON-NLS-1$
                        name = str;
                    } else if (var.equals("numchild")) { //$NON-NLS-1$
                        try {
                            numChild = Integer.parseInt(str.trim());
                        } catch (NumberFormatException e) {
                        }
                    } else if (var.equals("type")) { //$NON-NLS-1$
                        type = str;
                    } else if (var.equals("value")) { //$NON-NLS-1$
                        value = str;
                    }
                }
            }
        }
    }
    
    public String getType()
    {
    	return type;
    }
    
    public int getNumChildren()
    {
    	return numChild;
    }
    
    public String getName()
    {
    	return name;
    }
    
    public String getValue()
    {
    	return value;
    }

    public MIVar getMIVar() {
        if (child == null) {
            child = new MIVar(name, numChild, type);
        }
        return child;
    }
}
