/*******************************************************************************
 * Copyright (c) 201e Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.examples.dsf.gdb.service.command.output;

import org.eclipse.cdt.dsf.gdb.launching.LaunchUtils;
import org.eclipse.cdt.dsf.mi.service.command.output.MIConsoleStreamOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOOBRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIStreamRecord;

public class MIGDBVersionInfo extends MIInfo {

	private String fVersion;
	
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
			fVersion = LaunchUtils.getGDBVersionFromText(builder.toString());
		}
	}

	public String getVersion() {
		return fVersion;
	}
}
