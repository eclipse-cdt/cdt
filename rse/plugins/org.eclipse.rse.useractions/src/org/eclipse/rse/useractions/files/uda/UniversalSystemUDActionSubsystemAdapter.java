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
