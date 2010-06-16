/*******************************************************************************
 * Copyright (c) 2008, 2010 Institute for Software, HSR Hochschule fuer Technik  
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

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
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.text.edits.TextEditGroup;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConversionName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTOperatorName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.dom.parser.c.CASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompoundStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarationStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTEqualsInitializer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTExpressionStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionCallExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTQualifiedName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTReturnStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTemplateDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVariableReadWriteFlags;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;

import org.eclipse.cdt.internal.ui.refactoring.AddDeclarationNodeToClassChange;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringDescription;
import org.eclipse.cdt.internal.ui.refactoring.Container;
import org.eclipse.cdt.internal.ui.refactoring.MethodContext;
import org.eclipse.cdt.internal.ui.refactoring.MethodContext.ContextType;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.NodeContainer;
import org.eclipse.cdt.internal.ui.refactoring.NodeContainer.NameInformation;
import org.eclipse.cdt.internal.ui.refactoring.utils.ASTHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.CPPASTAllVisitor;
import org.eclipse.cdt.internal.ui.refactoring.utils.NodeHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.SelectionHelper;

public class ExtractFunctionRefactoring extends CRefactoring {
	
	public static final String ID = "org.eclipse.cdt.internal.ui.refactoring.extractfunction.ExtractFunctionRefactoring"; //$NON-NLS-1$

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
	private final INodeFactory factory = CPPNodeFactory.getDefault();

	public ExtractFunctionRefactoring(IFile file, ISelection selection,
			ExtractFunctionInformation info, ICProject project) {
		super(file, selection, null, project);
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
		try {
			lockIndex();

			try {
				RefactoringStatus status = super.checkInitialConditions(sm.newChild(6));
				if(status.hasError()) {
					return status;
				}

				container = findExtractableNodes();
				sm.worked(1);

				if (isProgressMonitorCanceld(sm, initStatus))
					return initStatus;

				checkForNonExtractableStatements(container, initStatus);
				sm.worked(1);

				if (isProgressMonitorCanceld(sm, initStatus))
					return initStatus;

				container.findAllNames();
				markWriteAccess();
				sm.worked(1);

				if (isProgressMonitorCanceld(sm, initStatus))
					return initStatus;

				container.getAllAfterUsedNames();
				info.setAllUsedNames(container.getUsedNamesUnique());

				if (container.size() < 1) {
					initStatus.addFatalError(Messages.ExtractFunctionRefactoring_NoStmtSelected);
					sm.done();
					return initStatus;
				}

				if (container.getAllDeclaredInScope().size() > 1) {
					initStatus.addFatalError(Messages.ExtractFunctionRefactoring_TooManySelected);
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

				if (info.getInScopeDeclaredVariable() != null) {
					info.getInScopeDeclaredVariable().setUserSetIsReturnValue(true);
				}
				for (int i = 0; i < info.getAllUsedNames().size(); i++) {
					if (!info.getAllUsedNames().get(i).isDeclarationInScope()) {
						NameInformation name = info.getAllUsedNames().get(i);
						if(!name.isReturnValue()) {
							name.setUserSetIsReference(name.isReference());
						}
					}
				}
				sm.done();
			}
			finally {
				unlockIndex();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return initStatus;
	}

	private void markWriteAccess() throws CoreException {
		ArrayList<NameInformation> paras = container.getNames();

		for (NameInformation name : paras) {
			int flag = CPPVariableReadWriteFlags.getReadWriteFlags(name
					.getName());
			if ((flag & PDOMName.WRITE_ACCESS) != 0) {
				name.setWriteAccess(true);
			}
		}

	}

	private void checkForNonExtractableStatements(NodeContainer cont,
			RefactoringStatus status) {

		NonExtractableStmtFinder vis = new NonExtractableStmtFinder();
		for (IASTNode node : cont.getNodesToWrite()) {
			node.accept(vis);
			if (vis.containsContinue()) {
				initStatus.addFatalError(Messages.ExtractFunctionRefactoring_Error_Continue);
				break;
			} else if (vis.containsBreak()) {
				initStatus.addFatalError(Messages.ExtractFunctionRefactoring_Error_Break);
				break;
			}
		}

		ReturnStatementFinder rFinder = new ReturnStatementFinder();
		for (IASTNode node : cont.getNodesToWrite()) {
			node.accept(rFinder);
			if (rFinder.containsReturn()) {
				initStatus.addFatalError(Messages.ExtractFunctionRefactoring_Error_Return);
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
		RefactoringStatus finalConditions = null;
		try {
			lockIndex();
			try {
				finalConditions = super.checkFinalConditions(pm);


				final IASTName astMethodName = new CPPASTName(info.getMethodName()
						.toCharArray());
				MethodContext context = NodeHelper.findMethodContext(container.getNodesToWrite().get(0), getIndex());

				if (context.getType() == ContextType.METHOD && !context.isInline()) {
					ICPPASTCompositeTypeSpecifier classDeclaration = (ICPPASTCompositeTypeSpecifier) context
					.getMethodDeclaration().getParent();
					IASTSimpleDeclaration methodDeclaration = getDeclaration(astMethodName);

					if (isMethodAllreadyDefined(methodDeclaration, classDeclaration, getIndex())) {
						finalConditions.addError(Messages.ExtractFunctionRefactoring_NameInUse);
						return finalConditions;
					}
				}
				for (NameInformation name : info.getAllUsedNames()) {
					if (name.isUserSetIsReturnValue()) {
						info.setReturnVariable(name);
					}

				}
			}
			finally {
				unlockIndex();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return finalConditions;
	}

	@Override
	protected void collectModifications(IProgressMonitor pm,
			ModificationCollector collector) throws CoreException,
			OperationCanceledException {
		try {
			lockIndex();
			try {
				final IASTName astMethodName = new CPPASTName(info.getMethodName()
						.toCharArray());

				MethodContext context = NodeHelper.findMethodContext(container.getNodesToWrite().get(0), getIndex());

				// Create Declaration in Class
				if (context.getType() == ContextType.METHOD && !context.isInline()) {
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
			finally {
				unlockIndex();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

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

		if (info.isReplaceDuplicates()) {
			replaceSimilar(collector, astMethodName, implementationFile, context.getType());
		}

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
			IASTBinaryExpression rootBinExp = getRootBinExp(parent, list);
			newParentNode.setParent(rootBinExp.getParent());
			newParentNode.setOperand1(leftSubTree.copy());
			newParentNode.setOperator(op);
			newParentNode.setOperand2((IASTExpression) methodCall);
			rewriter.replace(rootBinExp, newParentNode, editGroup);
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

		IASTSimpleDeclaration methodDeclaration = getDeclaration(collector, astMethodName);

		AddDeclarationNodeToClassChange.createChange(classDeclaration, info
				.getVisibility(), methodDeclaration, false, collector);

	}

	private void replaceSimilar(ModificationCollector collector, final IASTName astMethodName,
			final IFile implementationFile,
			final ContextType contextType) {
		// Find similar code
		final List<IASTNode> nodesToRewriteWithoutComments = new LinkedList<IASTNode>();

		for (IASTNode node : container.getNodesToWrite()) {
			if (!(node instanceof IASTComment)) {
				nodesToRewriteWithoutComments.add(node);
			}
		}

		final Vector<IASTNode> initTrail = getTrail(nodesToRewriteWithoutComments);
		final String title;
		if (contextType == MethodContext.ContextType.METHOD) {
			title = Messages.ExtractFunctionRefactoring_CreateMethodCall;
		} else {
			title = Messages.ExtractFunctionRefactoring_CreateFunctionCall;
		}

		if (!hasNameResolvingForSimilarError) {
			unit.accept(new SimilarFinderVisitor(this, collector, initTrail, implementationFile,
					astMethodName, nodesToRewriteWithoutComments, title));
		}
	}

	protected Vector<IASTNode> getTrail(List<IASTNode> stmts) {
		final Vector<IASTNode> trail = new Vector<IASTNode>();

		nameTrail = new HashMap<String, Integer>();
		final Container<Integer> trailCounter = new Container<Integer>(NULL_INTEGER);

		for (IASTNode node : stmts) {
			node.accept(new CPPASTAllVisitor() {
				@Override
				public int visitAll(IASTNode node) {

					if (node instanceof IASTComment) {
						// Visit Comment, but don't add them to the trail
						return super.visitAll(node);
					} else if (node instanceof IASTNamedTypeSpecifier) {
						// Skip if somewhere is a named Type Specifier
						trail.add(node);
						return PROCESS_SKIP;
					} else if (node instanceof IASTName) {
						if (node instanceof ICPPASTConversionName && node instanceof ICPPASTOperatorName
								&& node instanceof ICPPASTTemplateId) {
							trail.add(node);
							return super.visitAll(node);
						} else {
							// Save Name Sequenz Number
							IASTName name = (IASTName) node;
							TrailName trailName = new TrailName(name);
							int actCount = trailCounter.getObject().intValue();
							if (nameTrail.containsKey(name.getRawSignature())) {
								Integer value = nameTrail.get(name.getRawSignature());
								actCount = value.intValue();
							} else {
								trailCounter.setObject(Integer.valueOf(++actCount));
								nameTrail.put(name.getRawSignature(), trailCounter.getObject());
							}
							trailName.setNameNumber(actCount);

							if (info.getReturnVariable() != null
									&& info.getReturnVariable().getName().getRawSignature().equals(
											name.getRawSignature())) {
								returnNumber.setObject(Integer.valueOf(actCount));
							}

							trail.add(trailName);
							return PROCESS_SKIP;
						}
					} else {
						trail.add(node);
						return super.visitAll(node);
					}
				}
			});

		}

		return trail;
	}

	protected boolean isStatementInTrail(IASTStatement stmt, final Vector<IASTNode> trail, IIndex index) {
		final Container<Boolean> same = new Container<Boolean>(Boolean.TRUE);
		final TrailNodeEqualityChecker equalityChecker = new TrailNodeEqualityChecker(names, namesCounter, index);

		stmt.accept(new CPPASTAllVisitor() {
			@Override
			public int visitAll(IASTNode node) {

				int pos = trailPos.getObject().intValue();

				if (trail.size() <= 0 || pos >= trail.size()) {
					same.setObject(Boolean.FALSE);
					return PROCESS_ABORT;
				}

				if (node instanceof IASTComment) {
					// Visit Comment, but they are not in the trail
					return super.visitAll(node);
				}

				IASTNode trailNode = trail.get(pos);
				trailPos.setObject(Integer.valueOf(pos + 1));

				if (equalityChecker.isEquals(trailNode, node)) {
					if (node instanceof ICPPASTQualifiedName || node instanceof IASTNamedTypeSpecifier) {
						return PROCESS_SKIP;
					} else {
						return super.visitAll(node);
					}

				} else {
					same.setObject(new Boolean(false));
					return PROCESS_ABORT;
				}
			}
		});

		return same.getObject().booleanValue();
	}

	private boolean isMethodAllreadyDefined(
			IASTSimpleDeclaration methodDeclaration,
			ICPPASTCompositeTypeSpecifier classDeclaration, IIndex index) {
		TrailNodeEqualityChecker equalityChecker = new TrailNodeEqualityChecker(
				names, namesCounter, index);

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
			if(context.getMethodQName() != null) {
				for (int i = 0; i < (context.getMethodQName().getNames().length - 1); i++) {
					qname.addName(new CPPASTName(context.getMethodQName().getNames()[i].toCharArray()));
				}
			}
		}
		qname.addName(astMethodName);

		IASTFunctionDefinition func = new CPPASTFunctionDefinition();
		func.setParent(unit);

		IASTDeclSpecifier returnType = getReturnType();
		func.setDeclSpecifier(returnType);
		
		IASTStandardFunctionDeclarator createdFunctionDeclarator = extractedFunctionConstructionHelper
				.createFunctionDeclarator(qname, info.getDeclarator(), info
						.getReturnVariable(), container.getNodesToWrite(), info
						.getAllUsedNames(), unit.getASTNodeFactory());
		func.setDeclarator(createdFunctionDeclarator);

		IASTCompoundStatement compound = new CPPASTCompoundStatement();
		func.setBody(compound);
		
		ASTRewrite subRW;
		if(insertpoint.getParent() instanceof ICPPASTTemplateDeclaration) {
			
			CPPASTTemplateDeclaration templateDeclaration = new CPPASTTemplateDeclaration();
			templateDeclaration.setParent(unit);
			
			for(ICPPASTTemplateParameter templateParameter : ((ICPPASTTemplateDeclaration) insertpoint.getParent()).getTemplateParameters()) {
				templateDeclaration.addTemplateParameter(templateParameter.copy());
			}
			
			templateDeclaration.setDeclaration(func);

			subRW = rewriter.insertBefore(insertpoint.getParent().getParent(), insertpoint.getParent(), templateDeclaration, group);

		} else {
			
			subRW = rewriter.insertBefore(insertpoint.getParent(), insertpoint, func, group);
		}
		
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
		List<IASTInitializerClause> args = new ArrayList<IASTInitializerClause>();

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
						addAParameterIfPossible(args, declarations,
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
					args.add(expression);

					if (theRetName) {
						theRetName = false;
						retName = fieldName;
					}
				}
			}
		}

		callExpression.setArguments(args.toArray(new IASTInitializerClause[args.size()]));
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
		List<IASTInitializerClause> args = getCallParameters();
		callExpression.setArguments(args.toArray(new IASTInitializerClause[args.size()]));
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

			decl.setDeclSpecifier(orgDecl.getDeclSpecifier().copy());

			IASTDeclarator declarator = new CPPASTDeclarator();

			declarator.setName(retname);

			for (IASTPointerOperator pointer : orgDecl.getDeclarators()[0]
					.getPointerOperators()) {
				declarator.addPointerOperator(pointer.copy());
			}

			IASTEqualsInitializer initializer = new CPPASTEqualsInitializer();
			initializer.setInitializerClause(callExpression);
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
		IASTStandardFunctionDeclarator declarator = extractedFunctionConstructionHelper
				.createFunctionDeclarator(name, info.getDeclarator(), info
						.getReturnVariable(), container.getNodesToWrite(), info
						.getAllUsedNames(), unit.getASTNodeFactory());
		simpleDecl.addDeclarator(declarator);
		return simpleDecl;
	}

	private IASTSimpleDeclaration getDeclaration(ModificationCollector collector,IASTName name) {
		IASTDeclSpecifier declSpec = getReturnType();
		IASTSimpleDeclaration simpleDecl = factory.newSimpleDeclaration(declSpec);
		if(info.isVirtual() && declSpec instanceof ICPPASTDeclSpecifier) {
			((ICPPASTDeclSpecifier)declSpec).setVirtual(true);
		}
		simpleDecl.setParent(unit);
		IASTStandardFunctionDeclarator declarator = extractedFunctionConstructionHelper
				.createFunctionDeclarator(name, info.getDeclarator(), info
						.getReturnVariable(), container.getNodesToWrite(), info
						.getAllUsedNames(), unit.getASTNodeFactory());
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
				if ( SelectionHelper.isSelectedFile(region, stmt, file)) {
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

	public List<IASTInitializerClause> getCallParameters() {
		List<IASTInitializerClause> args = new ArrayList<IASTInitializerClause>();
		Vector<IASTName> declarations = new Vector<IASTName>();
		for (NameInformation nameInf : container.getNames()) {
			addAParameterIfPossible(args, declarations, nameInf);
		}
		return args;
	}

	private void addAParameterIfPossible(List<IASTInitializerClause> args,
			Vector<IASTName> declarations, NameInformation nameInf) {
		if (!nameInf.isDeclarationInScope()) {
			IASTName declaration = nameInf.getDeclaration();
			if (!declarations.contains(declaration)) {
				declarations.add(declaration);
				IASTIdExpression expression = new CPPASTIdExpression();
				expression.setName(newName(declaration));
				args.add(expression);
			}
		}
	}

	@Override
	protected RefactoringDescriptor getRefactoringDescriptor() {
		Map<String, String> arguments = getArgumentMap();
		RefactoringDescriptor desc = new ExtractFunctionRefactoringDescription( project.getProject().getName(), "Extract Method Refactoring", "Create method " + info.getMethodName(), arguments);  //$NON-NLS-1$//$NON-NLS-2$
		return desc;
	}

	private Map<String, String> getArgumentMap() {
		Map<String, String> arguments = new HashMap<String, String>();
		arguments.put(CRefactoringDescription.FILE_NAME, file.getLocationURI().toString());
		arguments.put(CRefactoringDescription.SELECTION, region.getOffset() + "," + region.getLength()); //$NON-NLS-1$
		arguments.put(ExtractFunctionRefactoringDescription.NAME, info.getMethodName());
		arguments.put(ExtractFunctionRefactoringDescription.VISIBILITY, info.getVisibility().toString());
		arguments.put(ExtractFunctionRefactoringDescription.REPLACE_DUBLICATES, Boolean.toString(info.isReplaceDuplicates()));
		return arguments;
	}

}
