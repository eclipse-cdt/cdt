/*******************************************************************************
 * Copyright (c) 2012, 2016 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *     Nathan Ridge
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.PRVALUE;

import java.util.Arrays;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.InstantiationContext;
import org.eclipse.core.runtime.CoreException;

/**
 * Performs evaluation of an expression.
 */
public class EvalFunctionSet extends CPPDependentEvaluation {
	private final CPPFunctionSet fFunctionSet;
	private final boolean fQualified;
	private final boolean fAddressOf;

	// Where an EvalFunctionSet is created for an expression of the form 'obj.member_function',
	// the type of 'obj' (needed for correct overload resolution of 'member_function' later).
	// Otherwise null.
	private final IType fImpliedObjectType;

	// Used to represent an EvalFunctionSet with zero functions.
	// (We need the name in resolveFunction() - usually we get it from the CPPFunctionSet
	// by asking the first function in the set for its name.)
	// Exactly one of fFunctionSet and fName should be non-null.
	private final char[] fName;
	private boolean fCheckedIsConstantExpression;
	private boolean fIsConstantExpression;

	public EvalFunctionSet(CPPFunctionSet set, boolean qualified, boolean addressOf, IType impliedObjectType,
			IASTNode pointOfDefinition) {
		this(set, qualified, addressOf, impliedObjectType, findEnclosingTemplate(pointOfDefinition));
	}

	public EvalFunctionSet(CPPFunctionSet set, boolean qualified, boolean addressOf, IType impliedObjectType,
			IBinding templateDefinition) {
		super(templateDefinition);
		fFunctionSet = set;
		fQualified = qualified;
		fAddressOf = addressOf;
		fImpliedObjectType = impliedObjectType;
		fName = null;
	}

	public EvalFunctionSet(char[] name, boolean qualified, boolean addressOf, IASTNode pointOfDefinition) {
		this(name, qualified, addressOf, findEnclosingTemplate(pointOfDefinition));
	}

	public EvalFunctionSet(char[] name, boolean qualified, boolean addressOf, IBinding templateDefinition) {
		super(templateDefinition);
		fFunctionSet = null;
		fQualified = qualified;
		fAddressOf = addressOf;
		fImpliedObjectType = null;
		fName = name;
	}

	public CPPFunctionSet getFunctionSet() {
		return fFunctionSet;
	}

	public boolean isQualified() {
		return fQualified;
	}

	public boolean isAddressOf() {
		return fAddressOf;
	}

	public IType getImpliedObjectType() {
		return fImpliedObjectType;
	}

	@Override
	public boolean isInitializerList() {
		return false;
	}

	@Override
	public boolean isFunctionSet() {
		return true;
	}

	@Override
	public boolean isTypeDependent() {
		if (fFunctionSet == null)
			return true;
		final ICPPTemplateArgument[] args = fFunctionSet.getTemplateArguments();
		if (args != null) {
			for (ICPPTemplateArgument arg : args) {
				if (CPPTemplates.isDependentArgument(arg))
					return true;
			}
		}
		for (ICPPFunction f : fFunctionSet.getBindings()) {
			if (f instanceof ICPPUnknownBinding)
				return true;
		}
		return false;
	}

	@Override
	public boolean isValueDependent() {
		// The value of the function set (which function it resolves to) is
		// dependent under the same circumstances when its type is dependent.
		return isTypeDependent();
	}

	@Override
	public boolean isConstantExpression() {
		if (!fCheckedIsConstantExpression) {
			fCheckedIsConstantExpression = true;
			fIsConstantExpression = computeIsConstantExpression();
		}
		return fIsConstantExpression;
	}

