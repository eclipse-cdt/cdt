/*******************************************************************************
 * Copyright (c) 2007, 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Anton Leherbauer (Wind River Systems)
 *     Sergey Prigogin (Google)
 *     Jens Elmenthaler - http://bugs.eclipse.org/173458 (camel case completion)
 *     Nathan Ridge
 *     Thomas Corbat (IFS)
 *     Mohamed Azab (Mentor Graphics) - Bug 438549. Add mechanism for parameter guessing.
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionStyleMacroParameter;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.c.ICFunctionScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.IContentAssistMatcher;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ICPartitions;

import org.eclipse.cdt.internal.core.dom.parser.c.CBuiltinParameter;
import org.eclipse.cdt.internal.core.dom.parser.c.CBuiltinVariable;
import org.eclipse.cdt.internal.core.dom.parser.c.CImplicitFunction;
import org.eclipse.cdt.internal.core.dom.parser.c.CImplicitTypedef;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBuiltinParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBuiltinVariable;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPImplicitFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPImplicitMethod;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPImplicitTypedef;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.AccessContext;
import org.eclipse.cdt.internal.core.parser.util.ContentAssistMatcherFactory;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;

/**
 * Searches the DOM (both the AST and the index) for completion proposals.
 *
 * @author Bryan Wilkinson
 */
public class DOMCompletionProposalComputer extends ParsingBasedProposalComputer {
	private static final String HASH = "#"; //$NON-NLS-1$;
	private static final String DEFAULT_ARGUMENT_PATTERN = " = {0}"; //$NON-NLS-1$;
	private static final String TEMPLATE_PARAMETER_PATTERN = "template<{0}> class"; //$NON-NLS-1$;
	private static final String TYPENAME = "typename"; //$NON-NLS-1$;
	private static final String ELLIPSIS = "..."; //$NON-NLS-1$;
	private String fPrefix;
	private ArrayList<IBinding> fAvailableElements;

	/**
	 * Default constructor is required (executable extension).
	 */
	public DOMCompletionProposalComputer() {
		fPrefix = ""; //$NON-NLS-1$
	}

	@Override
	protected List<ICompletionProposal> computeCompletionProposals(
			CContentAssistInvocationContext context,
			IASTCompletionNode completionNode, String prefix) {
		fPrefix = prefix;
		initializeDefinedElements(context);
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

		if (inPreprocessorDirective(context)) {
			if (!inPreprocessorKeyword(context)) {
				// add only macros
				if (prefix.length() == 0) {
					try {
						prefix= context.computeIdentifierPrefix().toString();
					} catch (BadLocationException exc) {
						CUIPlugin.log(exc);
					}
				}
				addMacroProposals(context, prefix, proposals);
			}
		} else {
			boolean handleMacros= false;
			IASTName[] names = completionNode.getNames();

			for (IASTName name : names) {
				if (name.getTranslationUnit() == null)
					// The node isn't properly hooked up, must have backtracked out of this node
					continue;

				IASTCompletionContext astContext = name.getCompletionContext();
				if (astContext == null) {
					continue;
				} else if (astContext instanceof IASTIdExpression
						|| astContext instanceof IASTNamedTypeSpecifier) {
					// handle macros only if there is a prefix
					handleMacros = prefix.length() > 0;
				}

				IBinding[] bindings = astContext.findBindings(name, !context.isContextInformationStyle());

				if (bindings != null) {
					AccessContext accessibilityContext = new AccessContext(name);
					for (IBinding binding : bindings) {
						if (accessibilityContext.isAccessible(binding))
							handleBinding(binding, context, prefix, astContext, proposals);
					}
				}
			}

			if (handleMacros)
				addMacroProposals(context, prefix, proposals);
		}

		return proposals;
	}

