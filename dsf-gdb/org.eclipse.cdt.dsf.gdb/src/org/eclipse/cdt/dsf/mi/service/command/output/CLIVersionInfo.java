/*******************************************************************************
 * Copyright (c) 2025 John Dallaway and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Dallaway - Initial implementation (#1186)
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

/**
 * CLI 'version' command returns the debugger version.
 *
 * sample output:
 *
 * (gdb) version
 * ~"lldb version 20.1.2\n"
 * ^done
 *
 * @since 7.2
 */
public class CLIVersionInfo extends MIInfo {

	private String fFullOutput;

	public CLIVersionInfo(MIOutput record) {
		super(record);
		parse();
	}

	protected void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIOOBRecord[] records = out.getMIOOBRecords();
			StringBuilder builder = new StringBuilder();
			for (MIOOBRecord rec : records) {
				if (rec instanceof MIConsoleStreamOutput) {
					MIStreamRecord o = (MIStreamRecord) rec;
					builder.append(o.getString());
				}
			}
			fFullOutput = builder.toString();
		}
	}

	public String getFullOutput() {
		return fFullOutput;
	}

}
