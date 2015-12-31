/*******************************************************************************
 * Copyright (c) 2010, 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;


import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * [Current thread is 1 (Thread 0xb7cc56b0 (LWP 5488))]
 * 
 * @since 3.0
 */
public class CLIThreadInfo extends MIInfo {

	private Integer fCurrentThread;
	
	public CLIThreadInfo(MIOutput out) {
		super(out);
		parse();
	}

	public Integer getCurrentThread(){
		return fCurrentThread; 
	}

	protected void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIOOBRecord[] oobs = out.getMIOOBRecords();
			for (int i = 0; i < oobs.length; i++) {
				if (oobs[i] instanceof MIConsoleStreamOutput) {
					MIStreamRecord cons = (MIStreamRecord) oobs[i];
					String str = cons.getString();
					// We are interested in finding the current thread
					parseThreadInfo(str.trim());
				}
			}
		}
	}

	protected void parseThreadInfo(String str) {
			// Fetch the OS ThreadId & Find the current thread 
			if(!str.isEmpty() ){
				Pattern pattern = Pattern.compile("Current thread is (\\d+)",  Pattern.MULTILINE); //$NON-NLS-1$
				Matcher matcher = pattern.matcher(str);
				if (matcher.find()) {
					String id = matcher.group(1).trim();
					fCurrentThread = Integer.parseInt(id);
				}
			}
	}
}