	/**
	 * Test whether the invocation offset is inside or before the preprocessor directive keyword.
	 *
	 * @param context  the invocation context
	 * @return <code>true</code> if the invocation offset is inside or before the directive keyword
	 */
	private boolean inPreprocessorKeyword(CContentAssistInvocationContext context) {
		IDocument doc = context.getDocument();
		int offset = context.getInvocationOffset();

		try {
			final ITypedRegion partition=
					TextUtilities.getPartition(doc, ICPartitions.C_PARTITIONING, offset, true);
			if (ICPartitions.C_PREPROCESSOR.equals(partition.getType())) {
				String ppPrefix= doc.get(partition.getOffset(), offset - partition.getOffset());
				if (ppPrefix.matches("\\s*#\\s*\\w*")) { //$NON-NLS-1$
					// we are inside the directive keyword
					return true;
				}
			}

		} catch (BadLocationException exc) {
		}
		return false;
	}

	/**
	 * Check if the invocation offset is inside a preprocessor directive.
	 *
	 * @param context  the content asist invocation context
	 * @return <code>true</code> if invocation offset is inside a preprocessor directive
	 */
	private boolean inPreprocessorDirective(CContentAssistInvocationContext context) {
		IDocument doc = context.getDocument();
		int offset = context.getInvocationOffset();

		try {
			final ITypedRegion partition=
					TextUtilities.getPartition(doc, ICPartitions.C_PARTITIONING, offset, true);
			if (ICPartitions.C_PREPROCESSOR.equals(partition.getType())) {
				return true;
			}

		} catch (BadLocationException exc) {
		}
		return false;
	}

	private void addMacroProposals(CContentAssistInvocationContext context, String prefix,
			List<ICompletionProposal> proposals) {
		IASTCompletionNode completionNode = context.getCompletionNode();
		addMacroProposals(context, prefix, proposals,
				completionNode.getTranslationUnit().getMacroDefinitions());
		addMacroProposals(context, prefix, proposals,
				completionNode.getTranslationUnit().getBuiltinMacroDefinitions());
	}

	private void addMacroProposals(CContentAssistInvocationContext context, String prefix,
			List<ICompletionProposal> proposals, IASTPreprocessorMacroDefinition[] macros) {
		if (macros != null) {
			char[] prefixChars= prefix.toCharArray();
			final boolean matchPrefix= !context.isContextInformationStyle();
			if (matchPrefix) {
				IContentAssistMatcher matcher = ContentAssistMatcherFactory.getInstance().createMatcher(prefixChars);
				for (int i = 0; i < macros.length; ++i) {
					final char[] macroName= macros[i].getName().toCharArray();
					if (matcher.match(macroName)) {
						handleMacro(macros[i], context, prefix, proposals);
					}
				}
			} else {
				for (int i = 0; i < macros.length; ++i) {
					final char[] macroName= macros[i].getName().toCharArray();
					if (CharArrayUtils.equals(macroName, 0, macroName.length, prefixChars, true)) {
						handleMacro(macros[i], context, prefix, proposals);
					}
				}
			}
		}
	}

	private void handleMacro(IASTPreprocessorMacroDefinition macro, CContentAssistInvocationContext context,
			String prefix, List<ICompletionProposal> proposals) {
		final String macroName = macro.getName().toString();
		final int baseRelevance= computeBaseRelevance(prefix, macroName);

		Image image = getImage(CElementImageProvider.getMacroImageDescriptor());

		if (macro instanceof IASTPreprocessorFunctionStyleMacroDefinition) {
			IASTPreprocessorFunctionStyleMacroDefinition functionMacro =
					(IASTPreprocessorFunctionStyleMacroDefinition) macro;

			StringBuilder repStringBuff = new StringBuilder();
			repStringBuff.append(macroName);
			repStringBuff.append('(');

			StringBuilder args = new StringBuilder();

			IASTFunctionStyleMacroParameter[] params = functionMacro.getParameters();
			if (params != null) {
				final String parameterDelimiter = context.getFunctionParameterDelimiter();
				for (int i = 0; i < params.length; ++i) {
					if (i > 0) {
						args.append(parameterDelimiter);
					}
					args.append(params[i].getParameter());
				}
			}
			String argString = args.toString();

			StringBuilder descStringBuff = new StringBuilder(repStringBuff.toString());
			descStringBuff.append(argString);
			descStringBuff.append(')');

			repStringBuff.append(')');
			String repString = repStringBuff.toString();
			String descString = descStringBuff.toString();

			CCompletionProposal proposal = createProposal(repString, descString, prefix.length(), image,
					baseRelevance + RelevanceConstants.MACRO_TYPE_RELEVANCE, context);
			if (!context.isContextInformationStyle()) {
				if (argString.length() > 0) {
					proposal.setCursorPosition(repString.length() - 1);
				} else {
					proposal.setCursorPosition(repString.length());
				}
			}

			if (argString.length() > 0) {
				CProposalContextInformation info = new CProposalContextInformation(image, descString, argString);
				info.setContextInformationPosition(context.getContextInformationOffset());
				proposal.setContextInformation(info);
			}

			proposals.add(proposal);
		} else {
			proposals.add(createProposal(macroName, macroName, prefix.length(), image,
					baseRelevance + RelevanceConstants.MACRO_TYPE_RELEVANCE, context));
		}
	}

