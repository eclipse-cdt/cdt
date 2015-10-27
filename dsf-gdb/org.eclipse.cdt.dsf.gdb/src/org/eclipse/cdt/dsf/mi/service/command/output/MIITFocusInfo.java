/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

/**
 * @since 4.9 
 */
public class MIITFocusInfo extends MIInfo {

	private static final String FOCUS_MARKER = "Focus is `"; //$NON-NLS-1$
	private String fFocus;

    public MIITFocusInfo(MIOutput output) {
    	super(output);
       	parse();
    }

    public String getFocus() {
		return fFocus;
	}
    
    private void parse() {
		if (isDone()) {
			// This is some CLI parsing
			MIOutput out = getMIOutput();
			for (MIOOBRecord oob : out.getMIOOBRecords()) {
				if (oob instanceof MIConsoleStreamOutput) {
					String line = ((MIConsoleStreamOutput)oob).getString().trim();
					int pos = line.indexOf(FOCUS_MARKER);
					if (pos >= 0 ) {
						pos += FOCUS_MARKER.length();
						fFocus = line.substring(pos);
						fFocus.substring(0, fFocus.indexOf("'")); //$NON-NLS-1$
						break;
					}
				}
			}
		}
    }
}