/*******************************************************************************
 * Copyright (c) 2008, 2011 Ericsson and others.
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
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import java.util.ArrayList;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIListThreadGroupsInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 *  -list-thread-groups [--available | GROUP] [ --recurse 1 ]
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
 *  {id="xxx",type="process",pid="yyy",num_children="1",cores=[1,2]}
 *
 *  The id of a thread group should be considered an opaque string.
 *
 *  As of GDB 7.1, the --recurse option has been added.  If this option is
 *  present, then every reported thread group will also include its children,
 *  either as `group' or `threads' field.
 *
 *  In general, any combination of option and parameters is permitted, with
 *  the following caveats:
 *    - When a single thread group is passed, the output will typically be the
 *      `threads' result. Because threads may not contain anything, the
 *      `recurse' option will be ignored.
 *    - When the `--available' option is passed, limited information may be
 *      available. In particular, the list of threads of a process might be
 *      inaccessible. Further, specifying specific thread groups might not give
 *      any performance advantage over listing all thread groups. The frontend
 *      should assume that `-list-thread-groups --available' is always an
 *      expensive operation and cache the results.
 *
 *  As of GDB 7.1, the 'core' output field has been added.
 *   - cores This field is a list of integers, each identifying a core that one
 *     thread of the group is running on. This field may be absent if such
 *     information is not available.
 *
 * @since 1.1
 *
 */
public class MIListThreadGroups extends MICommand<MIListThreadGroupsInfo> {

	/**
	 *  List all groups (processes) being debugged.
	 */
	public MIListThreadGroups(ICommandControlDMContext ctx) {
		this(ctx, false);
	}

	/**
	 *  If the parameter groupId is null, list all groups (processes) being debugged.
	 *  If the parameter groupId is a valid group, list all threads
	 *  which are children of the specified group
	 */
	public MIListThreadGroups(ICommandControlDMContext ctx, String groupId) {
		this(ctx, groupId, false, false);
	}

	/**
	 * If the parameter listAll is true, list all processes running on the
	 * target (not just the debugged ones).
	 * If the parameter listAll is false, list only the processes being debugged.
	 */
	public MIListThreadGroups(ICommandControlDMContext ctx, boolean listAll) {
		this(ctx, null, listAll, false);
	}

	/**
	 * If the parameter recurse is true, list all threads of all processes.
	 * @since 4.1
	 */
	public MIListThreadGroups(ICommandControlDMContext ctx, boolean listAll, boolean recurse) {
		this(ctx, null, listAll, recurse);
	}

	// There should be no reason to have both listAll and groupId specified,
	// so this constructor is private, and exists to avoid duplicating code.
	private MIListThreadGroups(ICommandControlDMContext ctx, String groupId, boolean listAll, boolean recurse) {
		super(ctx, "-list-thread-groups"); //$NON-NLS-1$

		assert !((groupId != null) && listAll); // see comment above

		final ArrayList<String> arguments = new ArrayList<>();
		if (listAll) {
			arguments.add("--available"); //$NON-NLS-1$
		}

		if (recurse) {
			arguments.add("--recurse"); //$NON-NLS-1$
			arguments.add("1"); //$NON-NLS-1$
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
