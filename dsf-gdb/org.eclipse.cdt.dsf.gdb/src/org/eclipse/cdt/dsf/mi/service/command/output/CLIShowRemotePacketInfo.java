/*******************************************************************************
 * Copyright (c) 2016 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Teodor Madan (Freescale Semiconductor) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

/**
 * 'show remote ....-packet' returns if a remote packet feature is supported/enabled.
 * <br> 
 * sample output: 
 * <br> 
 * (gdb) show remote XXXXX-packet<br> 
 * Support for the `YYYYY' packet is auto-detected, currently enabled.<br> 
 * Support for the `YYYYY' packet is auto-detected, currently disabled.<br> 
 * Support for the `YYYYY' packet is auto-detected, currently unknown.<br> 
 * Support for the `YYYYY' packet is currently enabled.<br> 
 * Support for the `YYYYY' packet is currently disabled.<br> 
 * 
 * @since 5.0
 */
public class CLIShowRemotePacketInfo extends MIInfo {

	public enum State {
		ENABLED,
		DISABLED,
		UNKNOWN
	};
	
	final private static String ENABLED_TEXT = "currently enabled"; //$NON-NLS-1$
	final private static String DISABLED_TEXT = "currently disabled"; //$NON-NLS-1$
	
	private State fPacketState = State.UNKNOWN;
	
	public CLIShowRemotePacketInfo(MIOutput record) {
		super(record);
		parse();
	}

	protected void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			for (MIOOBRecord oob : out.getMIOOBRecords()) {
				if (oob instanceof MIConsoleStreamOutput) {
					String line = ((MIConsoleStreamOutput)oob).getString().trim();
					if (line.indexOf(ENABLED_TEXT) >= 0 ) {
						fPacketState = State.ENABLED;
						break;
					} else if (line.indexOf(DISABLED_TEXT) >= 0 ) {
						fPacketState = State.DISABLED;
						break;
					}
				}
			}
		}
	}

	public State getPacketState() {
		return fPacketState;
	}
}
