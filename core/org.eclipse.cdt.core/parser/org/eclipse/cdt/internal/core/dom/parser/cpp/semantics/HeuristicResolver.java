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
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownMember;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownMemberClass;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownType;

public class HeuristicResolver {
	/**
	 * Given a dependent type, heuristically tries to find a concrete scope (i.e. not an unknown scope)
	 * for it.
	 *
	 * @param point the point of instantiation for name lookups
	 */
	public static IScope findConcreteScopeForType(IType type, IASTNode point) {
		if (type instanceof ICPPUnknownType) {
			type = resolveUnknownType((ICPPUnknownType) type, point);
		}
		if (type instanceof ICompositeType) {
			return ((ICompositeType) type).getCompositeScope();
		}
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
			IScope lookupScope = null;
			if (lookupType instanceof ICPPClassType) {
				lookupScope = ((ICPPClassType) lookupType).getCompositeScope();
			} else if (lookupType instanceof ICPPEnumeration) {
				lookupScope = ((ICPPEnumeration) lookupType).asScope();
			}
			if (lookupScope != null) {
				LookupData lookup = new LookupData(name, templateArgs, point);
				lookup.fHeuristicBaseLookup = true;
				try {
					CPPSemantics.lookup(lookup, lookupScope);
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
	 * Given an unknown type, heuristically tries to find a concrete type (i.e. not an unknown type)
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
					IType argument = unary.getArgument().getType(point);
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
					IBinding[] candidates = lookInside(fieldOwner.getType(point), 
							id.isPointerDeref(), id.getName(), id.getTemplateArgs(), point);
					if (candidates.length == 1) {
						return typeForBinding(candidates[0]);
					}
				}
			} else if (evaluation instanceof EvalFunctionCall) {
				EvalFunctionCall evalFunctionCall = (EvalFunctionCall) evaluation;
				ICPPEvaluation function = evalFunctionCall.getArguments()[0];
				IType functionType = function.getType(point);
				if (functionType instanceof ICPPUnknownType) {
					functionType = resolveUnknownType((ICPPUnknownType) functionType, point);
				}
				return ExpressionTypes.typeFromFunctionCall(functionType);
			}
			// TODO(nathanridge): Handle more cases.
		} else if (type instanceof ICPPUnknownMemberClass) {
			ICPPUnknownMemberClass member = (ICPPUnknownMemberClass) type;
			IBinding[] candidates = lookInside(member.getOwnerType(), false, member.getNameCharArray(), 
					null, point);
			if (candidates.length == 1) { 
				if (candidates[0] instanceof IType) {
					return (IType) candidates[0];
				}
			}
		}
		return null;
	}
	
	/**
	 * Given an unknown binding, heuristically tries to find concrete bindings (i.e. not unknown bindings)
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
