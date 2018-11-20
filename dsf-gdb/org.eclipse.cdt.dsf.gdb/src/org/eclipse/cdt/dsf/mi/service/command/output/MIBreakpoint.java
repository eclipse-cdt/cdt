/*******************************************************************************
 * Copyright (c) 2000, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *     Ericsson             - Modified for the breakpoint service
 *     Ericsson             - Added Tracepoint support (284286)
 *     Abeer Bagul (Tensilica) - Differentiate between hw breakpoint and watchpoint
 *     Marc Khouzam (Ericsson) - Add 'thread-group' field (bug 360735)
 *     Marc Khouzam (Ericsson) - Support for dynamic printf (bug 400628)
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.TracepointActionManager;
import org.eclipse.cdt.dsf.mi.service.MIBreakpoints;

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
 *
 * Pending breakpoint
 * -break-insert -f NotLoadedLibrary.c:26
 * &"No source file named NotLoadedLibrary.c.\n"
 * ^done,bkpt={number="9",type="breakpoint",disp="keep",enabled="y",addr="<PENDING>",pending="NotLoadedLibrary.c:26",times="0",original-location="NotLoadedLibrary.c:26"}
 *
 * Note that any breakpoint that fails to install will be marked as pending when the -f option is used.
 * <p>
 * <b>Note on using constructor directly:</b></a> As this class can be extended by third-parties it is
 * important to allow third parties the ability to have the correct version of MIBreakpoint
 * created. {@code MIBreakpoint}s should therefore be created via factory methods that can be overloaded.
 * For examples, see {@link MIBreakpoints#createMIBreakpoint(MITuple)} or
 * {@link MIBreakInsertInfo#createMIBreakpoint(MITuple)}
 */
public class MIBreakpoint {

	String number = ""; //$NON-NLS-1$
	String type = ""; //$NON-NLS-1$
	String disp = ""; //$NON-NLS-1$
	boolean enabled = false;
	String address = ""; //$NON-NLS-1$
	String func = ""; //$NON-NLS-1$
	String fullName = ""; //$NON-NLS-1$
	String file = ""; //$NON-NLS-1$
	int line = -1;
	String cond = ""; //$NON-NLS-1$
	int times = 0;
	String exp = ""; //$NON-NLS-1$
	String threadId = "0"; //$NON-NLS-1$
	int ignore = 0;
	String commands = ""; //$NON-NLS-1$
	String originalLocation = ""; //$NON-NLS-1$

	// For tracepoints
	int passcount = 0;
	// For dynamic printf
	String printfString;

	boolean isWpt = false;
	boolean isAWpt = false;
	boolean isRWpt = false;
	boolean isWWpt = false;
	boolean isHdw = false;

	// Indicate if we are dealing with a tracepoint.
	// (if its a fast or normal tracepoint can be known through the 'type' field)
	boolean isTpt = false;

	/** See {@link #isCatchpoint()} */
	boolean isCatchpoint;

	/** See {@link #getCatchpointType()} */
	private String catchpointType;

	/** See {@link #isDynamicPrintf()} */
	private boolean isDynPrintf;

	/**
	 * A pending breakpoint is a breakpoint that did not install properly,
	 * but that will be kept in the hopes that it installs later, triggered by
	 * the loading of a library.
	 * This concept is only supported starting with GDB 6.8
	 */
	private boolean pending;

	/**
	 * The list of groupIds to which this breakpoint applies.
	 * This field is only reported by MI starting with GDB 7.6.
	 * null will be returned if this field is not present.
	 */
	private String[] groupIds;

	/**
	 * No arguments constructor.
	 * <p>
	 * See {@link MIBreakpoint} class comment "Note on using constructor
	 * directly"
	 */
	public MIBreakpoint() {
	}

	/**
	 * Copy-constructor
	 * <p>
	 * See {@link MIBreakpoint} class comment "Note on using constructor
	 * directly"
	 * <p>
	 *
	 * @param other
	 *            breakpoint to copy from
	 */
	public MIBreakpoint(MIBreakpoint other) {
		number = other.number;
		type = other.type;
		disp = other.disp;
		enabled = other.enabled;
		address = other.address;
		func = other.func;
		fullName = other.fullName;
		file = other.file;
		line = other.line;
		cond = other.cond;
		times = other.times;
		exp = other.exp;
		threadId = other.threadId;
		ignore = other.ignore;
		commands = other.commands;
		passcount = other.passcount;
		isWpt = other.isWpt;
		isAWpt = other.isAWpt;
		isRWpt = other.isRWpt;
		isWWpt = other.isWWpt;
		isHdw = other.isHdw;
		isTpt = other.isTpt;
		isCatchpoint = other.isCatchpoint;
		catchpointType = other.catchpointType;
		isDynPrintf = other.isDynPrintf;
		pending = other.pending;
		originalLocation = other.originalLocation;
		if (other.groupIds != null) {
			groupIds = Arrays.copyOf(other.groupIds, other.groupIds.length);
		}
	}

