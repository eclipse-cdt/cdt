/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Dec 9, 2004
 */
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IPointerType;

/**
 * @author aniefer
 */
public interface ICPPPointerToMemberType extends IPointerType {

	/**
	 * Get the class to whose members this points to
	 * 
	 * @return
	 */
	public ICPPClassType getMemberOfClass();
}
