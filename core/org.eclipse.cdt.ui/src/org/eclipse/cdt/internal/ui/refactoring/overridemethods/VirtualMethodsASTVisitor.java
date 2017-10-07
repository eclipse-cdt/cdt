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

import org.eclipse.jface.text.ITextSelection;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;

/**
 * Visits the class definition inside which the selection (cursor) is located.
 * Gets binding for this class, and from this binding all other necessary
 * informations are gathered inside fMethodContainer.
 * @author mayfa
 *
 */
public class VirtualMethodsASTVisitor extends ASTVisitor {
	private ITextSelection fSelection;
	private String fFileName;
	private VirtualMethodContainer fMethodContainer;
	private IASTNode fClassNode;
	private String fClassName;
	private ICPPClassType fClassBinding;
	
	/**
	 * 
	 * @param textSelection
	 * @param fileName
	 * @param methodContainer the VirtualMethodContainer to be filled by this
	 * visitor.
	 */
	public VirtualMethodsASTVisitor(ITextSelection textSelection, String fileName,
			VirtualMethodContainer methodContainer) {
		// Visit only decl specifier.
		shouldVisitDeclSpecifiers = true;

		this.fSelection = textSelection;
		this.fFileName = fileName;
		this.fMethodContainer= methodContainer;
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
	private boolean isInsideSelection(IASTNode node) {
		IASTFileLocation location = node.getFileLocation();
		
		// node has no location if it is for example built-in macro
		if (location == null) {
			return false;
		}
		
		if (location.getNodeOffset() <= fSelection.getOffset() &&
				fSelection.getOffset() <= location.getNodeOffset() + location.getNodeLength() &&
				location.getFileName().contains(fFileName))
		{
			return true;
		}
		else {
			return false;
		}
	}
	
	@Override
	public int visit(IASTDeclSpecifier declSpecifier) {
		// In a class or struct
		if (declSpecifier instanceof ICPPASTCompositeTypeSpecifier) {
			// Check if this class is enclosing text selection.
			if (isInsideSelection(declSpecifier)) {

				// Store.
				fClassNode = declSpecifier;
				
				// Get binding.
				ICPPASTCompositeTypeSpecifier typeSpec = (ICPPASTCompositeTypeSpecifier)declSpecifier;
				IBinding binding = typeSpec.getName().getBinding();
				
				// Check if the binding is already created.
				if (binding == null) {
					// TODO typeSpec.getName().resolveBinding();
					return PROCESS_CONTINUE;
				}
				
				// Check if the binding is of class type.
				if (!(binding instanceof ICPPClassType)) {
					return PROCESS_CONTINUE;
				}
				// Store class namegetRecursivelyAllBases
				fClassName = binding.getName();

				ICPPClassType classType = (ICPPClassType)binding;
				fClassBinding= classType;
				
				MethodCollector methodCollector= new MethodCollector();
				methodCollector.fillContainer(fMethodContainer, classType);
				
				return PROCESS_ABORT;
			}
			else {
				return PROCESS_CONTINUE;
			}
		}
		// TODO return PROCESS_SKIP?
		return PROCESS_CONTINUE;
		
	}
}