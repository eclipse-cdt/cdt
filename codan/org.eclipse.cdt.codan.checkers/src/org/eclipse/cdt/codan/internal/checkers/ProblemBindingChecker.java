/*******************************************************************************
 * Copyright (c) 2010 Marc-Andre Laperle and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Marc-Andre Laperle - Initial API and implementation
 * Tomasz Wesolowski  - Extension for fixes
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers;

import org.eclipse.cdt.codan.core.cxx.CxxAstUtils;
import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.index.IIndex;

public class ProblemBindingChecker extends AbstractIndexAstChecker {

	public static String ERR_ID_OverloadProblem = "org.eclipse.cdt.codan.internal.checkers.OverloadProblem"; //$NON-NLS-1$
	public static String ERR_ID_AmbiguousProblem = "org.eclipse.cdt.codan.internal.checkers.AmbiguousProblem"; //$NON-NLS-1$
	public static String ERR_ID_CircularReferenceProblem = "org.eclipse.cdt.codan.internal.checkers.CircularReferenceProblem"; //$NON-NLS-1$
	public static String ERR_ID_RedeclarationProblem = "org.eclipse.cdt.codan.internal.checkers.RedeclarationProblem"; //$NON-NLS-1$
	public static String ERR_ID_RedefinitionProblem = "org.eclipse.cdt.codan.internal.checkers.RedefinitionProblem"; //$NON-NLS-1$
	public static String ERR_ID_MemberDeclarationNotFoundProblem = "org.eclipse.cdt.codan.internal.checkers.MemberDeclarationNotFoundProblem"; //$NON-NLS-1$
	public static String ERR_ID_LabelStatementNotFoundProblem = "org.eclipse.cdt.codan.internal.checkers.LabelStatementNotFoundProblem"; //$NON-NLS-1$
	public static String ERR_ID_InvalidTemplateArgumentsProblem = "org.eclipse.cdt.codan.internal.checkers.InvalidTemplateArgumentsProblem"; //$NON-NLS-1$
	public static String ERR_ID_TypeResolutionProblem = "org.eclipse.cdt.codan.internal.checkers.TypeResolutionProblem"; //$NON-NLS-1$
	public static String ERR_ID_FunctionResolutionProblem = "org.eclipse.cdt.codan.internal.checkers.FunctionResolutionProblem"; //$NON-NLS-1$
	public static String ERR_ID_InvalidArguments = "org.eclipse.cdt.codan.internal.checkers.InvalidArguments"; //$NON-NLS-1$
	public static String ERR_ID_MethodResolutionProblem = "org.eclipse.cdt.codan.internal.checkers.MethodResolutionProblem"; //$NON-NLS-1$
	public static String ERR_ID_FieldResolutionProblem = "org.eclipse.cdt.codan.internal.checkers.FieldResolutionProblem"; //$NON-NLS-1$
	public static String ERR_ID_VariableResolutionProblem = "org.eclipse.cdt.codan.internal.checkers.VariableResolutionProblem"; //$NON-NLS-1$
	public static String ERR_ID_Candidates = "org.eclipse.cdt.codan.internal.checkers.Candidates"; //$NON-NLS-1$
	
	@Override
	public boolean runInEditor() {
		return true;
	}

	public void processAst(IASTTranslationUnit ast) {
		
		try {
			ast.accept(new ASTVisitor() {
				{
					shouldVisitNames = true;
				}

				@Override
				public int visit(IASTName name) {
					try {
						IBinding binding = name.resolveBinding();
						if (binding instanceof IProblemBinding) {
							IASTNode parentNode = name.getParent();
							
							// Don't report multiple problems with qualified names
							if(parentNode instanceof ICPPASTQualifiedName) {
								if(((ICPPASTQualifiedName)parentNode).resolveBinding() instanceof IProblemBinding)
									return PROCESS_CONTINUE;	
							}
							
							String contextFlagsString = createContextFlagsString(name);
							
							IProblemBinding problemBinding = (IProblemBinding) binding;
							int id = problemBinding.getID();
							
							if(id == IProblemBinding.SEMANTIC_INVALID_OVERLOAD) {
								reportProblem(ERR_ID_OverloadProblem, name, name.getRawSignature(), contextFlagsString);
								return PROCESS_CONTINUE;
							}
							
							if(id == IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP) {
								String candidatesString = getCandidatesString(problemBinding);
								reportProblem(ERR_ID_AmbiguousProblem, name, name.getRawSignature(), candidatesString, contextFlagsString);
								return PROCESS_CONTINUE;
							}
							
							if(id == IProblemBinding.SEMANTIC_CIRCULAR_INHERITANCE) {
								String typeString;
								IASTNode problemNode;
								if(parentNode instanceof IASTFieldReference) {
									IASTExpression ownerExpression = ((IASTFieldReference)parentNode).getFieldOwner();
									typeString = ASTTypeUtil.getType(ownerExpression.getExpressionType());
									problemNode = ownerExpression;
								} else {
									problemNode = name;
									typeString = name.getRawSignature();
								}
								reportProblem(ERR_ID_CircularReferenceProblem, problemNode, typeString, contextFlagsString);	
								return PROCESS_CONTINUE;
							}
							
							if(id == IProblemBinding.SEMANTIC_INVALID_REDECLARATION) {
								reportProblem(ERR_ID_RedeclarationProblem, name, name.getRawSignature(), contextFlagsString);
								return PROCESS_CONTINUE;
							}
							
							if(id == IProblemBinding.SEMANTIC_INVALID_REDEFINITION) {
								reportProblem(ERR_ID_RedefinitionProblem, name, name.getRawSignature(), contextFlagsString);
								return PROCESS_CONTINUE;
							}
							
							if(id == IProblemBinding.SEMANTIC_MEMBER_DECLARATION_NOT_FOUND) {
								reportProblem(ERR_ID_MemberDeclarationNotFoundProblem, name, contextFlagsString);
								return PROCESS_CONTINUE;
							}
							
							if(id == IProblemBinding.SEMANTIC_LABEL_STATEMENT_NOT_FOUND) {
								reportProblem(ERR_ID_LabelStatementNotFoundProblem, name, name.getRawSignature(), contextFlagsString);
								return PROCESS_CONTINUE;
							}
							
							if(id == IProblemBinding.SEMANTIC_INVALID_TEMPLATE_ARGUMENTS) {
								// We use the templateName since we don't want the whole
								// argument list to be underlined. That way we can see which argument is invalid.
								IASTNode templateName = getTemplateName(name);
								reportProblem(ERR_ID_InvalidTemplateArgumentsProblem, templateName, contextFlagsString);
								return PROCESS_CONTINUE;
							}
							
							// From this point, we'll deal only with NAME_NOT_FOUND problems. 
							// If it's something else continue because we don't want to give bad messages
							if(id != IProblemBinding.SEMANTIC_NAME_NOT_FOUND) {
								return PROCESS_CONTINUE;
							}
							
							if(isFunctionCall(parentNode)) {
								handleFunctionProblem(name, problemBinding, contextFlagsString);
							} else if (parentNode instanceof IASTFieldReference) {
								handleMemberProblem(name, parentNode, problemBinding, contextFlagsString);
							} else if (parentNode instanceof IASTNamedTypeSpecifier) {
								reportProblem(ERR_ID_TypeResolutionProblem, name, name.getRawSignature(), contextFlagsString);
							} 
							// Probably a variable
							else {
								handleVariableProblem(name, contextFlagsString);
							}
						}
					} catch (DOMException e) {
						e.printStackTrace();
					}
					
					return PROCESS_CONTINUE;
				}

			});
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	protected String createContextFlagsString(IASTName name) {
		StringBuilder buf = new StringBuilder();
		if (isInClassContext(name)) {
			buf.append(":class"); //$NON-NLS-1$
		}
		if (isInFunctionContext(name)) {
			buf.append(":func"); //$NON-NLS-1$
		}
		return buf.toString();
	}
	
	private boolean isInClassContext(IASTName name) {
		CxxAstUtils utils = CxxAstUtils.getInstance();
		if (utils.getEnclosingCompositeTypeSpecifier(name) != null) {
			return true;
		}
		IASTFunctionDefinition function = utils.getEnclosingFunction(name);
		if (name == null || function == null) {
			return false;
		}
		IIndex index = name.getTranslationUnit().getIndex();
		IASTCompositeTypeSpecifier composite = utils.getCompositeTypeFromFunction(function, index);
		
		return composite != null;
	}
	
	private boolean isInFunctionContext(IASTName name) {
		CxxAstUtils utils = CxxAstUtils.getInstance();
		IASTFunctionDefinition function = utils.getEnclosingFunction(name);
		return (function != null);
	}
	

	private void handleFunctionProblem(IASTName name, IProblemBinding problemBinding, String contextFlagsString)
			throws DOMException {
		if(problemBinding.getCandidateBindings().length == 0) {
			reportProblem(ERR_ID_FunctionResolutionProblem, name.getLastName(), name.getRawSignature(), contextFlagsString);
		} else {
			String candidatesString = getCandidatesString(problemBinding);
			reportProblem(ERR_ID_InvalidArguments, name.getLastName(), candidatesString, contextFlagsString);
		}
	}

	private void handleMemberProblem(IASTName name, IASTNode parentNode,
			IProblemBinding problemBinding, String contextFlagsString) throws DOMException {
		IASTNode parentParentNode = parentNode.getParent();
		if(parentParentNode instanceof IASTFunctionCallExpression) {
			if(problemBinding.getCandidateBindings().length == 0) {
				reportProblem(ERR_ID_MethodResolutionProblem, name.getLastName(), name.getRawSignature(), contextFlagsString);
			} else {
				String candidatesString = getCandidatesString(problemBinding);
				reportProblem(ERR_ID_InvalidArguments, name.getLastName(), candidatesString, contextFlagsString);
			}
		} else {
			reportProblem(ERR_ID_FieldResolutionProblem, name.getLastName(), name.getRawSignature(), contextFlagsString);
		}
	}

	private void handleVariableProblem(IASTName name, String contextFlagsString) {
		reportProblem(ERR_ID_VariableResolutionProblem, name, name.getRawSignature(), contextFlagsString);
	}
	
	private boolean isFunctionCall(IASTNode parentNode) {
		if(parentNode instanceof IASTIdExpression) {
			IASTIdExpression expression = (IASTIdExpression)parentNode;
			IASTNode parentParentNode = expression.getParent();
			if(parentParentNode instanceof IASTFunctionCallExpression && 
					expression.getPropertyInParent().getName().equals(IASTFunctionCallExpression.FUNCTION_NAME.getName())) {
				return true;
			}
		}
		return false;
	}

	protected IASTNode getTemplateName(IASTName name) {
		IASTName nameToGetTempate = name.getLastName();
		if(nameToGetTempate instanceof ICPPASTTemplateId) {
			return ((ICPPASTTemplateId)nameToGetTempate).getTemplateName();
		}
		
		return nameToGetTempate;
	}

	/**
	 * Returns a string of the candidates for the binding
	 * 
	 * @param problemBinding
	 * @return A string of the candidates, one per line
	 * @throws DOMException
	 */
	private String getCandidatesString(IProblemBinding problemBinding) throws DOMException {
		String candidatesString = "\n" + CheckersMessages.ProblemBindingChecker_Candidates + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		String lastSignature = ""; //$NON-NLS-1$
		for(IBinding candidateBinding : problemBinding.getCandidateBindings()) {
			if(candidateBinding instanceof ICPPFunction) {
				ICPPFunction functionBinding = (ICPPFunction)candidateBinding;
				String signature = getFunctionSignature(functionBinding);
				if(!signature.equals(lastSignature)) {
					candidatesString += signature + "\n"; //$NON-NLS-1$
					lastSignature = signature;
				}
			} else if(candidateBinding instanceof ICPPClassType) {
				ICPPClassType classType = (ICPPClassType)candidateBinding;
				for(ICPPFunction constructor : classType.getConstructors()) {
					String signature = getFunctionSignature(constructor);
					if(!signature.equals(lastSignature)) {
						candidatesString += signature + "\n"; //$NON-NLS-1$
						lastSignature = signature;
					}
				}
			}
			
		} 
		return candidatesString;
	}
	
	/**
	 * Returns a string of the function signature : returntype + function + parameters
	 * 
	 * @param functionBinding The function to get the signature
	 * @return A string of the function signature
	 * @throws DOMException
	 */
	private String getFunctionSignature(ICPPFunction functionBinding) throws DOMException {
		IFunctionType functionType = functionBinding.getType();
		String returnTypeString = ASTTypeUtil.getType(functionBinding.getType().getReturnType()) + " "; //$NON-NLS-1$
		String functionName = functionBinding.getName();
		String parameterTypeString = ASTTypeUtil.getParameterTypeString(functionType);
		return returnTypeString + functionName + parameterTypeString;
	}

}
