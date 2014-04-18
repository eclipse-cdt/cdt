/*******************************************************************************
 * Copyright (c) 2007, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.REF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

/**
 * Helps analysis of the class declaration for user declared members relevant
 * to deciding which implicit bindings to declare.
 *
 * @see chapter 12 of the ISO specification
 */
class ImplicitsAnalysis {
	private boolean hasUserDeclaredConstructor;
	private boolean hasUserDeclaredCopyConstructor;
	private boolean hasUserDeclaredCopyAssignmentOperator;
	private boolean hasUserDeclaredDestructor;
	private final ICPPClassType classType;

	ImplicitsAnalysis(ICPPASTCompositeTypeSpecifier compositeTypeSpecifier, ICPPClassType classType) {
		this.classType= classType;
		analyzeMembers(compositeTypeSpecifier);
	}

	public boolean hasUserDeclaredConstructor() {
		return hasUserDeclaredConstructor;
	}

	public boolean hasUserDeclaredCopyConstructor() {
		return hasUserDeclaredCopyConstructor;
	}

	public boolean hasUserDeclaredCopyAssignmentOperator() {
		return hasUserDeclaredCopyAssignmentOperator;
	}

	public boolean hasUserDeclaredDestructor() {
		return hasUserDeclaredDestructor;
	}

	public int getImplicitsToDeclareCount() {
		return (!hasUserDeclaredDestructor ? 1 : 0)
			+ (!hasUserDeclaredConstructor ? 1 : 0)
			+ (!hasUserDeclaredCopyConstructor ? 1 : 0)
			+ (!hasUserDeclaredCopyAssignmentOperator ? 1 : 0);
	}

	private void analyzeMembers(ICPPASTCompositeTypeSpecifier compositeTypeSpecifier) {
		IASTDeclaration[] members = compositeTypeSpecifier.getMembers();
		char[] name = compositeTypeSpecifier.getName().getLookupKey();
        for (IASTDeclaration member : members) {
    		IASTDeclarator dcltor = null;
    		IASTDeclSpecifier spec = null;
			if (member instanceof IASTSimpleDeclaration) {
			    IASTDeclarator[] dtors = ((IASTSimpleDeclaration) member).getDeclarators();
			    if (dtors.length == 0 || dtors.length > 1)
			    	continue;
			    dcltor = dtors[0];
			    spec = ((IASTSimpleDeclaration) member).getDeclSpecifier();
			} else if (member instanceof IASTFunctionDefinition) {
			    dcltor = ((IASTFunctionDefinition) member).getDeclarator();
			    spec = ((IASTFunctionDefinition) member).getDeclSpecifier();
			}

			if (!(dcltor instanceof ICPPASTFunctionDeclarator))
				continue;

			char[] declName= ASTQueries.findInnermostDeclarator(dcltor).getName().getLookupKey();

			if (spec instanceof IASTSimpleDeclSpecifier &&
					((IASTSimpleDeclSpecifier) spec).getType() == IASTSimpleDeclSpecifier.t_unspecified) {
				if (CharArrayUtils.equals(declName, name)) {
					hasUserDeclaredConstructor = true;
					IASTParameterDeclaration[] ps = ((ICPPASTFunctionDeclarator) dcltor).getParameters();
		        	if (ps.length >= 1) {
		        		if (hasTypeReferenceToClassType(ps[0]) && parametersHaveInitializers(ps, 1)) {
		        			hasUserDeclaredCopyConstructor= true;
		        		}
		        	}
				} if (declName.length > 0 && declName[0] == '~' &&
						CharArrayUtils.equals(declName, 1, name.length, name)) {
					hasUserDeclaredDestructor = true;
				}
			} if (CharArrayUtils.equals(declName, OverloadableOperator.ASSIGN.toCharArray())) {
				IASTParameterDeclaration[] ps = ((ICPPASTFunctionDeclarator) dcltor).getParameters();
	        	if (ps.length == 1 && hasTypeReferenceToClassType(ps[0]))
	        		hasUserDeclaredCopyAssignmentOperator = true;
			}

			if (hasUserDeclaredCopyConstructor && hasUserDeclaredDestructor && hasUserDeclaredCopyAssignmentOperator)
				break;
        }
	}

	private boolean hasTypeReferenceToClassType(IASTParameterDeclaration dec) {
		if (dec instanceof ICPPASTParameterDeclaration) {
			IType t= CPPVisitor.createType((ICPPASTParameterDeclaration) dec, false);
			if (t != null) {
				t= SemanticUtil.getNestedType(t, TDEF);
				if (t instanceof ICPPReferenceType) {
					if (!((ICPPReferenceType) t).isRValueReference()) {
						t= SemanticUtil.getNestedType(t, TDEF|REF|CVTYPE);
						return classType.isSameType(t);
					}
				}
			}
		}
		return false;
	}

	/**
	 * Checks whether all parameters starting at offset have initializers.
	 */
	private boolean parametersHaveInitializers(IASTParameterDeclaration[] params, int offset) {
		for (int i = offset; i < params.length; i++) {
			if (params[i].getDeclarator().getInitializer() == null) {
				return false;
			}
		}
		return true;
	}
}