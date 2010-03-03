/*******************************************************************************
 * Copyright (c) 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Ericsson             - Created version for Mac OS
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output.macos;

import java.util.List;

import org.eclipse.cdt.dsf.mi.service.command.output.MIList;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResult;
import org.eclipse.cdt.dsf.mi.service.command.output.MITuple;
import org.eclipse.cdt.dsf.mi.service.command.output.MIValue;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarChange;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarUpdateInfo;

/**
 * GDB/MI var-update for Mac OS.
 * -var-update *
 * ^done,changelist=[varobj={name="var1",in_scope="true",type_changed="false"}],time={.....}
 * 
 * @since 3.0
 */
public class MacOSMIVarUpdateInfo extends MIVarUpdateInfo {

	public MacOSMIVarUpdateInfo(MIOutput record) {
		super(record);
	}

	/**
	 * For MI2 the format is now a MIList.
	 * @param tuple
	 * @param aList
	 */
	@Override
	protected void parseChangeList(MIList miList, List<MIVarChange> aList) {
		// The MIList in Apple gdb contains MIResults instead of MIValues. It looks like:
		// ^done,changelist=[varobj={name="var1",in_scope="true",type_changed="false"}],time={.....}
		// Bug 250037
		MIResult[] results = miList.getMIResults();
		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			if (var.equals("varobj")) { //$NON-NLS-1$
				MIValue value = results[i].getMIValue();
				if (value instanceof MITuple) {
					parseChangeList((MITuple)value, aList);
				} else if (value instanceof MIList) {
					parseChangeList((MIList)value, aList);
				}
			}
		}
	}
}