	private boolean computeIsConstantExpression() {
		if (fFunctionSet == null)
			return false;
		for (ICPPFunction f : fFunctionSet.getBindings()) {
			if (!f.isConstexpr()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isEquivalentTo(ICPPEvaluation other) {
		if (!(other instanceof EvalFunctionSet)) {
			return false;
		}
		EvalFunctionSet o = (EvalFunctionSet) other;
		if (fFunctionSet == null) {
			return o.fFunctionSet == null;
		}
		return fFunctionSet.equals(o.fFunctionSet);
	}

	@Override
	public IType getType() {
		return new FunctionSetType(fFunctionSet, fAddressOf);
	}

	@Override
	public IValue getValue() {
		return IntegralValue.UNKNOWN;
	}

	@Override
	public ValueCategory getValueCategory() {
		return PRVALUE;
	}

	// Descriptive names for flags used during serialization.
	private final static short FLAG_ADDRESS_OF = ITypeMarshalBuffer.FLAG1;
	private final static short FLAG_HAS_FUNCTION_SET = ITypeMarshalBuffer.FLAG2;
	private final static short FLAG_HAS_TEMPLATE_ARGS = ITypeMarshalBuffer.FLAG3;
	private final static short FLAG_QUALIFIED = ITypeMarshalBuffer.FLAG4;

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		short firstBytes = ITypeMarshalBuffer.EVAL_FUNCTION_SET;
		if (fQualified)
			firstBytes |= FLAG_QUALIFIED;
		if (fAddressOf)
			firstBytes |= FLAG_ADDRESS_OF;
		if (fFunctionSet != null) {
			firstBytes |= FLAG_HAS_FUNCTION_SET;
			final ICPPFunction[] bindings = fFunctionSet.getBindings();
			final ICPPTemplateArgument[] args = fFunctionSet.getTemplateArguments();
			if (args != null)
				firstBytes |= FLAG_HAS_TEMPLATE_ARGS;

			buffer.putShort(firstBytes);
			buffer.putInt(bindings.length);
			for (ICPPFunction binding : bindings) {
				buffer.marshalBinding(binding);
			}
			if (args != null) {
				buffer.putInt(args.length);
				for (ICPPTemplateArgument arg : args) {
					buffer.marshalTemplateArgument(arg);
				}
			}
			buffer.marshalType(fImpliedObjectType);
		} else {
			buffer.putShort(firstBytes);
			buffer.putCharArray(fName);
		}
		marshalTemplateDefinition(buffer);
	}

	public static ICPPEvaluation unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		final boolean qualified = (firstBytes & FLAG_QUALIFIED) != 0;
		final boolean addressOf = (firstBytes & FLAG_ADDRESS_OF) != 0;
		if ((firstBytes & FLAG_HAS_FUNCTION_SET) != 0) {
			int bindingCount = buffer.getInt();
			ICPPFunction[] bindings = new ICPPFunction[bindingCount];
			for (int i = 0; i < bindings.length; i++) {
				bindings[i] = (ICPPFunction) buffer.unmarshalBinding();
			}
			ICPPTemplateArgument[] args = null;
			if ((firstBytes & FLAG_HAS_TEMPLATE_ARGS) != 0) {
				int len = buffer.getInt();
				args = new ICPPTemplateArgument[len];
				for (int i = 0; i < args.length; i++) {
					args[i] = buffer.unmarshalTemplateArgument();
				}
			}
			IType impliedObjectType = buffer.unmarshalType();
			IBinding templateDefinition = buffer.unmarshalBinding();
			return new EvalFunctionSet(new CPPFunctionSet(bindings, args, null), qualified, addressOf,
					impliedObjectType, templateDefinition);
		} else {
			char[] name = buffer.getCharArray();
			IBinding templateDefinition = buffer.unmarshalBinding();
			return new EvalFunctionSet(name, qualified, addressOf, templateDefinition);
		}
	}

	@Override
	public ICPPEvaluation instantiate(InstantiationContext context, int maxDepth) {
		if (fFunctionSet == null)
			return this;

		ICPPTemplateArgument[] originalArguments = fFunctionSet.getTemplateArguments();
		ICPPTemplateArgument[] arguments = originalArguments;
		if (originalArguments != null)
			arguments = instantiateArguments(originalArguments, context, true);

		IBinding originalOwner = fFunctionSet.getOwner();
		IBinding owner = originalOwner;
		if (owner instanceof ICPPUnknownBinding) {
			owner = resolveUnknown((ICPPUnknownBinding) owner, context);
		} else if (owner instanceof ICPPClassTemplate) {
			owner = resolveUnknown(CPPTemplates.createDeferredInstance((ICPPClassTemplate) owner), context);
		} else if (owner instanceof IType) {
			IType type = CPPTemplates.instantiateType((IType) owner, context);
			if (type instanceof IBinding)
				owner = (IBinding) type;
		}
		ICPPFunction[] originalFunctions = fFunctionSet.getBindings();
		ICPPFunction[] functions = originalFunctions;
		if (owner instanceof ICPPClassSpecialization && owner != originalOwner) {
			ICPPClassSpecialization ownerClass = (ICPPClassSpecialization) owner;
			functions = new ICPPFunction[originalFunctions.length];
			for (int i = 0; i < originalFunctions.length; i++) {
				functions[i] = (ICPPFunction) ownerClass.specializeMember(originalFunctions[i]);
			}
		}
		// No need to instantiate the implied object type. An EvalFunctioNSet should only be created
		// with an implied object type when that type is not dependent.
		if (Arrays.equals(arguments, originalArguments) && functions == originalFunctions)
			return this;
		return new EvalFunctionSet(new CPPFunctionSet(functions, arguments, null), fQualified, fAddressOf,
				fImpliedObjectType, getTemplateDefinition());
	}

	@Override
	public ICPPEvaluation computeForFunctionCall(ActivationRecord record, ConstexprEvaluationContext context) {
		return this;
	}

	/**
	 * Attempts to resolve the function using the parameters of a function call.
	 *
	 * @param args the arguments of a function call
	 * @return the resolved or the original evaluation depending on whether function resolution
	 *     succeeded or not
	 */
	public ICPPEvaluation resolveFunction(ICPPEvaluation[] args) {
		// Set up the LookupData.
		LookupData data;
		ICPPFunction[] functions = null;
		IASTNode point = CPPSemantics.getCurrentLookupPoint();
		if (fFunctionSet == null) {
			data = new LookupData(fName, null, point);
		} else {
			functions = fFunctionSet.getBindings();
			data = new LookupData(functions[0].getNameCharArray(), fFunctionSet.getTemplateArguments(), point);
			data.foundItems = functions;
		}
		data.setFunctionArguments(false, args);
		if (fImpliedObjectType != null)
			data.setImpliedObjectType(fImpliedObjectType);

		try {
			// Perform ADL if appropriate.
			if (!fQualified && fImpliedObjectType == null && !data.hasTypeOrMemberFunctionOrVariableResult()) {
				CPPSemantics.doArgumentDependentLookup(data);

				Object[] foundItems = (Object[]) data.foundItems;
				if (foundItems != null && (functions == null || foundItems.length > functions.length)) {
					// ADL found additional functions.
					int start = functions == null ? 0 : functions.length;
					for (int i = start; i < foundItems.length; i++) {
						Object obj = foundItems[i];
						if (obj instanceof ICPPFunction) {
							functions = ArrayUtil.append(ICPPFunction.class, functions, (ICPPFunction) obj);
						} else if (obj instanceof ICPPClassType) {
							functions = ArrayUtil.addAll(ICPPFunction.class, functions,
									((ICPPClassType) obj).getConstructors());
						}
					}

					// doArgumentDependentLookup() may introduce duplicates into the result. These must be
					// eliminated to avoid resolveFunction() reporting an ambiguity. (Normally, when
					// lookup() and doArgumentDependentLookup() are called on the same LookupData object, the
					// two functions coordinate using data stored in that object to eliminate
					// duplicates, but in this case lookup() was called before with a different
					// LookupData object and now we are only calling doArgumentDependentLookup()).
					functions = ArrayUtil.removeDuplicates(functions);
				}
			}

			// Perform template instantiation and overload resolution.
			IBinding binding = CPPSemantics.resolveFunction(data, functions, true, true);
			if (binding == null || binding instanceof IProblemBinding)
				return EvalFixed.INCOMPLETE;
			if (binding instanceof ICPPFunction && !(binding instanceof ICPPUnknownBinding))
				return new EvalBinding(binding, null, getTemplateDefinition());
		} catch (DOMException e) {
			CCorePlugin.log(e);
		}
		return this;
	}

	@Override
	public int determinePackSize(ICPPTemplateParameterMap tpMap) {
		int r = CPPTemplates.PACK_SIZE_NOT_FOUND;
		if (fFunctionSet != null) {
			ICPPTemplateArgument[] templateArguments = fFunctionSet.getTemplateArguments();
			if (templateArguments != null) {
				for (ICPPTemplateArgument arg : templateArguments) {
					r = CPPTemplates.combinePackSize(r, CPPTemplates.determinePackSize(arg, tpMap));
				}
			}
		}
		return r;
	}

	@Override
	public boolean referencesTemplateParameter() {
		return false;
	}

	@Override
	public boolean isNoexcept() {
		assert false; // Shouldn't exist outside of a dependent context
		return true;
	}
}
