/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/

package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterGroup;
import org.eclipse.cdt.debug.mi.core.cdi.RegisterManager;
import org.eclipse.cdt.debug.mi.core.cdi.Session;

/**
 * RegisterGroup
 */
public class RegisterGroup extends CObject implements ICDIRegisterGroup {

	String fName;

	/**
	 * @param t
	 */
	public RegisterGroup(Target t, String name) {
		super(t);
		fName = name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterGroup#getRegisterDescriptors()
	 */
	public ICDIRegisterDescriptor[] getRegisterDescriptors() throws CDIException {
		Target target = (Target)getTarget();
		Session session = (Session)target.getSession();
		RegisterManager regMgr = session.getRegisterManager();
		return regMgr.getRegisterDescriptors(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterGroup#getName()
	 */
	public String getName() {
		return fName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterGroup#hasRegisters()
	 */
	public boolean hasRegisters() throws CDIException {
		return true;
	}

}
