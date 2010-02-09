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
 *     Ericsson             - Modified for the breakpoint service
 *     Ericsson             - Added Tracepoint support (284286)
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

/**
 * Contain info about the GDB/MI breakpoint.
 * 
 * (gdb)
 * -break-insert main
 * ^done,bkpt={number="1",type="breakpoint",disp="keep",enabled="y",addr="0x0804846b",func="main",file="hello.c",line="4",times="0"}
 * (gdb)
 * -break-insert -t main
 * ^done,bkpt={number="2",type="breakpoint",disp="del",enabled="y",addr="0x0804846b",func="main",file="hello.c",line="4",times="0"}
 * (gdb)
 * -break-insert -c 1 main
 * ^done,bkpt={number="3",type="breakpoint",disp="keep",enabled="y",addr="0x0804846b",func="main",file="hello.c",line="4",cond="1",times="0"}
 * (gdb)
 * -break-insert -h main
 * ^done,bkpt={number="4",type="hw breakpoint",disp="keep",enabled="y",addr="0x0804846b",func="main",file="hello.c",line="4",times="0"}
 * (gdb)
 * -break-insert -p 0 main
 * ^done,bkpt={number="5",type="breakpoint",disp="keep",enabled="y",addr="0x0804846b",func="main",file="hello.c",line="4",thread="0",thread="0",times="0"}
 * (gdb)
 * -break-watch -a p
 * ^done,hw-awpt={number="2",exp="p"}
 * (gdb)
 * -break-watch -r p
 * ^done,hw-rwpt={number="4",exp="p"}
 * (gdb)
 * -break-watch p
 * ^done,wpt={number="6",exp="p"}
 * (gdb)
 *
 * Tracepoints:
 * bkpt={number="5",type="tracepoint",disp="keep",enabled="y",addr="0x0804846b",func="main",file="hello.c",line="4",thread="0",thread="0",times="0"}
 * bkpt={number="1",type="tracepoint",disp="keep",enabled="y",addr="0x0041bca0",func="main",file="hello.c",line="4",times="0",pass="4",original-location="hello.c:4"},
 * bkpt={number="5",type="fast tracepoint",disp="keep",enabled="y",addr="0x0804852d",func="testTracepoints()",file="TracepointTestApp.cc",fullname="/local/src/TracepointTestApp.cc",line="84",times="0",original-location="TracepointTestApp.cc:84"}
 * */
public class MIBreakpoint  {

    int     number   = -1;
    String  type     = "";  //$NON-NLS-1$
    String  disp     = "";  //$NON-NLS-1$
    boolean enabled  = false;
    String  address  = "";  //$NON-NLS-1$
    String  func     = "";  //$NON-NLS-1$
    String  fullName = "";  //$NON-NLS-1$
    String  file     = "";  //$NON-NLS-1$
    int     line     = -1;
    String  cond     = "";  //$NON-NLS-1$
    int     times    = 0;
    String  exp      = "";  //$NON-NLS-1$
    String  threadId = "0"; //$NON-NLS-1$
    int     ignore   = 0;
    String  commands = ""; //$NON-NLS-1$
    
    // For tracepoints
    int     passcount = 0;

    boolean isWpt  = false;
    boolean isAWpt = false;
    boolean isRWpt = false;
    boolean isWWpt = false;
    boolean isHdw  = false;

    // Indicate if we are dealing with a tracepoint. 
    // (if its a fast or slow tracepoint can be known through the 'type' field)
    boolean isTpt = false;
    
    public MIBreakpoint() {
	}

    public MIBreakpoint(MIBreakpoint other) {
        number   = other.number;
        type     = new String(other.type);
        disp     = new String(other.disp);
        enabled  = other.enabled;
        address  = new String(other.address);
        func     = new String(other.func);
        fullName = new String(other.fullName);
        file     = new String(other.file);
        line     = other.line;
        cond     = new String(other.cond);
        times    = other.times;
        exp      = new String(other.exp);
        threadId = new String(other.threadId);
        ignore   = other.ignore;
        commands = other.commands;
        passcount= other.passcount;
        isWpt    = other.isWpt;
        isAWpt   = other.isAWpt;
        isRWpt   = other.isRWpt;
        isWWpt   = other.isWWpt;
        isHdw    = other.isHdw;
        isTpt    = other.isTpt;
	}

