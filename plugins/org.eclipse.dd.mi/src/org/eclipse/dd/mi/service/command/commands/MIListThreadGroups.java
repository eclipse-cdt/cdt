/*******************************************************************************
 * Copyright (c) 2008 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.mi.service.command.commands;

import java.util.ArrayList;

import org.eclipse.dd.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.dd.mi.service.IMIExecutionGroupDMContext;
import org.eclipse.dd.mi.service.command.output.MIListThreadGroupsInfo;
import org.eclipse.dd.mi.service.command.output.MIOutput;

/**
 *  -list-thread-groups [--available] [GROUP]
 *      
 *  When used without GROUP parameter, this will list top-level 
 *  thread groups that are been debugged.  When used with the GROUP 
 *  parameter, the children of the specified group will be listed. 
 *  The children can be either threads, or other groups. At present,
 *  GDB will not report both threads and groups as children at the 
 *  same time, but it may change in future.
 *    
 *  With the --available option, instead of reporting groups that are
 *  been debugged, GDB will report all thread groups available on the 
 *  target, not only the presently debugged ones. Using the --available 
 *  option together with explicit GROUP is not likely to work on all targets.
 *
 *  The output of the command is:
 *
 *  ^done,threads=[<thread>],groups=[<group>]
 *  
 *  where each thread group is like this:
 *  
 *  {id="xxx",type="process",pid="yyy",num_children="1"}
 *  
 *  The id of a thread group should be considered an opaque string.
 *
 */
public class MIListThreadGroups extends MICommand<MIListThreadGroupsInfo> {
	
	public MIListThreadGroups(IContainerDMContext ctx) {
		this(ctx, false);
	}

	public MIListThreadGroups(IContainerDMContext ctx, boolean listAll) {
		super(ctx, "-list-thread-groups"); //$NON-NLS-1$
        
		final ArrayList<String> arguments = new ArrayList<String>();
		if (listAll) {
			arguments.add("--available"); //$NON-NLS-1$
		}

		// If the context is a thread-group, use the thread-group name
		// to list its children; if it is not, then we don't use any name to get
		// the list of all thread-groups
		if (ctx instanceof IMIExecutionGroupDMContext) {
			arguments.add(((IMIExecutionGroupDMContext)ctx).getGroupId());
		}

		if (!arguments.isEmpty()) {
			setParameters(arguments.toArray(new String[0]));
		}
	}

	@Override
    public MIListThreadGroupsInfo getResult(MIOutput out) {
        return new MIListThreadGroupsInfo(out);
    }
}
