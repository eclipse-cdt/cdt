/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.contentassist;

import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.contentassist.ICompletionContributor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.ITextViewer;

/**
 * @author Doug Schaefer
 * 
 * Completion contributor that looks up prefixes in the PDOM.
 */
public class PDOMCompletionContributor extends DOMCompletionContributor implements ICompletionContributor {

	public void contributeCompletionProposals(
			final ITextViewer viewer,
			final int offset,
			final IWorkingCopy workingCopy,
			final ASTCompletionNode completionNode,
			final String prefix,
			final List proposals) {
		
		if (completionNode == null)
			return;

		// Return anyway
		if (completionNode != null)
			return;
		
		try {
			IPDOM pdom = CCorePlugin.getPDOMManager().getPDOM(workingCopy.getCProject());
			IASTName[] names = completionNode.getNames();
			for (int i = 0; i < names.length; ++i) {
				IASTName name = names[i];
				IASTNode parent = name.getParent();
				if (parent instanceof IASTIdExpression || parent instanceof IASTNamedTypeSpecifier) {
					pdom.accept(new IPDOMVisitor() {
						public boolean visit(IPDOMNode node) throws CoreException {
							if (node instanceof PDOMLinkage)
								return true;
							
							if (node instanceof PDOMBinding) {
								PDOMBinding binding = (PDOMBinding)node;
								if (binding.getName().startsWith(prefix)) {
									handleBinding(binding, completionNode, offset, viewer, proposals);
								}
							}
							return false;
						}
					});
				} else if (parent instanceof IASTFieldReference) {
					// Find the type the look at the fields
					IASTFieldReference fieldRef = (IASTFieldReference)parent;
					IASTExpression expression = fieldRef.getFieldOwner();
					IType type = expression.getExpressionType();
					if (type != null && type instanceof IBinding) {
						IBinding binding = (IBinding)type;
						PDOMLinkage linkage = ((PDOM)pdom).getLinkage(workingCopy.getLanguage());
						PDOMBinding pdomBinding = linkage.adaptBinding(binding);
						if (pdomBinding != null) {
							pdomBinding.accept(new IPDOMVisitor() {
								public boolean visit(IPDOMNode node) throws CoreException {
									if (node instanceof IField) {
										PDOMBinding binding = (PDOMBinding)node;
										if (binding.getName().startsWith(prefix))
											handleBinding(binding, completionNode, offset, viewer, proposals);
									}
									return false;
								}
							});
						}
					}
				}
			}
		} catch (CoreException e) {
			CUIPlugin.getDefault().log(e);
		}
	}

}