	/**
	 * Construct an MIBreakpoint from the MI Tuple received from GDB
	 * <p>
	 * See {@link MIBreakpoint} class comment "Note on using constructor
	 * directly"
	 * <p>
	 *
	 * @param tuple
	 *            data received from GDB
	 */
	public MIBreakpoint(MITuple tuple) {
		parse(tuple);
	}

	/**
	 * This constructor is used for catchpoints. Catchpoints are not yet
	 * supported in MI, so we end up using CLI.
	 *
	 * <p>
	 * Note that this poses at least one challenge for us. Normally, upon
	 * creating a breakpoint/watchpoint/tracepoint via mi, we get back a command
	 * result from gdb that contains all the details of the newly created
	 * object, and we use that detailed information to create the MIBreakpoint.
	 * This is the same data we'll get back if we later ask gdb for the
	 * breakpoint list. However, because we're using CLI for cathpoints, we
	 * don't get that detailed information from gdb at creation time, but the
	 * detail will be there if we later ask for the breakpoint list. What this
	 * all means is that we can't compare the two MIBreakponts (the one we
	 * construct at creation time, and the one we get by asking gdb for the
	 * breakpoint list). The most we can do is compare the breakpoint number.
	 * That for sure should be the same.
	 *
	 * <p>
	 * The detail we get from querying the breakpoint list, BTW, won't even
	 * reveal that it's a catchpoint. gdb simply reports it as a breakpoint,
	 * probably because that's what it really sets under the cover--an address
	 * breakpoint. This is another thing we need to keep in mind and creatively
	 * deal with. When we set the catchpoint, this constructor is used. When the
	 * breakpoint list is queried, {@link #MIBreakpoint(MITuple)} is used for
	 * that same breakpoint number, and a consumer of that MIBreakpoint won't be
	 * able to tell it's a catchpoint. Quite the mess. Wish gdb would treat
	 * catchpoints like first class citizens.
	 *
	 * <p>
	 * See {@link MIBreakpoint} class comment "Note on using constructor directly"
	 *
	 * @param cliResult
	 *            the output from the CLI command. Example:
	 *            "Catchpoint 1 (catch)"
	 * @since 3.0
	 */
	public MIBreakpoint(String cliResult) {
		if (cliResult.startsWith("Catchpoint ")) { //$NON-NLS-1$
			String bkptNumber = ""; //$NON-NLS-1$

			StringTokenizer tokenizer = new StringTokenizer(cliResult);
			for (int i = 0; tokenizer.hasMoreTokens(); i++) {
				String sub = tokenizer.nextToken();
				switch (i) {
				case 0: // first token is "Catchpoint"
					break;
				case 1: // second token is the breakpoint number
					bkptNumber = sub;
					break;
				case 2: // third token is the event type; drop the parenthesis
					if (sub.startsWith("(")) { //$NON-NLS-1$
						sub = sub.substring(1, sub.length() - 1);
					}
					catchpointType = sub;
					break;
				}
			}

			number = bkptNumber;
			isCatchpoint = true;
			enabled = true;
		} else {
			assert false : "unexpected CLI output: " + cliResult; //$NON-NLS-1$
		}
	}

	///////////////////////////////////////////////////////////////////////////
	// Properties getters
	///////////////////////////////////////////////////////////////////////////

