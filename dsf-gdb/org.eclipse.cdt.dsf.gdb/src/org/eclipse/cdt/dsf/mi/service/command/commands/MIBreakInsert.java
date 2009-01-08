/*******************************************************************************
 * Copyright (c) 2000, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *     Ericsson             - Modified for bug 219920
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakInsertInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * -break-insert [ -t ] [ -h ] [ -r ]
 *       [ -c CONDITION ] [ -i IGNORE-COUNT ]
 *       [ -p THREAD ] [ LINE | ADDR ]
 *  
 * If specified, LINE, can be one of:
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
 *
 * '-r'
 *     Insert a regular breakpoint in all the functions whose names match
 *     the given regular expression.  Other flags are not applicable to
 *     regular expression.
 *
 * '-c CONDITION'
 *     Make the breakpoint conditional on CONDITION.
 *
 * '-i IGNORE-COUNT'
 *     Initialize the IGNORE-COUNT (number of breakpoint hits before breaking).
 *
 * '-p THREAD'
 *     THREAD on which to apply the breakpoint
 */
public class MIBreakInsert extends MICommand<MIBreakInsertInfo> 
{
	public MIBreakInsert(IBreakpointsTargetDMContext ctx, String func) {
		this(ctx, false, false, null, 0, func, 0);
	}

	public MIBreakInsert(IBreakpointsTargetDMContext ctx, boolean isTemporary, boolean isHardware,
			String condition, int ignoreCount, String line, int tid) {
		super(ctx, "-break-insert"); //$NON-NLS-1$

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
        String[] opts = new String[i];

        // Fill in the optional parameters  
        i = 0;
        if (isTemporary) {
            opts[i] = "-t"; //$NON-NLS-1$
            i++;
        } 
        if (isHardware) {
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
        }

        if (opts.length > 0) {
            setOptions(opts);
        }
        setParameters(new Adjustable[]{ new PathAdjustable(line)});
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
