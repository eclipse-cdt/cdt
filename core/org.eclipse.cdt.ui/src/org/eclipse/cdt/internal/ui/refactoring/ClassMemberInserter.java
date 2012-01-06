/*******************************************************************************
 * Copyright (c) 2007, 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.osgi.util.NLS;
import org.eclipse.text.edits.TextEditGroup;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;

import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTVisibilityLabel;

import org.eclipse.cdt.internal.ui.refactoring.utils.VisibilityEnum;

/**
 * Adds a declaration to an existing class via the ModificationCollector. Automatically determines 
 * an appropriate insertion point for the desired visibility.
 * 
 * @author Mirko Stocker
 */
public class ClassMemberInserter {
	
	public static void createChange(ICPPASTCompositeTypeSpecifier classNode,
			VisibilityEnum visibility, IASTNode nodeToAdd, boolean isField,
			ModificationCollector collector) {
		createChange(classNode, visibility,	Collections.singletonList(nodeToAdd), isField, collector);
	}

	public static void createChange(ICPPASTCompositeTypeSpecifier classNode,
			VisibilityEnum visibility, List<IASTNode> nodesToAdd, boolean isField,
			ModificationCollector collector) {
		nodesToAdd = new ArrayList<IASTNode>(nodesToAdd);
		VisibilityEnum defaultVisibility = classNode.getKey() == IASTCompositeTypeSpecifier.k_struct ?
				VisibilityEnum.v_public : VisibilityEnum.v_private;
		VisibilityEnum currentVisibility = defaultVisibility;

		boolean ascendingVisibilityOrder = isAscendingVisibilityOrder(classNode);
		int lastFunctionIndex = -1;
		int lastFieldIndex = -1;
		int lastMatchingVisibilityIndex = -1;
		int lastPrecedingVisibilityIndex = -1;
		IASTDeclaration[] members = classNode.getMembers();
		
		// Find the insert location by iterating over the elements of the class 
		// and remembering the last element with the matching visibility and the last element
		// with preceding visibility (according to the visibility order preference).  
		for (int i = 0; i < members.length; i++) {
			IASTDeclaration declaration = members[i];
			
			if (declaration instanceof ICPPASTVisibilityLabel) {
				currentVisibility = VisibilityEnum.from((ICPPASTVisibilityLabel) declaration);
			}
			if (currentVisibility == visibility) {
				lastMatchingVisibilityIndex = i;
				if (declaration instanceof IASTSimpleDeclaration) {
					IASTDeclarator[] declarators = ((IASTSimpleDeclaration) declaration).getDeclarators();
					if (declarators.length > 0 && declarators[0] != null) {
						if (declarators[0] instanceof IASTFunctionDeclarator) {
							lastFunctionIndex = i;
						} else {
							lastFieldIndex = i;
						}
					}
				} else if (declaration instanceof ICPPASTFunctionDefinition) {
					lastFunctionIndex = i;
				}
			} else if (currentVisibility.compareTo(visibility) < 0 == ascendingVisibilityOrder) {
				lastPrecedingVisibilityIndex = i;
			}
		}

		int index = isField && lastFieldIndex >= 0 || !isField && lastFunctionIndex < 0 ?
				lastFieldIndex : lastFunctionIndex;
		if (index < 0)
			index = lastMatchingVisibilityIndex;
		if (index < 0)
			index = lastPrecedingVisibilityIndex;
		index++;
		IASTNode nextNode = index < members.length ? members[index] : null;
		
		if (lastMatchingVisibilityIndex < 0 &&
				!(index == 0 && classNode.getKey() == IASTCompositeTypeSpecifier.k_struct && visibility == defaultVisibility)) {
			nodesToAdd.add(0, new CPPASTVisibilityLabel(visibility.getVisibilityLabelValue()));
			if (index == 0 && nextNode != null && !(nextNode instanceof ICPPASTVisibilityLabel)) {
				nodesToAdd.add(new CPPASTVisibilityLabel(defaultVisibility.getVisibilityLabelValue()));
			}
		}

		ASTRewrite rewrite = collector.rewriterForTranslationUnit(classNode.getTranslationUnit());
		for (IASTNode node : nodesToAdd) {
			rewrite.insertBefore(classNode, nextNode, node, createEditDescription(classNode));
		}
	}	

	// Not instantiatable. All methods are static.
	private ClassMemberInserter() {
	}

	private static TextEditGroup createEditDescription(ICPPASTCompositeTypeSpecifier classNode) {
		return new TextEditGroup(NLS.bind(Messages.AddDeclarationNodeToClassChange_AddDeclaration,
				classNode.getName()));
	}

	private static boolean isAscendingVisibilityOrder(ICPPASTCompositeTypeSpecifier classNode) {
		IPreferencesService preferences = Platform.getPreferencesService();
		IASTTranslationUnit ast = classNode.getTranslationUnit();
		ITranslationUnit tu = ast.getOriginatingTranslationUnit();
		IProject project = tu != null ? tu.getCProject().getProject() : null;
		return preferences.getBoolean(CUIPlugin.PLUGIN_ID,
    			PreferenceConstants.CLASS_MEMBER_ASCENDING_VISIBILITY_ORDER, false,
    			PreferenceConstants.getPreferenceScopes(project));
	}
}
