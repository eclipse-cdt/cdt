package org.eclipse.cdt.core.dom.ast;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

public class SemanticQueries {

	private static boolean isCallableWithNumberOfArguments(ICPPFunction function, int numArguments) {
		return function.getParameters().length >= numArguments
			&& function.getRequiredArgumentCount() <= numArguments; 
	}
	
	private static boolean isCopyOrMoveConstructor(ICPPConstructor constructor, boolean isRvalue) {
		// 12.8/2-3 [class.copy]: 
		// "A non-template constructor for class X is a copy [move] constructor
		//  if its first parameter is of type X&[&], const X&[&], volatile X&[&] 
		//  or const volatile X&[&], and either there are no other parametrs or
		//  else all other parametrs have default arguments."
		if (constructor instanceof ICPPFunctionTemplate)
			return false;
		if (!isCallableWithNumberOfArguments(constructor, 1))
			return false;
		IType firstArgumentType = constructor.getType().getParameterTypes()[0];
		firstArgumentType = SemanticUtil.getNestedType(firstArgumentType, TDEF);
		if (!(firstArgumentType instanceof ICPPReferenceType))
			return false;
		ICPPReferenceType firstArgReferenceType = (ICPPReferenceType) firstArgumentType;
		if (firstArgReferenceType.isRValueReference() != isRvalue)
			return false;
		firstArgumentType = firstArgReferenceType.getType();
		firstArgumentType = SemanticUtil.getNestedType(firstArgumentType, CVTYPE);
		return firstArgumentType.isSameType(constructor.getClassOwner());
	}
	
	public static boolean isMoveConstructor(ICPPConstructor constructor) {
		return isCopyOrMoveConstructor(constructor, true);
	}

	public static boolean isCopyConstructor(ICPPConstructor constructor) {
		return isCopyOrMoveConstructor(constructor, false);
	}
}
