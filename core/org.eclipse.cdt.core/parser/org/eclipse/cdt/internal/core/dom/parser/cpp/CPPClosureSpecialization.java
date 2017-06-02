package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLambdaExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;

/**
 * Specialization of a closure type.
 */
public class CPPClosureSpecialization extends CPPClosureType implements ICPPClassSpecialization {
	private CPPClosureType fSpecialized;
	private ICPPTemplateParameterMap fMap;
	
	public CPPClosureSpecialization(ICPPASTLambdaExpression lambda, CPPClosureType specialized,
			InstantiationContext context) {
		super(lambda);
		fSpecialized = specialized;
		fMap = context.getParameterMap();
		ICPPMethod[] methods = specialized.getMethods();
		fMethods = new ICPPMethod[methods.length];
		for (int i = 0; i < methods.length; ++i) {
			fMethods[i] = (ICPPMethod) specializeMember(methods[i], context.getPoint());
		}
	}

	@Override
	public ICPPTemplateParameterMap getTemplateParameterMap() {
		return fMap;
	}

	@Override
	public ICPPClassType getSpecializedBinding() {
		return fSpecialized;
	}

	@Override
	public IBinding specializeMember(IBinding binding) {
		return specializeMember(binding, null);
	}

	@Override
	public IBinding specializeMember(IBinding binding, IASTNode point) {
		// TODO: Cache specialized members the way class template specializations do?
		return CPPTemplates.createSpecialization(this, binding, point);
	}

	@Override
	public ICPPBase[] getBases(IASTNode point) {
		return ICPPBase.EMPTY_BASE_ARRAY;
	}

	@Override
	public ICPPConstructor[] getConstructors(IASTNode point) {
		return getConstructors();
	}

	@Override
	public ICPPField[] getDeclaredFields(IASTNode point) {
		return ICPPField.EMPTY_CPPFIELD_ARRAY;
	}

	@Override
	public ICPPMethod[] getMethods(IASTNode point) {
		return getMethods();
	}

	@Override
	public ICPPMethod[] getAllDeclaredMethods(IASTNode point) {
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

	@Override
	public ICPPMethod[] getDeclaredMethods(IASTNode point) {
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

	@Override
	public IBinding[] getFriends(IASTNode point) {
		return IBinding.EMPTY_BINDING_ARRAY;
	}

	@Override
	public ICPPField[] getFields(IASTNode point) {
		return ICPPField.EMPTY_CPPFIELD_ARRAY;
	}

	@Override
	public ICPPClassType[] getNestedClasses(IASTNode point) {
		return ICPPClassType.EMPTY_CLASS_ARRAY;
	}

	@Override
	public ICPPUsingDeclaration[] getUsingDeclarations(IASTNode point) {
		return ICPPUsingDeclaration.EMPTY_USING_DECL_ARRAY;
	}
}
