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
 *     Ericsson             - Modified for bug 219920
 *     Ericsson             - Modified for tracepoints (284286)
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakInsertInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * -break-insert [ -t ] [ -h ] [ -f ] [ -d ] [ -a ]
 *       [ -c CONDITION ] [ -i IGNORE-COUNT ]
 *       [ -p THREAD ] [ LOCATION ]
 *  
 * If specified, LOCATION, can be one of:
 *  * function
 *  * filename:linenum
 *  * filename:function
 *  * *address
 * 
 * The possible optional parameters of this command are:
 *
 * '-t'
 *      Insert a temporary breakpoint.
 *
 * '-h'
 *      Insert a hardware breakpoint.
 *      When inserting a tracepoint (-a), this option indicates a fast tracepoint
 *
 * '-c CONDITION'
 *      Make the breakpoint conditional on CONDITION.
 *
 * '-i IGNORE-COUNT'
 *      Initialize the IGNORE-COUNT (number of breakpoint hits before breaking).
 *
 * '-f'
 *      If location cannot be parsed (for example if it refers to unknown files or 
 *      functions), create a pending breakpoint. Without this flag, if a location
 *      cannot be parsed, the breakpoint will not be created and an error will be
 *      reported.
 *      Only available starting GDB 6.8
 *
 * '-d'
 *      Create a disabled breakpoint.
 *      Only available starting GDB 7.0
 *      
 * '-a'
 *      Insert a tracepoint instead of a breakpoint
 *      Only available starting GDB 7.1
 *      
 * '-p THREAD'
 *      THREAD on which to apply the breakpoint
 */
public class MIBreakInsert extends MICommand<MIBreakInsertInfo> 
{
	/** @since 4.0 */
	public MIBreakInsert(IBreakpointsTargetDMContext ctx, String func, boolean allowPending) {
		this(ctx, false, false, null, 0, func, 0, allowPending);
	}
	
	/** @since 4.0 */
	public MIBreakInsert(IBreakpointsTargetDMContext ctx, boolean isTemporary, boolean isHardware,
			String condition, int ignoreCount, String line, int tid, boolean allowPending) {
		this(ctx, isTemporary, isHardware, condition, ignoreCount, line, tid, false, false, allowPending);
	}
	
	/**
	 * This constructor allows to specify if the breakpoint should actually be
	 * a tracepoint (this will only work starting with GDB 7.1)
	 * It also includes if a breakpoint should be created disabled (starting GDB 7.0)
	 * @since 4.0
	 */
	public MIBreakInsert(IBreakpointsTargetDMContext ctx, boolean isTemporary, boolean isHardware,
			String condition, int ignoreCount, String location, int tid, boolean disabled, boolean isTracepoint,
			boolean allowPending) {
		super(ctx, "-break-insert"); //$NON-NLS-1$

		// For a tracepoint, force certain parameters to what is allowed
		if (isTracepoint) {
			// A tracepoint cannot be temporary
			isTemporary = false;
			// GDB 7.1 does not support ignore-counts for tracepoints
			// and passcounts cannot be set by a -break-insert
			ignoreCount = 0;
			// GDB 7.1 only supports tracepoints that apply to all threads
			tid = 0;
		}

        // Determine the number of optional parameters that are present
        // and allocate a corresponding string array
        int i = 0;
        if (isTemporary) {
            i++;
        }
        if (isHardware) {
            i++;
        }
        if (condition != null && condition.length() > 0) {
            i += 2;
        }
        if (ignoreCount > 0) {
            i += 2;
        }
        if (tid > 0) {
            i += 2;
        }
        if (disabled) {
        	i++;
        }
        if (isTracepoint) {
            i++;
        }
        if (allowPending) {
        	i ++;
        }

        String[] opts = new String[i];

        // Fill in the optional parameters  
        i = 0;
        if (isTemporary) {
            opts[i] = "-t"; //$NON-NLS-1$
            i++;
        } 
        if (isHardware) {
        	// For tracepoints, this will request a fast tracepoint
            opts[i] = "-h"; //$NON-NLS-1$
            i++;
        }
        if (condition != null && condition.length() > 0) {
            opts[i] = "-c"; //$NON-NLS-1$
            i++;
            opts[i] = condition;
            i++;
        }
        if (ignoreCount > 0) {
            opts[i] = "-i"; //$NON-NLS-1$
            i++;
            opts[i] = Integer.toString(ignoreCount);
            i++;
        }
        if (tid > 0) {
            opts[i] = "-p"; //$NON-NLS-1$
            i++;
            opts[i] = Integer.toString(tid);
            i++;
        }
        if (disabled) {
            opts[i] = "-d"; //$NON-NLS-1$
            i++;
        }
        if (isTracepoint) {
            opts[i] = "-a"; //$NON-NLS-1$
            i++;
        }
        if (allowPending) {
        	opts[i] = "-f"; //$NON-NLS-1$
        	i ++;
        }

        if (opts.length > 0) {
            setOptions(opts);
        }
        setParameters(new Adjustable[]{ new PathAdjustable(location)});
    }

    @Override
    public MIBreakInsertInfo getResult(MIOutput output) {
        return new MIBreakInsertInfo(output);
    }
    
	/**
	 * This adjustable makes sure that the path parameter will not get the
	 * backslashes substituted with double backslashes.
	 */
	private class PathAdjustable
			extends
			org.eclipse.cdt.dsf.mi.service.command.commands.MICommand.MIStandardParameterAdjustable {

		public PathAdjustable(String path) {
			super(path);
		}

		@Override
		public String getAdjustedValue() {
			String adjustedValue = super.getAdjustedValue();
			return adjustedValue.replace("\\\\", "\\"); //$NON-NLS-1$//$NON-NLS-2$
		}
	}
}
