/*******************************************************************************
 * Copyright (c) 2007, 2012 Institute for Software, HSR Hochschule fuer Technik
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
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTVisibilityLabel;
import org.eclipse.cdt.internal.ui.refactoring.utils.VisibilityEnum;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.osgi.util.NLS;
import org.eclipse.text.edits.TextEditGroup;

/**
 * Adds a declaration to an existing class via the ModificationCollector. Automatically determines
 * an appropriate insertion point for the desired visibility.
 *
 * @author Mirko Stocker
 */
public class ClassMemberInserter {
	public static class InsertionInfo {
		private final IASTNode parentNode;
		/**
		 * The node before which the new node should be inserted. A null value indicates insertion
		 * to the end of parentNode
		 */
		private IASTNode insertBeforeNode; //
		/**  Visibility label to insert before the new node or null. */
		private ICPPASTVisibilityLabel prologue;
		/**  Visibility label to insert after the new node or null. */
		private ICPPASTVisibilityLabel epilogue;

		public InsertionInfo(IASTNode parentNode, IASTNode insertBeforeNode) {
			this.parentNode = parentNode;
			this.insertBeforeNode = insertBeforeNode;
		}

		public InsertionInfo(IASTNode parentNode) {
			this(parentNode, null);
		}

		public IASTNode getParentNode() {
			return parentNode;
		}

		public IASTNode getInsertBeforeNode() {
			return insertBeforeNode;
		}

		public ICPPASTVisibilityLabel getPrologue() {
			return prologue;
		}

		public ICPPASTVisibilityLabel getEpilogue() {
			return epilogue;
		}
	}

	// Not instantiatable. All methods are static.
	private ClassMemberInserter() {
	}

	/**
	 * Inserts a node inside a class declaration.
	 *
	 * @param classNode the type specifier of the class declaration
	 * @param visibility desired visibility of the inserted declaration
	 * @param nodeToAdd the node to add
	 * @param isField whether the node being inserted is a field declaration or not
	 * @param collector the modification collector recording the insertion
	 * @return the rewriter for making further modifications to the inserted node
	 */
	public static ASTRewrite createChange(ICPPASTCompositeTypeSpecifier classNode, VisibilityEnum visibility,
			IASTNode nodeToAdd, boolean isField, ModificationCollector collector) {
		List<ASTRewrite> addedNodesRewrites = createChange(classNode, visibility, Collections.singletonList(nodeToAdd),
				isField, collector);
		return addedNodesRewrites.get(0);
	}

	/**
	 * Inserts one more adjacent nodes inside a class declaration.
	 *
	 * @param classNode the type specifier of the class declaration
	 * @param visibility desired visibility of the inserted declarations
	 * @param nodesToAdd the nodes to add
	 * @param isField whether the nodes being inserted are field declaration or not
	 * @param collector the modification collector recording the insertion
	 * @return the rewriters for making further modifications to the inserted nodes
	 */
	public static List<ASTRewrite> createChange(ICPPASTCompositeTypeSpecifier classNode, VisibilityEnum visibility,
			List<IASTNode> nodesToAdd, boolean isField, ModificationCollector collector) {
		InsertionInfo info = findInsertionPoint(classNode, visibility, isField);
		ASTRewrite rewrite = collector.rewriterForTranslationUnit(classNode.getTranslationUnit());

		if (info.getPrologue() != null) {
			rewrite.insertBefore(info.getParentNode(), info.getInsertBeforeNode(), info.getPrologue(),
					createEditDescription(classNode));
		}

		List<ASTRewrite> addedNodeRewrites = new ArrayList<>(nodesToAdd.size());
		for (IASTNode node : nodesToAdd) {
			addedNodeRewrites.add(rewrite.insertBefore(info.getParentNode(), info.getInsertBeforeNode(), node,
					createEditDescription(classNode)));
		}

		if (info.getEpilogue() != null) {
			rewrite.insertBefore(info.getParentNode(), info.getInsertBeforeNode(), info.getEpilogue(),
					createEditDescription(classNode));
		}

		return addedNodeRewrites;
	}

	public static InsertionInfo findInsertionPoint(ICPPASTCompositeTypeSpecifier classNode, VisibilityEnum visibility,
			boolean isField) {
		InsertionInfo info = new InsertionInfo(classNode);
		VisibilityEnum defaultVisibility = classNode.getKey() == IASTCompositeTypeSpecifier.k_struct
				? VisibilityEnum.v_public
				: VisibilityEnum.v_private;
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

		int index = isField && lastFieldIndex >= 0 || !isField && lastFunctionIndex < 0 ? lastFieldIndex
				: lastFunctionIndex;
		if (index < 0)
			index = lastMatchingVisibilityIndex;
		if (index < 0)
			index = lastPrecedingVisibilityIndex;
		index++;
		if (index < members.length)
			info.insertBeforeNode = members[index];

		if (lastMatchingVisibilityIndex < 0 && !(index == 0 && classNode.getKey() == IASTCompositeTypeSpecifier.k_struct
				&& visibility == defaultVisibility)) {
			info.prologue = new CPPASTVisibilityLabel(visibility.getVisibilityLabelValue());
			if (index == 0 && info.insertBeforeNode != null
					&& !(info.insertBeforeNode instanceof ICPPASTVisibilityLabel)) {
				info.epilogue = new CPPASTVisibilityLabel(defaultVisibility.getVisibilityLabelValue());
			}
		}
		return info;
	}

	private static TextEditGroup createEditDescription(ICPPASTCompositeTypeSpecifier classNode) {
		return new TextEditGroup(
				NLS.bind(Messages.AddDeclarationNodeToClassChange_AddDeclaration, classNode.getName()));
	}

	private static boolean isAscendingVisibilityOrder(ICPPASTCompositeTypeSpecifier classNode) {
		IPreferencesService preferences = Platform.getPreferencesService();
		IASTTranslationUnit ast = classNode.getTranslationUnit();
		ITranslationUnit tu = ast.getOriginatingTranslationUnit();
		IProject project = tu != null ? tu.getCProject().getProject() : null;
		return preferences.getBoolean(CUIPlugin.PLUGIN_ID, PreferenceConstants.CLASS_MEMBER_ASCENDING_VISIBILITY_ORDER,
				false, PreferenceConstants.getPreferenceScopes(project));
	}
}
