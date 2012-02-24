/*******************************************************************************
 * Copyright (c) 2008, 2012 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *    Institute for Software - initial API and implementation
 *    Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractconstant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.text.edits.TextEditGroup;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansionLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.dom.rewrite.DeclarationGenerator;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;

import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTEqualsInitializer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPMethod;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringDescriptor;
import org.eclipse.cdt.internal.ui.refactoring.ClassMemberInserter;
import org.eclipse.cdt.internal.ui.refactoring.MethodContext;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.utils.NodeHelper;
import org.eclipse.cdt.internal.ui.util.NameComposer;

/**
 * The main class of the Extract Constant refactoring.
 * 
 * @author Mirko Stocker
 */
public class ExtractConstantRefactoring extends CRefactoring {
	public static final String ID =
			"org.eclipse.cdt.ui.refactoring.extractconstant.ExtractConstantRefactoring"; //$NON-NLS-1$

	private IASTLiteralExpression target;
	private final ExtractConstantInfo info;
	private final ArrayList<IASTExpression> literalsToReplace = new ArrayList<IASTExpression>();

	public ExtractConstantRefactoring(ICElement element, ISelection selection, ICProject project) {
		super(element, selection, project);
		this.info = new ExtractConstantInfo();
		name = Messages.ExtractConstantRefactoring_ExtractConst; 
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		SubMonitor sm = SubMonitor.convert(pm, 10);

		try {
			RefactoringStatus status = super.checkInitialConditions(sm.newChild(8));
			if (status.hasError()) {
				return status;
			}
	
			Collection<IASTLiteralExpression> literalExpressionCollection = findAllLiterals(sm.newChild(1));
			if (literalExpressionCollection.isEmpty()) {
				initStatus.addFatalError(Messages.ExtractConstantRefactoring_LiteralMustBeSelected); 
				return initStatus;
			}
	
			boolean oneMarked =
					selectedRegion != null && isOneMarked(literalExpressionCollection, selectedRegion);
			if (!oneMarked) { 
				// None or more than one literal selected
				if (target == null) {
					// No l found;
					initStatus.addFatalError(Messages.ExtractConstantRefactoring_NoLiteralSelected); 
				} else {
					// To many selection found
					initStatus.addFatalError(Messages.ExtractConstantRefactoring_TooManyLiteralSelected); 
				}
				return initStatus;
			}
	
			findAllNodesForReplacement(literalExpressionCollection);
	
			info.addNamesToUsedNames(findAllDeclaredNames());
			if (info.getName().isEmpty()) {
				info.setName(getDefaultName(target));
			}
			info.setMethodContext(NodeHelper.findMethodContext(target, refactoringContext, sm.newChild(1)));
			return initStatus;
		} finally {
			sm.done();
		}
	}

	private String getDefaultName(IASTLiteralExpression literal) {
		String nameString = literal.toString();
		switch (literal.getKind()) {
		case IASTLiteralExpression.lk_char_constant:
		case IASTLiteralExpression.lk_string_literal:
			int beginIndex = 1;
			if (nameString.startsWith("L")) {  //$NON-NLS-1$
				beginIndex = 2;
			}
			final int len= nameString.length();
			if (beginIndex < len && len > 0) {
				nameString = nameString.substring(beginIndex, len - 1);
			}
			break;

		default:
			break;
		}

    	IPreferencesService preferences = Platform.getPreferencesService();
    	int capitalization = preferences.getInt(CUIPlugin.PLUGIN_ID,
    			PreferenceConstants.NAME_STYLE_CONSTANT_CAPITALIZATION,
    			PreferenceConstants.NAME_STYLE_CAPITALIZATION_UPPER_CASE, null);
    	String wordDelimiter = preferences.getString(CUIPlugin.PLUGIN_ID,
    			PreferenceConstants.NAME_STYLE_CONSTANT_WORD_DELIMITER, "_", null); //$NON-NLS-1$
    	String prefix = preferences.getString(CUIPlugin.PLUGIN_ID,
						PreferenceConstants.NAME_STYLE_CONSTANT_PREFIX, "", null); //$NON-NLS-1$
    	String suffix = preferences.getString(CUIPlugin.PLUGIN_ID,
    			PreferenceConstants.NAME_STYLE_CONSTANT_SUFFIX, "", null); //$NON-NLS-1$
    	NameComposer composer = new NameComposer(capitalization, wordDelimiter, prefix, suffix);
    	return composer.compose(nameString);
	}

