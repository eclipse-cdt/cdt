/*******************************************************************************
 * Copyright (c) 2014 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

import org.eclipse.cdt.dsf.gdb.launching.LaunchUtils;
import org.eclipse.cdt.dsf.mi.service.command.output.MIConsoleStreamOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOOBRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIStreamRecord;

/**
 * '-gdb-version' Show version information for gdb.
 *
 * sample output:
 * 
 * -gdb-version
 * ~"GNU gdb (Ubuntu 7.7-0ubuntu3.1) 7.7\n"
 * ~"Copyright (C) 2014 Free Software Foundation, Inc.\n"
 * ~"License GPLv3+: GNU GPL version 3 or later <http://gnu.org/licenses/gpl.html>\nThis is free software: you are free to change and redistribute it.\nThere is NO WARRANTY, to the extent permitted by law.  Type \"show copying\"\nand \"show warranty\" for details.\n"
 * ~"This GDB was configured as \"x86_64-linux-gnu\".\nType \"show configuration\" for configuration details."
 * ~"\nFor bug reporting instructions, please see:\n"
 *  ~"<http://www.gnu.org/software/gdb/bugs/>.\n"
 * ~"Find the GDB manual and other documentation resources online at:\n<http://www.gnu.org/software/gdb/documentation/>.\n"
 * ~"For help, type \"help\".\n"
 * ~"Type \"apropos word\" to search for commands related to \"word\".\n"
 * ^done
 *
 * @since 4.6
 */
public class MIGDBVersionInfo extends MIInfo {

	private String fVersion;
	private String fFullOutput;
	
	public MIGDBVersionInfo(MIOutput record) {
		super(record);
		parse();
	}

	protected void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIOOBRecord[] records = out.getMIOOBRecords();
			StringBuilder builder = new StringBuilder();
			for(MIOOBRecord rec : records) {
                if (rec instanceof MIConsoleStreamOutput) {
                    MIStreamRecord o = (MIStreamRecord)rec;
                    builder.append(o.getString());
                }
			}
			fFullOutput = builder.toString();
			fVersion = parseVersion(fFullOutput);
		}
	}

	protected String parseVersion(String output) {
		return LaunchUtils.getGDBVersionFromText(output);
	}
	
	public String getFullOutput() {
		return fFullOutput;
	}

	public String getVersion() {
		return fVersion;
	}
}
