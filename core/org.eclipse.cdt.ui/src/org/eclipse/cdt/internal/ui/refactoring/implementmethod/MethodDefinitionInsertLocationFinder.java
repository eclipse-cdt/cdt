/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.implementmethod;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.model.ITranslationUnit;

import org.eclipse.cdt.internal.core.resources.ResourceLookup;

import org.eclipse.cdt.internal.ui.editor.SourceHeaderPartnerFinder;
import org.eclipse.cdt.internal.ui.refactoring.utils.DefinitionFinder;
import org.eclipse.cdt.internal.ui.refactoring.utils.FileHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.NodeHelper;

/**
 * Finds the information that are needed to tell where a MethodDefinition of a certain
 * method declaration should be inserted.
 * 
 * @author Mirko Stocker, Lukas Felber
 */
public class MethodDefinitionInsertLocationFinder {
	
	private static IASTNode findFunctionDefinitionInParents(IASTNode node) {
		if (node == null) {
			return null;
		} else if (node instanceof IASTFunctionDefinition) {
			if (node.getParent() instanceof ICPPASTTemplateDeclaration) {
				node = node.getParent();
			}
			return node;
		}
		return findFunctionDefinitionInParents(node.getParent());
	}
	
	private static IASTNode findFirstSurroundingParentFunctionNode(IASTNode definition) {
		IASTNode functionDefinitionInParents = findFunctionDefinitionInParents(definition);
		if (functionDefinitionInParents == null ||
				functionDefinitionInParents.getNodeLocations().length == 0) {
			return null;
		}
		return functionDefinitionInParents;
	}

	public static InsertLocation find(IASTFileLocation methodDeclarationLocation, IASTNode parent,
			IFile file) throws CoreException {
		IASTName definition = null;
		IASTDeclaration[] declarations = NodeHelper.getDeclarations(parent);
		InsertLocation result = new InsertLocation();

		for (IASTSimpleDeclaration simpleDeclaration : getAllPreviousSimpleDeclarationsFromClassInReverseOrder(declarations, methodDeclarationLocation)) {
			definition = DefinitionFinder.getDefinition(simpleDeclaration, file);

			if (definition != null) {
				result.setNodeToInsertAfter(findFirstSurroundingParentFunctionNode(definition));
				result.setInsertFile(FileHelper.getIFilefromIASTNode(definition));
			}
		}

		for (IASTSimpleDeclaration simpleDeclaration : getAllFollowingSimpleDeclarationsFromClass(declarations, methodDeclarationLocation)) {
			definition = DefinitionFinder.getDefinition(simpleDeclaration, file);

			if (definition != null) {
				result.setNodeToInsertBefore(findFirstSurroundingParentFunctionNode(definition));
				result.setInsertFile(FileHelper.getIFilefromIASTNode(definition));
			}
		}

		ITranslationUnit tu = (ITranslationUnit) CCorePlugin.getDefault().getCoreModel().create(file);
		ITranslationUnit partner = SourceHeaderPartnerFinder.getPartnerTranslationUnit(tu);
		if (partner != null) {
			IFile fileForLocation = ResourceLookup.selectFileForLocation(partner.getLocation(), file.getProject());
			if (fileForLocation != null && fileForLocation.exists()) {
				result.setInsertFile(fileForLocation);
			}
		}
		
		return result;
	}

	/**
	 * Searches the given class for all IASTSimpleDeclarations occurring before 'method'
	 * and returns them in reverse order.
	 * 
	 * @param declarations to be searched
	 * @param methodPosition on which the search aborts
	 * @return all declarations, sorted in reverse order
	 */
	private static Collection<IASTSimpleDeclaration> getAllPreviousSimpleDeclarationsFromClassInReverseOrder(
			IASTDeclaration[] declarations, IASTFileLocation methodPosition) {
		ArrayList<IASTSimpleDeclaration> allIASTSimpleDeclarations = new ArrayList<IASTSimpleDeclaration>();
		for (IASTDeclaration decl : declarations) {
			if (decl.getFileLocation().getStartingLineNumber() >= methodPosition.getStartingLineNumber()) {
				return allIASTSimpleDeclarations;
			}
			if (isMemberFunctionDeclaration(decl)) {
				allIASTSimpleDeclarations.add(0, (IASTSimpleDeclaration) decl);
			}
		}
		return allIASTSimpleDeclarations;
	}

	private static Collection<IASTSimpleDeclaration> getAllFollowingSimpleDeclarationsFromClass(
			IASTDeclaration[] declarations, IASTFileLocation methodPosition) {
		ArrayList<IASTSimpleDeclaration> allIASTSimpleDeclarations = new ArrayList<IASTSimpleDeclaration>();

		for (IASTDeclaration decl : declarations) {
			if (isMemberFunctionDeclaration(decl) &&
					decl.getFileLocation().getStartingLineNumber() > methodPosition.getStartingLineNumber() ) {
				allIASTSimpleDeclarations.add((IASTSimpleDeclaration) decl);
			}
		}
		return allIASTSimpleDeclarations;
	}
	
	private static boolean isMemberFunctionDeclaration(IASTDeclaration decl) {
		return decl instanceof IASTSimpleDeclaration &&
				((IASTSimpleDeclaration) decl).getDeclarators().length > 0 &&
				((IASTSimpleDeclaration) decl).getDeclarators()[0] instanceof IASTFunctionDeclarator;
	}
}
