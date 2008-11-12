/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractfunction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.text.edits.TextEditGroup;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.dom.parser.c.CASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompoundStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarationStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTExpressionList;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTExpressionStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionCallExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTInitializerExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTQualifiedName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTReturnStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTemplateDeclaration;

import org.eclipse.cdt.internal.ui.refactoring.AddDeclarationNodeToClassChange;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.Container;
import org.eclipse.cdt.internal.ui.refactoring.MethodContext;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.NodeContainer;
import org.eclipse.cdt.internal.ui.refactoring.MethodContext.ContextType;
import org.eclipse.cdt.internal.ui.refactoring.NodeContainer.NameInformation;
import org.eclipse.cdt.internal.ui.refactoring.utils.ASTHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.NodeHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.SelectionHelper;

public class ExtractFunctionRefactoring extends CRefactoring {

	static final Integer NULL_INTEGER = Integer.valueOf(0);
	static final char[] ZERO= "0".toCharArray(); //$NON-NLS-1$


	NodeContainer container;
	final ExtractFunctionInformation info;

	final Map<String, Integer> names;
	final Container<Integer> namesCounter;
	final Container<Integer> trailPos;
	private final Container<Integer> returnNumber;

	protected boolean hasNameResolvingForSimilarError = false;

	HashMap<String, Integer> nameTrail;

	private ExtractedFunctionConstructionHelper extractedFunctionConstructionHelper;

	public ExtractFunctionRefactoring(IFile file, ISelection selection,
			ExtractFunctionInformation info) {
		super(file, selection, null);
		this.info = info;
		name = Messages.ExtractFunctionRefactoring_ExtractFunction;
		names = new HashMap<String, Integer>();
		namesCounter = new Container<Integer>(NULL_INTEGER);
		trailPos = new Container<Integer>(NULL_INTEGER);
		returnNumber = new Container<Integer>(NULL_INTEGER);
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		SubMonitor sm = SubMonitor.convert(pm, 10);
		RefactoringStatus status = super.checkInitialConditions(sm.newChild(6));

		container = findExtractableNodes();
		sm.worked(1);

		if (isProgressMonitorCanceld(sm, initStatus))
			return initStatus;

		checkForNonExtractableStatements(container, status);
		sm.worked(1);

		if (isProgressMonitorCanceld(sm, initStatus))
			return initStatus;

		container.findAllNames();
		sm.worked(1);

		if (isProgressMonitorCanceld(sm, initStatus))
			return initStatus;

		container.getAllAfterUsedNames();
		info.setAllUsedNames(container.getUsedNamesUnique());

		if (container.size() < 1) {
			status
					.addFatalError(Messages.ExtractFunctionRefactoring_NoStmtSelected);
			sm.done();
			return status;
		}

		if (container.getAllDeclaredInScope().size() > 1) {
			status
					.addFatalError(Messages.ExtractFunctionRefactoring_TooManySelected);
		} else if (container.getAllDeclaredInScope().size() == 1) {
			info.setInScopeDeclaredVariable(container.getAllDeclaredInScope().get(0));
		}

		extractedFunctionConstructionHelper = ExtractedFunctionConstructionHelper
				.createFor(container.getNodesToWrite());

		boolean isExtractExpression = container.getNodesToWrite().get(0) instanceof IASTExpression;
		info.setExtractExpression(isExtractExpression);

		info.setDeclarator(getDeclaration(container.getNodesToWrite().get(0)));
		MethodContext context = NodeHelper.findMethodContext(container.getNodesToWrite().get(0), getIndex());
		info.setMethodContext(context);
		sm.done();
		return status;
	}

