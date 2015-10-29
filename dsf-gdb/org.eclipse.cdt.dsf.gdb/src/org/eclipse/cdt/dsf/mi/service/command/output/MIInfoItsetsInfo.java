/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;

/**
 * Num     Name   What
 *  2       b     1.1
 *  3       c     1.1,1.4
 *  4       a              
 *
 * @since 4.9
 */
public class MIInfoItsetsInfo extends MIInfo {

	public class ITSet {
		String id;
		String name;
		String content;
		
		public String getId() {
			return id;
		}
		public String getName() {
			return name;
		}
		public String getContent() {
			return content;
		}
	}
	
    private ITSet[] fITSets;

    public MIInfoItsetsInfo(MIOutput rr) {
        super(rr);
        parse();
    }

    public ITSet[] getITSets() {
        return fITSets;
    }

    void parse() {
		if (isDone()) {
			// CLI parsing
			MIOutput out = getMIOutput();
			MIOOBRecord[] oobs = out.getMIOOBRecords();
			// Skip the first line by starting at 1
			fITSets = new ITSet[oobs.length -1];
			for (int i = 1; i < oobs.length; i++ ) {
				MIOOBRecord oob = oobs[i];
				if (oob instanceof MIConsoleStreamOutput) {
					String line = ((MIConsoleStreamOutput)oob).getString().trim();
					String[] fields = line.split("\\s+"); //$NON-NLS-1$
					assert fields.length == 3;
					fITSets[i-1] = new ITSet();
					fITSets[i-1].id = fields[0];
					fITSets[i-1].name = fields[1];
					fITSets[i-1].content = fields[2];
				}
			}
		}
    }
}
