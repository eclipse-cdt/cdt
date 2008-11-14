/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Kevin Doyle (IBM) - initial API and implementation
 ********************************************************************************/

package org.eclipse.rse.internal.useractions.files.compile;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.useractions.api.files.compile.ISystemCompileManagerAdapter;
import org.eclipse.rse.internal.useractions.ui.compile.SystemCompileManager;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.SystemAdapterHelpers;

public class CompilablePropertyTester extends PropertyTester {

	public static final String PROPERTY_ISCOMPILABLE = "iscompilable"; //$NON-NLS-1$

	
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {

		boolean test = ((Boolean) expectedValue).booleanValue();

		if (property.equals(PROPERTY_ISCOMPILABLE) && receiver != null && receiver instanceof IAdaptable) {
			ISubSystem subsystem = null;
			
			ISystemRemoteElementAdapter remoteAdapter = SystemAdapterHelpers.getRemoteAdapter(receiver);
			if (remoteAdapter != null) 
				subsystem = remoteAdapter.getSubSystem(receiver);
			
			ISystemCompileManagerAdapter adapter = (ISystemCompileManagerAdapter) ((IAdaptable) receiver).getAdapter(ISystemCompileManagerAdapter.class);
			if (subsystem != null)
			{
				SystemCompileManager compileManager = null;
				
				if (adapter != null)
					compileManager = adapter.getSystemCompileManager(subsystem.getSubSystemConfiguration());
				
				if (compileManager == null)
					compileManager = new UniversalCompileManager();	 // Use the Default Universal Compile Manager			
				
				compileManager.setSubSystemFactory(subsystem.getSubSystemConfiguration());	
				
				return compileManager.isCompilable(receiver) == test;
			}
		}		
		return test == false;
	}
	
}