	private void checkForNonExtractableStatements(NodeContainer cont,
			RefactoringStatus status) {

		NonExtractableStmtFinder vis = new NonExtractableStmtFinder();
		for (IASTNode node : cont.getNodesToWrite()) {
			node.accept(vis);
			if (vis.containsContinue()) {
				initStatus
						.addFatalError(Messages.ExtractFunctionRefactoring_Error_Continue);
				break;
			} else if (vis.containsBreak()) {
				initStatus
						.addFatalError(Messages.ExtractFunctionRefactoring_Error_Break);
				break;
			}
		}

		ReturnStatementFinder rFinder = new ReturnStatementFinder();
		for (IASTNode node : cont.getNodesToWrite()) {
			node.accept(rFinder);
			if (rFinder.containsReturn()) {
				initStatus
						.addFatalError(Messages.ExtractFunctionRefactoring_Error_Return);
				break;
			}
		}

	}

	private ICPPASTFunctionDeclarator getDeclaration(IASTNode node) {

		while (node != null && !(node instanceof IASTFunctionDefinition)) {
			node = node.getParent();
		}
		if (node != null) {
			IASTFunctionDeclarator declarator = ((IASTFunctionDefinition) node)
					.getDeclarator();
			if (declarator instanceof ICPPASTFunctionDeclarator) {
				return (ICPPASTFunctionDeclarator) declarator;
			}
		}
		return null;
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		RefactoringStatus status = super.checkFinalConditions(pm);

		final IASTName astMethodName = new CPPASTName(info.getMethodName()
				.toCharArray());
		MethodContext context = NodeHelper.findMethodContext(container.getNodesToWrite().get(0), getIndex());

		if (context.getType() == ContextType.METHOD) {
			ICPPASTCompositeTypeSpecifier classDeclaration = (ICPPASTCompositeTypeSpecifier) context
					.getMethodDeclaration().getParent();
			IASTSimpleDeclaration methodDeclaration = getDeclaration(astMethodName);

			if (isMethodAllreadyDefined(methodDeclaration, classDeclaration)) {
				status.addError(Messages.ExtractFunctionRefactoring_NameInUse);
				return status;
			}
		}
		for (NameInformation name : info.getAllUsedNames()) {
			if (name.isUserSetIsReturnValue()) {
				info.setReturnVariable(name);
			}

		}

		return status;
	}

	@Override
	protected void collectModifications(IProgressMonitor pm,
			ModificationCollector collector) throws CoreException,
			OperationCanceledException {
		final IASTName astMethodName = new CPPASTName(info.getMethodName()
				.toCharArray());

		MethodContext context = NodeHelper.findMethodContext(container.getNodesToWrite().get(0), getIndex());

		// Create Declaration in Class
		if (context.getType() == ContextType.METHOD) {
			createMethodDeclaration(astMethodName, context, collector);
		}
		// Create Method Definition
		IASTNode firstNode = container.getNodesToWrite().get(0);
		IPath implPath = new Path(firstNode.getContainingFilename());
		final IFile implementationFile = ResourcesPlugin.getWorkspace()
				.getRoot().getFileForLocation(implPath);

		createMethodDefinition(astMethodName, context, firstNode,
				implementationFile, collector);

		createMethodCalls(astMethodName, implementationFile, context, collector);

	}

	private void createMethodCalls(final IASTName astMethodName,
			final IFile implementationFile, MethodContext context,
			ModificationCollector collector) throws CoreException {

		String title;
		if (context.getType() == MethodContext.ContextType.METHOD) {
			title = Messages.ExtractFunctionRefactoring_CreateMethodCall;
		} else {
			title = Messages.ExtractFunctionRefactoring_CreateFunctionCall;
		}

		IASTNode methodCall = getMethodCall(astMethodName);

		IASTNode firstNodeToWrite = container.getNodesToWrite().get(0);
		ASTRewrite rewriter = collector
				.rewriterForTranslationUnit(firstNodeToWrite
						.getTranslationUnit());
		TextEditGroup editGroup = new TextEditGroup(title);
		if(methodCall instanceof IASTDeclaration){
			CPPASTDeclarationStatement declarationStatement = new CPPASTDeclarationStatement((IASTDeclaration) methodCall);
			methodCall = declarationStatement;
		}
		insertCallintoTree(methodCall, container.getNodesToWrite(), rewriter, editGroup);
		for (IASTNode node : container.getNodesToWrite()) {
			if (node != firstNodeToWrite) {
				rewriter.remove(node, editGroup);
			}
		}		
	}

