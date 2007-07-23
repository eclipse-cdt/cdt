/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [165674] Sort subsystem configurations by priority then Id
 *******************************************************************************/
package org.eclipse.rse.internal.core.subsystems;

import java.util.Comparator;

import org.eclipse.rse.core.subsystems.ISubSystemConfigurationProxy;

public class SubSystemConfigurationProxyComparator implements Comparator {

	/**
	 * Constructor.
	 */
	public SubSystemConfigurationProxyComparator() {
	}

	/**
	 * Compares subsystem configuration proxies by Priority, then Id.
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object o1, Object o2) {
		
		if (o1 instanceof ISubSystemConfigurationProxy && o2 instanceof ISubSystemConfigurationProxy) {
			ISubSystemConfigurationProxy proxy1 = (ISubSystemConfigurationProxy)o1;
			ISubSystemConfigurationProxy proxy2 = (ISubSystemConfigurationProxy)o2;
			
			if (proxy1.getPriority() < proxy2.getPriority()) {
				return -1;
			}
			else if (proxy1.getPriority() > proxy2.getPriority()) {
				return +1;
			} else {
				return proxy1.getId().compareTo(proxy2.getId());
			}
		}
		return 0;
	}
}
