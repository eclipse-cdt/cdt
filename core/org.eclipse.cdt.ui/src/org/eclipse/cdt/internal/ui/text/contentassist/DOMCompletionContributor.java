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
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.contentassist.ICompletionContributor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.graphics.Image;

public class DOMCompletionContributor implements ICompletionContributor {

	public void contributeCompletionProposals(ITextViewer viewer,
											  int offset,
											  ASTCompletionNode completionNode,
											  List proposals) {
		if (completionNode != null) {
			IASTName[] names = completionNode.getNames();
			for (int i = 0; i < names.length; ++i) {
				IBinding[] bindings = names[i].resolvePrefix();
				for (int j = 0; j < bindings.length; ++j)
					handleBinding(names[i], bindings[j], completionNode, offset, viewer, proposals);
			}
		}
	}

	private void handleBinding(IASTName name, IBinding binding, ASTCompletionNode completionNode, int offset, ITextViewer viewer, List proposals) {
		if (binding instanceof IFunction)
			handleFunction(name, (IFunction)binding, completionNode, offset, viewer, proposals);
		else
			proposals.add(createProposal(binding.getName(), binding.getName(), getImage(binding), completionNode, offset, viewer));
	}
	
	private void handleFunction(IASTName name, IFunction function, ASTCompletionNode completionNode, int offset, ITextViewer viewer, List proposals) {
		Image image = getImage(CElementImageProvider.getFunctionImageDescriptor());
		
		StringBuffer repStringBuff = new StringBuffer();
		repStringBuff.append(function.getName());
		repStringBuff.append('(');
		
		StringBuffer args = new StringBuffer();
		String returnTypeStr = null;
		try {
			IParameter[] params = function.getParameters();
			if (params != null)
				for (int i = 0; i < params.length; ++i) {
					IType paramType = params[i].getType();
					if (i > 0)
						args.append(',');
					
					args.append(ASTTypeUtil.getType(paramType));
					String paramName = params[i].getName();
					if (paramName != null) {
						args.append(' ');
						args.append(paramName);
					}
				}
			
			IType returnType = function.getType().getReturnType();
			if (returnType != null)
				returnTypeStr = ASTTypeUtil.getType(returnType);
		} catch (DOMException e) {
		}
		String argString = args.toString();
		
		StringBuffer descStringBuff = new StringBuffer(repStringBuff.toString());
		descStringBuff.append(argString);
		descStringBuff.append(')');
		
		if (returnTypeStr != null) {
			descStringBuff.append(' ');
			descStringBuff.append(returnTypeStr);
		}
		
		repStringBuff.append(')');
		String repString = repStringBuff.toString();
		String descString = descStringBuff.toString();
		
		CCompletionProposal proposal = createProposal(repString, descString, image, completionNode, offset, viewer);
		proposal.setCursorPosition(repString.length() - 1);
		
		if (argString.length() > 0) {
			CProposalContextInformation info = new CProposalContextInformation(repString, argString);
			info.setContextInformationPosition(offset);
			proposal.setContextInformation(info);
		}
		
		proposals.add(proposal);
	}
	
	private CCompletionProposal createProposal(String repString, String dispString, Image image, ASTCompletionNode completionNode, int offset, ITextViewer viewer) {
		int repLength = completionNode.getLength();
		int repOffset = offset - repLength;
		return new CCompletionProposal(repString, repOffset, repLength, image, dispString, 1, viewer);
	}

	private Image getImage(ImageDescriptor desc) {
		return desc != null ? CUIPlugin.getImageDescriptorRegistry().get(desc) : null;
	}
	
	private Image getImage(IBinding binding) {
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
		
		return imageDescriptor != null
			? CUIPlugin.getImageDescriptorRegistry().get( imageDescriptor )
			: null;
	}
	
}