	protected void handleBinding(IBinding binding, CContentAssistInvocationContext cContext, String prefix,
			IASTCompletionContext astContext, List<ICompletionProposal> proposals) {
		if ((binding instanceof CPPImplicitFunction
				|| binding instanceof CPPImplicitTypedef
				|| binding instanceof CPPBuiltinVariable
				|| binding instanceof CPPBuiltinParameter
				|| binding instanceof CImplicitFunction
				|| binding instanceof CImplicitTypedef
				|| binding instanceof CBuiltinVariable
				|| binding instanceof CBuiltinParameter
				|| binding instanceof ICPPClassTemplatePartialSpecialization)
				&& !(binding instanceof CPPImplicitMethod)) {
			return;
		}

		if (!isAnonymousBinding(binding)) {
			final String name = binding.getName();
			final int baseRelevance= computeBaseRelevance(prefix, name);
			if (binding instanceof ICPPClassType) {
				handleClass((ICPPClassType) binding, astContext, cContext, baseRelevance, proposals);
			} else if (binding instanceof IFunction) {
				handleFunction((IFunction) binding, cContext, baseRelevance, proposals);
			} else if (binding instanceof IVariable) {
				handleVariable((IVariable) binding, cContext, baseRelevance, proposals);
			} else if (!cContext.isContextInformationStyle()) {
				if (binding instanceof ITypedef) {
					proposals.add(createProposal(name, name, getImage(binding),
							baseRelevance + RelevanceConstants.TYPEDEF_TYPE_RELEVANCE, cContext));
				} else if (binding instanceof ICPPNamespace) {
					handleNamespace((ICPPNamespace) binding, astContext, cContext, baseRelevance, proposals);
				} else if (binding instanceof IEnumeration) {
					proposals.add(createProposal(name, name, getImage(binding),
							baseRelevance + RelevanceConstants.ENUMERATION_TYPE_RELEVANCE, cContext));
				} else if (binding instanceof IEnumerator) {
					proposals.add(createProposal(name, name, getImage(binding),
							baseRelevance + RelevanceConstants.ENUMERATOR_TYPE_RELEVANCE, cContext));
				} else {
					proposals.add(createProposal(name, name, getImage(binding),
							baseRelevance + RelevanceConstants.DEFAULT_TYPE_RELEVANCE, cContext));
				}
			}
		}
	}

	private boolean isAnonymousBinding(IBinding binding) {
		char[] name= binding.getNameCharArray();
		return name.length == 0 || name[0] == '{';
	}

