/*******************************************************************************
 * Copyright (c) 2008, 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.utils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;

import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNamedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTQualifiedName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTemplateId;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTypeId;

/**
 * Helper class to find Namespace informations.
 * @author Mirko Stocker
 *
 */
public class NamespaceHelper {
	
	/**
	 * Returns the qualified name of all namespaces that are defined at the specified file and offset.
	 * 
	 * @param insertFile
	 * @param offset
	 * @return ICPPASTQualifiedName with the names of all namespaces
	 * @throws CoreException 
	 */
	public static ICPPASTQualifiedName getSurroundingNamespace(final IFile insertFile, final int offset) throws CoreException {
		
		final CPPASTQualifiedName qualifiedName = new CPPASTQualifiedName();
	
		TranslationUnitHelper.loadTranslationUnit(insertFile, false).accept(new CPPASTAllVisitor() {
			
				@Override
				public int visit(IASTDeclSpecifier declSpec) {
					if (declSpec instanceof ICPPASTCompositeTypeSpecifier && checkFileNameAndLocation(insertFile, offset, declSpec)) {
							qualifiedName.addName(createNameWithTemplates(declSpec));
						}
						return super.visit(declSpec);
					}
			
				@Override
				public int visit(ICPPASTNamespaceDefinition namespace) {
					if (checkFileNameAndLocation(insertFile, offset, namespace)) {
						qualifiedName.addName((namespace).getName().copy()); 
					}
					
					return super.visit(namespace);
				}
			});
		
		return qualifiedName;
	}
	
	private static boolean checkFileNameAndLocation(final IFile insertFile, final int offset, IASTNode namespace) {
		boolean fileNameOk = namespace.getFileLocation().getFileName().endsWith(insertFile.getLocation().toOSString());
		if(!fileNameOk) {
			return false;
		}
		
		for(IASTNodeLocation nodeLocation : namespace.getNodeLocations()) {
			int nodeOffset = nodeLocation.getNodeOffset();
			boolean locationOk = offset >= nodeOffset && offset < nodeOffset + nodeLocation.getNodeLength();
			if(locationOk) {
				return true;
			}
		}
		
		return false;
	}
	
	private static IASTName createNameWithTemplates(IASTNode declarationParent) {
		IASTName parentName;
		parentName = ((ICPPASTCompositeTypeSpecifier) declarationParent).getName().copy(
				CopyStyle.withLocations);
		
		if(classHasTemplates(declarationParent)) {
			CPPASTTemplateId templateId = new CPPASTTemplateId();
			templateId.setTemplateName(parentName);
				
			for(ICPPASTTemplateParameter templateParameter : ((ICPPASTTemplateDeclaration) declarationParent.getParent().getParent() ).getTemplateParameters()) {
					
				if (templateParameter instanceof CPPASTSimpleTypeTemplateParameter) {
					CPPASTSimpleTypeTemplateParameter simpleTypeTemplateParameter = (CPPASTSimpleTypeTemplateParameter) templateParameter;
					
					CPPASTTypeId id = new CPPASTTypeId();
					
					CPPASTNamedTypeSpecifier namedTypeSpecifier = new CPPASTNamedTypeSpecifier();
					namedTypeSpecifier.setName(simpleTypeTemplateParameter.getName().copy(
							CopyStyle.withLocations));
					id.setDeclSpecifier(namedTypeSpecifier);
					
					templateId.addTemplateArgument(id);
				}
			}
			
			parentName = templateId;
		}
		return parentName;
	}

	private static boolean classHasTemplates(IASTNode declarationParent) {
		return declarationParent.getParent() != null 
			&& declarationParent.getParent().getParent() instanceof ICPPASTTemplateDeclaration;
	}
}
