/*******************************************************************************
 * Copyright (c) 2017 Pavel Marek
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Pavel Marek - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.overridemethods;

import org.eclipse.cdt.core.browser.IQualifiedTypeName;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.text.ITextSelection;

/**
 * Visits the class definition inside which the selection (cursor) is located.
 * Gets binding for this class, and from this binding all other necessary
 * informations are gathered inside fMethodContainer.
 */
public class VirtualMethodsASTVisitor extends ASTVisitor {
	private ITextSelection fSelection;
	private String fFileName;
	private VirtualMethodContainer fMethodContainer;
	private IASTNode fClassNode;
	private String fClassName;
	private ICPPClassType fClassBinding;
	private String fElementName;

	/**
	 *
	 * @param textSelection
	 * @param fileName
	 * @param methodContainer the VirtualMethodContainer to be filled by this
	 * visitor.
	 */
	public VirtualMethodsASTVisitor(ITextSelection textSelection, String fileName,
			VirtualMethodContainer methodContainer, String elName) {
		// Visit only decl specifier.
		shouldVisitDeclSpecifiers = true;

		this.fClassNode = null;
		this.fSelection = textSelection;
		this.fFileName = fileName;
		this.fMethodContainer = methodContainer;
		this.fElementName = elName;
	}

	/**
	 *
	 */
	public VirtualMethodContainer getVirtualMethodContainer() {
		return fMethodContainer;
	}

	/**
	 * Returns class node encapsulating text selection.
	 * @return null if no class was encountered, IASTNode otherwise.
	 */
	public IASTNode getClassNode() {
		return fClassNode;
	}

	public String getClassName() {
		return fClassName;
	}

	public ICPPClassType getClassBinding() {
		return fClassBinding;
	}

	/**
	 * Check if node is enclosing text selection.
	 * @param node
	 * @return
	 */
	private boolean isInsideSelection(ICPPASTCompositeTypeSpecifier node) {
		IASTFileLocation location = node.getFileLocation();

		// node has no location if it is for example built-in macro
		if (location == null) {
			return false;
		}

		if (fSelection == null) {
			IBinding binding = node.getName().resolveBinding();
			if (!(binding instanceof ICPPBinding))
				return false;
			try {
				String elName = String.join(IQualifiedTypeName.QUALIFIER, ((ICPPBinding) binding).getQualifiedName());
				if (elName.equals(fElementName))
					return true;
			} catch (DOMException e) {
				CUIPlugin.log(e);
			}
			return false;
		}

		if (location.getNodeOffset() <= fSelection.getOffset()
				&& fSelection.getOffset() <= location.getNodeOffset() + location.getNodeLength()
				&& location.getFileName().contains(fFileName)) {
			return true;
		} else {
			return false;
		}
	}

	public void visitAst(IASTTranslationUnit ast) {
		ast.accept(this);
		if (fClassNode != null) {
			MethodCollector methodCollector = new MethodCollector();
			methodCollector.fillContainer(fMethodContainer, fClassBinding, (IASTDeclSpecifier) fClassNode);
		}
	}

	@Override
	public int visit(IASTDeclSpecifier declSpecifier) {
		// In a class or struct
		if (declSpecifier instanceof ICPPASTCompositeTypeSpecifier) {
			/*
			 * Check if this class is enclosing text selection and go ahead,
			 * we could hit the selection but there's a nested class so we need
			 * to process the most inner node with selection at the end of tree visit.
			 */
			if (isInsideSelection((ICPPASTCompositeTypeSpecifier) declSpecifier)) {

				// Store.
				fClassNode = declSpecifier;

				// Get binding.
				ICPPASTCompositeTypeSpecifier typeSpec = (ICPPASTCompositeTypeSpecifier) declSpecifier;
				IBinding binding = typeSpec.getName().getBinding();

				// Check if the binding is of class type.
				if (!(binding instanceof ICPPClassType)) {
					fClassNode = null;
					return PROCESS_CONTINUE;
				}
				// Store class namegetRecursivelyAllBases
				fClassName = binding.getName();

				ICPPClassType classType = (ICPPClassType) binding;
				fClassBinding = classType;
			}
		}
		return PROCESS_CONTINUE;
	}
}
