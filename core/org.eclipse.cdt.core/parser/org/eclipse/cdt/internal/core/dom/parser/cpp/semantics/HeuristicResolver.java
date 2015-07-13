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
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownMember;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownType;

public class HeuristicResolver {
	/**
	 * Given a dependent type, heuristically try to find a concrete scope (i.e. not an unknown scope) for it.
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
	
	/**
	 * Helper function for resolveUnknownType() and resolveUnknownBinding().
	 * Heuristically resolves the given unknown type and performs name lookup inside it.
	 * 
	 * @param ownerType the type to perform name lookup inside
	 * @param isPointerDeref true if 'ownerType' is a pointer type
	 * @param name the name to be looked up
	 * @param templateArgs template arguments following the name, if any
	 * @param point point of instantiation for name lookups
	 * @return results of the name lookup
	 */
	private static IBinding[] lookInside(IType ownerType, boolean isPointerDeref, char[] name, 
			ICPPTemplateArgument[] templateArgs, IASTNode point) {
		// The pointer type might be outside of the dependent type...
		ownerType = SemanticUtil.getSimplifiedType(ownerType);
		if (isPointerDeref && ownerType instanceof IPointerType) {
			ownerType = ((IPointerType) ownerType).getType();
			isPointerDeref = false;
		}
		if (ownerType instanceof ICPPUnknownType) {
			IType lookupType = resolveUnknownType((ICPPUnknownType) ownerType, point);
			// ... or inside the dependent type.
			if (isPointerDeref) {
				lookupType = SemanticUtil.getSimplifiedType(lookupType);
				if (lookupType instanceof IPointerType) {
					lookupType = ((IPointerType) lookupType).getType();
				} else {
					lookupType = null;
				}
			}
			if (lookupType instanceof ICPPClassType) {
				LookupData lookup = new LookupData(name, templateArgs, point);
				lookup.fHeuristicBaseLookup = true;
				try {
					CPPSemantics.lookup(lookup, ((ICPPClassType) lookupType).getCompositeScope());
					IBinding[] foundBindings = lookup.getFoundBindings();
					if (foundBindings.length > 0) {
						return foundBindings;
					}
				} catch (DOMException e) {
				}
			}
		}
		return IBinding.EMPTY_BINDING_ARRAY;
	}
	
	/**
	 * Helper function for resolveUnknownType(). 
	 * Returns the type of a binding, or if the binding is a type, that type.
	 */
	private static IType typeForBinding(IBinding binding) {
		if (binding instanceof IType) {
			return (IType) binding;
		} else if (binding instanceof IVariable) {
			return ((IVariable) binding).getType();
		} else if (binding instanceof IEnumerator) {
			return ((IEnumerator) binding).getType();
		} else if (binding instanceof IFunction) {
			return ((IFunction) binding).getType();
		}
		return null;
	}
	
	/**
	 * Given an unknown type, heuristically try to find a concrete type (i.e. not an unknown type)
	 * corresponding to it.
	 * 
	 * Returns null if no heuristic resolution could be performed.

	 * @param point the point of instantiation for lookups
	 */
	private static IType resolveUnknownType(ICPPUnknownType type, IASTNode point) {
		if (type instanceof ICPPDeferredClassInstance) {
			return ((ICPPDeferredClassInstance) type).getClassTemplate();
		} else if (type instanceof TypeOfDependentExpression) {
			ICPPEvaluation evaluation = ((TypeOfDependentExpression) type).getEvaluation();
			if (evaluation instanceof EvalUnary) {
				EvalUnary unary = (EvalUnary) evaluation;
				// Handle the common case of a dependent type representing the result of
				// dereferencing another dependent type.
				if (unary.getOperator() == IASTUnaryExpression.op_star) {
					IType argument = unary.getArgument().getTypeOrFunctionSet(point);
					if (argument instanceof ICPPUnknownType) {
						IType resolved = resolveUnknownType((ICPPUnknownType) argument, point);
						resolved = SemanticUtil.getSimplifiedType(resolved);
						if (resolved instanceof IPointerType) {
							return ((IPointerType) resolved).getType();
						}
					}
				}
			} else if (evaluation instanceof EvalID) {
				EvalID id = (EvalID) evaluation;
				ICPPEvaluation fieldOwner = id.getFieldOwner();
				if (fieldOwner != null) {
					IBinding[] candidates = lookInside(fieldOwner.getTypeOrFunctionSet(point), 
							id.isPointerDeref(), id.getName(), id.getTemplateArgs(), point);
					if (candidates.length == 1) {
						return typeForBinding(candidates[0]);
					}
				}
			}
			// TODO(nathanridge): Handle more cases.
		}
		return null;
	}
	
	/**
	 * Given an unknown binding, heuristically try to find concrete bindings (i.e. not unknown bindings)
	 * corresponding to it.
	 * 
	 * Returns an empty array if no heuristic resolution could be performed.

	 * @param point the point of instantiation for lookups
	 */
	public static IBinding[] resolveUnknownBinding(ICPPUnknownBinding binding, IASTNode point) {
		if (binding instanceof ICPPDeferredClassInstance) {
			return new IBinding[] { ((ICPPDeferredClassInstance) binding).getClassTemplate() };
		} else if (binding instanceof ICPPUnknownMember) {
			return lookInside(((ICPPUnknownMember) binding).getOwnerType(), false,
					binding.getNameCharArray(), null, point);
		}
		return IBinding.EMPTY_BINDING_ARRAY;
	}
}