	private void insertCallintoTree(IASTNode methodCall, List<IASTNode> list,
			ASTRewrite rewriter, TextEditGroup editGroup) {
		IASTNode firstNode = list.get(0);
		if(list.size() > 1 && firstNode.getParent() instanceof IASTBinaryExpression &&
				firstNode.getParent().getParent() instanceof IASTBinaryExpression) {
			IASTBinaryExpression parent = (IASTBinaryExpression) firstNode.getParent();
			IASTExpression leftSubTree = parent.getOperand1();
			int op = parent.getOperator();
			IASTBinaryExpression newParentNode = new CPPASTBinaryExpression();
			CPPASTLiteralExpression placeholder = new CPPASTLiteralExpression(IASTLiteralExpression.lk_integer_constant, ZERO);
			IASTBinaryExpression rootBinExp = getRootBinExp(parent, list);
			newParentNode.setParent(rootBinExp.getParent());
			newParentNode.setOperand1(placeholder);
			newParentNode.setOperator(op);
			newParentNode.setOperand2((IASTExpression) methodCall); // TODO check
			ASTRewrite callRewrite = rewriter.replace(rootBinExp, newParentNode, editGroup);
			callRewrite.replace(placeholder, leftSubTree, editGroup);
		}else {
			rewriter.replace(firstNode, methodCall, editGroup);
		}
	}

	private IASTBinaryExpression getRootBinExp(IASTBinaryExpression binExp, List<IASTNode> nodeList) {
		while(binExp.getParent() instanceof IASTBinaryExpression && nodeList.contains(((IASTBinaryExpression) binExp.getParent()).getOperand2())) {
			binExp = (IASTBinaryExpression) binExp.getParent();
		}
		return binExp;
	}

	private void createMethodDefinition(final IASTName astMethodName,
			MethodContext context, IASTNode firstNode,
			final IFile implementationFile, ModificationCollector collector) {

		IASTFunctionDefinition node = NodeHelper.findFunctionDefinitionInAncestors(firstNode); 
		if (node != null) { 
			String title;
			if (context.getType() == MethodContext.ContextType.METHOD) {
				title = Messages.ExtractFunctionRefactoring_CreateMethodDef;
			} else {
				title = Messages.ExtractFunctionRefactoring_CreateFunctionDef;
			}

			ASTRewrite rewriter = collector.rewriterForTranslationUnit(node.getTranslationUnit());
			addMethod(astMethodName, context, rewriter, node, new TextEditGroup(title));
		}
	}

	private void createMethodDeclaration(final IASTName astMethodName,
			MethodContext context, ModificationCollector collector) {
		ICPPASTCompositeTypeSpecifier classDeclaration = (ICPPASTCompositeTypeSpecifier) context
				.getMethodDeclaration().getParent();

		IASTSimpleDeclaration methodDeclaration = getDeclaration(astMethodName);

		AddDeclarationNodeToClassChange.createChange(classDeclaration, info
				.getVisibility(), methodDeclaration, false, collector);

	}

