/*******************************************************************************
 * Copyright (c) 2010 Tomasz Wesolowski
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Tomasz Wesolowski - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.cdt.codan.core.cxx.CxxAstUtils;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;

public class QuickFixCreateField extends AbstractAstRewriteQuickFix {
	public String getLabel() {
		return Messages.QuickFixCreateField_0;
	}

	@Override
	public void modifyAST(IIndex index, IMarker marker) {
		CxxAstUtils utils = CxxAstUtils.getInstance();
		try {
			IASTTranslationUnit ast = getTranslationUnitViaEditor(marker)
					.getAST(null, ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
			IASTName astName = getASTNameFromMarker(marker, ast);
			if (astName == null) {
				return;
			}
			IASTDeclaration declaration = utils
					.createDeclaration(astName, ast.getASTNodeFactory());
			IASTCompositeTypeSpecifier targetCompositeType = utils
					.getEnclosingCompositeTypeSpecifier(astName);
			if (targetCompositeType == null) {
				// We're not in an inline method;
				// check if we're in a method at all
				targetCompositeType = utils.getCompositeTypeFromFunction(
						utils.getEnclosingFunction(astName), index);
				if (targetCompositeType == null) {
					return;
				}
			}
			ASTRewrite r = ASTRewrite.create(targetCompositeType
					.getTranslationUnit());
			IASTNode where = findInsertionPlace(targetCompositeType);
			r.insertBefore(targetCompositeType, where, declaration, null);
			Change c = r.rewriteAST();
			c.perform(new NullProgressMonitor());
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}



	/**
	 * Suggests a default place to insert a field:
	 * 
	 * Default place to insert:
	 * <ul>
	 * <li>If in a class, after last private field or at the end</li>
	 * <li>If in a struct, after last public field or at the end</li>
	 * </ul>
	 * 
	 * @param composite
	 *        the composite to search
	 * @return an ASTNode inside composite to insert before, or null to insert
	 *         at the end
	 */
	protected IASTNode findInsertionPlace(IASTCompositeTypeSpecifier composite) {
		boolean wantPublicContext;
		boolean inDesiredAccessibilityContext;
		// Default: private context for classes, public otherwise
		wantPublicContext = !(composite.getKey() == ICPPASTCompositeTypeSpecifier.k_class);
		inDesiredAccessibilityContext = true;
		IASTNode bestMatch = null;
		IASTNode[] children = composite.getChildren();
		// Get initial candidate at the beginning (after class name and
		// composite type specifiers)
		for (IASTNode child : children) {
			if (child instanceof IASTName
					|| child instanceof ICPPASTBaseSpecifier) {
				continue;
			}
			bestMatch = child;
			break;
		}
		// Check the class body for a better place (i.e. after the last variable
		// declaration in the expected access scope)
		for (int i = 0; i < children.length; ++i) {
			IASTNode child = children[i];
			if (child instanceof ICPPASTVisibilityLabel) {
				ICPPASTVisibilityLabel label = (ICPPASTVisibilityLabel) child;
				inDesiredAccessibilityContext = (wantPublicContext && label
						.getVisibility() == ICPPASTVisibilityLabel.v_public)
						|| (!wantPublicContext && label.getVisibility() == ICPPASTVisibilityLabel.v_private);
			} else if (inDesiredAccessibilityContext
					&& (child instanceof IASTDeclaration)
					&& !(child instanceof IASTFunctionDefinition)) {
				// TODO: the above condition needs to also check if child is not
				// a typedef
				for (IASTNode gchild : child.getChildren()) {
					if ((gchild instanceof IASTDeclarator)
							&& !(gchild instanceof IASTFunctionDeclarator)) {
						// Before the next node or at the end (= after the
						// current node)
						bestMatch = (i + 1 < children.length) ? children[i + 1]
								: null;
						break;
					}
				}
			}
		}
		return bestMatch;
	}

	@Override
	public boolean isApplicable(IMarker marker) {
		String problemArgument = getProblemArgument(marker, 1);
		return problemArgument.contains(":class") && problemArgument.contains(":func"); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