	private ArrayList<String> findAllDeclaredNames() {
		ArrayList<String>names = new ArrayList<String>();
		IASTFunctionDefinition funcDef = CPPVisitor.findAncestorWithType(target, IASTFunctionDefinition.class);
		ICPPASTCompositeTypeSpecifier comTypeSpec = getCompositeTypeSpecifier(funcDef);
		if (comTypeSpec != null) {
			for(IASTDeclaration dec : comTypeSpec.getMembers()) {
				if (dec instanceof IASTSimpleDeclaration) {
					IASTSimpleDeclaration simpDec = (IASTSimpleDeclaration) dec;
					for(IASTDeclarator decor : simpDec.getDeclarators()) {
						names.add(decor.getName().getRawSignature());
					}
				}
			}
		}
		return names;
	}

	private ICPPASTCompositeTypeSpecifier getCompositeTypeSpecifier(IASTFunctionDefinition funcDef) {
		if (funcDef != null) {
			IBinding binding = funcDef.getDeclarator().getName().resolveBinding();
			if (binding instanceof CPPMethod) {
				CPPMethod methode = (CPPMethod) binding;
				IASTNode[] declarations = methode.getDeclarations();

				IASTNode decl;
				if (declarations != null) {
					decl = declarations[0];
				} else {
					decl = methode.getDefinition();
				}

				IASTNode spec = decl.getParent().getParent();
				if (spec instanceof ICPPASTCompositeTypeSpecifier) {
					ICPPASTCompositeTypeSpecifier compTypeSpec = (ICPPASTCompositeTypeSpecifier) spec;
					return compTypeSpec;
				}
			}
		}
		return null;
	}

	private void findAllNodesForReplacement(Collection<IASTLiteralExpression> literalExpressionCollection) {
		if (target.getParent() instanceof IASTUnaryExpression) {
			IASTUnaryExpression unary = (IASTUnaryExpression) target.getParent();
			for (IASTLiteralExpression expression : literalExpressionCollection) {
				if (target.getKind() == expression.getKind()
						&& target.toString().equals(expression.toString()) 
						&& expression.getParent() instanceof IASTUnaryExpression
						&& unary.getOperator() == ((IASTUnaryExpression)expression.getParent()).getOperator()) {
					literalsToReplace.add(((IASTUnaryExpression)expression.getParent()));
				}
			}
		} else {
			for (IASTLiteralExpression expression : literalExpressionCollection) {
				if (target.getKind() == expression.getKind()
						&& target.toString().equals(expression.toString())) {
					literalsToReplace.add(expression);
				}
			}
		}
	}

	private boolean isOneMarked(Collection<IASTLiteralExpression> selectedNodes,
			Region textSelection) {
		boolean oneMarked = false;
		for (IASTLiteralExpression expression : selectedNodes) {
			if (expression.isPartOfTranslationUnitFile() &&
					isExpressionInSelection(expression, textSelection)) {
				if (target == null) {
					target = expression;
					oneMarked = true;
				} else if (!isTargetChild(expression)) {
					oneMarked = false;
				}
			}
		}
		return oneMarked;
	}

	private static boolean isExpressionInSelection(IASTExpression expression, Region selection) {
		IASTFileLocation location = expression.getFileLocation();
		int expressionStart = location.getNodeOffset();
		int expressionEnd = expressionStart + location.getNodeLength();
		int selectionStart = selection.getOffset();
		int selectionEnd = selectionStart + selection.getLength();
		return expressionStart >= selectionStart && expressionEnd <= selectionEnd;
	}

