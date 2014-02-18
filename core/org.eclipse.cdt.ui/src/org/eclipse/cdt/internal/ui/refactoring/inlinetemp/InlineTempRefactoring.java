/*******************************************************************************
 * Copyright (c) 2013 Simon Taddiken
 * University of Bremen.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Simon Taddiken (University of Bremen)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.inlinetemp;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.text.edits.TextEditGroup;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;

import org.eclipse.cdt.internal.core.dom.parser.c.CASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionCallExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPQualifierType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVariableReadWriteFlags;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringDescriptor;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.inlinetemp.InlineTempSettings.SelectionType;
import org.eclipse.cdt.internal.ui.refactoring.rename.ASTManager;

public class InlineTempRefactoring extends CRefactoring {
	
	public static final String ID =
			"org.eclipse.cdt.internal.ui.refactoring.inlinetemp.InlineTempRefactoring"; //$NON-NLS-1$
	
	private InlineTempSettings settings;
	

	public InlineTempRefactoring(ICElement element, ISelection selection, 
			ICProject project) {
		super(element, selection, project);
		this.settings = new InlineTempSettings();
	}
	
	

	@Override
	protected RefactoringDescriptor getRefactoringDescriptor() {
		Map<String, String> arguments = getArgumentMap();
		RefactoringDescriptor desc = new InlineTempRefactoringDescriptor(
				project.getProject().getName(),
				"Inline Temp Refactoring",  //$NON-NLS-1$
				"Inline " + this.settings.getDeclarator().getRawSignature(),  //$NON-NLS-1$
				arguments);
		return desc;
	}

	
	
	private Map<String, String> getArgumentMap() {
		Map<String, String> arguments = new HashMap<String, String>();
		arguments.put(CRefactoringDescriptor.FILE_NAME, tu.getLocationURI().toString());
		arguments.put(CRefactoringDescriptor.SELECTION, 
				selectedRegion.getOffset() + "," + selectedRegion.getLength()); //$NON-NLS-1$
		return arguments;
	}
	
	
	
	public InlineTempSettings getSettings() {
		return this.settings;
	}
	
	
	
	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		final RefactoringStatus status = super.checkInitialConditions(pm);
		if (status.hasError()) {
			return status;
		}
		
		if (this.selectedRegion == null) {
			this.initStatus.addError(Messages.InlineTemp_invalidSelection);
			return this.initStatus;
		}
		
		final IASTTranslationUnit ast = this.getAST(this.tu, pm);
		this.examineSelection(ast, this.settings);
		
		
		boolean constant = false;
		if (this.settings.getSelectionType() == SelectionType.INVALID || 
				!(this.settings.getSelected() instanceof IVariable)) {
			this.initStatus.addFatalError(Messages.InlineTemp_invalidSelection);
			return this.initStatus;
		} else {
			final IVariable var = (IVariable) this.settings.getSelected();
			final IType type = var.getType();
			
			if (!this.isLocalVariabl(var)) {
				this.initStatus.addFatalError(Messages.InlineTemp_noLocalVar);
			}
			
			if (var.getInitialValue() == null) {
				this.initStatus.addFatalError(Messages.InlineTemp_noInitializer);
				return this.initStatus;
			}
			
			constant = 
					type instanceof IPointerType   && ((IPointerType) type).isConst() ||
					type instanceof IQualifierType && ((IQualifierType) type).isConst();
			
		}
		
		final IASTDeclarator declarator = this.findDeclaratorForBinding(ast, 
				this.settings.getSelected());
		
		if (declarator == null) {
			this.initStatus.addFatalError(Messages.InlineTemp_invalidSelection);
			return this.initStatus;
		}
		
		this.settings.setDeclarator(declarator);
		this.settings.setReferences(this.findReferences(ast, this.settings.getSelected()));
		
		if (!constant && this.containsWriteAccess(this.settings.getReferences())) {
			status.addError(Messages.InlineTemp_variableIsModified);
		} else if (!constant && !this.settings.getReferences().isEmpty()) {
			status.addWarning(Messages.InlineTemp_noReadOnlyGuarantee);
		}
		if (this.settings.getReferences().isEmpty() && 
				this.settings.getSelectionType() != SelectionType.USAGE) {
			status.addWarning(Messages.InlineTemp_constantNotUsed);
		}
		return this.initStatus;
	}
	
	
	
	@Override
	protected RefactoringStatus checkFinalConditions(IProgressMonitor subProgressMonitor,
			CheckConditionsContext checkContext) throws CoreException, OperationCanceledException {
		final RefactoringStatus status = super.checkFinalConditions(
				subProgressMonitor, checkContext);
		
		if (this.settings.getSelectionType() == SelectionType.DECLARATION && 
				!this.settings.isInlineAll()) {
			status.addWarning(Messages.InlineTemp_warningInlineAll);
		}
		
		if (this.settings.isRemoveDeclaration() && !this.settings.isInlineAll() && 
				this.settings.getReferences().size() > 1) {
			status.addError(Messages.InlineTemp_cantRemoveDeclarator);
		}
		return status;
	}
	
	
	
	
	@Override
	protected void collectModifications(IProgressMonitor pm, ModificationCollector collector)
			throws CoreException, OperationCanceledException {
		
		final IASTNode initializer = getInitializer(this.settings.getDeclarator());
		final IASTTranslationUnit ast = this.getAST(this.tu, pm);
		final ASTRewrite rw = collector.rewriterForTranslationUnit(ast);
		final TextEditGroup group = new TextEditGroup(""); //$NON-NLS-1$
		
		if (this.settings.getSelectionType() == SelectionType.USAGE && !settings.isInlineAll()) {
			// only inline selected usage
			this.replaceUsage(this.settings.getSelectedNode(), initializer, rw, 
					group);
		} else {
			// inline every occurrence
			for (final IASTName usage : this.settings.getReferences()) {
				this.replaceUsage(usage, initializer, rw, group);
			}
		}
		
		if (this.settings.isRemoveDeclaration()) {
			this.removeDeclaration(this.settings.getDeclarator(), rw, group);
		}
	}
	
	
	
	
	/**
	 * Determines whether parenthesis must be added around inlined expression
	 * 
	 * @param targetExpression Expression where the inlined expression is inserted.
	 * @param expressionToInsert The expression to inline.
	 * @return Whether parenthesis must be added when inlining.
	 */
	private boolean mustInsertParenthesis(IASTNode targetExpression, IASTNode expressionToInsert) {
		// TODO: replace with real intelligent algorithm which takes operator precedence
		//		 into account.
		final int sourceOps = ExpressionOperatorCounter.count(expressionToInsert);
		final int targetOps = ExpressionOperatorCounter.count(targetExpression);
		
		// if either expressions does not contain any operator, no parenthesis needed
		return targetOps != 0 && sourceOps != 0 &&
				!(expressionToInsert instanceof IASTFunctionCallExpression); 
	}
	
	
	
	/**
	 * Replaces a single usage with the expression to inline. 
	 * @param usage The usage of the variable to inline.
	 * @param replacement The node to insert here.
	 * @param rw Rewrite instance to perform the change.
	 * @param group Group for performing the change.
	 */
	private void replaceUsage(IASTName usage, IASTNode replacement, ASTRewrite rw, 
			TextEditGroup group) {
		final boolean addParenthesis = this.settings.isAlwaysAddParenthesis() ||
				this.mustInsertParenthesis(usage.getParent(), replacement);
		
		if (addParenthesis) {
			final IASTNode parenthesized = 
					ASTNodeFactoryFactory.getDefaultCPPNodeFactory().newUnaryExpression(
					IASTUnaryExpression.op_bracketedPrimary, 
					(IASTExpression) replacement.copy());
			
			rw.replace(usage, parenthesized, group);
		} else {
			rw.replace(usage, replacement, group);
		}
	}
	
	
	
	private void removeDeclaration(IASTDeclarator declarator, ASTRewrite rw, 
			TextEditGroup group) {
		final IASTSimpleDeclaration decl = (IASTSimpleDeclaration) declarator.getParent();
		if (decl.getDeclarators().length == 1) {
			rw.remove(decl, group);
		} else {
			final IASTDeclaration replacement = this.createReplacement(decl, declarator);
			rw.replace(decl, replacement, group);
		}
	}
	
	
	
	/**
	 * Recreates a simple declaration from a given one, but leaves out the specified
	 * declarator.
	 * 
	 * @param decl The declaration to copy.
	 * @param remove The declarator that will not be contained within the result.
	 * @return A new declaration node.
	 */
	private final IASTDeclaration createReplacement(
			IASTSimpleDeclaration decl, IASTDeclarator remove) {
		final IASTSimpleDeclaration simple = 
				ASTNodeFactoryFactory.getDefaultCPPNodeFactory().newSimpleDeclaration(
						decl.getDeclSpecifier().copy());
		for (final IASTDeclarator declarator : decl.getDeclarators()) {
			if (declarator != remove) {
				simple.addDeclarator(declarator.copy());
			}
		}
		return simple;
	}
	
	
	
	/**
	 * Extracts the initializer expression from the given declarator. For simple 
	 * initializers a la <code>type name = init</code>, the init part will be returned as
	 * expression. For C++ constructor initializers a la 
	 * <code>type name(param, param)</code>, a corresponding constructor call will be 
	 * created. In the above case this would be <code>type(param, param)</code>. Special
	 * care is taken for pointer types.
	 * 
	 * @param declarator The declarator for which the initializer part is extracted. It 
	 * 		is assumed to always exists.
	 * @return The extracted (and optionally transformed) initializer expression.
	 */
	public static IASTInitializerClause getInitializer(IASTDeclarator declarator) {
		final IASTInitializer init = declarator.getInitializer();
		
		if (init instanceof IASTEqualsInitializer) {
			final IASTEqualsInitializer eqinit = (IASTEqualsInitializer) init;
			return eqinit.getInitializerClause();
		} else if (init instanceof ICPPASTConstructorInitializer) {
			// transform constructor call from
			// type name(param1, param2) 
			// to
			// type(param1, param2)
			final ICPPASTConstructorInitializer coninit = (ICPPASTConstructorInitializer) init;
			
			// as by precondition checks, following cast is safe
			final IVariable binding = (IVariable) declarator.getName().getBinding();
			
			// if the type is a pointer, we extract its wrapped type
			IType inlineType = binding.getType();
			if (inlineType instanceof IQualifierType) {
				// remove qualification
				IQualifierType type = (IQualifierType) inlineType;
				inlineType = new CPPQualifierType(type.getType(), false, false);
			}
			
			final String typeName = ASTTypeUtil.getType(inlineType, false);
			final IASTName astTypeName = new CASTName(typeName.toCharArray());
			
			final IASTIdExpression name = new CPPASTIdExpression(astTypeName);
			final IASTInitializerClause[] args = new IASTInitializerClause[coninit.getArguments().length];
			for (int i = 0; i < args.length; ++i) {
				args[i] = coninit.getArguments()[i].copy();
			}
			final ICPPASTFunctionCallExpression result = 
					new CPPASTFunctionCallExpression(name, args);
			
			return result;
		}
		return null;
	}
	
	
	
	/**
	 * Tries to figure out whether any of the occurrences in the provided list is 
	 * subject to a typical modification operator. Those are the assignment op as well
	 * as pre/post increment/decrement. Checks whether any names occurs as an argument 
	 * to a reference parameter is missing.
	 * 
	 * @param occurrences Array of occurrences to check.
	 * @return 
	 * @throws CoreException 
	 * @throws OperationCanceledException 
	 */
	private boolean containsWriteAccess(Collection<IASTName> occurrences) 
			throws OperationCanceledException, CoreException {
		for (final IASTName usage : occurrences) {
			if (!CPPVariableReadWriteFlags.isReadOnly(usage)) {
				return true;
			}
		}
		return false;
	}

	
	
	private IASTDeclarator findDeclaratorForBinding(IASTTranslationUnit ast,  
			IBinding binding) {
		final IASTName[] names = ast.getDefinitionsInAST(binding);
		if (names.length != 1) {
			return null;
		}
		final IASTDeclarator declarator = CPPVisitor.findAncestorWithType(names[0], 
				IASTDeclarator.class);
		
		return declarator;
	}
	
	
	
	private Collection<IASTName> findReferences(IASTTranslationUnit ast, IBinding binding) {
		final IASTName[] references = ast.getReferences(binding);
		if (references == null) {
			return Collections.emptyList();
		}
		return Arrays.asList(references);
	}
	
	
	
	private void examineSelection(IASTTranslationUnit ast, InlineTempSettings settings) {
		final IASTNodeSelector selector = ast.getNodeSelector(null);
		final IASTNode node = selector.findEnclosingNode(this.selectedRegion.getOffset(), 
				this.selectedRegion.getLength());
		
		settings.setSelectionType(SelectionType.INVALID);
		if (node == null) {
			return;
		} else if (!(node instanceof IASTName)) {
			final IASTDeclarator parent = CPPVisitor.findAncestorWithType(node, 
					IASTDeclarator.class);
			
			if (parent != null) {
				settings.setSelected(parent.getName().resolveBinding());
				settings.setSelectedNode(parent.getName());
				settings.setInlineAll(true);
				settings.setSelectionType(SelectionType.DECLARATION);
				return;
			}
			
		} else { // if (node instanceof IASTName) {
			final IASTName name = (IASTName) node;
			final IBinding binding = name.resolveBinding();

			settings.setSelectedNode(name);
			settings.setSelected(binding);
			if (name.getParent() instanceof IASTDeclarator) {
				settings.setSelectionType(SelectionType.DECLARATION);
				settings.setInlineAll(true);
			} else if (name.getParent() instanceof IASTIdExpression) {
				settings.setSelectionType(SelectionType.USAGE);
			}
		}
	}
	
	
	
	/**
	 * Synonym for same method in ASTManager of Rename Refactoring. 
	 * @param v
	 * @return
	 */
	private boolean isLocalVariabl(IVariable v) {
		return ASTManager.isLocalVariable(v);
	}
}
