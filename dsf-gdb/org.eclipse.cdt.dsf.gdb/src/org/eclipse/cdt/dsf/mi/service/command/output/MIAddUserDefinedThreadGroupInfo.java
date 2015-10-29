/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

/**
 * @since 5.1
 */
public class MIAddUserDefinedThreadGroupInfo extends MIInfo {

	private String fGroupId;
	
	public MIAddUserDefinedThreadGroupInfo(MIOutput record) {
		super(record);
		parse();
	}

	public String getGroupId() {
		return fGroupId;
	}
	
	private void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results =  rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("id")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MIConst) {
							fGroupId = ((MIConst)val).getString();
							return;
						}
					}
				}
			}
		}
	}
	
}