	private void addProposalForClassTemplate(ICPPClassTemplate templateType, CContentAssistInvocationContext context,
			int baseRelevance, List<ICompletionProposal> proposals) {
		int relevance = getClassTypeRelevance(templateType);
		StringBuilder representation = new StringBuilder(templateType.getName());
		boolean inUsingDeclaration = context.isInUsingDirective();
		String templateParameterRepresentation = ""; //$NON-NLS-1$
		if (!inUsingDeclaration) {
			representation.append("<{0}>"); //$NON-NLS-1$
			templateParameterRepresentation = buildTemplateParameters(templateType, context);
		} else if (!context.isFollowedBySemicolon()) {
			representation.append(';');
		}
		String representationString = MessageFormat.format(representation.toString(), ""); //$NON-NLS-1$
		String displayString = MessageFormat.format(representation.toString(), templateParameterRepresentation);
		CCompletionProposal proposal = createProposal(representationString, displayString, getImage(templateType),
				baseRelevance + relevance, context);

		if (!inUsingDeclaration) {
			CProposalContextInformation info =
					new CProposalContextInformation(getImage(templateType), displayString, templateParameterRepresentation);
			info.setContextInformationPosition(context.getContextInformationOffset());
			proposal.setContextInformation(info);
			if (!context.isContextInformationStyle()) {
				proposal.setCursorPosition(representationString.length() - 1);
			}
		}
		proposals.add(proposal);
	}

	private String buildTemplateParameters(ICPPClassTemplate templateType, CContentAssistInvocationContext context) {
		ICPPTemplateParameter[] parameters = templateType.getTemplateParameters();
		StringBuilder representation = new StringBuilder();

		final String parameterDelimiter = context.getTemplateParameterDelimiter();
		final boolean addDefaultedParameters = isDisplayDefaultedParameters();
		final boolean addDefaultArguments = isDisplayDefaultArguments();
		for (int i = 0; i < parameters.length; i++) {
			ICPPTemplateParameter parameter = parameters[i];
			ICPPTemplateArgument defaultValue = parameter.getDefaultValue();
			if (!addDefaultedParameters && defaultValue != null) {
				break;
			}
			if (i > 0) {
				representation.append(parameterDelimiter);
			}
			if (parameter instanceof ICPPTemplateNonTypeParameter) {
				IType parameterType = ((ICPPTemplateNonTypeParameter) parameter).getType();
				String typeName = ASTTypeUtil.getType(parameterType);
				representation.append(typeName);
			} else if (parameter instanceof ICPPTemplateTypeParameter) {
				representation.append(TYPENAME);
			} else if (parameter instanceof ICPPTemplateTemplateParameter) {
				String templateParameterParameters = buildTemplateParameters((ICPPTemplateTemplateParameter) parameter, context);
				representation.append(MessageFormat.format(TEMPLATE_PARAMETER_PATTERN, templateParameterParameters));
				representation.append(templateParameterParameters);
			}
			if (parameter.isParameterPack()) {
				representation.append(ELLIPSIS);
			}
			representation.append(' ');
			representation.append(parameter.getName());
			if (addDefaultArguments && defaultValue != null) {
				String defaultArgumentRepresentation = MessageFormat.format(DEFAULT_ARGUMENT_PATTERN, defaultValue);
				for (int parameterIndex = 0; parameterIndex < i; parameterIndex++) {
					String templateArgumentID = HASH + parameterIndex;
					String templateArgumentValue = parameters[parameterIndex].getName();
					defaultArgumentRepresentation = defaultArgumentRepresentation.replaceAll(templateArgumentID, templateArgumentValue);
				}
				representation.append(defaultArgumentRepresentation);
			}
		}
		return representation.toString();
	}

	private void handleClass(ICPPClassType classType, IASTCompletionContext astContext,
			CContentAssistInvocationContext context, int baseRelevance, List<ICompletionProposal> proposals) {
		if (context.isContextInformationStyle() && context.isAfterOpeningParenthesis()) {
			addProposalsForConstructors(classType, context, baseRelevance, proposals);
		} else if (classType instanceof ICPPClassTemplate) {
			addProposalForClassTemplate((ICPPClassTemplate) classType, context, baseRelevance, proposals);
		} else {
			int relevance = getClassTypeRelevance(classType);
			if (astContext instanceof IASTName && !(astContext instanceof ICPPASTQualifiedName)) {
				IASTName name= (IASTName)astContext;
				if (name.getParent() instanceof IASTDeclarator) {
					proposals.add(createProposal(classType.getName() + "::", classType.getName(), //$NON-NLS-1$
							getImage(classType), baseRelevance + relevance, context));
				}
			}
			proposals.add(createProposal(classType.getName(), classType.getName(), getImage(classType),
					baseRelevance + RelevanceConstants.CLASS_TYPE_RELEVANCE, context));
		}
	}