	private boolean isTargetChild(IASTExpression child) {
		if (target == null) {
			return false;
		}
		IASTNode node = child;
		while (node != null) {
			if (node.getParent() == target)
				return true;
			node = node.getParent();
		}
		return false;
	}

	private Collection<IASTLiteralExpression> findAllLiterals(IProgressMonitor pm)
			throws OperationCanceledException, CoreException {
		final Collection<IASTLiteralExpression> result = new ArrayList<IASTLiteralExpression>();

		IASTTranslationUnit ast = getAST(tu, pm);
		ast.accept(new ASTVisitor() {
			{
				shouldVisitExpressions = true;
			}

			@Override
			public int visit(IASTExpression expression) {
				if (expression instanceof IASTLiteralExpression) {
					if (!(expression.getNodeLocations().length == 1 &&
							expression.getNodeLocations()[0] instanceof IASTMacroExpansionLocation)) {
						IASTLiteralExpression literal = (IASTLiteralExpression) expression;
						result.add(literal);
					}
				}
				return super.visit(expression);
			}
		});

		return result;
	}

	@Override
	protected void collectModifications(IProgressMonitor pm, ModificationCollector collector)
			throws CoreException, OperationCanceledException{
		SubMonitor progress = SubMonitor.convert(pm, 10);
		MethodContext context = info.getMethodContext();
		Collection<IASTExpression> locLiteralsToReplace = new ArrayList<IASTExpression>();

		IASTTranslationUnit ast = getAST(tu, progress.newChild(9));
		if (context.getType() == MethodContext.ContextType.METHOD) {
			SubMonitor loopProgress = progress.newChild(1).setWorkRemaining(literalsToReplace.size());
			for (IASTExpression expression : literalsToReplace) {
				MethodContext exprContext =
						NodeHelper.findMethodContext(expression, refactoringContext, loopProgress.newChild(1));
				if (exprContext.getType() == MethodContext.ContextType.METHOD) {
					if (context.getMethodQName() != null) {
						if (MethodContext.isSameClass(exprContext.getMethodQName(), context.getMethodQName())) {
							locLiteralsToReplace.add(expression);
						}
					} else {
						if (MethodContext.isSameClass(exprContext.getMethodDeclarationName(),
								context.getMethodDeclarationName())) {
							locLiteralsToReplace.add(expression);
						}
					}
				}
			}
		} else {
			for (IASTExpression expression : literalsToReplace) {
				ITranslationUnit expressionTu = expression.getTranslationUnit().getOriginatingTranslationUnit();
				if (expressionTu.getResource() != null) {
					locLiteralsToReplace.add(expression);
				}
			}
		}

		// Create all changes for literals
		String constName = info.getName();
		createLiteralToConstantChanges(constName, locLiteralsToReplace, collector);

		if (context.getType() == MethodContext.ContextType.METHOD) {
			ICPPASTCompositeTypeSpecifier classDefinition =
					(ICPPASTCompositeTypeSpecifier) context.getMethodDeclaration().getParent();
			ClassMemberInserter.createChange(classDefinition, info.getVisibility(),
					getConstNodesClass(constName), true, collector);
		} else {
			IASTDeclaration nodes = getConstNodesGlobal(constName, ast.getASTNodeFactory());
			ASTRewrite rewriter = collector.rewriterForTranslationUnit(ast);
			rewriter.insertBefore(ast, getFirstNode(ast), nodes,
					new TextEditGroup(Messages.ExtractConstantRefactoring_CreateConstant));
		}
	}

