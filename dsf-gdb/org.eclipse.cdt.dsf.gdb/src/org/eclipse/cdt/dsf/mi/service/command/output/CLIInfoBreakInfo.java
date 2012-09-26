/*******************************************************************************
 * Copyright (c) 2012 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;

import java.util.ArrayList;
import java.util.List;

/**
 * 'info break [BP_REFERENCE] will return information about
 * the specified breakpoint.  We use it to find out to which
 * inferiors a breakpoint is applicable.
 * 
 * sample output: 
 *
 * (gdb) inf b 1
 * Num     Type           Disp Enb Address    What
 * 1       breakpoint     keep y   <MULTIPLE> 
 * 1.1                         y     0x08048533 in main() at loopfirst.cc:8 inf 2
 * 1.2                         y     0x08048533 in main() at loopfirst.cc:8 inf 1
 * 
 * Note that the below output is theoretically possible looking at GDB's code but
 * I haven't figured out a way to trigger it.  Still, we should be prepared for it:
 * (gdb) info b 2
 * Num     Type           Disp Enb Address    What
 * 2       breakpoint     keep y   0x08048553 in main() at loopsecond.cc:9 inf 3, 2, 1
 *
 * If only one inferior is being debugged:
 * (gdb) info b 2
 * Num     Type           Disp Enb Address    What
 * 2       breakpoint     keep y   0x08048553 in main() at loopsecond.cc:9
 *
 * 
 * @since 4.2
 */
public class CLIInfoBreakInfo extends MIInfo {

	private String[] fInferiorIds = null;

	public CLIInfoBreakInfo(MIOutput out) {		
		super(out);
		parse();
	}
	
	/**
	 * Return the list of inferior ids to which this breakpoint applies or an
	 * empty array if there is only a single inferior being debugged.
	 */
	public String[] getInferiorIds() {
		return fInferiorIds; 
	}
	
	protected void parse() {
		final String INFERIOR_PREFIX = " inf "; //$NON-NLS-1$
		if (isDone()) {
			List<String> inferiorIdList = new ArrayList<String>();
			MIOutput out = getMIOutput();
			for (MIOOBRecord oob : out.getMIOOBRecords()) {
				if (oob instanceof MIConsoleStreamOutput) {
					String line = ((MIConsoleStreamOutput)oob).getString().trim();
					int loc = line.indexOf(INFERIOR_PREFIX);
					if (loc >= 0) {
						// Get the comma-separated list of inferiors
						String inferiorIdStr = line.substring(loc + INFERIOR_PREFIX.length()).trim();
						// Split the list into individual ids
						for (String id : inferiorIdStr.split(",")) { //$NON-NLS-1$
							inferiorIdList.add(id.trim());							
						}
					}
				}
			}
			
			fInferiorIds = inferiorIdList.toArray(new String[inferiorIdList.size()]);
		}
	}
}

