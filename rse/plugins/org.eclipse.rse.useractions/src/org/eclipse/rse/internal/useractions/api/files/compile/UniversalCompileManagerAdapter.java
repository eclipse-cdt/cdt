/*********************************************************************************
 * Copyright (c) 2008 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Xuan Chen (IBM) - [222470] initial contribution.
 *********************************************************************************/
package org.eclipse.rse.internal.useractions.api.files.compile;

import java.util.HashMap;

import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.useractions.files.compile.UniversalCompileManager;
import org.eclipse.rse.internal.useractions.ui.compile.SystemCompileManager;

/**
 * Universal Compile Manager Adapter.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same. Please do not use this API without consulting with
 * the <a href="http://www.eclipse.org/tm/">Target Management</a> team.
 * </p>
 */
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
