/*******************************************************************************
 * Copyright (c) 2015 Nathan Ridge.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nathan Ridge - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownMember;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownMemberClass;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownMemberClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownType;

/**
 * The purpose of this class is to perform heuristic binding resolution
 * in contexts where the results of ordinary binding resolution (whose
 * approach to templates is "defer actual resolution until template
 * arguments become available") are undesirable.
 *
 * Usually, this comes up in cases where the user is trying to invoke
 * certain editor functionality inside a template.
 *
 * For example, consider the following code:
 *
 *   struct Cat {
 *       void meow();
 *   };
 *
 *   template <typename T>
 *   struct B {
 *       Cat foo();
 *   };
 *
 *   template <typename T>
 *   void foo(B<T> a) {
 *       a.foo().
 *   }
 *
 * and suppose content assist is invoked after the "a.foo().".
 * To determine what completions to provide in that context, we try
 * to determine the type of 'a.foo()', and then look to see what
 * members are inside that type.
 *
 * However, because we're in a template, the type of 'a.foo()' is
 * a deferred / unknown type (in this case, a TypeOfDependentExpression),
 * so we don't know what members it has.
 *
 * HeuristicResolver maps that unknown type to a concrete type
 * (in this case, 'Cat') by applying the following heuristic:
 * whenever name lookup is deferred because the lookup scope is
 * the scope of a dependent template instantiation, assume the
 * instantiation uses the primary template (as opposed to a partial
 * or explicit specialization), and perform the lookup in the
 * primary template scope. This heuristic gives the right answer
 * in many cases, including this one.
 *
 * HeuristicResolver can handle some more complex situations as well,
 * such as metafunction calls, typedefs, and nested templates. See
 * CompletionTests.testDependentScopes_bug472818c for a test case
 * that pushes it to its limit.
 *
 * However, due to the nature of its heuristic, it cannot handle
 * cases where the correct answer requires selecting a specialization
 * rather than the primary template. Bug 487700 is on file for
 * implementing more advanced heuristics that could deal with this.
 */
public class HeuristicResolver {
	/**
	 * Given a dependent type, heuristically tries to find a concrete scope (i.e. not an unknown scope)
	 * for it.
	 */
	public static IScope findConcreteScopeForType(IType type) {
		if (type instanceof ICPPUnknownType) {
			type = resolveUnknownType((ICPPUnknownType) type,
					SemanticUtil.TDEF | SemanticUtil.REF | SemanticUtil.CVTYPE);
		}
		type = SemanticUtil.getNestedType(type, SemanticUtil.PTR);
		if (type instanceof ICompositeType) {
			return ((ICompositeType) type).getCompositeScope();
		} else if (type instanceof ICPPEnumeration) {
			return ((ICPPEnumeration) type).asScope();
		}
		return null;
	}

	/**
	 * Helper function for lookInside().
	 * Specializes the given bindings in the given context.
	 */
	private static IBinding[] specializeBindings(IBinding[] bindings, ICPPClassSpecialization context) {
		IBinding[] result = new IBinding[bindings.length];
		for (int i = 0; i < bindings.length; ++i) {
			result[i] = context.specializeMember(bindings[i]);
		}
		return result;
	}

	/**
	 * An extension of CPPDeferredClassInstance that implements ICPPClassSpecialization,
	 * allowing its members to be specialized via specializeMember().
	 */
	private static class CPPDependentClassInstance extends CPPDeferredClassInstance implements ICPPClassSpecialization {

		public CPPDependentClassInstance(ICPPDeferredClassInstance deferredInstance) {
			super(chooseTemplateForDeferredInstance(deferredInstance), deferredInstance.getTemplateArguments());
		}

		@Override
		public ICPPClassType getSpecializedBinding() {
			return (ICPPClassType) super.getSpecializedBinding();
		}

		@Override
		@Deprecated
		public IBinding specializeMember(IBinding binding, IASTNode point) {
			return specializeMember(binding);
		}

		// This overload of specializeMember() is all we're interested in.
		// Everything else is unsupported.
		@Override
		public IBinding specializeMember(IBinding binding) {
			return CPPTemplates.createSpecialization(this, binding);
		}

