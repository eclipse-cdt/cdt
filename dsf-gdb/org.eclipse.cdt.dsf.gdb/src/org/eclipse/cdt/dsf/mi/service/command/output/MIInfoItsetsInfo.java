/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;

import java.util.ArrayList;
import java.util.List;

/**
 * Num     Name   What
 *  2       b     1.1
 *  3       c     1.1,1.4
 *  4       a              
 *
 * @since 5.0
 */
public class MIInfoItsetsInfo extends MIInfo {

	public class ITSet {
		String id;
		String name;
		String spec;
		
		public ITSet(String id, String name, String spec) {
			this.id = id;
			this.name = name;
			this.spec = spec;
		}
		
		public String getId() {
			return id;
		}
		public String getName() {
			return name;
		}
		public String getSpec() {
			return spec;
		}
	}
	
	private List<ITSet> fITSets = new ArrayList<ITSet>();

    public MIInfoItsetsInfo(MIOutput rr) {
        super(rr);
        parse();
    }

    public ITSet[] getITSets() {
    	if (fITSets != null) {
    		ITSet[] itsetArray = new ITSet[fITSets.size()];
    		return fITSets.toArray(itsetArray);
    	}
    	return new ITSet[0];
    }

    void parse() {
    	boolean inPrintout = false;
		if (isDone()) {
			// CLI parsing
			MIOutput out = getMIOutput();
			MIOOBRecord[] oobs = out.getMIOOBRecords();
			for (int i = 0; i < oobs.length; i++ ) {
				MIOOBRecord oob = oobs[i];
				if (oob instanceof MIConsoleStreamOutput) {
					String line = ((MIConsoleStreamOutput)oob).getString().trim();
					
					// skip lines until we see something that looks like the printout we want
					if (isBeginningPrintout(line)) {
						inPrintout = true;
						continue;
					}
					if (!inPrintout) {
						continue;
					}
					
					String[] fields = line.split("\\s+"); //$NON-NLS-1$
					assert fields.length == 3;
					
					// hack - make groupId compatible with id returned by -list-thread-groups by adding a "u" in front
					fITSets.add(new ITSet("u"+fields[0],fields[1],fields[2])); //$NON-NLS-1$
				}
			}
		}
    }
    
    // sanity check - try to identify if the first line corresponds
    // to the "info itset" response printout
    // Num     Name   What
    private boolean isBeginningPrintout(String line) {
    	if (line.contains("Num") && line.contains("Name") && line.contains("What")) {  //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
    		return true;
    	}
    	return false;
    }
}
