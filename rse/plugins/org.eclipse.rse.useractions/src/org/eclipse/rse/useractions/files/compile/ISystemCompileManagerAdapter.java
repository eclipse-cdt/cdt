package org.eclipse.rse.useractions.files.compile;

import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.useractions.ui.compile.SystemCompileManager;


public interface ISystemCompileManagerAdapter {

	public SystemCompileManager getSystemCompileManager(ISubSystemConfiguration ssc);

}