		@Override
		public ICPPBase[] getBases(IASTNode point) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ICPPConstructor[] getConstructors(IASTNode point) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ICPPField[] getDeclaredFields(IASTNode point) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ICPPMethod[] getMethods(IASTNode point) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ICPPMethod[] getAllDeclaredMethods(IASTNode point) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ICPPMethod[] getDeclaredMethods(IASTNode point) {
			throw new UnsupportedOperationException();
		}

		@Override
		public IBinding[] getFriends(IASTNode point) {
			throw new UnsupportedOperationException();
		}

		@Override
		public IField[] getFields(IASTNode point) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ICPPClassType[] getNestedClasses(IASTNode point) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ICPPUsingDeclaration[] getUsingDeclarations(IASTNode point) {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Represents a lookup of a name in a primary template scope.
	 * The set of such lookups during a heuristic resolution operation is
	 * tracked, to avoid infinite recursion.
	 */
	private static class HeuristicLookup {
		public IScope scope;
		public char[] name;

		public HeuristicLookup(IScope scope, char[] name) {
			this.scope = scope;
			this.name = name;
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof HeuristicLookup)) {
				return false;
			}
			HeuristicLookup otherLookup = (HeuristicLookup) other;
			return scope == otherLookup.scope && CharArrayUtils.equals(name, otherLookup.name);
		}

		@Override
		public int hashCode() {
			return scope.hashCode() * (29 + name.hashCode());
		}
	}

