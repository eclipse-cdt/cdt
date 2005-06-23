/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.model; 

import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterGroup;
import org.eclipse.cdt.debug.core.model.IRegisterDescriptor;
 
/**
 * A register descriptor. Temporary, need to change the related CDI interfaces. 
 */
public class CRegisterDescriptor implements IRegisterDescriptor {

	private String fName;
	private String fGroupName;
	private ICDIRegisterDescriptor fCDIDescriptor = null;

	/** 
	 * Constructor for CRegisterDescriptor. 
	 */
	public CRegisterDescriptor( String name, String groupName ) {
		fName = name;
		fGroupName = groupName;
	}

	/** 
	 * Constructor for CRegisterDescriptor. 
	 */
	public CRegisterDescriptor( ICDIRegisterGroup group, ICDIRegisterDescriptor desc ) {
		fName = desc.getName();
		fGroupName = group.getName();
		fCDIDescriptor = desc;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IRegisterDescriptor#getName()
	 */
	public String getName() {
		return fName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IRegisterDescriptor#getGroupName()
	 */
	public String getGroupName() {
		return fGroupName;
	}

	public ICDIRegisterDescriptor getCDIDescriptor() {
		return fCDIDescriptor;
	}
	
	public void setCDIDescriptor( ICDIRegisterDescriptor descriptor ) {
		fCDIDescriptor = descriptor;
	}
}
