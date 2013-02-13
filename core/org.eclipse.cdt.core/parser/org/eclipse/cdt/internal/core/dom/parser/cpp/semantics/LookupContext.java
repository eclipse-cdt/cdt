/*******************************************************************************
 * Copyright (c) 2013 Nathan Ridge.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nathan Ridge - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;

/**
 * Stores information about the context in which name lookups are to be performed.
 * When resolving dependent names inside a template, name lookup is performed
 * from two points: the point of definition of the template, and the point of
 * instantiation. This class allows tracking both points.
 */
public class LookupContext {
	private IASTNode fPointOfInstantiation;
	private IScope fPointOfDefinition;

	public LookupContext(IASTNode pointOfInstantiation, IBinding pointOfDefinition) {
		fPointOfInstantiation = pointOfInstantiation;
		if (pointOfDefinition != null) {
			if (pointOfDefinition instanceof ICPPClassType) {
				fPointOfDefinition = ((ICPPClassType) pointOfDefinition).getCompositeScope();
			}
			try {
				fPointOfDefinition = pointOfDefinition.getScope();
			} catch (DOMException e) {
			}		
		}
	}
	
	public IASTNode getPointOfInstantiation() {
		return fPointOfInstantiation;
	}
	
	public IScope getPointOfDefinition() {
		return fPointOfDefinition;
	}
}
