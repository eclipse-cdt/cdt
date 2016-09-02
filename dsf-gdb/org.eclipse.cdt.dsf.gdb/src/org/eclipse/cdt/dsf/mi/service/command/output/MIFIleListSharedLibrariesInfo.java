/*******************************************************************************
 * Copyright (c) 2000, 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Ericsson AB			- Modified for DSF GDB reference implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 5.1
 * 
 */
public class MIFIleListSharedLibrariesInfo extends MIInfo {

	MISharedInfo[] shared;

	public MIFIleListSharedLibrariesInfo(MIOutput out) {
		super(out);
		parse();
	}

	public MISharedInfo[] getMIShared() {
		return shared;
	}

	void parse() {
		List<MISharedInfo> aList = new ArrayList<MISharedInfo>();
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			MIResult[] results = rr.getMIResults();
			for (MIResult result : results) {
				MIValue miValue = result.getMIValue();
				if (miValue instanceof MIList) {
					MIList miList = (MIList) miValue;
					MIValue[] sharedLibraries = miList.getMIValues();
					for (MIValue sharedLibrary : sharedLibraries) {
						if (sharedLibrary instanceof MITuple) {
							parseShared((MITuple) sharedLibrary, aList);
						}
					}
				}
			}
		}
		shared = new MISharedInfo[aList.size()];
		for (int i = 0; i < aList.size(); i++) {
			shared[i] = aList.get(i);
		}
	}

	void parseShared(MITuple sharedLibraryValue, List<MISharedInfo> aList) {
		MIValue from = sharedLibraryValue.getField("from"); //$NON-NLS-1$
		MIValue to = sharedLibraryValue.getField("to"); //$NON-NLS-1$
		MIValue symsRead = sharedLibraryValue.getField("syms-read"); //$NON-NLS-1$
		MIValue name = sharedLibraryValue.getField("name"); //$NON-NLS-1$
		if (from instanceof MIConst && to instanceof MIConst && symsRead instanceof MIConst
				&& name instanceof MIConst) {
			boolean isSymsRead = (((MIConst) symsRead).getCString()).equals("1") ? true : false; //$NON-NLS-1$
			MISharedInfo s = new MISharedInfo(((MIConst) from).getCString(), ((MIConst) from).getCString(), isSymsRead,
					((MIConst) name).getCString());
			aList.add(s);
		}
	}
}
