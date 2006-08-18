package org.eclipse.rse.core.internal.subsystems;

import java.util.Comparator;

import org.eclipse.rse.core.subsystems.ISubSystemConfigurationProxy;

public class SubSystemConfigurationProxyComparator implements Comparator {

	/**
	 * Constructor.
	 */
	public SubSystemConfigurationProxyComparator() {
	}

	/**
	 * Compares priorities of subsystem configuration proxies. 
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
				return 1;
			}
			else {
				return 0;
			}
		}
		else {
			return 0;
		}
	}
}