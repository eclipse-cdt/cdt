/*******************************************************************************
 * Copyright (c) 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.swt.graphics.Image;

import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTFunctionStyleMacroParameter;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ICPartitions;

import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;

/**
 * Searches the DOM (both the AST and the index) for completion proposals.
 * 
 * @author Bryan Wilkinson
 */
public class DOMCompletionProposalComputer extends ParsingBasedProposalComputer {

	/**
	 * Default constructor is required (executable extension).
	 */
	public DOMCompletionProposalComputer() {
	}
	
	protected List computeCompletionProposals(
			CContentAssistInvocationContext context,
			ASTCompletionNode completionNode, String prefix) {

		List proposals = new ArrayList();
		
		if(inPreprocessorDirective(context)) {
			// add only macros
			addMacroProposals(context, prefix, proposals);
		} else {
			boolean handleMacros= false;
			IASTName[] names = completionNode.getNames();

			for (int i = 0; i < names.length; ++i) {
				if (names[i].getTranslationUnit() == null)
					// The node isn't properly hooked up, must have backtracked out of this node
					continue;
				
				IASTCompletionContext astContext = names[i].getCompletionContext();
				if (astContext == null) {
					continue;
				} else if (astContext instanceof IASTIdExpression) {
					// handle macros only if there is a prefix
					handleMacros = prefix.length() > 0;
				}
				
				IBinding[] bindings = astContext.findBindings(
						names[i], !context.isContextInformationStyle());
				
				if (bindings != null)
					for (int j = 0; j < bindings.length; ++j)
						handleBinding(bindings[j], context, astContext, proposals);
			}

			if (handleMacros)
				addMacroProposals(context, prefix, proposals);
		}
		
		return proposals;
	}

	/**
	 * Check if given offset is inside a preprocessor directive.
	 * 
	 * @param doc  the document
	 * @param offset  the offset to check
	 * @return <code>true</code> if offset is inside a preprocessor directive
	 */
	private boolean inPreprocessorDirective(CContentAssistInvocationContext context) {
		IDocument doc = context.getViewer().getDocument();
		int offset = context.getParseOffset();
		
		if (offset > 0 && offset == doc.getLength()) {
		--offset;
		}
		try {
			return ICPartitions.C_PREPROCESSOR
					.equals(TextUtilities.getContentType(doc, ICPartitions.C_PARTITIONING, offset, false));
		} catch (BadLocationException exc) {
		}
		return false;
	}

	private void addMacroProposals(CContentAssistInvocationContext context, String prefix, List proposals) {
		char[] prefixChars= prefix.toCharArray();
		ASTCompletionNode completionNode = context.getCompletionNode();
		IASTPreprocessorMacroDefinition[] macros = completionNode.getTranslationUnit().getMacroDefinitions();
		if (macros != null)
			for (int i = 0; i < macros.length; ++i)
				if (CharArrayUtils.equals(macros[i].getName().toCharArray(), 0, prefixChars.length, prefixChars, false))
					handleMacro(macros[i], context, proposals);
		macros = completionNode.getTranslationUnit().getBuiltinMacroDefinitions();
		if (macros != null)
			for (int i = 0; i < macros.length; ++i)
				if (CharArrayUtils.equals(macros[i].getName().toCharArray(), 0, prefixChars.length, prefixChars, false))
					handleMacro(macros[i], context, proposals);
	}
	
	private void handleMacro(IASTPreprocessorMacroDefinition macro, CContentAssistInvocationContext context, List proposals) {
		String macroName = macro.getName().toString();
		Image image = getImage(CElementImageProvider.getMacroImageDescriptor());
		
		if (macro instanceof IASTPreprocessorFunctionStyleMacroDefinition) {
			IASTPreprocessorFunctionStyleMacroDefinition functionMacro = (IASTPreprocessorFunctionStyleMacroDefinition)macro;
			
			StringBuffer repStringBuff = new StringBuffer();
			repStringBuff.append(macroName);
			repStringBuff.append('(');
			
			StringBuffer args = new StringBuffer();

			IASTFunctionStyleMacroParameter[] params = functionMacro.getParameters();
			if (params != null)
				for (int i = 0; i < params.length; ++i) {
					if (i > 0)
						args.append(", "); //$NON-NLS-1$
					args.append(params[i].getParameter());
				}
			String argString = args.toString();
			
			StringBuffer descStringBuff = new StringBuffer(repStringBuff.toString());
			descStringBuff.append(argString);
			descStringBuff.append(')');
			
			repStringBuff.append(')');
			String repString = repStringBuff.toString();
			String descString = descStringBuff.toString();
			
			CCompletionProposal proposal = createProposal(repString, descString, image, context);
			if (!context.isContextInformationStyle()) {
				proposal.setCursorPosition(repString.length() - 1);
			}
			
			if (argString.length() > 0) {
				CProposalContextInformation info = new CProposalContextInformation(image, descString, argString);
				info.setContextInformationPosition(context.getContextInformationOffset());
				proposal.setContextInformation(info);
			}
			
			proposals.add(proposal);
		} else
			proposals.add(createProposal(macroName, macroName, image, context));
	}
	
