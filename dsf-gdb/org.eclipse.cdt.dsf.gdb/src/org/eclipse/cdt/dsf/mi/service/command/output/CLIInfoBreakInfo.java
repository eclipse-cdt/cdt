/*******************************************************************************
 * Copyright (c) 2012, 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 'info break [BP_REFERENCE] will return information about
 * the specified breakpoint.  We use it to find out to which
 * inferiors a breakpoint is applicable.
 *
 * sample output:
 *
 * (gdb) info break
 * Num     Type           Disp Enb Address    What
 * 1       breakpoint     keep y   <MULTIPLE>
 * 1.1                         y     0x08048533 in main() at loopfirst.cc:8 inf 2
 * 1.2                         y     0x08048533 in main() at loopfirst.cc:8 inf 1
 * 2       breakpoint     keep y   <MULTIPLE>
 * 2.1                         y     0x08048553 in main() at loopfirst.cc:9 inf 2
 * 2.2                         y     0x08048553 in main() at loopfirst.cc:9 inf 1
 *
 * If only one inferior is being debugged there is not mention of the inferior:
 * (gdb) info break
 * Num     Type           Disp Enb Address    What
 * 1       breakpoint     keep y   0x08048533 in main() at loopfirst.cc:8
 * 2       breakpoint     keep y   0x08048553 in main() at loopfirst.cc:9
 *
 * Note that the below output is theoretically possible looking at GDB's code but
 * I haven't figured out a way to trigger it.  Still, we should be prepared for it:
 * (gdb) info break
 * Num     Type           Disp Enb Address    What
 * 1       breakpoint     keep y   <MULTIPLE>
 * 1.1                         y     0x08048533 in main() at loopfirst.cc:8 inf 3, 2
 * 1.2                         y     0x08048533 in main() at loopfirst.cc:8 inf 2, 1
 * 2       breakpoint     keep y   <MULTIPLE>
 * 2.1                         y     0x08048553 in main() at loopfirst.cc:9 inf 2, 1
 * 2.2                         y     0x08048553 in main() at loopfirst.cc:9 inf 3, 2, 1
 *
 * @since 4.2
 */
public class CLIInfoBreakInfo extends MIInfo {

	private Map<String, String[]> fBreakpointToGroupMap = new HashMap<>();

	public CLIInfoBreakInfo(MIOutput out) {
		super(out);
		parse();
	}

	/**
	 * Returns the map of breakpoint to groupId array indicating to which thread-group
	 * each breakpoint applies.  If there is only a single thread-group being debugged, an
	 * empty map will be returned.
	 */
	public Map<String, String[]> getBreakpointToGroupMap() {
		return fBreakpointToGroupMap;
	}

	protected void parse() {
		final String INFERIOR_PREFIX = " inf "; //$NON-NLS-1$
		if (isDone()) {
			MIOutput out = getMIOutput();
			for (MIOOBRecord oob : out.getMIOOBRecords()) {
				if (oob instanceof MIConsoleStreamOutput) {
					String line = ((MIConsoleStreamOutput) oob).getString().trim();
					int loc = line.indexOf(INFERIOR_PREFIX);
					if (loc >= 0) {
						// Get the breakpoint id by extracting the first element before a white space
						// or before a period.  We can set a limit of 2 since we only need the first element
						String bpIdStr = line.split("[\\s\\.]", 2)[0]; //$NON-NLS-1$

						String[] groups = fBreakpointToGroupMap.get(bpIdStr);
						Set<String> groupIdList = new HashSet<>();
						if (groups != null) {
							// Since we already know about this breakpoint id we must retain the list
							// we have been building
							groupIdList.addAll(Arrays.asList(groups));
						}

						// Get the comma-separated list of inferiors
						// Split the list into individual ids
						String inferiorIdStr = line.substring(loc + INFERIOR_PREFIX.length()).trim();
						for (String id : inferiorIdStr.split(",")) { //$NON-NLS-1$
							// Add the 'i' prefix as GDB does for MI commands
							groupIdList.add("i" + id.trim()); //$NON-NLS-1$
						}

						fBreakpointToGroupMap.put(bpIdStr, groupIdList.toArray(new String[groupIdList.size()]));
					}
				}
			}
		}
	}
}
