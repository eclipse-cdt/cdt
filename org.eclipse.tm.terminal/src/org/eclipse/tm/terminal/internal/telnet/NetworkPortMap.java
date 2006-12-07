/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Michael Scharf (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 *******************************************************************************/
package org.eclipse.tm.terminal.internal.telnet;

import java.util.ArrayList;
import java.util.List;

public class NetworkPortMap {
	public static final String  PROP_NAMETGTCONS              = "tgtcons"; //$NON-NLS-1$
	public static final String  PROP_NAMETELNET                = "telnet"; //$NON-NLS-1$
	public static final String  PROP_VALUENET                  = "1233"; //$NON-NLS-1$
	public static final String  PROP_VALUETGTCONS             = "1232"; //$NON-NLS-1$
	public static final String  PROP_VALUETELNET               = "23"; //$NON-NLS-1$

	String[][] fPortMap=new String[][] {
			// portName, port
			{PROP_NAMETGTCONS, PROP_VALUETGTCONS},
			{PROP_NAMETELNET, PROP_VALUETELNET}
	};

	public String getDefaultNetworkPort() {
		return PROP_VALUETELNET;
	}

	public String findPortName(String strPort) {
		for (int i = 0; i < fPortMap.length; i++) {
			if(fPortMap[i][1].equals(strPort))
				return fPortMap[i][0];
		}
		return null;
	}

	public String findPort(String strPortName) {
		for (int i = 0; i < fPortMap.length; i++) {
			if(fPortMap[i][0].equals(strPortName))
				return fPortMap[i][1];
		}
		return null;
	}

	public List getNameTable() {
		List  names=new ArrayList();
		for (int i = 0; i < fPortMap.length; i++) {
			names.add(fPortMap[i][0]);
		}
		return names;
	}
}