	protected void handleBinding(IBinding binding,
			CContentAssistInvocationContext cContext,
			IASTCompletionContext astContext, List proposals) {
		if (binding instanceof ICPPClassType) {
			handleClass((ICPPClassType) binding, cContext, proposals);
		} else if (binding instanceof IFunction)  {
			handleFunction((IFunction)binding, cContext, proposals);
		} else if (binding instanceof IVariable)  {
			handleVariable((IVariable) binding, cContext, proposals);
		} else if (!cContext.isContextInformationStyle()) {
			proposals.add(createProposal(binding.getName(), binding.getName(), getImage(binding), cContext));
		}
	}
	
	private void handleClass(ICPPClassType classType, CContentAssistInvocationContext context, List proposals) {
		if (context.isContextInformationStyle()) {
			try {
				ICPPConstructor[] constructors = classType.getConstructors();
				for (int i = 0; i < constructors.length; i++) {
					handleFunction(constructors[i], context, proposals);
				}
			} catch (DOMException e) {
			}
		} else {
			proposals.add(createProposal(classType.getName(), classType.getName(), getImage(classType), context));
		}
	}
	
	private void handleFunction(IFunction function, CContentAssistInvocationContext context, List proposals) {
		Image image = getImage(function);
		
		StringBuffer repStringBuff = new StringBuffer();
		repStringBuff.append(function.getName());
		repStringBuff.append('(');
		
		StringBuffer dispargs = new StringBuffer(); // for the displayString
        StringBuffer idargs = new StringBuffer();   // for the idString
		String returnTypeStr = null;
		try {
			IParameter[] params = function.getParameters();
			if (params != null)
				for (int i = 0; i < params.length; ++i) {
					IType paramType = params[i].getType();
					if (i > 0) {
                        dispargs.append(',');
                        idargs.append(',');
                    }

					dispargs.append(ASTTypeUtil.getType(paramType));
                    idargs.append(ASTTypeUtil.getType(paramType));
					String paramName = params[i].getName();
					if (paramName != null && paramName.length() > 0) {
						dispargs.append(' ');
						dispargs.append(paramName);
					}
				}
			
			if (function.takesVarArgs()) {
				if (params.length > 0) {
                    dispargs.append(',');
                    idargs.append(',');
                }
                dispargs.append("..."); //$NON-NLS-1$
                idargs.append("..."); //$NON-NLS-1$
			} else if (params.length == 0) { // force the void in
			    dispargs.append("void"); //$NON-NLS-1$
                idargs.append("void"); //$NON-NLS-1$
            }
			
			IFunctionType functionType = function.getType();
			if (functionType != null) {
				IType returnType = functionType.getReturnType();
				if (returnType != null)
					returnTypeStr = ASTTypeUtil.getType(returnType);
			}
		} catch (DOMException e) {
		}
        
        String dispargString = dispargs.toString();
        String idargString = idargs.toString();
		
        StringBuffer dispStringBuff = new StringBuffer(repStringBuff.toString());
		dispStringBuff.append(dispargString);
        dispStringBuff.append(')');
        if (returnTypeStr != null && returnTypeStr.length() > 0) {
            dispStringBuff.append(' ');
            dispStringBuff.append(returnTypeStr);
        }
        String dispString = dispStringBuff.toString();

        StringBuffer idStringBuff = new StringBuffer(repStringBuff.toString());
        idStringBuff.append(idargString);
        idStringBuff.append(')');
        String idString = idStringBuff.toString();
		
        repStringBuff.append(')');
        String repString = repStringBuff.toString();

        CCompletionProposal proposal = createProposal(repString, dispString, idString, image, context);
		if (!context.isContextInformationStyle()) {
			proposal.setCursorPosition(repString.length() - 1);
		}
		
		if (dispargString.length() > 0) {
			CProposalContextInformation info = new CProposalContextInformation(image, dispString, dispargString);
			info.setContextInformationPosition(context.getContextInformationOffset());
			proposal.setContextInformation(info);
		}
		
		proposals.add(proposal);
	}
	
