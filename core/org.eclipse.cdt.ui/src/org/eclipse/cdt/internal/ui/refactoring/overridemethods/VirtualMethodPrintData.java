/******************************************************************************* 
 * Copyright (c) 2017 Pavel Marek 
 * All rights reserved. This program and the accompanying materials  
 * are made available under the terms of the Eclipse Public License v1.0  
 * which accompanies this distribution, and is available at  
 * http://www.eclipse.org/legal/epl-v10.html   
 *  
 * Contributors:  
 *      Pavel Marek - initial API and implementation 
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.overridemethods;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;

import org.eclipse.cdt.internal.ui.refactoring.ClassMemberInserter;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.utils.VisibilityEnum;

/**
 * Holds virtual member functions that should be printed (during 
 * <code> VirtualMethodRefactoring.collectModifications() </code>).
 * @author mayfa
 *
 */
public class VirtualMethodPrintData {
	private Set<Method> privateMethods= new HashSet<>();
	private Set<Method> protectedMethods= new HashSet<>();
	private Set<Method> publicMethods= new HashSet<>();

	/**
	 * Adds one method to print.
	 * @param method
	 */
	public void addMethod(Method method) {
		switch (method.getMethod().getVisibility()) {
		case ICPPASTVisibilityLabel.v_public:
			publicMethods.add(method);
			break;
		case ICPPASTVisibilityLabel.v_protected:
			protectedMethods.add(method);
			break;
		case ICPPASTVisibilityLabel.v_private:
			privateMethods.add(method);
			break;
		}
	}
	
	/**
	 * This method is called when ICPPClassType (parent) is selected in the
	 * tree and all its children are passed to this method.
	 * @param selectedMethods
	 */
	public void addMethods(List<Method> selectedMethods) {
		for (Method method : selectedMethods) {
			addMethod(method);
		}
	}

	/**
	 * Removes one method from (further) printing.
	 * @param method
	 */
	public void removeMethod(Method method) {
		switch (method.getMethod().getVisibility()) {
		case ICPPASTVisibilityLabel.v_public:
			publicMethods.remove(method);
			break;
		case ICPPASTVisibilityLabel.v_protected:
			protectedMethods.remove(method);
			break;
		case ICPPASTVisibilityLabel.v_private:
			privateMethods.remove(method);
			break;
		}
	}

	public void removeMethods(List<Method> selectedMethods) {
		for (Method method : selectedMethods) {
			removeMethod(method);
		}
	}

	public boolean isEmpty() {
		return privateMethods.isEmpty() && protectedMethods.isEmpty() &&
				publicMethods.isEmpty();
	}
	
	/**
	 * Parses all given methods.
	 * @param methods
	 * @return
	 */
	private List<IASTNode> parseAllMethods(Set<Method> methods) {
		CodeParser codeParser= new CodeParser();
		List<IASTNode> methodNodes= new ArrayList<>();
		
		for (Method method : methods) {
			methodNodes.add(codeParser.parse(method));
		}
		
		return methodNodes;
	}

	/**
	 * Rewrites all the changes.
	 */
	public void rewriteAST(ModificationCollector collector, 
			VirtualMethodsASTVisitor visitor) 
	{
		ICPPASTCompositeTypeSpecifier classNode=
				(ICPPASTCompositeTypeSpecifier) visitor.getClassNode();
		List<IASTNode> methodNodes= new ArrayList<>();

		methodNodes= parseAllMethods(publicMethods);
		if (!methodNodes.isEmpty()) {
			// Add all public methods to classNode.
			ClassMemberInserter.createChange(classNode, VisibilityEnum.v_public, 
					methodNodes, true, collector);
		}

		methodNodes= parseAllMethods(protectedMethods);
		if (!methodNodes.isEmpty()) {
			// Add all protected methods to classNode.
			ClassMemberInserter.createChange(classNode, VisibilityEnum.v_protected, methodNodes, true,
					collector);
		}

		methodNodes= parseAllMethods(privateMethods);
		if (!methodNodes.isEmpty()) {
			// Add all private methods to classNode.
			ClassMemberInserter.createChange(classNode, VisibilityEnum.v_private, methodNodes, true,
					collector);
		}
	}


}
