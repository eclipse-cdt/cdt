/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 **********************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.contentassist.ICompletionContributor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;

public class DOMCompletionContributor implements ICompletionContributor {

	public void contributeCompletionProposals(ITextViewer viewer,
											  int offset,
											  ASTCompletionNode completionNode,
											  List proposals) {
		if (completionNode != null) {
			int repLength = completionNode.getLength();
			int repOffset = offset - repLength;
			
			IASTName[] names = completionNode.getNames();
			for (int i = 0; i < names.length; ++i) {
				IBinding [] bindings = names[i].resolvePrefix();
				if (bindings != null)
					for (int j = 0; j < bindings.length; ++j)
						proposals.add(createBindingCompletionProposal(bindings[j], repOffset, repLength));
			}
		}
	}

	private ICompletionProposal createBindingCompletionProposal(IBinding binding, int offset, int length) {
		ImageDescriptor imageDescriptor = null;
		
		try {
			if (binding instanceof ITypedef) {
				imageDescriptor = CElementImageProvider.getTypedefImageDescriptor();
			} else if (binding instanceof ICompositeType) {
				if (((ICompositeType)binding).getKey() == ICPPClassType.k_class)
					imageDescriptor = CElementImageProvider.getClassImageDescriptor();
				else if (((ICompositeType)binding).getKey() == ICompositeType.k_struct)
					imageDescriptor = CElementImageProvider.getStructImageDescriptor();
				else if (((ICompositeType)binding).getKey() == ICompositeType.k_union)
					imageDescriptor = CElementImageProvider.getUnionImageDescriptor();
			} else if (binding instanceof IFunction) {
				imageDescriptor = CElementImageProvider.getFunctionImageDescriptor();
			} else if (binding instanceof IVariable) {
				imageDescriptor = CElementImageProvider.getVariableImageDescriptor();
			}
		} catch (DOMException e) {
		}
		
		Image image = imageDescriptor != null
			? CUIPlugin.getImageDescriptorRegistry().get( imageDescriptor )
			: null;

		return new CCompletionProposal(binding.getName(), offset, length, image, binding.getName(), 1);
	}

}