	private void handleVariable(IVariable variable, CContentAssistInvocationContext context, List proposals) {
		if (context.isContextInformationStyle()) return;
		
		StringBuffer repStringBuff = new StringBuffer();
		repStringBuff.append(variable.getName());
		
		String returnTypeStr = "<unknown>"; //$NON-NLS-1$
		try {
			IType varType = variable.getType();
			if (varType != null)
				returnTypeStr = ASTTypeUtil.getType(varType);
		} catch (DOMException e) {
		}
        
        StringBuffer dispStringBuff = new StringBuffer(repStringBuff.toString());
        if (returnTypeStr != null) {
            dispStringBuff.append(" : "); //$NON-NLS-1$
            dispStringBuff.append(returnTypeStr);
        }
        String dispString = dispStringBuff.toString();

        StringBuffer idStringBuff = new StringBuffer(repStringBuff.toString());
        String idString = idStringBuff.toString();
		
        String repString = repStringBuff.toString();

		Image image = getImage(variable);
		CCompletionProposal proposal = createProposal(repString, dispString, idString, image, context);
		proposals.add(proposal);
	}
	
	private CCompletionProposal createProposal(String repString, String dispString, Image image, CContentAssistInvocationContext context) {
		return createProposal(repString, dispString, null, image, context);
	}
	
	private CCompletionProposal createProposal(String repString, String dispString, String idString, Image image, CContentAssistInvocationContext context) {
		int parseOffset = context.getParseOffset();
		int invocationOffset = context.getInvocationOffset();
		boolean doReplacement = !context.isContextInformationStyle();
		
		int repLength = doReplacement ? context.getCompletionNode().getLength() : 0;
		int repOffset = doReplacement ? parseOffset - repLength : invocationOffset;
		repString = doReplacement ? repString : ""; //$NON-NLS-1$
		
		return new CCompletionProposal(repString, repOffset, repLength, image, dispString, idString, 1, context.getViewer());
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
				if (((ICompositeType)binding).getKey() == ICPPClassType.k_class || binding instanceof ICPPClassTemplate)
					imageDescriptor = CElementImageProvider.getClassImageDescriptor();
				else if (((ICompositeType)binding).getKey() == ICompositeType.k_struct)
					imageDescriptor = CElementImageProvider.getStructImageDescriptor();
				else if (((ICompositeType)binding).getKey() == ICompositeType.k_union)
					imageDescriptor = CElementImageProvider.getUnionImageDescriptor();
			} else if (binding instanceof ICPPMethod) {
				switch (((ICPPMethod)binding).getVisibility()) {
				case ICPPMember.v_private:
					imageDescriptor = CElementImageProvider.getMethodImageDescriptor(ASTAccessVisibility.PRIVATE);
					break;
				case ICPPMember.v_protected:
					imageDescriptor = CElementImageProvider.getMethodImageDescriptor(ASTAccessVisibility.PROTECTED);
					break;
				default:
					imageDescriptor = CElementImageProvider.getMethodImageDescriptor(ASTAccessVisibility.PUBLIC);
					break;
				}
			} else if (binding instanceof IFunction) {
				imageDescriptor = CElementImageProvider.getFunctionImageDescriptor();
			} else if (binding instanceof ICPPField) {
				switch (((ICPPField)binding).getVisibility()) {
				case ICPPMember.v_private:
					imageDescriptor = CElementImageProvider.getFieldImageDescriptor(ASTAccessVisibility.PRIVATE);
					break;
				case ICPPMember.v_protected:
					imageDescriptor = CElementImageProvider.getFieldImageDescriptor(ASTAccessVisibility.PROTECTED);
					break;
				default:
					imageDescriptor = CElementImageProvider.getFieldImageDescriptor(ASTAccessVisibility.PUBLIC);
					break;
				}
			} else if (binding instanceof IField) {
				imageDescriptor = CElementImageProvider.getFieldImageDescriptor(ASTAccessVisibility.PUBLIC);
			} else if (binding instanceof IVariable) {
				imageDescriptor = CElementImageProvider.getVariableImageDescriptor();
			} else if (binding instanceof IEnumeration) {
				imageDescriptor = CElementImageProvider.getEnumerationImageDescriptor();
            } else if (binding instanceof IEnumerator) {
                imageDescriptor = CElementImageProvider.getEnumeratorImageDescriptor();
            } else if (binding instanceof ICPPNamespace) {
				imageDescriptor = CElementImageProvider.getNamespaceImageDescriptor();
			} else if (binding instanceof ICPPFunctionTemplate) {
				imageDescriptor = CElementImageProvider.getFunctionImageDescriptor();
			} else if (binding instanceof ICPPUsingDeclaration) {
				ICPPDelegate[] delegates = ((ICPPUsingDeclaration)binding).getDelegates();
				if (delegates.length > 0)
					return getImage(delegates[0]);
			}
		} catch (DOMException e) {
		}
		
		return imageDescriptor != null
			? CUIPlugin.getImageDescriptorRegistry().get( imageDescriptor )
			: null;
	}
}
