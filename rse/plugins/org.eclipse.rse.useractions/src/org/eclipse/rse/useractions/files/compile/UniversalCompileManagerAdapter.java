package org.eclipse.rse.useractions.files.compile;

import java.util.HashMap;

import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.useractions.files.compile.UniversalCompileManager;
import org.eclipse.rse.internal.useractions.ui.compile.SystemCompileManager;

public class UniversalCompileManagerAdapter implements
		ISystemCompileManagerAdapter {
	private HashMap compileManagers = new HashMap();
	
	public SystemCompileManager getSystemCompileManager(ISubSystemConfiguration ssc) {
		String configID = ssc.getId();
		
		SystemCompileManager thisCompileManager = (SystemCompileManager)compileManagers.get(configID);
		if (thisCompileManager == null)
		{
			thisCompileManager = new UniversalCompileManager();
			thisCompileManager.setSubSystemFactory(ssc);
			compileManagers.put(configID, thisCompileManager);
		}
		
		return thisCompileManager;
	}

}
