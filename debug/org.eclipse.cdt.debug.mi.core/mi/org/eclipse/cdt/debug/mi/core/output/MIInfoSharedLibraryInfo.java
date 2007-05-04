package org.eclipse.cdt.debug.mi.core.output;

import java.util.ArrayList;

public class MIInfoSharedLibraryInfo extends MIInfo {

	MIShared[] shared = new MIShared[0];

	public MIInfoSharedLibraryInfo(MIOutput record) {
		super(record);
		parse();
	}

	private void parse() {
		if (isDone()) {
			ArrayList aList = new ArrayList();
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results =  rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("shlib-info")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MITuple)
						{
							MIResult[] libResults = ((MITuple)val).getMIResults();
							String from = ""; //$NON-NLS-1$
							String to = ""; //$NON-NLS-1$
							boolean syms = true;
							String name = ""; //$NON-NLS-1$
							
							for (int j = 0; j < libResults.length; j++) {
								if (libResults[j].getVariable().equals("description")) //$NON-NLS-1$
								{
									name = libResults[j].getMIValue().toString();
								}
								if (libResults[j].getVariable().equals("loaded_addr")) //$NON-NLS-1$
								{
									from = libResults[j].getMIValue().toString();
									to = from;
								}
							}
							MIShared s = new MIShared(from, to, syms, name);
							aList.add(s);				
						}
					}
				}
			}
			shared = (MIShared[]) aList.toArray(new MIShared[aList.size()]);
		}
	}

	public MIShared[] getMIShared() {
		return shared;
	}

}