	private void addProposalsForConstructors(ICPPClassType classType,
			CContentAssistInvocationContext context, int baseRelevance, List<ICompletionProposal> proposals) {
		ICPPConstructor[] constructors = classType.getConstructors();
		for (ICPPConstructor constructor : constructors) {
			handleFunction(constructor, context, baseRelevance, proposals);
		}
	}

	private int getClassTypeRelevance(ICPPClassType classType) {
		int relevance= 0;
		switch (classType.getKey()) {
		case ICPPClassType.k_class:
			relevance= RelevanceConstants.CLASS_TYPE_RELEVANCE;
			break;
		case ICompositeType.k_struct:
			relevance= RelevanceConstants.STRUCT_TYPE_RELEVANCE;
			break;
		case ICompositeType.k_union:
			relevance= RelevanceConstants.UNION_TYPE_RELEVANCE;
			break;
		}
		return relevance;
	}

	private void handleFunction(IFunction function, CContentAssistInvocationContext context,
			int baseRelevance, List<ICompletionProposal> proposals) {
		Image image = getImage(function);

		StringBuilder repStringBuff = new StringBuilder();
		repStringBuff.append(function.getName());

		repStringBuff.append('(');

		StringBuilder dispargs = new StringBuilder(); // for the dispargString
		StringBuilder idargs = new StringBuilder();   // for the idargString
		boolean hasArgs = true;
		String returnTypeStr = null;
		IParameter[] params = function.getParameters();
		if (params != null) {
			final String parameterDelimiter = context.getFunctionParameterDelimiter();
			for (int i = 0; i < params.length; ++i) {
				IParameter param = params[i];
				if (skipDefaultedParameter(param)) {
					break;
				}
				IType paramType = param.getType();
				if (i > 0) {
					dispargs.append(parameterDelimiter);
					idargs.append(parameterDelimiter);
				}

				dispargs.append(ASTTypeUtil.getType(paramType, false));
				idargs.append(ASTTypeUtil.getType(paramType, false));
				String paramName = param.getName();
				if (paramName != null && paramName.length() > 0) {
					dispargs.append(' ');
					dispargs.append(paramName);
				}
				if (param instanceof ICPPParameter) {
					ICPPParameter cppParam = (ICPPParameter) param;
					if (cppParam.hasDefaultValue() && isDisplayDefaultArguments()) {
						dispargs.append(MessageFormat.format(DEFAULT_ARGUMENT_PATTERN, cppParam.getDefaultValue()));
					}
				}
			}

			if (function.takesVarArgs()) {
				if (params.length > 0) {
					dispargs.append(parameterDelimiter);
					idargs.append(parameterDelimiter);
				}
				dispargs.append("..."); //$NON-NLS-1$
				idargs.append("..."); //$NON-NLS-1$
			} else if (params.length == 0) { // force the void in
				dispargs.append("void"); //$NON-NLS-1$
				idargs.append("void"); //$NON-NLS-1$
			}
		}
		IFunctionType functionType = function.getType();
		if (functionType != null) {
			IType returnType = functionType.getReturnType();
			if (returnType != null)
				returnTypeStr = ASTTypeUtil.getType(returnType, false);
		}

		hasArgs = ASTTypeUtil.functionTakesParameters(function);

        String dispargString = dispargs.toString();
        String idargString = idargs.toString();
		String contextDispargString = hasArgs ? dispargString : null;
        StringBuilder dispStringBuff = new StringBuilder(repStringBuff.toString());
		dispStringBuff.append(dispargString);
        dispStringBuff.append(')');
        if (returnTypeStr != null && returnTypeStr.length() > 0) {
            dispStringBuff.append(" : "); //$NON-NLS-1$
            dispStringBuff.append(returnTypeStr);
        }
        String dispString = dispStringBuff.toString();

        StringBuilder idStringBuff = new StringBuilder(repStringBuff.toString());
        idStringBuff.append(idargString);
        idStringBuff.append(')');
        String idString = idStringBuff.toString();

		// In a using declaration, emitting parentheses after the function
		// name is useless, since the user will just have to delete them.
		// Instead, emitting a semicolon is useful.
		boolean inUsingDeclaration = context.isInUsingDirective();
		if (inUsingDeclaration) {
			repStringBuff.setLength(repStringBuff.length() - 1);  // Remove opening parenthesis
			if (!context.isFollowedBySemicolon()) {
				repStringBuff.append(';');
			}
		} else {
			repStringBuff.append(')');
		}

        String repString = repStringBuff.toString();

        final int relevance = function instanceof ICPPMethod ?
        		RelevanceConstants.METHOD_TYPE_RELEVANCE : RelevanceConstants.FUNCTION_TYPE_RELEVANCE;
		CCompletionProposal proposal = createProposal(repString, dispString, idString,
				context.getCompletionNode().getLength(), image, baseRelevance + relevance, context);
		if (!context.isContextInformationStyle()) {
			int cursorPosition = (!inUsingDeclaration && hasArgs) ? (repString.length() - 1) : repString.length();
			proposal.setCursorPosition(cursorPosition);
		}

		if (contextDispargString != null && !inUsingDeclaration) {
			CProposalContextInformation info =
					new CProposalContextInformation(image, dispString, contextDispargString);
			info.setContextInformationPosition(context.getContextInformationOffset());
			proposal.setContextInformation(info);
		}

		/*
		 * The ParameterGuessingProposal will be active if the content assist is invoked before typing
		 * any parameters. Otherwise, the normal Parameter Hint Proposal will be added.
		 */
		if (isBeforeParameters(context)) {
			proposals.add(ParameterGuessingProposal.createProposal(context, fAvailableElements, proposal, function, fPrefix));
		} else {
			proposals.add(proposal);
		}
	}

