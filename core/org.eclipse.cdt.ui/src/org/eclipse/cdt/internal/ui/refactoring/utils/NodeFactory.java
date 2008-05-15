package org.eclipse.cdt.internal.ui.refactoring.utils;

import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;

import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNamedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTParameterDeclaration;

public class NodeFactory {

	public static IASTParameterDeclaration createParameterDeclaration(String paramType, String paramName) {
		CPPASTNamedTypeSpecifier type = new CPPASTNamedTypeSpecifier(new CPPASTName(paramType.toCharArray()), false);
		CPPASTDeclarator declarator = new CPPASTDeclarator(new CPPASTName(paramName.toCharArray()));
		return new CPPASTParameterDeclaration(type,declarator);
	}

}
