/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;

/**
 * Specializations of all sorts of class types.
 * @since 5.1
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPClassSpecialization extends ICPPSpecialization, ICPPClassType {
	
	@Override
	ICPPClassType getSpecializedBinding();

	/**
	 * Creates a specialized binding for a member of the original class. The result is 
	 * a member of this class specialization.
	 */
	IBinding specializeMember(IBinding binding);
}
