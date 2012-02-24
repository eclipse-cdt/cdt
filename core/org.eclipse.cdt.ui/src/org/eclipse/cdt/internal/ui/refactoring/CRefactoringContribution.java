/*******************************************************************************
 * Copyright (c) 2009, 2012 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Institute for Software (IFS)- initial API and implementation
 *     Sergey Prigogin (Google) 
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import java.util.Map;

import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

/**
 * @author Emanuel Graf IFS
 */
public abstract class CRefactoringContribution extends RefactoringContribution {

	public CRefactoringContribution() {
		super();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Map retrieveArgumentMap(RefactoringDescriptor descriptor) {
		if (descriptor instanceof CRefactoringDescriptor) {
			CRefactoringDescriptor refDesc = (CRefactoringDescriptor) descriptor;
			return refDesc.getParameterMap();
		}
		return super.retrieveArgumentMap(descriptor);
	}
}