	/**
	 * Returns true if the invocation is at the function name or before typing any parameters
	 */
	private boolean isBeforeParameters(CContentAssistInvocationContext context) {
		/*
		 * Invocation offset and parse offset are the same if content assist is invoked while in the function
		 * name (i.e. before the '('). After that, the parse offset will indicate the end of the name part. If
		 * the diff. between them is zero, then we're still inside the function name part.
		 */
		int relativeOffset = context.getInvocationOffset() - context.getParseOffset();
		if (relativeOffset == 0)
			return true;
		int startOffset = context.getParseOffset();
		try {
			String completePrefix = context.getDocument().get(startOffset,
					context.getInvocationOffset() - startOffset);
			if (completePrefix.trim().endsWith("(")) //$NON-NLS-1$
				return true;
		} catch (BadLocationException e) {
			return false;
		}
		return false;
	}

	/**
	 * Initializes the list of defined elements at the start of the current statement.
	 */
	private void initializeDefinedElements(CContentAssistInvocationContext context) {
		/*
		 * Get all defined elements before the start of the statement.
		 * ex1:	int a = foo( 
		 * 					^ --> We don't want 'a' as a suggestion. 
		 * ex2:	char* foo(int a, int b) {return NULL;} 
		 * 		void bar(char* name) {} 
		 * 		...
		 * 		bar( foo( 
		 * 				 ^ --> If this offset is used, the only defined name will be "bar(char*)".
		 */
		int startOffset = getStatementStartOffset(context.getDocument(),
				context.getParseOffset() - fPrefix.length());
		fAvailableElements = new ArrayList<>();
		IASTCompletionNode node = null;
		// Create a content assist context that points to the start of the statement.
		CContentAssistInvocationContext newContext = new CContentAssistInvocationContext(
				context.getViewer(), startOffset, getCEditor(), true, false);
		try {
			node = newContext.getCompletionNode();
			if (node != null) {
				IASTName[] names = node.getNames();
				for (IASTName name : names) {
					IASTCompletionContext astContext = name.getCompletionContext();
					if (astContext != null) {
						IBinding[] bindings = astContext.findBindings(name, true);
						if (bindings != null) {
							AccessContext accessibilityContext = new AccessContext(name);
							for (IBinding binding : bindings) {
								if (accessibilityContext.isAccessible(binding))
									fAvailableElements.add(binding);
							}
						}
					}
				}
			}
		} finally {
			newContext.dispose();
		}
	}