	private boolean isMethodAllreadyDefined(
			IASTSimpleDeclaration methodDeclaration,
			ICPPASTCompositeTypeSpecifier classDeclaration) {
		TrailNodeEqualityChecker equalityChecker = new TrailNodeEqualityChecker(
				names, namesCounter);

		IBinding bind = classDeclaration.getName().resolveBinding();
		IASTStandardFunctionDeclarator declarator = (IASTStandardFunctionDeclarator) methodDeclaration
				.getDeclarators()[0];
		String name = new String(declarator.getName().toCharArray());
		if (bind instanceof ICPPClassType) {
			ICPPClassType classBind = (ICPPClassType) bind;
			try {
				IField[] fields = classBind.getFields();
				for (IField field : fields) {
					if (field.getName().equals(name)) {
						return true;
					}
				}
				ICPPMethod[] methods = classBind.getAllDeclaredMethods();
				for (ICPPMethod method : methods) {
					if (!method.takesVarArgs() && name.equals(method.getName())) {
						IParameter[] parameters = method.getParameters();
						if (parameters.length == declarator.getParameters().length) {
							for (int i = 0; i < parameters.length; i++) {
								IASTName[] origParameterName = unit
										.getDeclarationsInAST(parameters[i]);

								IASTParameterDeclaration origParameter = (IASTParameterDeclaration) origParameterName[0]
										.getParent().getParent();
								IASTParameterDeclaration newParameter = declarator
										.getParameters()[i];

								// if not the same break;
								if (!(equalityChecker.isEquals(origParameter
										.getDeclSpecifier(), newParameter
										.getDeclSpecifier()) && ASTHelper
										.samePointers(origParameter
												.getDeclarator()
												.getPointerOperators(),
												newParameter.getDeclarator()
														.getPointerOperators(),
												equalityChecker))) {
									break;
								}

								if (!(i < (parameters.length - 1))) {
									return true;
								}
							}
						}

					}
				}
				return false;
			} catch (DOMException e) {
				ILog logger = CUIPlugin.getDefault().getLog();
				IStatus status = new Status(IStatus.WARNING,
						CUIPlugin.PLUGIN_ID, IStatus.OK, e.getMessage(), e);

				logger.log(status);
			}
		}
		return true;
	}

	private void addMethod(IASTName astMethodName, MethodContext context,
			ASTRewrite rewriter, IASTNode insertpoint, TextEditGroup group) {
		
		ICPPASTQualifiedName qname = new CPPASTQualifiedName();
		if (context.getType() == ContextType.METHOD) {
			for (int i = 0; i < (context.getMethodQName().getNames().length - 1); i++) {
				qname.addName(new CPPASTName(context.getMethodQName().getNames()[i].toCharArray()));
			}
		}
		qname.addName(astMethodName);

		IASTFunctionDefinition func = new CPPASTFunctionDefinition();
		func.setParent(unit);

		ICPPASTSimpleDeclSpecifier dummyDeclSpecifier = new CPPASTSimpleDeclSpecifier();
		func.setDeclSpecifier(dummyDeclSpecifier);
		dummyDeclSpecifier.setType(IASTSimpleDeclSpecifier.t_void);
		
		IASTStandardFunctionDeclarator createdFunctionDeclarator = extractedFunctionConstructionHelper
				.createFunctionDeclarator(qname, info.getDeclarator(), info
						.getReturnVariable(), container.getNodesToWrite(), info
						.getAllUsedNames());
		func.setDeclarator(createdFunctionDeclarator);

		IASTCompoundStatement compound = new CPPASTCompoundStatement();
		func.setBody(compound);
		
		ASTRewrite subRW;
		if(insertpoint.getParent() instanceof ICPPASTTemplateDeclaration) {
			
			CPPASTTemplateDeclaration templateDeclaration = new CPPASTTemplateDeclaration();
			templateDeclaration.setParent(unit);
			
			for(ICPPASTTemplateParameter templateParameter : ((ICPPASTTemplateDeclaration) insertpoint.getParent()).getTemplateParameters()) {
				templateDeclaration.addTemplateParamter(templateParameter);
			}
			
			templateDeclaration.setDeclaration(func);

			subRW = rewriter.insertBefore(insertpoint.getParent().getParent(), insertpoint.getParent(), templateDeclaration, group);

		} else {
			
			subRW = rewriter.insertBefore(insertpoint.getParent(), insertpoint, func, group);
		}
		
		subRW.replace(dummyDeclSpecifier, getReturnType(), group);
		
		extractedFunctionConstructionHelper.constructMethodBody(compound,
				container.getNodesToWrite(), subRW, group);

		// Set return value
		if (info.getReturnVariable() != null) {
			IASTReturnStatement returnStmt = new CPPASTReturnStatement();
			if (info.getReturnVariable().getDeclaration().getParent() instanceof IASTExpression) {
				IASTExpression returnValue = (IASTExpression) info
						.getReturnVariable().getDeclaration().getParent();
				returnStmt.setReturnValue(returnValue);
			} else {
				IASTIdExpression expr = new CPPASTIdExpression();
				if (info.getReturnVariable().getUserSetName() == null) {
					expr.setName(newName(info.getReturnVariable().getName()));
				} else {
					expr.setName(new CPPASTName(info.getReturnVariable()
							.getUserSetName().toCharArray()));
				}
				returnStmt.setReturnValue(expr);
			}
			subRW.insertBefore(compound, null, returnStmt, group);
		}

	}

