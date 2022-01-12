/*******************************************************************************
 * Copyright (c) 2008, 2016 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractfunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
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
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConversionName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNameSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTOperatorName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.dom.rewrite.TypeHelper;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterOptions;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
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
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ASTWriterVisitor;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringDescriptor;
import org.eclipse.cdt.internal.ui.refactoring.ClassMemberInserter;
import org.eclipse.cdt.internal.ui.refactoring.ClassMemberInserter.InsertionInfo;
import org.eclipse.cdt.internal.ui.refactoring.Container;
import org.eclipse.cdt.internal.ui.refactoring.MethodContext;
import org.eclipse.cdt.internal.ui.refactoring.MethodContext.ContextType;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.NameInformation;
import org.eclipse.cdt.internal.ui.refactoring.NameInformation.Indirection;
import org.eclipse.cdt.internal.ui.refactoring.NodeContainer;
import org.eclipse.cdt.internal.ui.refactoring.utils.ASTHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.CPPASTAllVisitor;
import org.eclipse.cdt.internal.ui.refactoring.utils.Checks;
import org.eclipse.cdt.internal.ui.refactoring.utils.NodeHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.SelectionHelper;
import org.eclipse.cdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.osgi.util.NLS;
import org.eclipse.text.edits.TextEditGroup;

public class ExtractFunctionRefactoring extends CRefactoring {
	public static final String ID = "org.eclipse.cdt.internal.ui.refactoring.extractfunction.ExtractFunctionRefactoring"; //$NON-NLS-1$

	private static final String[] EMPTY_STRING_ARRAY = {};
	static final Integer NULL_INTEGER = Integer.valueOf(0);

	private NodeContainer container;
	private final ExtractFunctionInformation info;

	final Map<String, Integer> names;
	final Container<Integer> namesCounter;
	final Container<Integer> trailPos;
	private int returnNumber;

	HashMap<String, Integer> nameTrail;

	private FunctionExtractor extractor;
	private INodeFactory nodeFactory;
	private final DefaultCodeFormatterOptions formattingOptions;

	private IIndex index;
	private IASTTranslationUnit ast;

	public ExtractFunctionRefactoring(ICElement element, ISelection selection, ICProject project) {
		super(element, selection, project);
		info = new ExtractFunctionInformation();
		name = Messages.ExtractFunctionRefactoring_ExtractFunction;
		names = new HashMap<>();
		namesCounter = new Container<>(NULL_INTEGER);
		trailPos = new Container<>(NULL_INTEGER);
		formattingOptions = new DefaultCodeFormatterOptions(project.getOptions(true));
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		SubMonitor sm = SubMonitor.convert(pm, 10);

		try {
			RefactoringStatus status = super.checkInitialConditions(sm.newChild(8));
			if (status.hasError()) {
				return status;
			}

			index = getIndex();
			ast = getAST(tu, sm.newChild(1));
			nodeFactory = ast.getASTNodeFactory();
			container = findExtractableNodes();

			if (isProgressMonitorCanceled(sm, initStatus))
				return initStatus;

			if (container.isEmpty()) {
				initStatus.addFatalError(Messages.ExtractFunctionRefactoring_NoStmtSelected);
				return initStatus;
			}

			checkForNonExtractableStatements(container, initStatus);

			List<NameInformation> returnValueCandidates = container.getReturnValueCandidates();
			if (returnValueCandidates.size() > 1) {
				initStatus.addFatalError(Messages.ExtractFunctionRefactoring_TooManyDeclarations);
				return initStatus;
			} else if (returnValueCandidates.size() == 1) {
				info.setMandatoryReturnVariable(returnValueCandidates.get(0));
			}

			info.setParameters(container.getParameterCandidates());
			initStatus.merge(checkParameterAndReturnTypes());
			if (initStatus.hasFatalError())
				return initStatus;

			extractor = FunctionExtractor.createFor(container.getNodesToWrite());

			if (extractor.canChooseReturnValue() && info.getMandatoryReturnVariable() == null) {
				chooseReturnVariable();
			}

			IPreferencesService preferences = Platform.getPreferencesService();
			final boolean outFirst = preferences.getBoolean(CUIPlugin.PLUGIN_ID,
					PreferenceConstants.FUNCTION_OUTPUT_PARAMETERS_BEFORE_INPUT, false,
					PreferenceConstants.getPreferenceScopes(project.getProject()));
			info.sortParameters(outFirst);

			boolean isExtractExpression = container.getNodesToWrite().get(0) instanceof IASTExpression;
			info.setExtractExpression(isExtractExpression);

			info.setDeclarator(getDeclaration(container.getNodesToWrite().get(0)));
			MethodContext context = NodeHelper.findMethodContext(container.getNodesToWrite().get(0), refactoringContext,
					sm.newChild(1));
			if (context.getType() == ContextType.METHOD && context.getMethodDeclarationName() == null) {
				initStatus.addFatalError(Messages.ExtractFunctionRefactoring_no_declaration_of_surrounding_method);
				return initStatus;
			}
			info.setMethodContext(context);
			return initStatus;
		} finally {
			sm.done();
		}
	}

	private void chooseReturnVariable() {
		NameInformation candidate = null;
		for (NameInformation param : info.getParameters()) {
			if (param.isOutput()) {
				IASTDeclarator declarator = param.getDeclarator();
				IType type = TypeHelper.createType(declarator);
				type = SemanticUtil.getNestedType(type, SemanticUtil.CVTYPE | SemanticUtil.TDEF);
				if (type instanceof IBasicType) {
					if (((IBasicType) type).getKind() == IBasicType.Kind.eBoolean) {
						param.setReturnValue(true);
						return;
					}
				}
				if (candidate == null && !TypeHelper.shouldBePassedByReference(type, declarator.getTranslationUnit())) {
					candidate = param;
				}
			}
		}
		if (candidate != null)
			candidate.setReturnValue(true);
	}

	private void checkForNonExtractableStatements(NodeContainer container, RefactoringStatus status) {
		NonExtractableStatementFinder finder = new NonExtractableStatementFinder();
		for (IASTNode node : container.getNodesToWrite()) {
			node.accept(finder);
			if (finder.containsContinue()) {
				initStatus.addFatalError(Messages.ExtractFunctionRefactoring_Error_Continue);
				break;
			} else if (finder.containsBreak()) {
				initStatus.addFatalError(Messages.ExtractFunctionRefactoring_Error_Break);
				break;
			}
		}

		ReturnStatementFinder returnFinder = new ReturnStatementFinder();
		for (IASTNode node : container.getNodesToWrite()) {
			node.accept(returnFinder);
			if (returnFinder.containsReturn()) {
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
			IASTFunctionDeclarator declarator = ((IASTFunctionDefinition) node).getDeclarator();
			if (declarator instanceof ICPPASTFunctionDeclarator) {
				return (ICPPASTFunctionDeclarator) declarator;
			}
		}
		return null;
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext checkContext)
			throws CoreException, OperationCanceledException {
		RefactoringStatus status = new RefactoringStatus();

		final IASTName methodName = new CPPASTName(info.getMethodName().toCharArray());
		MethodContext context = info.getMethodContext();

		if (context.getType() == ContextType.METHOD && !context.isInline()) {
			IASTDeclaration contextDeclaration = context.getMethodDeclaration();
			ICPPASTCompositeTypeSpecifier classDeclaration = (ICPPASTCompositeTypeSpecifier) contextDeclaration
					.getParent();
			IASTSimpleDeclaration methodDeclaration = getDeclaration(methodName);

			if (isMethodAllreadyDefined(methodDeclaration, classDeclaration, getIndex())) {
				status.addError(Messages.ExtractFunctionRefactoring_name_in_use);
			}
		}
		return status;
	}

	@Override
	protected void collectModifications(IProgressMonitor pm, ModificationCollector collector)
			throws CoreException, OperationCanceledException {
		final IASTName methodName = new CPPASTName(info.getMethodName().toCharArray());
		MethodContext context = info.getMethodContext();

		// Create declaration in class.
		if (context.getType() == ContextType.METHOD && !context.isInline()) {
			createMethodDeclaration(methodName, context, collector);
		}
		// Create method definition.
		IASTNode firstNode = container.getNodesToWrite().get(0);
		createMethodDefinition(methodName, context, firstNode, collector);

		createMethodCalls(methodName, context, collector);
	}

	private void createMethodCalls(IASTName methodName, MethodContext context, ModificationCollector collector)
			throws CoreException {
		String title;
		if (context.getType() == MethodContext.ContextType.METHOD) {
			title = Messages.ExtractFunctionRefactoring_CreateMethodCall;
		} else {
			title = Messages.ExtractFunctionRefactoring_CreateFunctionCall;
		}

		IASTNode methodCall = getMethodCall(methodName);

		IASTNode firstNodeToWrite = container.getNodesToWrite().get(0);
		ASTRewrite rewriter = collector.rewriterForTranslationUnit(firstNodeToWrite.getTranslationUnit());
		TextEditGroup editGroup = new TextEditGroup(title);
		if (methodCall instanceof IASTDeclaration) {
			CPPASTDeclarationStatement declarationStatement = new CPPASTDeclarationStatement(
					(IASTDeclaration) methodCall);
			methodCall = declarationStatement;
		}
		insertCallIntoTree(methodCall, container.getNodesToWrite(), rewriter, editGroup);

		if (info.isReplaceDuplicates()) {
			replaceSimilar(collector, methodName);
		}

		for (IASTNode node : container.getNodesToWrite()) {
			if (node != firstNodeToWrite) {
				rewriter.remove(node, editGroup);
			}
		}
	}

	private void insertCallIntoTree(IASTNode methodCall, List<IASTNode> list, ASTRewrite rewriter,
			TextEditGroup editGroup) {
		IASTNode firstNode = list.get(0);
		if (list.size() > 1 && firstNode.getParent() instanceof IASTBinaryExpression
				&& firstNode.getParent().getParent() instanceof IASTBinaryExpression) {
			IASTBinaryExpression parent = (IASTBinaryExpression) firstNode.getParent();
			IASTExpression leftSubTree = parent.getOperand1();
			int op = parent.getOperator();
			IASTBinaryExpression newParentNode = new CPPASTBinaryExpression();
			IASTBinaryExpression rootBinExp = getRootBinExp(parent, list);
			newParentNode.setParent(rootBinExp.getParent());
			newParentNode.setOperand1(leftSubTree.copy(CopyStyle.withLocations));
			newParentNode.setOperator(op);
			newParentNode.setOperand2((IASTExpression) methodCall);
			rewriter.replace(rootBinExp, newParentNode, editGroup);
		} else {
			rewriter.replace(firstNode, methodCall, editGroup);
		}
	}

	private IASTBinaryExpression getRootBinExp(IASTBinaryExpression binExp, List<IASTNode> nodeList) {
		while (binExp.getParent() instanceof IASTBinaryExpression
				&& nodeList.contains(((IASTBinaryExpression) binExp.getParent()).getOperand2())) {
			binExp = (IASTBinaryExpression) binExp.getParent();
		}
		return binExp;
	}

	private void createMethodDefinition(final IASTName methodName, MethodContext context, IASTNode firstExtractedNode,
			ModificationCollector collector) {
		IASTFunctionDefinition functionToExtractFrom = ASTQueries.findAncestorWithType(firstExtractedNode,
				IASTFunctionDefinition.class);
		if (functionToExtractFrom != null) {
			String title;
			if (context.getType() == MethodContext.ContextType.METHOD) {
				title = Messages.ExtractFunctionRefactoring_CreateMethodDef;
			} else {
				title = Messages.ExtractFunctionRefactoring_CreateFunctionDef;
			}

			ASTRewrite rewriter = collector.rewriterForTranslationUnit(functionToExtractFrom.getTranslationUnit());
			addMethod(methodName, context, rewriter, functionToExtractFrom, new TextEditGroup(title));
		}
	}

	private void createMethodDeclaration(final IASTName astMethodName, MethodContext context,
			ModificationCollector collector) {
		ICPPASTCompositeTypeSpecifier classDeclaration = (ICPPASTCompositeTypeSpecifier) context.getMethodDeclaration()
				.getParent();

		IASTSimpleDeclaration methodDeclaration = getDeclaration(collector, astMethodName);

		ASTRewrite rewrite = ClassMemberInserter.createChange(classDeclaration, info.getVisibility(), methodDeclaration,
				false, collector);

		// Names of external bindings may have to be qualified to be used in a header file.
		if (classDeclaration.getTranslationUnit().isHeaderUnit())
			qualifyExternalReferences(methodDeclaration, classDeclaration, rewrite);
	}

	private void qualifyExternalReferences(IASTNode node, ICPPASTCompositeTypeSpecifier classDeclaration,
			final ASTRewrite rewrite) {
		final ICPPBinding owner = (ICPPBinding) classDeclaration.getName().resolveBinding();
		final String[] contextQualifiers;
		try {
			contextQualifiers = owner.getQualifiedName();
		} catch (DOMException e) {
			CUIPlugin.log(e);
			return;
		}

		node.accept(new ASTVisitor(true) {
			@Override
			public int visit(IASTName name) {
				qualifyForContext((ICPPASTName) name, contextQualifiers, rewrite);
				return PROCESS_SKIP; // Do non visit internals of qualified names.
			}
		});
	}

	private void qualifyForContext(ICPPASTName name, String[] contextQualifiers, ASTRewrite rewrite) {
		ICPPASTName originalName = (ICPPASTName) name.getOriginalNode();
		IBinding binding = originalName.resolveBinding();
		try {
			if (!(binding instanceof ICPPBinding))
				return; // Qualification is not needed.
			String[] names = ((ICPPBinding) binding).getQualifiedName();
			names = removeCommonPrefix(names, contextQualifiers);
			if (names.length <= 1)
				return; // Qualification is not needed.

			ICPPASTQualifiedName qualifiedName;
			if (name instanceof ICPPASTQualifiedName) {
				qualifiedName = (ICPPASTQualifiedName) name;
				if (qualifiedName.getQualifier().length >= names.length - 1)
					return; // Qualified already.
			} else {
				qualifiedName = new CPPASTQualifiedName(name.copy(CopyStyle.withLocations));
			}
			for (int i = 0; i < names.length - qualifiedName.getQualifier().length; i++) {
				qualifiedName.addNameSpecifier(new CPPASTName(names[i].toCharArray()));
			}
			if (!(name instanceof ICPPASTQualifiedName))
				rewrite.replace(name, qualifiedName, null);
		} catch (DOMException e) {
			CUIPlugin.log(e);
			return;
		}
	}

	private String[] removeCommonPrefix(String[] array1, String[] array2) {
		for (int i = 0; i < array1.length && i < array2.length; i++) {
			if (!array1[i].equals(array2[i])) {
				if (i == 0)
					return array1;
				return Arrays.copyOfRange(array1, i, array1.length);
			}
		}
		return EMPTY_STRING_ARRAY;
	}

	private void replaceSimilar(ModificationCollector collector, IASTName methodName) {
		// Find similar code.
		final List<IASTNode> nodesToRewriteWithoutComments = getNodesWithoutComments(container.getNodesToWrite());
		final List<IASTNode> initTrail = getTrail(nodesToRewriteWithoutComments);
		IASTTranslationUnit ast = nodesToRewriteWithoutComments.get(0).getTranslationUnit();
		ast.accept(new SimilarReplacerVisitor(this, container, collector, initTrail, methodName,
				nodesToRewriteWithoutComments));
	}

	public int getNumberOfDuplicates() {
		final List<IASTNode> nodesToRewriteWithoutComments = getNodesWithoutComments(container.getNodesToWrite());
		final List<IASTNode> initTrail = getTrail(nodesToRewriteWithoutComments);
		final int[] count = new int[1];
		IASTTranslationUnit ast = nodesToRewriteWithoutComments.get(0).getTranslationUnit();
		ast.accept(new SimilarFinderVisitor(this, container, initTrail, nodesToRewriteWithoutComments) {
			@Override
			protected void foundSimilar() {
				++count[0];
			}
		});
		return count[0];
	}

	private List<IASTNode> getNodesWithoutComments(List<IASTNode> nodes) {
		final List<IASTNode> nodesWithoutComments = new ArrayList<>(nodes.size());

		for (IASTNode node : nodes) {
			if (!(node instanceof IASTComment)) {
				nodesWithoutComments.add(node);
			}
		}
		return nodesWithoutComments;
	}

	private List<IASTNode> getTrail(List<IASTNode> stmts) {
		final List<IASTNode> trail = new ArrayList<>();
		nameTrail = new HashMap<>();
		final Container<Integer> trailCounter = new Container<>(NULL_INTEGER);

		for (IASTNode node : stmts) {
			node.accept(new CPPASTAllVisitor() {
				@Override
				public int visitAll(IASTNode node) {
					if (node instanceof IASTComment) {
						// Visit comments, but don't add them to the trail
						return super.visitAll(node);
					} else if (node instanceof IASTNamedTypeSpecifier) {
						// Skip if somewhere is a named type specifier
						trail.add(node);
						return PROCESS_SKIP;
					} else if (node instanceof IASTName) {
						if (node instanceof ICPPASTConversionName && node instanceof ICPPASTOperatorName
								&& node instanceof ICPPASTTemplateId) {
							trail.add(node);
							return super.visitAll(node);
						} else {
							// Save name sequence number
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

							if (info.getReturnVariable() != null && info.getReturnVariable().getName().getRawSignature()
									.equals(name.getRawSignature())) {
								returnNumber = actCount;
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

	boolean isStatementInTrail(final IASTStatement stmt, final List<IASTNode> trail) {
		final boolean same[] = { true };
		final TrailNodeEqualityChecker equalityChecker = new TrailNodeEqualityChecker(names, namesCounter, index);

		stmt.accept(new CPPASTAllVisitor() {
			@Override
			public int visitAll(IASTNode node) {
				int pos = trailPos.getObject().intValue();

				if (trail.size() <= 0 || pos >= trail.size()) {
					same[0] = false;
					return PROCESS_ABORT;
				}

				if (node instanceof IASTComment) {
					// Visit comments, but they are not in the trail
					return super.visitAll(node);
				}

				IASTNode trailNode = trail.get(pos);
				trailPos.setObject(Integer.valueOf(pos + 1));

				if (equalityChecker.isEqual(trailNode, node)) {
					if (node instanceof ICPPASTQualifiedName || node instanceof IASTNamedTypeSpecifier) {
						return PROCESS_SKIP;
					} else {
						return super.visitAll(node);
					}
				} else {
					same[0] = false;
					return PROCESS_ABORT;
				}
			}
		});

		return same[0];
	}

	private boolean isMethodAllreadyDefined(IASTSimpleDeclaration methodDeclaration,
			ICPPASTCompositeTypeSpecifier classDeclaration, IIndex index) {
		TrailNodeEqualityChecker equalityChecker = new TrailNodeEqualityChecker(names, namesCounter, index);

		IBinding bind = classDeclaration.getName().resolveBinding();
		IASTStandardFunctionDeclarator declarator = (IASTStandardFunctionDeclarator) methodDeclaration
				.getDeclarators()[0];
		String name = new String(declarator.getName().toCharArray());
		if (bind instanceof ICPPClassType) {
			ICPPClassType classBind = (ICPPClassType) bind;
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
							IASTName[] origParameterName = ast.getDeclarationsInAST(parameters[i]);
							IASTParameterDeclaration origParameter = (IASTParameterDeclaration) origParameterName[0]
									.getParent().getParent();
							IASTParameterDeclaration newParameter = declarator.getParameters()[i];

							// If not the same break;
							if (!(equalityChecker.isEqual(origParameter.getDeclSpecifier(),
									newParameter.getDeclSpecifier())
									&& ASTHelper.samePointers(origParameter.getDeclarator().getPointerOperators(),
											newParameter.getDeclarator().getPointerOperators(), equalityChecker))) {
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
		}
		return true;
	}

	private void addMethod(IASTName methodName, MethodContext context, ASTRewrite rewrite,
			IASTNode functionToExtractFrom, TextEditGroup group) {
		ICPPASTQualifiedName qname = new CPPASTQualifiedName((ICPPASTName) methodName);
		if (context.getType() == ContextType.METHOD) {
			if (context.getMethodQName() != null) {
				for (ICPPASTNameSpecifier segment : context.getMethodQName().getQualifier()) {
					qname.addNameSpecifier(segment.copy());
				}
			}
		}

		IASTFunctionDefinition func = new CPPASTFunctionDefinition();
		func.setParent(ast);

		List<IASTPointerOperator> pointerOperators = new ArrayList<>();
		IASTDeclSpecifier returnType = getReturnType(pointerOperators);
		func.setDeclSpecifier(returnType);

		IASTStandardFunctionDeclarator declarator = extractor.createFunctionDeclarator(qname, info.getDeclarator(),
				info.getReturnVariable(), container.getNodesToWrite(), info.getParameters(), nodeFactory);
		for (IASTPointerOperator operator : pointerOperators) {
			declarator.addPointerOperator(operator);
		}
		func.setDeclarator(declarator);

		IASTCompoundStatement compound = new CPPASTCompoundStatement();
		func.setBody(compound);

		ASTRewrite subRewrite;
		IASTNode parent = functionToExtractFrom.getParent();
		IASTNode nodeToInsert = func;
		if (parent instanceof ICPPASTTemplateDeclaration) {
			ICPPASTTemplateDeclaration parentTemplate = (ICPPASTTemplateDeclaration) parent;
			CPPASTTemplateDeclaration templateDeclaration = new CPPASTTemplateDeclaration();
			templateDeclaration.setParent(ast);

			for (ICPPASTTemplateParameter param : parentTemplate.getTemplateParameters()) {
				templateDeclaration.addTemplateParameter(param.copy(CopyStyle.withLocations));
			}

			functionToExtractFrom = parentTemplate;
			templateDeclaration.setDeclaration(func);
			nodeToInsert = templateDeclaration;
			parent = parent.getParent();
		}

		InsertionInfo insertion;
		if (parent instanceof ICPPASTCompositeTypeSpecifier) {
			// Inserting into a class declaration
			insertion = ClassMemberInserter.findInsertionPoint((ICPPASTCompositeTypeSpecifier) parent,
					info.getVisibility(), false);
		} else {
			// Inserting into a translation unit or a namespace.
			// TODO(sprigogin): Use insertBeforeNode instead of functionToExtractFrom when creating InsertionInfo
			//			IASTNode insertBeforeNode = info.getMethodContext().getType() == ContextType.METHOD ?
			//					null : functionToExtractFrom;
			insertion = new InsertionInfo(parent, functionToExtractFrom);
		}
		if (insertion.getPrologue() != null) {
			rewrite.insertBefore(insertion.getParentNode(), insertion.getInsertBeforeNode(), insertion.getPrologue(),
					group);
		}
		subRewrite = rewrite.insertBefore(insertion.getParentNode(), insertion.getInsertBeforeNode(), nodeToInsert,
				group);
		if (insertion.getEpilogue() != null) {
			rewrite.insertBefore(insertion.getParentNode(), insertion.getInsertBeforeNode(), insertion.getEpilogue(),
					group);
		}

		extractor.constructMethodBody(compound, container.getNodesToWrite(), info.getParameters(), subRewrite, group);

		// Set return value
		NameInformation returnVariable = info.getReturnVariable();
		if (returnVariable != null) {
			IASTReturnStatement returnStmt = new CPPASTReturnStatement();
			IASTIdExpression expr = new CPPASTIdExpression();
			if (returnVariable.getNewName() == null) {
				expr.setName(newName(returnVariable.getName()));
			} else {
				expr.setName(new CPPASTName(returnVariable.getNewName().toCharArray()));
			}
			returnStmt.setReturnValue(expr);
			subRewrite.insertBefore(compound, null, returnStmt, group);
		}
	}

	private IASTName newName(IASTName declaration) {
		return new CPPASTName(declaration.toCharArray());
	}

	private IASTDeclSpecifier getReturnType(List<IASTPointerOperator> pointerOperators) {
		IASTNode firstNodeToWrite = container.getNodesToWrite().get(0);
		NameInformation returnVariable = info.getReturnVariable();
		return extractor.determineReturnType(firstNodeToWrite, returnVariable, pointerOperators);
	}

	protected IASTNode getMethodCall(IASTName astMethodName, Map<String, Integer> trailNameTable,
			Map<String, Integer> similarNameTable, NodeContainer myContainer, NodeContainer mySimilarContainer) {
		IASTExpressionStatement stmt = new CPPASTExpressionStatement();
		IASTFunctionCallExpression callExpression = new CPPASTFunctionCallExpression();
		IASTIdExpression idExpression = new CPPASTIdExpression();
		idExpression.setName(astMethodName);
		List<IASTInitializerClause> args = new ArrayList<>();

		Set<IASTName> declarations = new HashSet<>();
		IASTName retName = null;
		boolean theRetName = false;

		for (NameInformation nameInfo : info.getParameters()) {
			String origName = null;
			Integer trailSeqNumber = trailNameTable.get(nameInfo.getDeclarationName().getRawSignature());
			if (trailSeqNumber != null) {
				for (Entry<String, Integer> entry : similarNameTable.entrySet()) {
					if (entry.getValue().equals(trailSeqNumber)) {
						origName = entry.getKey();
						if (info.getReturnVariable() != null && trailSeqNumber.intValue() == returnNumber) {
							theRetName = true;
						}
					}
				}
			} else {
				origName = String.valueOf(nameInfo.getDeclarationName().getSimpleID());
			}

			if (origName != null) {
				boolean found = false;
				for (NameInformation simNameInfo : mySimilarContainer.getNames()) {
					if (origName.equals(simNameInfo.getDeclarationName().getRawSignature())) {
						addParameterIfPossible(args, declarations, simNameInfo);
						found = true;

						if (theRetName) {
							theRetName = false;
							retName = new CPPASTName(simNameInfo.getDeclarationName().getRawSignature().toCharArray());
						}
					}
				}

				if (!found) {
					// should be a field, use the old name
					IASTIdExpression expression = new CPPASTIdExpression();
					CPPASTName fieldName = new CPPASTName(origName.toCharArray());
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

	private IASTNode getMethodCall(IASTName methodName) {
		IASTExpressionStatement statement = new CPPASTExpressionStatement();

		IASTFunctionCallExpression callExpression = new CPPASTFunctionCallExpression();
		IASTIdExpression idExpression = new CPPASTIdExpression();
		idExpression.setName(new CPPASTName(methodName.toCharArray()));
		List<IASTInitializerClause> args = getCallParameters();
		callExpression.setArguments(args.toArray(new IASTInitializerClause[args.size()]));
		callExpression.setFunctionNameExpression(idExpression);

		if (info.getReturnVariable() == null) {
			return getReturnAssignment(statement, callExpression);
		}
		IASTName retName = newName(info.getReturnVariable().getName());
		return getReturnAssignment(statement, callExpression, retName);
	}

	private IASTNode getReturnAssignment(IASTExpressionStatement stmt, IASTFunctionCallExpression callExpression,
			IASTName retname) {
		if (info.getReturnVariable().equals(info.getMandatoryReturnVariable())) {
			IASTSimpleDeclaration orgDecl = ASTQueries
					.findAncestorWithType(info.getReturnVariable().getDeclarationName(), IASTSimpleDeclaration.class);
			IASTSimpleDeclaration decl = new CPPASTSimpleDeclaration();

			decl.setDeclSpecifier(orgDecl.getDeclSpecifier().copy(CopyStyle.withLocations));

			IASTDeclarator declarator = new CPPASTDeclarator();
			declarator.setName(retname);

			for (IASTPointerOperator pointer : orgDecl.getDeclarators()[0].getPointerOperators()) {
				declarator.addPointerOperator(pointer.copy(CopyStyle.withLocations));
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

	private IASTNode getReturnAssignment(IASTExpressionStatement stmt, IASTExpression callExpression) {
		IASTNode node = container.getNodesToWrite().get(0);
		return extractor.createReturnAssignment(node, stmt, callExpression);
	}

	private IASTSimpleDeclaration getDeclaration(IASTName name) {
		IASTSimpleDeclaration simpleDecl = new CPPASTSimpleDeclaration();
		IASTStandardFunctionDeclarator declarator = extractor.createFunctionDeclarator(name, info.getDeclarator(),
				info.getReturnVariable(), container.getNodesToWrite(), info.getParameters(), nodeFactory);
		simpleDecl.addDeclarator(declarator);
		return simpleDecl;
	}

	private IASTSimpleDeclaration getDeclaration(ModificationCollector collector, IASTName name) {
		List<IASTPointerOperator> pointerOperators = new ArrayList<>();
		IASTDeclSpecifier declSpec = getReturnType(pointerOperators);
		IASTSimpleDeclaration simpleDecl = nodeFactory.newSimpleDeclaration(declSpec);
		if (info.isVirtual() && declSpec instanceof ICPPASTDeclSpecifier) {
			((ICPPASTDeclSpecifier) declSpec).setVirtual(true);
		}
		simpleDecl.setParent(ast);
		IASTStandardFunctionDeclarator declarator = extractor.createFunctionDeclarator(name, info.getDeclarator(),
				info.getReturnVariable(), container.getNodesToWrite(), info.getParameters(), nodeFactory);
		for (IASTPointerOperator operator : pointerOperators) {
			declarator.addPointerOperator(operator);
		}
		simpleDecl.addDeclarator(declarator);
		return simpleDecl;
	}

	private NodeContainer findExtractableNodes() {
		final NodeContainer container = new NodeContainer();
		ast.accept(new ASTVisitor() {
			{
				shouldVisitStatements = true;
				shouldVisitExpressions = true;
			}

			@Override
			public int visit(IASTStatement stmt) {
				if (isNodeInsideSelection(stmt)) {
					container.add(stmt);
					return PROCESS_SKIP;
				}
				return super.visit(stmt);
			}

			@Override
			public int visit(IASTExpression expression) {
				if (isNodeInsideSelection(expression)) {
					container.add(expression);
					return PROCESS_SKIP;
				}
				return super.visit(expression);
			}
		});
		return container;
	}

	private boolean isNodeInsideSelection(IASTNode node) {
		return node.isPartOfTranslationUnitFile() && SelectionHelper.isNodeInsideRegion(node, selectedRegion);
	}

	public List<IASTInitializerClause> getCallParameters() {
		List<IASTInitializerClause> args = new ArrayList<>();
		Set<IASTName> declarations = new HashSet<>();
		for (NameInformation nameInfo : info.getParameters()) {
			addParameterIfPossible(args, declarations, nameInfo);
		}
		return args;
	}

	private void addParameterIfPossible(List<IASTInitializerClause> args, Set<IASTName> declarations,
			NameInformation nameInfo) {
		if (!container.isDeclaredInSelection(nameInfo)) {
			IASTName declaration = nameInfo.getDeclarationName();
			if (declarations.add(declaration)) {
				IASTExpression expression = nodeFactory.newIdExpression(newName(declaration));
				if (nameInfo.getIndirection() == Indirection.POINTER) {
					expression = nodeFactory.newUnaryExpression(IASTUnaryExpression.op_amper, expression);
				}
				args.add(expression);
			}
		}
	}

	@Override
	protected RefactoringDescriptor getRefactoringDescriptor() {
		Map<String, String> arguments = getArgumentMap();
		RefactoringDescriptor desc = new ExtractFunctionRefactoringDescriptor(project.getProject().getName(),
				"Extract Method Refactoring", "Create method " + info.getMethodName(), arguments); //$NON-NLS-1$//$NON-NLS-2$
		return desc;
	}

	private Map<String, String> getArgumentMap() {
		Map<String, String> arguments = new HashMap<>();
		arguments.put(CRefactoringDescriptor.FILE_NAME, tu.getLocationURI().toString());
		arguments.put(CRefactoringDescriptor.SELECTION, selectedRegion.getOffset() + "," + selectedRegion.getLength()); //$NON-NLS-1$
		arguments.put(ExtractFunctionRefactoringDescriptor.NAME, info.getMethodName());
		arguments.put(ExtractFunctionRefactoringDescriptor.VISIBILITY, info.getVisibility().toString());
		arguments.put(ExtractFunctionRefactoringDescriptor.REPLACE_DUPLICATES,
				Boolean.toString(info.isReplaceDuplicates()));
		return arguments;
	}

	public ExtractFunctionInformation getRefactoringInfo() {
		return info;
	}

	/**
	 * Checks if the new method name is a valid method name. This method doesn't
	 * check if a method with the same name already exists in the hierarchy.
	 * @return validation status
	 */
	public RefactoringStatus checkMethodName() {
		return Checks.checkIdentifier(info.getMethodName(), tu);
	}

	/**
	 * Checks if the parameter names are valid.
	 * @return validation status
	 */
	public RefactoringStatus checkParameterNames() {
		RefactoringStatus result = new RefactoringStatus();
		List<NameInformation> parameters = info.getParameters();
		Set<String> usedNames = new HashSet<>();
		Set<IASTName> declarations = new HashSet<>();
		for (NameInformation nameInfo : container.getNames()) {
			IASTName declaration = nameInfo.getDeclarationName();
			if (declarations.add(declaration) && !parameters.contains(nameInfo)) {
				usedNames.add(String.valueOf(nameInfo.getName().getSimpleID()));
			}
		}
		for (NameInformation parameter : parameters) {
			result.merge(Checks.checkIdentifier(parameter.getNewName(), tu));
			for (NameInformation other : parameters) {
				if (parameter != other && other.getNewName().equals(parameter.getNewName())) {
					result.addError(NLS.bind(Messages.ExtractFunctionRefactoring_duplicate_parameter,
							BasicElementLabels.getCElementName(other.getNewName())));
					return result;
				}
			}
			if (parameter.isRenamed() && usedNames.contains(parameter.getNewName())) {
				result.addError(NLS.bind(Messages.ExtractFunctionRefactoring_parameter_name_in_use,
						BasicElementLabels.getCElementName(parameter.getNewName())));
				return result;
			}
		}
		return result;
	}

	/**
	 * Checks if the parameter names are valid.
	 * @return validation status
	 */
	public RefactoringStatus checkParameterAndReturnTypes() {
		RefactoringStatus result = new RefactoringStatus();
		for (NameInformation parameter : info.getParameters()) {
			String typeName = parameter.getTypeName();
			if (typeName == null || typeName.isEmpty()) {
				result.addError(NLS.bind(Messages.ExtractFunctionRefactoring_invalid_type,
						BasicElementLabels.getCElementName(parameter.getNewName())));
				return result;
			}
		}
		return result;
	}

	/**
	 * Returns the signature of the new method.
	 *
	 * @param methodName the method name used for the new method
	 * @return the signature of the extracted method
	 */
	public String getSignature(String methodName) {
		StringBuilder buf = new StringBuilder();
		NameInformation returnVariable = info.getReturnVariable();
		if (returnVariable != null) {
			String type = returnVariable.getReturnType();
			if (type != null) {
				buf.append(type);
			} else {
				buf.append("<unknown type>"); //$NON-NLS-1$
			}
		} else {
			buf.append("void"); //$NON-NLS-1$
		}
		buf.append(' ');
		buf.append(methodName);
		if (formattingOptions.insert_space_before_opening_paren_in_method_declaration)
			buf.append(' ');
		buf.append('(');
		List<NameInformation> parameters = info.getParameters();
		if (!parameters.isEmpty()) {
			if (formattingOptions.insert_space_after_opening_paren_in_method_declaration)
				buf.append(' ');
			boolean first = true;
			for (NameInformation parameter : parameters) {
				if (!first) {
					if (formattingOptions.insert_space_before_comma_in_method_declaration_parameters)
						buf.append(' ');
					buf.append(',');
					if (formattingOptions.insert_space_after_comma_in_method_declaration_parameters)
						buf.append(' ');
				}
				IASTParameterDeclaration declaration = parameter.getParameterDeclaration(nodeFactory);
				ASTWriterVisitor writer = new ASTWriterVisitor(parameter.getTranslationUnit());
				declaration.accept(writer);
				buf.append(writer.toString());
				first = false;
			}
			if (formattingOptions.insert_space_before_closing_paren_in_method_declaration)
				buf.append(' ');
		} else if (formattingOptions.insert_space_between_empty_parens_in_method_declaration) {
			buf.append(' ');
		}
		buf.append(')');
		return buf.toString();
	}
}