	/**
	 * Returns the position after last semicolon or opening or closing brace before the given offset.
	 */
	private static int getStatementStartOffset(IDocument doc, int offset) {
		if (offset != 0) {
			String docPart;
			try {
				docPart = doc.get(0, offset);
				int index = docPart.lastIndexOf(';');
				int tmpIndex = docPart.lastIndexOf('{');
				index = (index < tmpIndex) ? tmpIndex : index;
				tmpIndex = docPart.lastIndexOf('}');
				index = (index < tmpIndex) ? tmpIndex : index;
				return index + 1;
			} catch (BadLocationException e) {
				CUIPlugin.log(e);
			}
		}
		return offset;
	}

	/**
	 * Returns the currently active C/C++ editor, or <code>null</code> if it cannot be determined.
	 */
	private static CEditor getCEditor() {
		IEditorPart part = CUIPlugin.getActivePage().getActiveEditor();
		if (part instanceof CEditor) {
			return (CEditor) part;
		} else {
			return null;
		}
	}

	private boolean skipDefaultedParameter(IParameter param) {
		return !isDisplayDefaultedParameters() && param instanceof ICPPParameter && ((ICPPParameter)param).hasDefaultValue();
	}

	private void handleVariable(IVariable variable, CContentAssistInvocationContext context,
			int baseRelevance, List<ICompletionProposal> proposals) {
		if (context.isContextInformationStyle()) {
			IType t = variable.getType();
			t= unwindTypedefs(t);
			if (t instanceof ICPPClassType) {
				ICPPClassType classType= (ICPPClassType) t;
				IASTTranslationUnit ast = context.getCompletionNode().getTranslationUnit();
				ICPPConstructor[] constructors = ClassTypeHelper.getConstructors(classType, ast);
				for (ICPPConstructor constructor : constructors) {
					handleFunction(constructor, context, baseRelevance, proposals);
				}
			}
			return;
		}

		StringBuilder repStringBuff = new StringBuilder();
		repStringBuff.append(variable.getName());

		String returnTypeStr = "<unknown>"; //$NON-NLS-1$
		IType varType = variable.getType();
		if (varType != null)
			returnTypeStr = ASTTypeUtil.getType(varType, false);

        StringBuilder dispStringBuff = new StringBuilder(repStringBuff.toString());
        if (returnTypeStr != null) {
            dispStringBuff.append(" : "); //$NON-NLS-1$
            dispStringBuff.append(returnTypeStr);
        }
        String dispString = dispStringBuff.toString();

        StringBuilder idStringBuff = new StringBuilder(repStringBuff.toString());
        String idString = idStringBuff.toString();

        String repString = repStringBuff.toString();

		Image image = getImage(variable);
		final int relevance = isLocalVariable(variable) ?
					RelevanceConstants.LOCAL_VARIABLE_TYPE_RELEVANCE :
				isField(variable) ?
					RelevanceConstants.FIELD_TYPE_RELEVANCE :
					RelevanceConstants.VARIABLE_TYPE_RELEVANCE;
		CCompletionProposal proposal = createProposal(repString, dispString, idString,
				context.getCompletionNode().getLength(), image, baseRelevance + relevance, context);
		proposals.add(proposal);
	}

	private IType unwindTypedefs(final IType t) {
		IType r= t;
		while (r instanceof ITypedef) {
			r= ((ITypedef) r).getType();
		}
		return r != null ? r : t;
	}