	private IASTName newName(IASTName declaration) {
		return new CPPASTName(declaration.toCharArray());
	}

	private IASTDeclSpecifier getReturnType() {

		IASTNode firstNodeToWrite = container.getNodesToWrite().get(0);
		NameInformation returnVariable = info.getReturnVariable();

		return extractedFunctionConstructionHelper.determineReturnType(
				firstNodeToWrite, returnVariable);
	}

	protected IASTNode getMethodCall(IASTName astMethodName,
			Map<String, Integer> trailNameTable,
			Map<String, Integer> similarNameTable, NodeContainer myContainer,
			NodeContainer mySimilarContainer) {
		IASTExpressionStatement stmt = new CPPASTExpressionStatement();
		IASTFunctionCallExpression callExpression = new CPPASTFunctionCallExpression();
		IASTIdExpression idExpression = new CPPASTIdExpression();
		idExpression.setName(astMethodName);
		IASTExpressionList paramList = new CPPASTExpressionList();

		Vector<IASTName> declarations = new Vector<IASTName>();
		IASTName retName = null;
		boolean theRetName = false;

		for (NameInformation nameInfo : myContainer.getNames()) {
			Integer trailSeqNumber = trailNameTable.get(nameInfo
					.getDeclaration().getRawSignature());
			String orgName = null;
			for (Entry<String, Integer> entry : similarNameTable.entrySet()) {
				if (entry.getValue().equals(trailSeqNumber)) {
					orgName = entry.getKey();
					if (info.getReturnVariable() != null
							&& trailSeqNumber.equals(returnNumber.getObject())) {
						theRetName = true;
					}
				}
			}

			if (orgName != null) {
				boolean found = false;
				for (NameInformation simNameInfo : mySimilarContainer
						.getNames()) {
					if (orgName.equals(simNameInfo.getDeclaration()
							.getRawSignature())) {
						addAParameterIfPossible(paramList, declarations,
								simNameInfo);
						found = true;

						if (theRetName) {
							theRetName = false;
							retName = new CPPASTName(simNameInfo
									.getDeclaration().getRawSignature()
									.toCharArray());
						}
					}
				}

				if (!found) {
					// should be a field, use the old name
					IASTIdExpression expression = new CPPASTIdExpression();
					CPPASTName fieldName = new CPPASTName(orgName.toCharArray());
					expression.setName(fieldName);
					paramList.addExpression(expression);

					if (theRetName) {
						theRetName = false;
						retName = fieldName;
					}
				}
			}
		}

		callExpression.setParameterExpression(paramList);
		callExpression.setFunctionNameExpression(idExpression);

		if (info.getReturnVariable() == null) {
			return getReturnAssignment(stmt, callExpression);
		}
		return getReturnAssignment(stmt, callExpression, retName);
	}

	private IASTNode getMethodCall(IASTName astMethodName) {
		IASTExpressionStatement stmt = new CPPASTExpressionStatement();

		IASTFunctionCallExpression callExpression = new CPPASTFunctionCallExpression();
		IASTIdExpression idExpression = new CPPASTIdExpression();
		idExpression.setName(new CPPASTName(astMethodName.toCharArray()));
		IASTExpressionList paramList = getCallParameters();
		callExpression.setParameterExpression(paramList);
		callExpression.setFunctionNameExpression(idExpression);

		if (info.getReturnVariable() == null) {
			return getReturnAssignment(stmt, callExpression);
		}
		IASTName retname = newName(info.getReturnVariable().getName());
		return getReturnAssignment(stmt, callExpression, retname);

	}