	/** @since 5.0 */
	public String getNumber() {
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

	/**
	 * @since 4.2
	 */
	public String getOriginalLocation() {
		return originalLocation;
	}

	/**
	 * If isCatchpoint is true, then this indicates the type of catchpoint
	 * (event), as reported by gdb in its response to the CLI catch command.
	 * E.g., 'catch' or 'fork'
	 *
	 * @since 3.0
	 */
	public String getCatchpointType() {
		return catchpointType;
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
	 * This method will return true for both fast and normal tracepoints.
	 * To know of fast vs normal tracepoint use {@link getType()} and look
	 * for "tracepoint" or "fast tracepoint"
	 *
	 * @since 3.0
	 */
	public boolean isTracepoint() {
		return isTpt;
	}

	/**
	 * Indicates if we are dealing with a catchpoint.
	 *
	 * @since 3.0
	 */
	public boolean isCatchpoint() {
		return isCatchpoint;
	}

	/**
	 * Indicates if we are dealing with a dynamic printf.
	 *
	 * @since 4.4
	 */
	public boolean isDynamicPrintf() {
		return isDynPrintf;
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
		if (isTpt == false)
			return;
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

	/**
	 * Return the string the dynamic printf will print.
	 * Returns null if this breakpoint is not a dynamic printf
	 *
	 * @since 4.4
	 */
	public String getPrintfString() {
		if (!isDynamicPrintf())
			return null;

		if (printfString == null) {
			// The string is burried inside the list of commands.
			// There should be only one command so we shouldn't need the delimiter but it does
			// not hurt to use it.  This delimeter is inserted when we parse the commands
			// from the result obtained from GDB
			String[] commands = getCommands().split(TracepointActionManager.TRACEPOINT_ACTION_DELIMITER);
			final String printfToken = "printf"; //$NON-NLS-1$
			for (String cmd : commands) {
				int pos = cmd.indexOf(printfToken);
				if (pos != -1) {
					printfString = cmd.substring(pos + printfToken.length() + 1);
				}
			}
			//		assert false : "Could not get printf string from gdb output"; //$NON-NLS-1$
		}
		return printfString;
	}

	/**
	 * Returns wether this breakpoint is pending
	 *
	 * @since 4.0
	 */
	public boolean isPending() {
		return pending;
	}

	/**
	 * Returns the thread-groups to which this breakpoint applies.
	 * Returns null if the data is not known.
	 *
	 * @since 4.2
	 */
	public String[] getGroupIds() {
		return groupIds;
	}

	/**
	 * Sets the list of thread-groups to which this breakpoint applies.
	 *
	 * @since 4.2
	 */
	public void setGroupIds(String[] groups) {
		groupIds = groups;
	}

	// Parse the result string
	void parse(MITuple tuple) {
		MIResult[] results = tuple.getMIResults();
		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			MIValue value = results[i].getMIValue();
			String str = ""; //$NON-NLS-1$
			if (value != null && value instanceof MIConst) {
				str = ((MIConst) value).getCString();
			}

			if (var.equals("number")) { //$NON-NLS-1$
				number = str.trim();
			} else if (var.equals("type")) { //$NON-NLS-1$
				// Note that catchpoints are reported by gdb as address
				// breakpoints; there's really nothing we can go on to determine
				// that it's actually a catchpoint (short of using a really ugly
				// and fragile hack--looking at the 'what' field for specific values)

				type = str;
				//type="hw watchpoint"
				if (type.startsWith("hw")) { //$NON-NLS-1$
					isHdw = true;
					if (type.indexOf("watchpoint") != -1) { //$NON-NLS-1$
						isWWpt = true;
						isWpt = true;
					}
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
				if (type.startsWith("tracepoint") || //$NON-NLS-1$
						type.startsWith("fast tracepoint")) { //$NON-NLS-1$
					isTpt = true;
				}
				if (type.startsWith("catchpoint")) { //$NON-NLS-1$
					isCatchpoint = true;
				}
				if (type.startsWith("dprintf")) { //$NON-NLS-1$
					isDynPrintf = true;
				}
				// type="breakpoint"
				// default ok.
			} else if (var.equals("disp")) { //$NON-NLS-1$
				disp = str;
			} else if (var.equals("enabled")) { //$NON-NLS-1$
				enabled = str.equals("y"); //$NON-NLS-1$
			} else if (var.equals("addr")) { //$NON-NLS-1$
				address = str.trim();
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
			} else if (var.equals("pending")) { //$NON-NLS-1$
				// Only supported starting with GDB 6.8
				pending = true;
			} else if (var.equals("script")) { //$NON-NLS-1$
				if (value instanceof MITuple) {
					parseCommands((MITuple) value);
				}
			} else if (var.equals("thread-groups")) { //$NON-NLS-1$
				if (value instanceof MIList) {
					parseGroups((MIList) value);
				}
			} else if (var.equals("original-location")) { //$NON-NLS-1$
				originalLocation = str;
			}
		}
	}

	void parseCommands(MITuple tuple) {
		MIValue[] values = tuple.getMIValues();
		StringBuilder cmds = new StringBuilder();
		for (int i = 0; i < values.length; i++) {
			MIValue value = values[i];
			if (value != null && value instanceof MIConst) {
				if (i > 0) {
					// Insert a delimiter
					cmds.append(TracepointActionManager.TRACEPOINT_ACTION_DELIMITER);
				}
				cmds.append(((MIConst) value).getCString());
			}
		}
		setCommands(cmds.toString());

	}

	private void parseGroups(MIList list) {
		List<String> groups = new ArrayList<>();

		MIValue[] values = list.getMIValues();
		for (int i = 0; i < values.length; i++) {
			if (values[i] instanceof MIConst) {
				groups.add(((MIConst) values[i]).getCString());
			}
		}

		groupIds = groups.toArray(new String[groups.size()]);
	}
}
