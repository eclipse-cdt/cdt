/*******************************************************************************
 * Copyright (c) 2008, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *     Wind River Systems - refactored to match pattern in package
 *     John Dallaway - GDB 7.x getOsId() pattern match too restrictive (Bug 325552)
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
	
	final private String       fThreadId;
	final private String       fTargetId;
	final private String       fOsId;
	final private String       fParentId;
	final private MIFrame      fTopFrame;
	final private String       fDetails;
	final private String       fState;
	final private String       fCore;
	
	private MIThread(String threadId, String targetId, String osId, String parentId,
			          MIFrame topFrame, String details, String state, String core) {
		fThreadId  = threadId;
		fTargetId  = targetId;
		fOsId      = osId;
		fParentId  = parentId;
		fTopFrame  = topFrame;
		fDetails   = details;
		fState     = state;
		fCore      = core;
	}

	public String getThreadId()       { return fThreadId; }
	public String getTargetId()       { return fTargetId; }
	public String getOsId()           { return fOsId;     }
	public String getParentId()       { return fParentId; }
	public MIFrame getTopFrame()      { return fTopFrame; } 
	public String getDetails()        { return fDetails;  }
	public String getState()          { return fState;    }
	/**
	 * Available since GDB 7.1
	 * @since 4.0
	 */
	public String getCore()           { return fCore; }
	
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

        for (int j = 0; j < results.length; j++) {
            MIResult result = results[j];
            String var = result.getVariable();
            if (var.equals("id")) { //$NON-NLS-1$
                MIValue val = results[j].getMIValue();
                if (val instanceof MIConst) {
                    threadId = ((MIConst) val).getCString().trim();
                }
            }
            else if (var.equals("target-id")) { //$NON-NLS-1$
                MIValue val = results[j].getMIValue();
                if (val instanceof MIConst) {
                    targetId = ((MIConst) val).getCString().trim();
                    osId = parseOsId(targetId);
                    parentId = parseParentId(targetId);
                }
            }
            else if (var.equals("frame")) { //$NON-NLS-1$
                MITuple val = (MITuple)results[j].getMIValue();
                topFrame = new MIFrame(val);
            }
            else if (var.equals("state")) { //$NON-NLS-1$
                MIValue val = results[j].getMIValue();
                if (val instanceof MIConst) {
                    state = ((MIConst) val).getCString().trim();
                }
            }
            else if (var.equals("details")) { //$NON-NLS-1$
                MIValue val = results[j].getMIValue();
                if (val instanceof MIConst) {
                    details = ((MIConst) val).getCString().trim();
                }
            }
            else if (var.equals("core")) { //$NON-NLS-1$
                MIValue val = results[j].getMIValue();
                if (val instanceof MIConst) {
                    core = ((MIConst) val).getCString().trim();
                }
            }
        }
        
        return new MIThread(threadId, targetId, osId, parentId, topFrame, details, state, core);
	}
	
	// Note that windows gdbs returns lower case "thread" , so the matcher needs to be case-insensitive. 
    private static Pattern fgOsIdPattern1 = Pattern.compile("([Tt][Hh][Rr][Ee][Aa][Dd]\\s*)(0x[0-9a-fA-F]+|-?\\d+)(\\s*\\([Ll][Ww][Pp]\\s*)(\\d*)", 0); //$NON-NLS-1$
    private static Pattern fgOsIdPattern2 = Pattern.compile("[Tt][Hh][Rr][Ee][Aa][Dd]\\s*\\d+\\.(\\d+)", 0); //$NON-NLS-1$
    private static Pattern fgOsIdPattern3 = Pattern.compile("[Tt][Hh][Rr][Ee][Aa][Dd]\\s*(\\S+)", 0); //$NON-NLS-1$

    static String parseOsId(String str) {
        // General format:
        //      "Thread 0xb7c8ab90 (LWP 7010)"
    	//                              ^^^^
        //      "Thread 162.32942"
    	//                  ^^^^^
        //      "thread abc123"
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

        return null;
    }
    
    private static Pattern fgIdPattern = Pattern.compile("[Tt][Hh][Rr][Ee][Aa][Dd]\\s*(\\d+)\\.\\d+", 0); //$NON-NLS-1$ 

	/**
	 * This is used to parse the same ID fed to {@link #parseOsId(String)}. The
	 * difference is that we return the first portion when the ID is in format
	 * "Thread pppp.tttt". If the ID is not in that format, we return null.
	 */
    static String parseParentId(String str) {
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
