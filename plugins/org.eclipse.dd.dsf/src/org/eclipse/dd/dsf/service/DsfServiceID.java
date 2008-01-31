/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.dd.dsf.service;

/**
 *  Convenience class to create the somewhat complicated service ID which
 *  can then be used with the DsfServicesTracker or OSGI services tracker
 *  to find a desired service.
 */

public class DsfServiceID {

	@SuppressWarnings("unchecked")
	public static String createServiceId(Class serviceClass, String sessionId) {

		String serviceId = 
			"(&"                        + //$NON-NLS-1$
			"(OBJECTCLASS="             + //$NON-NLS-1$
			serviceClass.getName()      + 
			")"                         + //$NON-NLS-1$
			"("                         + //$NON-NLS-1$
			IDsfService.PROP_SESSION_ID + 
			"="                         + //$NON-NLS-1$
			sessionId                   + 
			")"                         + //$NON-NLS-1$
			")"                         ; //$NON-NLS-1$

		return serviceId;
	}
}
