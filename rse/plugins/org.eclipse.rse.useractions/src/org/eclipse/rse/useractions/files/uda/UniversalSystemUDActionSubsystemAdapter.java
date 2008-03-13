/*********************************************************************************
 * Copyright (c) 2008 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Xuan Chen (IBM) - [222470] initial contribution.
 *********************************************************************************/
package org.eclipse.rse.useractions.files.uda;

import java.util.HashMap;

import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.useractions.files.uda.UDActionSubsystemUniversalFiles;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDActionSubsystem;

public class UniversalSystemUDActionSubsystemAdapter implements ISystemUDActionSubsystemAdapter {
	private HashMap uDActionSubsystems = new HashMap();
	
	public SystemUDActionSubsystem getSystemUDActionSubsystem(ISubSystemConfiguration ssc) {
		String configID = ssc.getId();
		
		SystemUDActionSubsystem thisuDActionSubsystem = (SystemUDActionSubsystem)uDActionSubsystems.get(configID);
		if (thisuDActionSubsystem == null)
		{
			thisuDActionSubsystem = new UDActionSubsystemUniversalFiles();
			thisuDActionSubsystem.setSubSystemFactory(ssc);
			uDActionSubsystems.put(configID, thisuDActionSubsystem);
		}
		
		return thisuDActionSubsystem;
	}

}
