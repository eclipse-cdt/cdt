/*******************************************************************************
 * Copyright (c) 2000, 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *     Jens Elmenthaler (Advantest) - Fix empty names for functions in anonymous namespaces (bug 341336)
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;

/**
 * GDB/MI Frame tuple parsing.
 */
public class MIFrame {

    int level;
    String addr;
    String func = ""; //$NON-NLS-1$
    String file = ""; //$NON-NLS-1$
    // since gdb 6.4
    String fullname = ""; //$NON-NLS-1$
    int line;
    MIArg[] args = new MIArg[0];

    public MIFrame(MITuple tuple) {
        parse(tuple);
    }

    public MIArg[] getArgs() {
        return args;
    }

    public String getFile() {
        String fname = getFullname();
        return ( fname.length() != 0 ) ? fname : file;
    }

    public String getFullname() {
        return fullname;
    }

    public String getFunction() {
        return func;
    }

    public int getLine() {
        return line;
    }

    public String getAddress() {
        return addr;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("level=\"" + level + "\"");  //$NON-NLS-1$//$NON-NLS-2$
        buffer.append(",addr=\"" + addr + "\"");  //$NON-NLS-1$//$NON-NLS-2$
        buffer.append(",func=\"" + func + "\"");  //$NON-NLS-1$//$NON-NLS-2$
        buffer.append(",file=\"" + file + "\"");  //$NON-NLS-1$//$NON-NLS-2$
        buffer.append(",line=\"").append(line).append('"'); //$NON-NLS-1$
        buffer.append(",args=["); //$NON-NLS-1$
        for (int i = 0; i < args.length; i++) {
            if (i != 0) {
                buffer.append(',');
            }
            buffer.append("{name=\"" + args[i].getName() + "\"");//$NON-NLS-1$//$NON-NLS-2$
            buffer.append(",value=\"" + args[i].getValue() + "\"}");//$NON-NLS-1$//$NON-NLS-2$
        }
        buffer.append(']');
        return buffer.toString();
    }

    void parse(MITuple tuple) {
        MIResult[] results = tuple.getMIResults();
        for (int i = 0; i < results.length; i++) {
            String var = results[i].getVariable();
            MIValue value = results[i].getMIValue();
            String str = ""; //$NON-NLS-1$
            if (value != null && value instanceof MIConst) {
                str = ((MIConst)value).getCString();
            }

            if (var.equals("level")) { //$NON-NLS-1$
                try {
                    level = Integer.parseInt(str.trim());
                } catch (NumberFormatException e) {
                }
            } else if (var.equals("addr")) { //$NON-NLS-1$
                try {
                    addr = str.trim();
                    if ( str.equals( "<unavailable>" ) ) //$NON-NLS-1$
                    	addr = ""; //$NON-NLS-1$
                } catch (NumberFormatException e) {
                }
            } else if (var.equals("func")) { //$NON-NLS-1$
                func = null;
                if ( str != null ) {
                    str = str.trim();
                    if ( str.equals( "??" ) ) //$NON-NLS-1$
                        func = ""; //$NON-NLS-1$
                    else
                    {
						func = str;
						// In some situations gdb returns the function names that include parameter types.
						// To make the presentation consistent truncate the parameters. PR 46592
						// However PR180059: only cut it if it is last brackets represent parameters,
						// because gdb can return: func="(anonymous namespace)::func2((anonymous namespace)::Test*)"
						int closing = str.lastIndexOf(')');
						if (closing == str.length() - 1) {
							int end = getMatchingBracketIndex(str, closing - 1);
							if (end >= 0) {
								func = str.substring(0, end);
							}
						}
                    }
                }
            } else if (var.equals("file")) { //$NON-NLS-1$
                file = str;
            } else if (var.equals("fullname")) { //$NON-NLS-1$
                fullname = str;
            } else if (var.equals("line")) { //$NON-NLS-1$
                try {
                    line = Integer.parseInt(str.trim());
                } catch (NumberFormatException e) {
                }
            } else if (var.equals("args")) { //$NON-NLS-1$
                if (value instanceof MIList) {
                    args = MIArg.getMIArgs((MIList)value);
                } else if (value instanceof MITuple) {
                    args = MIArg.getMIArgs((MITuple)value);
                }
            }
        }
    }

	private int getMatchingBracketIndex(String str, int end) {
	    int depth = 1;
	    for (;end>=0;end--) {
	    	int c = str.charAt(end);
	    	if (c=='(') {
	    		depth--;
	    		if (depth==0) break;
	    	} else if (c==')') 
	    		depth++;
	    }
	    return end;
    }
}
