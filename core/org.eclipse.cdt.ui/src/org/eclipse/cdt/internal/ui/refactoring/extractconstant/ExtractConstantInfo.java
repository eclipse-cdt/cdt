/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Institute for Software (IFS)- initial API and implementation 
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractconstant;

import org.eclipse.cdt.internal.ui.refactoring.MethodContext;
import org.eclipse.cdt.internal.ui.refactoring.NameNVisibilityInformation;

/**
 * @author Emanuel Graf IFS
 *
 */
public class ExtractConstantInfo extends NameNVisibilityInformation{
	
	private MethodContext mContext;

	public MethodContext getMContext() {
		return mContext;
	}
	public void setMContext(MethodContext context) {
		mContext = context;
	}
	
	

}
