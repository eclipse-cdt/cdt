/*
 * Created on Jan 14, 2004
 *
 * Copyright (c) 2002,2003 QNX Software Systems Ltd.
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.testplugin;

import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICOwner;
import org.eclipse.core.runtime.CoreException;

public class TestProject implements ICOwner {

	public void configure(ICDescriptor cproject) throws CoreException {
	}

	public void update(ICDescriptor cproject, String extensionID) throws CoreException {
	}
}
