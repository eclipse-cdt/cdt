/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.tcmodification.extension;

import org.eclipse.cdt.managedbuilder.internal.tcmodification.ObjectSetList;

public abstract class ObjectSetListBasedDefinition {
	public static final int CONFLICT = 1;
	private ObjectSetList fList;
	
	protected ObjectSetListBasedDefinition(ObjectSetList list){
		fList = list;
	}

	public ObjectSetList getObjectSetList(){
		return fList;
	}
	
	public abstract int getType();
	
}
