/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.service;

/**
 *  Utility class containing status methods to use with DSF services.
 * 
 * @since 1.0
 */
public class DsfServices {

    /**
     * Creates a properly formatted OSGi service filter for a DSF service based
     * on service class and session ID.
     * @param serviceClass Class of the service to create the filter for.
     * @param sessionId Session ID of the session that the service belongs to.
     * @return Filter string to identify the given service. 
     */
	public static String createServiceFilter(Class<?> serviceClass, String sessionId) {
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
