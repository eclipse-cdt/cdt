/*******************************************************************************
 * Copyright (c) 2008, 2014 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNamedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTemplateId;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTypeId;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * Helper class to find Namespace informations.
 * @author Mirko Stocker
 */
public class NamespaceHelper {
	/**
	 * Returns the qualified name of all namespaces that are defined at the specified translation
	 * unit and offset.
	 *
	 * @param translationUnit
	 * @param offset
	 * @param astCache
	 * @return ICPPASTQualifiedName with the names of all namespaces
	 * @throws CoreException
	 */
	public static ICPPASTName[] getSurroundingNamespace(final ITranslationUnit translationUnit, final int offset,
			CRefactoringContext astCache) throws CoreException {
		final List<ICPPASTName> names = new ArrayList<>();

		astCache.getAST(translationUnit, null).accept(new CPPASTAllVisitor() {
			@Override
			public int visit(IASTDeclSpecifier declSpec) {
				if (declSpec instanceof ICPPASTCompositeTypeSpecifier
						&& checkFileNameAndLocation(translationUnit.getLocation(), offset, declSpec)) {
					names.add((ICPPASTName) createNameWithTemplates(declSpec));
				}
				return super.visit(declSpec);
			}

			@Override
			public int visit(ICPPASTNamespaceDefinition namespace) {
				if (checkFileNameAndLocation(translationUnit.getLocation(), offset, namespace)) {
					names.add((ICPPASTName) namespace.getName().copy(CopyStyle.withLocations));
				}

				return super.visit(namespace);
			}
		});

		return names.toArray(new ICPPASTName[names.size()]);
	}

	private static boolean checkFileNameAndLocation(final IPath path, final int offset, IASTNode namespace) {
		IASTFileLocation fileLoc = namespace.getFileLocation();
		if (fileLoc == null) {
			return false;
		}
		boolean fileNameOk = fileLoc.getFileName().endsWith(path.toOSString());
		if (!fileNameOk) {
			return false;
		}

		for (IASTNodeLocation nodeLocation : namespace.getNodeLocations()) {
			int nodeOffset = nodeLocation.getNodeOffset();
			boolean locationOk = offset >= nodeOffset && offset < nodeOffset + nodeLocation.getNodeLength();
			if (locationOk) {
				return true;
			}
		}

		return false;
	}

	private static IASTName createNameWithTemplates(IASTNode declarationParent) {
		IASTName parentName = ((ICPPASTCompositeTypeSpecifier) declarationParent).getName()
				.copy(CopyStyle.withLocations);

		if (classHasTemplates(declarationParent)) {
			CPPASTTemplateId templateId = new CPPASTTemplateId();
			templateId.setTemplateName(parentName);

			for (ICPPASTTemplateParameter templateParameter : ((ICPPASTTemplateDeclaration) declarationParent
					.getParent().getParent()).getTemplateParameters()) {
				if (templateParameter instanceof CPPASTSimpleTypeTemplateParameter) {
					CPPASTSimpleTypeTemplateParameter simpleTypeTemplateParameter = (CPPASTSimpleTypeTemplateParameter) templateParameter;

					CPPASTTypeId id = new CPPASTTypeId();

					CPPASTNamedTypeSpecifier namedTypeSpecifier = new CPPASTNamedTypeSpecifier();
					namedTypeSpecifier.setName(simpleTypeTemplateParameter.getName().copy(CopyStyle.withLocations));
					id.setDeclSpecifier(namedTypeSpecifier);

					templateId.addTemplateArgument(id);
				} else if (templateParameter instanceof ICPPASTParameterDeclaration) {
					ICPPASTParameterDeclaration parDecl = (ICPPASTParameterDeclaration) templateParameter;

					CPPASTTypeId id = new CPPASTTypeId();

					CPPASTNamedTypeSpecifier namedTypeSpecifier = new CPPASTNamedTypeSpecifier();
					namedTypeSpecifier.setName(parDecl.getDeclarator().getName().copy(CopyStyle.withLocations));
					id.setDeclSpecifier(namedTypeSpecifier);

					templateId.addTemplateArgument(id);
				} else if (templateParameter instanceof ICPPASTTemplatedTypeTemplateParameter) {
					ICPPASTTemplatedTypeTemplateParameter parDecl = (ICPPASTTemplatedTypeTemplateParameter) templateParameter;

					CPPASTTypeId id = new CPPASTTypeId();

					CPPASTNamedTypeSpecifier namedTypeSpecifier = new CPPASTNamedTypeSpecifier();
					namedTypeSpecifier.setName(parDecl.getName().copy(CopyStyle.withLocations));
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