    public MIBreakpoint(MITuple tuple) {
        parse(tuple);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Properties getters 
    ///////////////////////////////////////////////////////////////////////////

	public int getNumber() {
        return number;
    }

    public String getType() {
        return type;
    }

    public String getDisposition() {
        return disp;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean e) {
        enabled = e;
    }

    public String getAddress() {
        return address;
    }

    public String getFunction() {
        return func;
    }

    public String getFile() {
        return file;
    }

    public String getFullName() {
        return fullName;
    }

    public int getLine() {
        return line;
    }

    public String getCondition() {
        return cond;
    }

    public void setCondition(String condition) {
        cond = condition;
    }

    public int getIgnoreCount() {
        return ignore;
    }

    public void setIgnoreCount(int ignoreCount) {
        ignore = ignoreCount;
    }

    public String getThreadId() {
        return threadId;
    }

    public int getTimes() {
        return times;
    }

    public String getExpression() {
        return exp;
    }

    public boolean isTemporary() {
        return getDisposition().equals("del"); //$NON-NLS-1$
    }

    /**
     * Will return true if we are dealing with a hardware breakpoint.
     * Note that this method will return false for tracepoint, even
     * if it is a fast tracepoint.
     */
    public boolean isHardware() {
        return isHdw;
    }

	public void setHardware(boolean b) {
		isWpt = b;
		isHdw = b;		
	}

    public boolean isWatchpoint() {
        return isWpt;
    }

	public void isWatchpoint(boolean b) {
		isWpt = b;
	}

    public boolean isAccessWatchpoint() {
        return isAWpt;
    }

	public void setAccessWatchpoint(boolean b) {
		isWpt = b;
		isAWpt = b;		
	}

    public boolean isReadWatchpoint() {
        return isRWpt;
    }

	public void setReadWatchpoint(boolean b) {
		isWpt = b;
		isRWpt = b;
	}

    public boolean isWriteWatchpoint() {
        return isWWpt;
    }

	public void setWriteWatchpoint(boolean b) {
		isWpt = b;
		isWWpt = b;
	}

    /**
     * Return whether this breakpoint is actually a tracepoint.
     * This method will return true for both fast and slow tracepoints.
     * To know of fast vs slow tracepoint use {@link getType()} and look
     * for "tracepoint" or "fast tracepoint"
     * 
	 * @since 3.0
	 */
    public boolean isTracepoint() {
        return isTpt;
    }

    /**
     * Returns the passcount of a tracepoint.  Will return 0 if this
     * breakpoint is not a tracepoint.
     * 
	 * @since 3.0
	 */
    public int getPassCount() {
        return passcount;
    }

    /**
     * Set the passcount of a tracepoint.  Will not do anything if
     * this breakpoint is not a tracepoint.
	 * @since 3.0
	 */
    public void setPassCount(int count) {
    	if (isTpt == false) return;
        passcount = count;
    }
    
    /**
     * Return the commands associated with this breakpoint (or tracepoint)
     * 
	 * @since 3.0
	 */
    public String getCommands() {
        return commands;
    }

    /**
     * Sets the commands associated with this breakpoint (or tracepoint)
     * 
	 * @since 3.0
	 */
    public void setCommands(String cmds) {
        commands = cmds;
    }
    
    // Parse the result string
    void parse(MITuple tuple) {
        MIResult[] results = tuple.getMIResults();
        for (int i = 0; i < results.length; i++) {
            String var = results[i].getVariable();
            MIValue value = results[i].getMIValue();
            String str = ""; //$NON-NLS-1$
            if (value != null && value instanceof MIConst) {
                str = ((MIConst)value).getCString();
            }

            if (var.equals("number")) { //$NON-NLS-1$
                try {
                    number = Integer.parseInt(str.trim());
                } catch (NumberFormatException e) {
                }
            } else if (var.equals("type")) { //$NON-NLS-1$
                type = str;
                //type="hw watchpoint"
                if (type.startsWith("hw")) { //$NON-NLS-1$
                    isHdw = true;
                    isWWpt = true;
                    isWpt = true;
                }
                //type="acc watchpoint"
                if (type.startsWith("acc")) { //$NON-NLS-1$
                    isAWpt = true;
                    isWpt = true;
                }
                //type="read watchpoint"
                if (type.startsWith("read")) { //$NON-NLS-1$
                    isRWpt = true;
                    isWpt = true;
                }
                if (type.startsWith("tracepoint") ||  //$NON-NLS-1$
                    type.startsWith("fast tracepoint")) { //$NON-NLS-1$
                	isTpt = true;
                }
                // type="breakpoint"
                // default ok.
            } else if (var.equals("disp")) { //$NON-NLS-1$
                disp = str;
            } else if (var.equals("enabled")) { //$NON-NLS-1$
                enabled = str.equals("y"); //$NON-NLS-1$
            } else if (var.equals("addr")) { //$NON-NLS-1$
                try {
                    address = str.trim();
                } catch (NumberFormatException e) {
                }
            } else if (var.equals("func")) { //$NON-NLS-1$
                func = str;
            } else if (var.equals("file")) { //$NON-NLS-1$
                file = str;
            } else if (var.equals("fullname")) { //$NON-NLS-1$
                fullName = str;
            } else if (var.equals("thread")) { //$NON-NLS-1$
                threadId = str;
            } else if (var.equals("line")) { //$NON-NLS-1$
                try {
                    line = Integer.parseInt(str.trim());
                } catch (NumberFormatException e) {
                }
            } else if (var.equals("times")) { //$NON-NLS-1$
                try {
                    times = Integer.parseInt(str.trim());
                } catch (NumberFormatException e) {
                }
            } else if (var.equals("what") || var.equals("exp")) { //$NON-NLS-1$ //$NON-NLS-2$
                exp = str;
            } else if (var.equals("ignore")) { //$NON-NLS-1$
                try {
                    ignore = Integer.parseInt(str.trim());
                } catch (NumberFormatException e) {
                }
            } else if (var.equals("pass")) { //$NON-NLS-1$
                try {
                    passcount = Integer.parseInt(str.trim());
                } catch (NumberFormatException e) {
                }
            } else if (var.equals("cond")) { //$NON-NLS-1$
                cond = str;
            }
        }
    }

}
