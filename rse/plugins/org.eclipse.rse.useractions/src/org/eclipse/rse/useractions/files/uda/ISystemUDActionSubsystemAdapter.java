package org.eclipse.rse.useractions.files.uda;

import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDActionSubsystem;


public interface ISystemUDActionSubsystemAdapter {

	public SystemUDActionSubsystem getSystemUDActionSubsystem(ISubSystemConfiguration ssc);

}
