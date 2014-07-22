/*******************************************************************************
 * Copyright (c) 2014, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.mi.service.IMIExpressions;
import org.eclipse.cdt.dsf.mi.service.MIExpressions;
import org.eclipse.cdt.dsf.mi.service.MIVariableManager;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * This class allows further customization of {@link GdbDebugServicesFactory},
 * when extending {@link GdbDebugServicesFactory} itself is not convenient 
 * (because it has multiple subclasses already)
 * 
 * @since 4.5
 */
public class GdbDebugServicesFactoryExtensions {
	
	public IExpressions createExpressionService(DsfSession session) {
		IMIExpressions originialExpressionService = new MIExpressions(session) {
			@Override
			protected MIVariableManager createMIVariableManager() {
				return services_createMIVariableManager(getSession(), getServicesTracker());
			}
		};
		return new GDBPatternMatchingExpressions(session, originialExpressionService);
	}
	
	protected MIVariableManager services_createMIVariableManager(DsfSession session, 
			DsfServicesTracker servicesTracker) {
		return new MIVariableManager(session, servicesTracker);
	}
	
}