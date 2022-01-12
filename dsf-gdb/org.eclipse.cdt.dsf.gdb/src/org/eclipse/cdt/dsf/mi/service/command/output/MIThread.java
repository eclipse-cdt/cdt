/*******************************************************************************
 * Copyright (c) 2008, 2014 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *     Wind River Systems - refactored to match pattern in package
 *     John Dallaway - GDB 7.x getOsId() pattern match too restrictive (Bug 325552)
 *     Xavier Raynaud (Kalray) - MIThread can be overridden (Bug 429124)
 *     Alvaro Sanchez-Leon - Bug 451396 - Improve extensibility to process MI "-thread-info" results
 *     Simon Marchi (Ericsson) - Bug 378154 - Have MIThread provide thread name
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.dsf.concurrent.Immutable;

/**
 * GDB/MI Thread tuple parsing.
 *
 * @since 1.1
 */
@Immutable
public class MIThread {

	/** @since 4.1 */
	public final static String MI_THREAD_STATE_RUNNING = "running"; //$NON-NLS-1$
	/** @since 4.1 */
	public final static String MI_THREAD_STATE_STOPPED = "stopped"; //$NON-NLS-1$

	final private String fThreadId;
	final private String fTargetId;
	final private String fOsId;
	final private String fParentId;
	final private MIFrame fTopFrame;
	final private String fDetails;
	final private String fState;
	final private String fCore;
	final private String fName;

	/** @since 4.4 */
	protected MIThread(String threadId, String targetId, String osId, String parentId, MIFrame topFrame, String details,
			String state, String core) {
		this(threadId, targetId, osId, parentId, topFrame, details, state, core, null);
	}

	/** @since 4.6 */
	protected MIThread(String threadId, String targetId, String osId, String parentId, MIFrame topFrame, String details,
			String state, String core, String name) {
		fThreadId = threadId;
		fTargetId = targetId;
		fOsId = osId;
		fParentId = parentId;
		fTopFrame = topFrame;
		fDetails = details;
		fState = state;
		fCore = core;
		fName = name;
	}

	public String getThreadId() {
		return fThreadId;
	}

	public String getTargetId() {
		return fTargetId;
	}

	public String getOsId() {
		return fOsId;
	}

	public String getParentId() {
		return fParentId;
	}

	public MIFrame getTopFrame() {
		return fTopFrame;
	}

	public String getDetails() {
		return fDetails;
	}

	public String getState() {
		return fState;
	}

	/**
	 * Available since GDB 7.1
	 * @since 4.0
	 */
	public String getCore() {
		return fCore;
	}

	/** @since 4.6 */
	public String getName() {
		return fName;
	}

	public static MIThread parse(MITuple tuple) {
		MIResult[] results = tuple.getMIResults();

		String threadId = null;
		String targetId = null;
		String osId = null;
		String parentId = null;
		MIFrame topFrame = null;
		String state = null;
		String details = null;
		String core = null;
		String name = null;

		for (MIResult result : results) {
			String var = result.getVariable();
			if (var.equals("id")) { //$NON-NLS-1$
				MIValue val = result.getMIValue();
				if (val instanceof MIConst) {
					threadId = ((MIConst) val).getCString().trim();
				}
			} else if (var.equals("target-id")) { //$NON-NLS-1$
				MIValue val = result.getMIValue();
				if (val instanceof MIConst) {
					targetId = ((MIConst) val).getCString().trim();
					osId = parseOsId(targetId);
					parentId = parseParentId(targetId);
				}
			} else if (var.equals("frame")) { //$NON-NLS-1$
				MITuple val = (MITuple) result.getMIValue();
				topFrame = new MIFrame(val);
			} else if (var.equals("state")) { //$NON-NLS-1$
				MIValue val = result.getMIValue();
				if (val instanceof MIConst) {
					state = ((MIConst) val).getCString().trim();
				}
			} else if (var.equals("details")) { //$NON-NLS-1$
				MIValue val = result.getMIValue();
				if (val instanceof MIConst) {
					details = ((MIConst) val).getCString().trim();
				}
			} else if (var.equals("core")) { //$NON-NLS-1$
				MIValue val = result.getMIValue();
				if (val instanceof MIConst) {
					core = ((MIConst) val).getCString().trim();
				}
			} else if (var.equals("name")) { //$NON-NLS-1$
				MIValue val = result.getMIValue();
				if (val instanceof MIConst) {
					name = ((MIConst) val).getCString().trim();
				}
			}
		}

		return new MIThread(threadId, targetId, osId, parentId, topFrame, details, state, core, name);
	}

	// Note that windows gdbs returns lower case "thread" , so the matcher needs to be case-insensitive.
	private static Pattern fgOsIdPattern1 = Pattern
			.compile("([Tt][Hh][Rr][Ee][Aa][Dd]\\s*)(0x[0-9a-fA-F]+|-?\\d+)(\\s*\\([Ll][Ww][Pp]\\s*)(\\d*)", 0); //$NON-NLS-1$
	private static Pattern fgOsIdPattern2 = Pattern.compile("[Tt][Hh][Rr][Ee][Aa][Dd]\\s*\\d+\\.(\\d+)", 0); //$NON-NLS-1$
	private static Pattern fgOsIdPattern3 = Pattern.compile("[Tt][Hh][Rr][Ee][Aa][Dd]\\s*(\\S+)", 0); //$NON-NLS-1$
	private static Pattern fgOsIdPattern4 = Pattern.compile("[Pp][Rr][Oo][Cc][Ee][Ss][Ss]\\s*(\\S+)", 0); //$NON-NLS-1$

	/**
	 * @since 4.6
	 */
	protected static String parseOsId(String str) {
		// General format:
		//      "Thread 0xb7c8ab90 (LWP 7010)"
		//                              ^^^^
		//      "Thread 162.32942"
		//                  ^^^^^
		//      "thread abc123"
		//
		//      "process 12345"    => Linux without pthread.  The process as one thread, the process thread.
		//              ^^^^^^
		// PLEASE UPDATE MIThreadTests.java IF YOU TWEAK THIS CODE

		Matcher matcher = fgOsIdPattern1.matcher(str);
		if (matcher.find()) {
			return matcher.group(4);
		}

		matcher = fgOsIdPattern2.matcher(str);
		if (matcher.find()) {
			return matcher.group(1);
		}

		matcher = fgOsIdPattern3.matcher(str);
		if (matcher.find()) {
			return matcher.group(1);
		}

		matcher = fgOsIdPattern4.matcher(str);
		if (matcher.find()) {
			return matcher.group(1);
		}

		return null;
	}

	private static Pattern fgIdPattern = Pattern.compile("[Tt][Hh][Rr][Ee][Aa][Dd]\\s*(\\d+)\\.\\d+", 0); //$NON-NLS-1$

	/**
	 * This is used to parse the same ID fed to {@link #parseOsId(String)}. The
	 * difference is that we return the first portion when the ID is in format
	 * "Thread pppp.tttt". If the ID is not in that format, we return null.
	 * @since 4.6
	 */
	protected static String parseParentId(String str) {
		// General format:
		//      "Thread 162.32942"
		//              ^^^
		// PLEASE UPDATE MIThreadTests.java IF YOU TWEAK THIS CODE

		Matcher matcher = fgIdPattern.matcher(str);
		if (matcher.find()) {
			return matcher.group(1);
		}

		return null;
	}

}