	private IASTNode getReturnAssignment(IASTExpressionStatement stmt,
			IASTFunctionCallExpression callExpression, IASTName retname) {
		if (info.getReturnVariable().equals(info.getInScopeDeclaredVariable())) {
			IASTSimpleDeclaration orgDecl = NodeHelper.findSimpleDeclarationInParents(info
					.getReturnVariable().getDeclaration());
			IASTSimpleDeclaration decl = new CPPASTSimpleDeclaration();

			decl.setDeclSpecifier(orgDecl.getDeclSpecifier());

			IASTDeclarator declarator = new CPPASTDeclarator();

			declarator.setName(retname);

			for (IASTPointerOperator pointer : orgDecl.getDeclarators()[0]
					.getPointerOperators()) {
				declarator.addPointerOperator(pointer);
			}

			IASTInitializerExpression initializer = new CPPASTInitializerExpression();
			initializer.setExpression(callExpression);
			declarator.setInitializer(initializer);

			decl.addDeclarator(declarator);

			return decl;
		}
		IASTBinaryExpression binaryExpression = new CASTBinaryExpression();
		binaryExpression.setOperator(IASTBinaryExpression.op_assign);
		IASTIdExpression nameExpression = new CPPASTIdExpression();

		nameExpression.setName(retname);
		binaryExpression.setOperand1(nameExpression);
		binaryExpression.setOperand2(callExpression);

		return getReturnAssignment(stmt, binaryExpression);
	}

	private IASTNode getReturnAssignment(IASTExpressionStatement stmt,
			IASTExpression callExpression) {

		IASTNode node = container.getNodesToWrite().get(0);
		return extractedFunctionConstructionHelper.createReturnAssignment(node,
				stmt, callExpression);

	}

	private IASTSimpleDeclaration getDeclaration(IASTName name) {
		IASTSimpleDeclaration simpleDecl = new CPPASTSimpleDeclaration();
		simpleDecl.setParent(unit);
		IASTDeclSpecifier declSpec = getReturnType();
		simpleDecl.setDeclSpecifier(declSpec);
		IASTStandardFunctionDeclarator declarator = extractedFunctionConstructionHelper
				.createFunctionDeclarator(name, info.getDeclarator(), info
						.getReturnVariable(), container.getNodesToWrite(), info
						.getAllUsedNames());
		simpleDecl.addDeclarator(declarator);
		return simpleDecl;
	}

	private NodeContainer findExtractableNodes() {
		final NodeContainer container = new NodeContainer();
		unit.accept(new CPPASTVisitor() {
			{
				shouldVisitStatements = true;
				shouldVisitExpressions = true;
			}

			@Override
			public int visit(IASTStatement stmt) {
				if (!(stmt instanceof IASTCompoundStatement)
						&& SelectionHelper.isSelectedFile(region, stmt, file)) {
					container.add(stmt);
					return PROCESS_SKIP;
				}
				return super.visit(stmt);
			}

			@Override
			public int visit(IASTExpression expression) {
				if (SelectionHelper.isSelectedFile(region, expression, file)) {
					container.add(expression);
					return PROCESS_SKIP;
				}
				return super.visit(expression);
			}
		});
		return container;
	}

	public IASTExpressionList getCallParameters() {
		IASTExpressionList paramList = new CPPASTExpressionList();
		Vector<IASTName> declarations = new Vector<IASTName>();
		for (NameInformation nameInf : container.getNames()) {
			addAParameterIfPossible(paramList, declarations, nameInf);
		}
		return paramList;
	}

	private void addAParameterIfPossible(IASTExpressionList paramList,
			Vector<IASTName> declarations, NameInformation nameInf) {
		if (!nameInf.isDeclarationInScope()) {
			IASTName declaration = nameInf.getDeclaration();
			if (!declarations.contains(declaration)) {
				declarations.add(declaration);
				IASTIdExpression expression = new CPPASTIdExpression();
				expression.setName(newName(declaration));
				paramList.addExpression(expression);
			}
		}
	}

}