	/**
	 * @return the first node in the translation unit or null
	 */
	private static IASTNode getFirstNode(IASTTranslationUnit ast) {
		IASTDeclaration firstNode = null;
		for (IASTDeclaration each : ast.getDeclarations()) {
			if (firstNode == null) {
				firstNode = each;
			} else if (each.getNodeLocations() != null && 
					each.getNodeLocations()[0].getNodeOffset() < firstNode.getNodeLocations()[0].getNodeOffset()) {
				firstNode = each;
			}
		}
		return firstNode;
	}

	@Override
	protected RefactoringDescriptor getRefactoringDescriptor() {
		Map<String, String> arguments = getArgumentMap();
		RefactoringDescriptor desc = new ExtractConstantRefactoringDescriptor(project.getProject().getName(),
				"Extract Constant Refactoring", "Create constant for " + target.getRawSignature(), //$NON-NLS-1$ //$NON-NLS-2$
				arguments);
		return desc;
	}

	private Map<String, String> getArgumentMap() {
		Map<String, String> arguments = new HashMap<String, String>();
		arguments.put(CRefactoringDescriptor.FILE_NAME, tu.getLocationURI().toString());
		arguments.put(CRefactoringDescriptor.SELECTION, selectedRegion.getOffset() + "," + selectedRegion.getLength()); //$NON-NLS-1$
		arguments.put(ExtractConstantRefactoringDescriptor.NAME, info.getName());
		arguments.put(ExtractConstantRefactoringDescriptor.VISIBILITY, info.getVisibility().toString());
		return arguments;
	}

	private void createLiteralToConstantChanges(String constName,
			Iterable<? extends IASTExpression> literals, ModificationCollector collector) {
		for (IASTExpression each : literals) {
			ASTRewrite rewrite = collector.rewriterForTranslationUnit(each.getTranslationUnit());
			CPPASTIdExpression idExpression =
					new CPPASTIdExpression(new CPPASTName(constName.toCharArray()));
			rewrite.replace(each, idExpression,
					new TextEditGroup(Messages.ExtractConstantRefactoring_ReplaceLiteral));
		}
	}

	private IASTSimpleDeclaration getConstNodes(String newName) {
		ICPPNodeFactory factory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
		DeclarationGenerator generator = DeclarationGenerator.create(factory);

		IType type = target.getExpressionType();

		IASTDeclSpecifier declSpec = generator.createDeclSpecFromType(type);
		declSpec.setConst(true);

		IASTDeclarator declarator = generator.createDeclaratorFromType(type, newName.toCharArray());

		IASTSimpleDeclaration simple = new CPPASTSimpleDeclaration();
		simple.setDeclSpecifier(declSpec);

		IASTEqualsInitializer init = new CPPASTEqualsInitializer(); 
		if (target.getParent() instanceof IASTUnaryExpression) {
			IASTUnaryExpression unary = (IASTUnaryExpression) target.getParent();
			init.setInitializerClause(unary);
		} else {
			CPPASTLiteralExpression expression =
					new CPPASTLiteralExpression(target.getKind(), target.getValue());
			init.setInitializerClause(expression);
		}
		declarator.setInitializer(init);
		simple.addDeclarator(declarator);

		return simple;
	}

	private IASTDeclaration getConstNodesGlobal(String newName, INodeFactory nodeFactory) {
		IASTSimpleDeclaration simple = getConstNodes(newName);

		if (nodeFactory instanceof ICPPNodeFactory) {
			ICPPASTNamespaceDefinition namespace =
					((ICPPNodeFactory) nodeFactory).newNamespaceDefinition(new CPPASTName());
			namespace.addDeclaration(simple);
			return namespace;
		}

		simple.getDeclSpecifier().setStorageClass(IASTDeclSpecifier.sc_static);
		return simple;
	}

	private IASTDeclaration getConstNodesClass(String newName) {
		IASTSimpleDeclaration simple = getConstNodes(newName);
		simple.getDeclSpecifier().setStorageClass(IASTDeclSpecifier.sc_static);
		return simple;
	}

	public ExtractConstantInfo getRefactoringInfo() {
		return info;
	}
}