	/**
	 * Helper function for resolveUnknownType() and resolveUnknownBinding().
	 * Heuristically resolves the given unknown type and performs name lookup inside it.
	 *
	 * If name lookup is performed inside a template scope to approximate lookup
	 * in the scope of a dependent instantiation, the lookup results are
	 * specialized in the context of the dependent instantiation.
	 *
	 * @param ownerType the type to perform name lookup inside
	 * @param isPointerDeref true if 'ownerType' is a pointer type
	 * @param name the name to be looked up
	 * @param templateArgs template arguments following the name, if any
	 * @param lookupSet the set of lookups performed so far; lookups during this call are added to this
	 * @param point point of instantiation for name lookups
	 * @return results of the name lookup
	 */
	private static IBinding[] lookInside(IType ownerType, boolean isPointerDeref, char[] name,
			ICPPTemplateArgument[] templateArgs, Set<HeuristicLookup> lookupSet) {
		// If this is a pointer dereference, the pointer type might be outside of the dependent type.
		ownerType = SemanticUtil.getSimplifiedType(ownerType);
		if (isPointerDeref && ownerType instanceof IPointerType) {
			ownerType = ((IPointerType) ownerType).getType();
			isPointerDeref = false;
		}

		if (ownerType instanceof IQualifierType) {
			ownerType = ((IQualifierType) ownerType).getType();
		}

		IType lookupType = ownerType;
		ICPPClassSpecialization specializationContext = null;
		if (lookupType instanceof ICPPUnknownType) {
			// Here we have a loop similar to the one in resolveUnknownType(), but we stop when
			// we get a result that's an ICPPClassSpecialization or an ICPPDeferredClassInstance,
			// so we can use it to specialize the lookup results as appropriate.
			while (true) {
				if (lookupType instanceof ICPPClassSpecialization) {
					specializationContext = (ICPPClassSpecialization) lookupType;
					lookupType = specializationContext.getSpecializedBinding();
					break;
				} else if (lookupType instanceof ICPPDeferredClassInstance) {
					specializationContext = new CPPDependentClassInstance((ICPPDeferredClassInstance) lookupType);
					lookupType = specializationContext.getSpecializedBinding();
					break;
				}
				IType resolvedType = resolveUnknownTypeOnce((ICPPUnknownType) lookupType, lookupSet);
				resolvedType = SemanticUtil.getNestedType(resolvedType,
						SemanticUtil.TDEF | SemanticUtil.REF | SemanticUtil.CVTYPE);

				// If this is a pointer dereference, and the pointer type wasn't
				// outside the
				// dependent type, it might be inside the dependent type.
				if (isPointerDeref) {
					if (resolvedType instanceof IPointerType) {
						isPointerDeref = false;
						resolvedType = ((IPointerType) resolvedType).getType();
					} else {
						resolvedType = null;
					}
				}

				resolvedType = SemanticUtil.getNestedType(resolvedType, SemanticUtil.CVTYPE | SemanticUtil.TDEF);

				if (resolvedType == lookupType || !(resolvedType instanceof ICPPUnknownType)) {
					lookupType = resolvedType;
					break;
				} else {
					lookupType = resolvedType;
					continue;
				}
			}

		}

		IScope lookupScope = null;
		if (lookupType instanceof ICPPClassType) {
			lookupScope = ((ICPPClassType) lookupType).getCompositeScope();
		} else if (lookupType instanceof ICPPEnumeration) {
			lookupScope = ((ICPPEnumeration) lookupType).asScope();
		}
		if (lookupScope != null) {
			HeuristicLookup entry = new HeuristicLookup(lookupScope, name);
			if (lookupSet.add(entry)) {
				LookupData lookup = new LookupData(name, templateArgs, CPPSemantics.getCurrentLookupPoint());
				lookup.fHeuristicBaseLookup = true;
				try {
					CPPSemantics.lookup(lookup, lookupScope);
					IBinding[] foundBindings = lookup.getFoundBindings();
					if (foundBindings.length > 0) {
						if (specializationContext != null) {
							foundBindings = specializeBindings(foundBindings, specializationContext);
						}
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
	 * Returns the type of a binding.
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
	 * Given an unknown type, heuristically tries to find a concrete type
	 * (i.e. not an unknown type) corresponding to it.
	 *
	 * Returns null if no heuristic resolution could be performed.
	 *
	 * Multiple rounds of resolution are performed, as the result of a single
	 * round may yield a type which is still dependent. Resolution stops when
	 * a concrete type is found, or the resolution of the last resolution
	 * round is the same as the result of the previous resolution round.
	 * In between each round, typedefs are unwrapped.
	 */
	public static IType resolveUnknownType(ICPPUnknownType type) {
		return resolveUnknownType(type, SemanticUtil.TDEF | SemanticUtil.CVTYPE);
	}

	/**
	 * Similar to resolveUnknownType(type, point), but allows specifying
	 * things other than typedefs to unwrap between rounds of resolution
	 * (e.g. references).
	 */
	private static IType resolveUnknownType(ICPPUnknownType type, int unwrapOptions) {
		while (true) {
			Set<HeuristicLookup> lookupSet = new HashSet<>();
			IType resolvedType = resolveUnknownTypeOnce(type, lookupSet);
			resolvedType = SemanticUtil.getNestedType(resolvedType, unwrapOptions);

			if (resolvedType != type && resolvedType instanceof ICPPUnknownType) {
				type = (ICPPUnknownType) resolvedType;
				continue;
			}
			return resolvedType;
		}
	}

	/**
	 * Heuristically choose between the primary template and any partial specializations
	 * for a deferred template instance.
	 */
	private static ICPPClassTemplate chooseTemplateForDeferredInstance(ICPPDeferredClassInstance instance) {
		ICPPClassTemplate template = instance.getClassTemplate();
		if (!instance.isExplicitSpecialization()) {
			try {
				IBinding partial = CPPTemplates.selectSpecialization(template, instance.getTemplateArguments(), false);
				if (partial != null && partial instanceof ICPPTemplateInstance)
					return (ICPPClassTemplate) ((ICPPTemplateInstance) partial).getTemplateDefinition();
			} catch (DOMException e) {
			}
		}
		return template;
	}

	private static IType resolveEvalType(ICPPEvaluation evaluation, Set<HeuristicLookup> lookupSet) {
		if (evaluation instanceof EvalUnary) {
			EvalUnary unary = (EvalUnary) evaluation;
			// Handle the common case of a dependent type representing the
			// result of dereferencing another dependent type.
			if (unary.getOperator() == IASTUnaryExpression.op_star) {
				IType argument = unary.getArgument().getType();
				if (argument instanceof ICPPUnknownType) {
					IType resolved = resolveUnknownType((ICPPUnknownType) argument);
					if (resolved instanceof IPointerType) {
						return ((IPointerType) resolved).getType();
					}
				}
			}
		} else if (evaluation instanceof EvalID) {
			EvalID id = (EvalID) evaluation;
			ICPPEvaluation fieldOwner = id.getFieldOwner();
			if (fieldOwner != null) {
				IType fieldOwnerType = fieldOwner.getType();
				IBinding[] candidates = lookInside(fieldOwnerType, id.isPointerDeref(), id.getName(),
						id.getTemplateArgs(), lookupSet);
				if (candidates.length > 0) {
					// If there is more than one candidate, for now just
					// choose the first one. A better thing to do would
					// be to perform heuristic overload resolution (TODO).
					return typeForBinding(candidates[0]);
				}
			}
		} else if (evaluation instanceof EvalFunctionCall) {
			EvalFunctionCall evalFunctionCall = (EvalFunctionCall) evaluation;
			ICPPEvaluation function = evalFunctionCall.getArguments()[0];
			IType functionType = function.getType();
			if (functionType instanceof ICPPUnknownType) {
				functionType = resolveUnknownType((ICPPUnknownType) functionType);
			}
			return ExpressionTypes.typeFromFunctionCall(functionType);
		} else if (evaluation instanceof EvalMemberAccess) {
			IBinding member = ((EvalMemberAccess) evaluation).getMember();
			// Presumably the type will be unknown. That's fine, it will be
			// resolved during subsequent resolution rounds.
			return typeForBinding(member);
		} else if (evaluation instanceof EvalTypeId) {
			EvalTypeId evalTypeId = (EvalTypeId) evaluation;
			IType result = evalTypeId.getInputType();
			if (evalTypeId.representsNewExpression()) {
				result = new CPPPointerType(result);
			}
			return result;
		} else if (evaluation instanceof EvalBinding) {
			return evaluation.getType();
		}
		// TODO(nathanridge): Handle more cases.
		return null;
	}

	/**
	 * Helper function for {@link #resolveUnknownType} which does one round of resolution.
	 */
	private static IType resolveUnknownTypeOnce(ICPPUnknownType type, Set<HeuristicLookup> lookupSet) {
		if (type instanceof ICPPDeferredClassInstance) {
			ICPPDeferredClassInstance deferredInstance = (ICPPDeferredClassInstance) type;
			return chooseTemplateForDeferredInstance(deferredInstance);
		} else if (type instanceof TypeOfDependentExpression) {
			ICPPEvaluation evaluation = ((TypeOfDependentExpression) type).getEvaluation();
			return resolveEvalType(evaluation, lookupSet);
		} else if (type instanceof ICPPUnknownMemberClass) {
			ICPPUnknownMemberClass member = (ICPPUnknownMemberClass) type;
			IType ownerType = member.getOwnerType();
			IBinding[] candidates = lookInside(ownerType, false, member.getNameCharArray(), null, lookupSet);
			if (candidates.length == 1) {
				if (candidates[0] instanceof IType) {
					IType result = (IType) candidates[0];
					if (type instanceof ICPPUnknownMemberClassInstance) {
						ICPPTemplateArgument[] args = ((ICPPUnknownMemberClassInstance) type).getArguments();
						if (result instanceof ICPPClassTemplate) {
							result = (IType) CPPTemplates.instantiate((ICPPClassTemplate) result, args);
						} else if (result instanceof ICPPAliasTemplate) {
							result = (IType) CPPTemplates.instantiateAliasTemplate((ICPPAliasTemplate) result, args);
						}
					}
					return result;
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
	 */
	public static IBinding[] resolveUnknownBinding(ICPPUnknownBinding binding) {
		if (binding instanceof ICPPDeferredClassInstance) {
			return new IBinding[] { chooseTemplateForDeferredInstance((ICPPDeferredClassInstance) binding) };
		} else if (binding instanceof ICPPUnknownMember) {
			Set<HeuristicLookup> lookupSet = new HashSet<>();
			return lookInside(((ICPPUnknownMember) binding).getOwnerType(), false, binding.getNameCharArray(), null,
					lookupSet);
		} else if (binding instanceof ICPPUnknownType) {
			IType resolved = resolveUnknownType((ICPPUnknownType) binding);
			if (resolved != binding && resolved instanceof IBinding) {
				return new IBinding[] { (IBinding) resolved };
			}
		}
		return IBinding.EMPTY_BINDING_ARRAY;
	}
}