	private static boolean isField(IVariable variable) {
		return variable instanceof IField;
	}

	private static boolean isLocalVariable(IVariable variable) {
		try {
			return isLocalScope(variable.getScope());
		} catch (DOMException exc) {
			return false;
		}
	}

    private static boolean isLocalScope(IScope scope) {
        while (scope != null) {
            if (scope instanceof ICPPFunctionScope ||
                    scope instanceof ICPPBlockScope ||
                    scope instanceof ICFunctionScope) {
                return true;
            }
            try {
                scope= scope.getParent();
            } catch (DOMException e) {
                scope= null;
            }
        }
        return false;
    }

	private void handleNamespace(ICPPNamespace namespace, IASTCompletionContext astContext,
			CContentAssistInvocationContext cContext, int baseRelevance,
			List<ICompletionProposal> proposals) {
		if (astContext instanceof ICPPASTQualifiedName) {
			IASTCompletionContext parent = ((ICPPASTQualifiedName) astContext)
					.getCompletionContext();
			handleNamespace(namespace, parent, cContext, baseRelevance, proposals);
			return;
		}

		StringBuilder repStringBuff = new StringBuilder();
		repStringBuff.append(namespace.getName());

		if (!(astContext instanceof ICPPASTUsingDeclaration)
				&& !(astContext instanceof ICPPASTUsingDirective)) {
			repStringBuff.append("::"); //$NON-NLS-1$
		}

		String repString = repStringBuff.toString();
		proposals.add(createProposal(repString, namespace.getName(), getImage(namespace),
				baseRelevance + RelevanceConstants.NAMESPACE_TYPE_RELEVANCE, cContext));
	}

	private CCompletionProposal createProposal(String repString, String dispString, Image image,
			int relevance, CContentAssistInvocationContext context) {
		return createProposal(repString, dispString, null, context.getCompletionNode().getLength(), image,
				relevance, context);
	}

	private CCompletionProposal createProposal(String repString, String dispString, int prefixLength,
			Image image, int relevance, CContentAssistInvocationContext context) {
		return createProposal(repString, dispString, null, prefixLength, image, relevance, context);
	}

	private CCompletionProposal createProposal(String repString, String dispString, String idString,
			int prefixLength, Image image, int relevance, CContentAssistInvocationContext context) {
		int parseOffset = context.getParseOffset();
		int invocationOffset = context.getInvocationOffset();
		boolean doReplacement = !context.isContextInformationStyle();

		int repLength = doReplacement ? prefixLength : 0;
		int repOffset = doReplacement ? parseOffset - repLength : invocationOffset;
		repString = doReplacement ? repString : ""; //$NON-NLS-1$

		return new CCompletionProposal(repString, repOffset, repLength, image, dispString, idString,
				relevance, context.getViewer());
	}

	private Image getImage(ImageDescriptor desc) {
		return desc != null ? CUIPlugin.getImageDescriptorRegistry().get(desc) : null;
	}

	private Image getImage(IBinding binding) {
		ImageDescriptor imageDescriptor = null;

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
			IBinding[] delegates = ((ICPPUsingDeclaration) binding).getDelegates();
			if (delegates.length > 0)
				return getImage(delegates[0]);
		}

		return imageDescriptor != null ?
				CUIPlugin.getImageDescriptorRegistry().get(imageDescriptor) : null;
	}

	private static boolean isDisplayDefaultArguments() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		return preferenceStore.getBoolean(ContentAssistPreference.DEFAULT_ARGUMENT_DISPLAY_ARGUMENTS);
	}

	private static boolean isDisplayDefaultedParameters() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		return preferenceStore.getBoolean(ContentAssistPreference.DEFAULT_ARGUMENT_DISPLAY_PARAMETERS_WITH_DEFAULT_ARGUMENT);
	}

	private static IPreferenceStore getPreferenceStore() {
		return CUIPlugin.getDefault().getPreferenceStore();
	}
}
