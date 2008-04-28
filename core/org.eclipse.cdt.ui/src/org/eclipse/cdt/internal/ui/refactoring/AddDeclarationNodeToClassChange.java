/*******************************************************************************
 * Copyright (c) 2007, 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *    Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import org.eclipse.osgi.util.NLS;
import org.eclipse.text.edits.TextEditGroup;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTVisibilityLabel;

import org.eclipse.cdt.internal.ui.refactoring.utils.VisibilityEnum;

/**
 * Adds a declaration to an existing class via the ModificationCollector. It automatically searches 
 * the correct insertion point for the desired visibility.
 * 
 * @author Mirko Stocker
 *
 */
public class AddDeclarationNodeToClassChange {

	private final ICPPASTCompositeTypeSpecifier nodeClass;
	private final VisibilityEnum visibility;
	private final IASTNode fieldNodes;
	private final ModificationCollector collector;
	
	public static void createChange(ICPPASTCompositeTypeSpecifier nodeClass, VisibilityEnum visibility, IASTNode fieldNodes, boolean isField, ModificationCollector collector) {
		new AddDeclarationNodeToClassChange(nodeClass, visibility, fieldNodes, collector, isField);
	}

	private AddDeclarationNodeToClassChange(ICPPASTCompositeTypeSpecifier nodeClass, VisibilityEnum visibility, IASTNode fieldNodes, ModificationCollector collector, boolean isField) {
		this.nodeClass = nodeClass;		
		this.visibility = visibility;
		this.fieldNodes = fieldNodes;
		this.collector = collector;
		createRewrites(isField);
	}
	
	private void createRewrites(boolean isField) {
		int lastFunctionDeclaration = -1;
		int lastFieldDeclaration = -1;
		IASTDeclaration[] members = nodeClass.getMembers();
		
		// XXX Don't we have to differentiate between the default visibility in a class and a struct?
		VisibilityEnum currentVisibility = VisibilityEnum.v_private;
	
		// Find the insert location by iterating over the elements of the class 
		// and remembering the last element with the matching visibility
		for(int i = 0; i < members.length; i++) {
			IASTDeclaration declaration = members[i];
			
			if(declaration instanceof ICPPASTVisibilityLabel){
				currentVisibility = VisibilityEnum.from((ICPPASTVisibilityLabel) declaration);
			}
			
			if (declaration instanceof IASTSimpleDeclaration) {
				IASTSimpleDeclaration simple = (IASTSimpleDeclaration) declaration;
				IASTDeclarator[] declarators = simple.getDeclarators();
				if(declarators.length > 0 && declarators[0] != null && declarators[0] instanceof IASTFunctionDeclarator){
					if(currentVisibility.equals(visibility)){
						lastFunctionDeclaration = i;
					}
					
				} else if (declarators.length > 0 && declarators[0] != null){
					if(currentVisibility.equals(visibility)){
						lastFieldDeclaration = i;
					}
				}
			}
		}

		IASTDeclaration nextFunctionDeclaration = null;
		if (lastFunctionDeclaration < members.length-1 && lastFunctionDeclaration >= 0)
			nextFunctionDeclaration = members[lastFunctionDeclaration+1];

		IASTDeclaration nextFieldDeclaration = null;
		if (lastFieldDeclaration < members.length-1 && lastFieldDeclaration >= 0)
			nextFieldDeclaration = members[lastFieldDeclaration   +1];
		
		createInsert(isField, nextFunctionDeclaration, nextFieldDeclaration, currentVisibility);
	}

	private void createInsert(boolean isField, IASTDeclaration nextFunctionDeclaration, IASTDeclaration nextFieldDeclaration, VisibilityEnum currentVisibility) {
		if(isField) {
			if(nextFieldDeclaration != null) {
				insertBefore(nextFieldDeclaration);
			} else if(nextFunctionDeclaration != null){
				insertBefore(nextFunctionDeclaration);
			} else {
				insertAtTheEnd(currentVisibility);
			}
		} else {
			if(nextFunctionDeclaration != null){
				insertBefore(nextFunctionDeclaration);
			} else if(nextFieldDeclaration != null) {
				insertBefore(nextFieldDeclaration);
			} else {
				insertAtTheEnd(currentVisibility);
			}
		}
	}

	private void insertBefore(IASTNode nearestNode) {			
		ASTRewrite rewrite = collector.rewriterForTranslationUnit(nearestNode.getTranslationUnit());
		rewrite.insertBefore(nearestNode.getParent(), nearestNode, fieldNodes, createEditDescription());
	}

	private void insertAtTheEnd(VisibilityEnum currentVisibility) {
		
		ASTRewrite rewrite = collector.rewriterForTranslationUnit(nodeClass.getTranslationUnit());
		
		if(! currentVisibility.equals(visibility)) {
			ICPPASTVisibilityLabel label = new CPPASTVisibilityLabel(visibility.getICPPASTVisiblityLabelVisibility());
			rewrite.insertBefore(nodeClass, null, label, createEditDescription());
		}
		
		rewrite.insertBefore(nodeClass, null, fieldNodes, createEditDescription());
	}
	
	private TextEditGroup createEditDescription() {
		return new TextEditGroup(NLS.bind(Messages.AddDeclarationNodeToClassChange_AddDeclaration, nodeClass.getName()));
	}
}
