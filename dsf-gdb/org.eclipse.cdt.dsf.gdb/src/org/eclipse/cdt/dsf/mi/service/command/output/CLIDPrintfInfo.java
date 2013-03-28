/*******************************************************************************
 * Copyright (c) 2013 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * GDB/MI trace command output parsing.
 * 
 * ~"DPrintf 2 at 0x4035a9: file /scratch/marc/test/src/main.cxx, line 109"
 * 
 * @since 4.3
 */
public class CLIDPrintfInfo extends MIInfo {

	public CLIDPrintfInfo(MIOutput out) {
		super(out);
		parse();
	}

	private Integer fReference = null;
	
	public Integer getReference(){
		return fReference; 
	}

	protected void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			if (out == null) {
				fReference = null;
				return;
			}
			MIOOBRecord[] oobs = out.getMIOOBRecords();
			for (int i = 0; i < oobs.length; i++) {
				if (oobs[i] instanceof MIConsoleStreamOutput) {
					MIStreamRecord cons = (MIStreamRecord) oobs[i];
					String str = cons.getString().trim();
					if(str.length() > 0 ){
						Pattern pattern = Pattern.compile("^Dprintf\\s(\\d+)", Pattern.MULTILINE); //$NON-NLS-1$
						Matcher matcher = pattern.matcher(str);
						if (matcher.find()) {
							String id = matcher.group(1);
							try {
								fReference = Integer.parseInt(id);
							} catch (NumberFormatException e) {
								fReference = null;
							}
						}
					}
				}
			}
		}
	}
}
