/*******************************************************************************
 * Copyright (c) 2008, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import java.util.ArrayList;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIListThreadGroupsInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 *  -list-thread-groups [--available | GROUP]
 *      
 *  When used without GROUP parameter, this will list top-level 
 *  thread groups that are being debugged.  When used with the GROUP 
 *  parameter, the children of the specified group will be listed. 
 *  The children can be either threads, or other groups. At present,
 *  GDB will not report both threads and groups as children at the 
 *  same time, but it may change in future.
 *    
 *  With the --available option, instead of reporting groups that are
 *  being debugged, GDB will report all thread groups available on the 
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
 * @since 1.1
 *
 */
public class MIListThreadGroups extends MICommand<MIListThreadGroupsInfo> {
	
	// List all groups being debugged
	public MIListThreadGroups(ICommandControlDMContext ctx) {
		this(ctx, false);
	}

	// List all groups or threads being debugged which are children of the specified group
	public MIListThreadGroups(ICommandControlDMContext ctx, String groupId) {
		this(ctx, groupId, false);
	}

	// List all groups available on the target
	public MIListThreadGroups(ICommandControlDMContext ctx, boolean listAll) {
		this(ctx, null, listAll);
	}

	// There should be no reason to have both listAll and groupId specified,
	// so this constructor is private, and exists to avoid duplicating code.
	private MIListThreadGroups(ICommandControlDMContext ctx, String groupId, boolean listAll) {
		super(ctx, "-list-thread-groups"); //$NON-NLS-1$
		
		assert !((groupId != null) && listAll); // see comment above
        
		final ArrayList<String> arguments = new ArrayList<String>();
		if (listAll) {
			arguments.add("--available"); //$NON-NLS-1$
		}

		if (groupId != null) {
			assert groupId.trim().length() > 0;
			arguments.add(groupId);
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
