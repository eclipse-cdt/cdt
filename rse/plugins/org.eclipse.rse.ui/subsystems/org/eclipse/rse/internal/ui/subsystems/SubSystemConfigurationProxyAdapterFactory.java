/********************************************************************************
 * Copyright (c) 2007 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David Dykstal (IBM) - 168870: created adapter for ui portions of SubSystemConfigurationProxy
 ********************************************************************************/
package org.eclipse.rse.internal.ui.subsystems;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.rse.core.subsystems.ISubSystemConfigurationProxy;

/**
 * This class is used to create the adapater for a SubSystemConfigurationProxy.
 * It is meant to be used in the AdapterManager framework of the platform.
 * Internal Use Only.
 */
public class SubSystemConfigurationProxyAdapterFactory implements IAdapterFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		Object result = null;
		if (adaptableObject instanceof ISubSystemConfigurationProxy) {
			if (adapterType == SubSystemConfigurationProxyAdapter.class) {
				ISubSystemConfigurationProxy proxy = (ISubSystemConfigurationProxy) adaptableObject;
				result = new SubSystemConfigurationProxyAdapter(proxy);
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return new Class[] {SubSystemConfigurationProxyAdapter.class};
	}

}
