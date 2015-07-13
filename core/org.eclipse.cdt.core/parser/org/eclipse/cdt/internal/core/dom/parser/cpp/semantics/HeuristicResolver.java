/*******************************************************************************
 * Copyright (c) 2015 Nathan Ridge.
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
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;

public class HeuristicResolver {
	/**
	 * Given a dependent type, heuristically tries to find a concrete scope (i.e. not an unknown scope)
	 * for it.
	 *
	 * @param point the point of instantiation for name lookups
	 */
	public static IScope findConcreteScopeForType(IType type, IASTNode point) {
		if (type instanceof ICPPDeferredClassInstance) {
			// If this scope is for a deferred-class-instance, use the scope of the primary template.
			ICPPDeferredClassInstance instance = (ICPPDeferredClassInstance) type;
			return instance.getClassTemplate().getCompositeScope();
		} else if (type instanceof TypeOfDependentExpression) {
			// If this scope is for the id-expression of a field reference, and the field owner
			// is a deferred-class-instance, look up the field in the scope of the primary template,
			// and use the scope of the resulting field type.
			ICPPEvaluation evaluation = ((TypeOfDependentExpression) type).getEvaluation();
			if (evaluation instanceof EvalID) {
				EvalID evalId = (EvalID) evaluation;
				ICPPEvaluation fieldOwner = evalId.getFieldOwner();
				if (fieldOwner != null) {
					IType fieldOwnerType = fieldOwner.getTypeOrFunctionSet(point);
					if (fieldOwnerType instanceof ICPPDeferredClassInstance) {
						ICPPDeferredClassInstance instance = (ICPPDeferredClassInstance) fieldOwnerType;
						IScope scope = instance.getClassTemplate().getCompositeScope();
						LookupData lookup = new LookupData(evalId.getName(), evalId.getTemplateArgs(), point);
						lookup.qualified = evalId.isQualified();
						try {
							CPPSemantics.lookup(lookup, scope);
						} catch (DOMException e) {
							return null;
						}
						IBinding[] bindings = lookup.getFoundBindings();
						if (bindings.length == 1 && bindings[0] instanceof IField) {
							IType fieldType = ((IField) bindings[0]).getType();
							if (fieldType instanceof ICompositeType) {
								return ((ICompositeType) fieldType).getCompositeScope(); 
							}
						}
					}
				}
			}
		}
		// TODO(nathanridge): Handle more cases.
		return null;
	}
}
