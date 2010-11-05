/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
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
	private boolean isDynamic = false;
	private boolean hasMore = false;
	private MIDisplayHint displayHint = MIDisplayHint.NONE;
	
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
					} else if (var.equals("dynamic") && str.trim().equals("1")) { //$NON-NLS-1$ //$NON-NLS-2$
						isDynamic = true;
					} else if (var.equals("has_more") && str.trim().equals("1")) { //$NON-NLS-1$ //$NON-NLS-2$
						hasMore = true;
                    } else if (var.equals("displayhint")) { //$NON-NLS-1$
                    	displayHint = new MIDisplayHint(str);
                    }
                }
            }
        }
    }
    
    public String getType()
    {
    	return type;
    }
    
	/**
	 * @return Whether the created variable's value and children are provided
	 *         by a pretty printer.
	 *         
	 * @since 4.0
	 */
	public boolean isDynamic() {
		return isDynamic;
	}

	/**
	 * @return The number of children. If {@link #isDynamic()} returns true,
	 *         the returned value only reflects the number of children currently
	 *         fetched by gdb. Check {@link #hasMore()} in order to find out
	 *         whether the are more children. 
	 */
    public int getNumChildren()
    {
    	return numChild;
    }
    
	/**
	 * @return For dynamic varobjs ({@link #isDynamic() returns true} this
	 *         method returns whether there are children in addition to the
	 *         currently fetched, i.e. whether there are more children than
	 *         {@link #getNumChildren()} returns.
	 *         
	 * @since 4.0
	 */
	public boolean hasMore() {
		return hasMore;
	}

    public String getName()
    {
    	return name;
    }
    
    public String getValue()
    {
    	return value;
    }
    
	/**
	 * @return Whether the underlying value conceptually represents a string,
	 *         array, or map.
	 *         
	 * @since 4.0
	 */
    public MIDisplayHint getDisplayHint() {
    	return displayHint;
    }

    public MIVar getMIVar() {
        if (child == null) {
			child = new MIVar(name, isDynamic, numChild, hasMore, type, displayHint);
        }
        return child;
    }
}
