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
package org.eclipse.cdt.internal.ui.refactoring.pullup.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;
import org.eclipse.osgi.util.NLS;
import org.eclipse.text.edits.TextEditGroup;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.index.IIndex;

import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.pullup.InsertionPoint;
import org.eclipse.cdt.internal.ui.refactoring.pullup.InsertionPoint.InsertType;
import org.eclipse.cdt.internal.ui.refactoring.pullup.Messages;
import org.eclipse.cdt.internal.ui.refactoring.pullup.PullUpHelper;


/**
 * Holds the logic for moving a single method from one class to another. This action can
 * only be executed within a {@link MoveActionGroup} because it requires some post 
 * processing when being run together with further actions.
 * 
 * <p>The tasks executed by this action are split into many functions which perform 
 * required sub tasks. This enables sub classes to specialize only a certain aspect
 * of moving a method.</p>
 * 
 * @author Simon Taddiken
 */
class MoveMethodAction extends MoveMemberAction {
	
	/** Declarator of the function to move if any */
	protected final IASTFunctionDeclarator declarator;
	
	/** Definition of the function to move if any */
	protected final IASTFunctionDefinition definition;
	
	/** 
	 * Target insertion point of the definition, calculated during 
	 * {@link #run(ModificationCollector, TextEditGroup, IProgressMonitor)}. This point
	 * is still valid if no definition is currently present. 
	 */
	protected InsertionPoint definitionInsertPoint;
	
	/** 
	 * Target insertion point of the declaration, calculated during 
	 * {@link #run(ModificationCollector, TextEditGroup, IProgressMonitor)}. This point
	 * is still valid if no declaration is currently present. 
	 */
	protected InsertionPoint declarationInsertPoint;
	
	
	
	/**
	 * Creates a new MoveMethodAction.
	 * 
	 * @param group The group in which this action is being executed.
	 * @param context The refactoring context.
	 * @param member Binding of the member to move
	 * @param target Binding of the target class
	 * @param visibility Target visibility
	 */
	public MoveMethodAction(MoveActionGroup group, CRefactoringContext context, 
			ICPPMember member, ICPPClassType target, int visibility) {
		super(group, context, member, target, visibility);
		
		this.declarator = PullUpHelper.findFunctionDeclarator(this.index, context, member);
		this.definition = PullUpHelper.findFunctionDefinition(this.index, context, member);
	}
	
	
	
	/**
	 * Given the type of expression to insert and the information about he source and 
	 * target stored in this class' attributes, this method calculates a suitable
	 * {@link InsertionPoint} which can then be used to insert a node at the 
	 * target.
	 * @param type Type of the node to insert
	 * @return The insertion point
	 */
	protected InsertionPoint calculateInsertion(InsertType type) {
		return InsertionPoint.calculate(this.context, this.group.labels, this.visibility, 
				this.declarator, this.definition, this.targetClass, type);
	}
	
	
	
	@Override
	public boolean isPossible(RefactoringStatus status, IProgressMonitor pm) {
		// either definition or declaration might be null
		if (!checkDeclarationIsValid(status)) {
			return false;
		}
		
		final ICPPMethod method = (ICPPMethod) this.member;
		final SubMonitor sm = SubMonitor.convert(pm, this.target.getMethods().length);
		
		try {
			for (final ICPPMethod mtd : this.target.getDeclaredMethods()) {
				if (mtd.getName().equals(method.getName())) {
					if (PullUpHelper.checkContains(this.target, mtd)) {
						
						final IASTName name = PullUpHelper.findName(this.index, 
								this.context, mtd, IIndex.FIND_DECLARATIONS);
						final RefactoringStatusContext c = this.getStatusContext(name);
						status.addError(
								NLS.bind(Messages.PullUpRefactoring_declarationExists, 
								mtd.getName()), c);
						return false;
					}
				}
				sm.worked(1);
			}
		} finally {
			sm.done();
		}
		return true;
	}



	/**
	 * Checks whether either a declaration or a definition could be resolved. If both 
	 * could not be resolved, an error is added to the refactoring status.
	 * 
	 * @param status The status to fill with an error if necessary.
	 * @return <code>true</code> if either declaration or definition is not 
	 * 		<code>null</code>
	 */
	protected boolean checkDeclarationIsValid(RefactoringStatus status) {
		if (this.definition == null && this.declarator == null) {
			status.addFatalError(NLS.bind(
					Messages.PullUpRefactoring_canUniquelyResolveDeclaration, 
					PullUpHelper.getMemberString(this.member)));
			return false;
		}
		return true;
	}
	
	
	
	@Override
	public void run(ModificationCollector mc, TextEditGroup editGroup, IProgressMonitor pm) 
			throws CoreException {
		this.declarationInsertPoint = this.calculateInsertion(InsertType.DECLARATION);
		this.definitionInsertPoint = this.calculateInsertion(InsertType.DEFINITION);
		
		this.runForDefinition(this.definition, mc, editGroup);
		this.runForDeclaration(this.declarator, mc, editGroup);
	}
	
	
	
	/**
	 * Performs the required changes for the member's definition within its current class.
	 * I.e. it removes the definition if its exists.
	 * 
	 * @param def The definition. May be <code>null</code> if none exists.
	 * @param mc Provides {@link ASTRewrite} instances to perform the required changes.
	 * @param editGroup EditGroup in which the changes are stored.
	 * @throws CoreException
	 */
	protected void runSourceChangeForDefinition(IASTFunctionDefinition def, 
			ModificationCollector mc, TextEditGroup editGroup) throws CoreException {
		
		if (def == null) {
			return;
		}
		
		// perform source changes
		final ASTRewrite rw = mc.rewriterForTranslationUnit(def.getTranslationUnit());
		final IASTNode remove = PullUpHelper.findRemovePoint(def);
		this.group.performRemove(remove, rw, editGroup);
	}
	
	
	
	/**
	 * Performs the required changes for the member's definition within the target class.
	 * I.e. the correct target file (header or cpp) is determined and the definition
	 * is inserted at proper location (by obeying the target visibility).
	 * 
	 * @param def The definition. May be <code>null</code> if none exists.
	 * @param mc Provides {@link ASTRewrite} instances to perform the required changes.
	 * @param editGroup EditGroup in which the changes are stored.
	 * @throws CoreException
	 */
	protected void runTargetChangeForDefinition(IASTFunctionDefinition def, 
			ModificationCollector mc, TextEditGroup editGroup) throws CoreException {
		
		if (def == null) {
			return;
		}
		final InsertionPoint insertion = this.definitionInsertPoint;
		
		// If declaration will be created, the declaration gets the leading comments
		final IASTNode commentSource = insertion.mustTransformDefinitionToDeclaration() 
				? null
				: def;
		
		// perform target changes
		final IASTNode copy = this.copyDefinitionFor(
				this.sourceClass,
				this.targetClass,
				insertion,
				false,
				def);
		insertion.perform(mc, copy, commentSource, editGroup);
	}
	
	
	
	/**
	 * Runs {@link #runSourceChangeForDefinition(IASTFunctionDefinition, ModificationCollector, TextEditGroup)}
	 * and 
	 * {@link #runTargetChangeForDefinition(IASTFunctionDefinition, ModificationCollector, TextEditGroup)}
	 * 
	 * @param def The definition. May be <code>null</code> if none exists.
	 * @param mc Provides {@link ASTRewrite} instances to perform the required changes.
	 * @param editGroup EditGroup in which the changes are stored.
	 * @throws CoreException
	 */
	protected void runForDefinition(IASTFunctionDefinition def, ModificationCollector mc, 
			TextEditGroup editGroup) throws CoreException {

		this.runSourceChangeForDefinition(def, mc, editGroup);
		this.runTargetChangeForDefinition(def, mc, editGroup);
	}
	
	
	
	protected void runSourceChangeForDeclaration(IASTFunctionDeclarator decl, 
			ModificationCollector mc, TextEditGroup editGroup) throws CoreException {
		
		if (decl == null) {
			return;
		}
		
		// perform source changes
		final ASTRewrite rw = mc.rewriterForTranslationUnit(decl.getTranslationUnit());
		final IASTDeclaration declaration = CPPVisitor.findAncestorWithType(
				decl, IASTDeclaration.class);
		
		final IASTNode remove = PullUpHelper.findRemovePoint(declaration);
		this.group.performRemove(remove, rw, editGroup);
	}
	
	
	
	protected void runTargetChangeForDeclaration(IASTFunctionDeclarator decl, 
			ModificationCollector mc, TextEditGroup editGroup) throws CoreException {
		
		IASTNode commentSource = null;
		if (this.definitionInsertPoint.mustTransformDefinitionToDeclaration()) {
			assert decl == null;
			assert this.definition != null;
			IASTSimpleDeclaration decla = this.definitionToDeclaration(this.definition); 
			decl = (IASTFunctionDeclarator) decla.getDeclarators()[0];
			
			commentSource = this.definition;
		} else if (decl != null){
			// find outermost template declaration
			commentSource = PullUpHelper.findRemovePoint(decl.getParent());
		} else {
			return;
		}
		
		// perform target changes
		final IASTDeclaration declaration = this.copyDeclarationFor(
				this.targetClass, decl, false);
		final InsertionPoint insertion = this.declarationInsertPoint;
		insertion.perform(mc, declaration, commentSource, editGroup);
	}
	
	
	
	protected void runForDeclaration(IASTFunctionDeclarator decl, ModificationCollector mc, 
			TextEditGroup editGroup) throws CoreException {
		this.runSourceChangeForDeclaration(decl, mc, editGroup);
		this.runTargetChangeForDeclaration(decl, mc, editGroup);
	}
	
	
	
	
	protected IASTDeclaration copyDeclarationFor(IASTCompositeTypeSpecifier target,
			IASTFunctionDeclarator decl, boolean makePureVirtual) {
		final IASTSimpleDeclaration simpleDec = CPPVisitor.findAncestorWithType(
				decl, IASTSimpleDeclaration.class);
		final IASTSimpleDeclaration copy = simpleDec.copy();
		
		if (makePureVirtual) {
			// make declaration pure virtual
			final IASTFunctionDeclarator declarator = 
					(IASTFunctionDeclarator) copy.getDeclarators()[0];
			final IASTEqualsInitializer init = nodeFactory().newEqualsInitializer(
					nodeFactory().newLiteralExpression(
					IASTLiteralExpression.lk_integer_constant, "0")); //$NON-NLS-1$
			declarator.setInitializer(init);
			final ICPPASTDeclSpecifier spec = (ICPPASTDeclSpecifier) copy.getDeclSpecifier();
			spec.setVirtual(true);
		}
		
		return PullUpHelper.getTemplateDeclaration(simpleDec, copy);
	}
	
	
	
	/**
	 * Copies a definition and inserts the qualified name of the target class if required.
	 * @param source The source class.
	 * @param target The target class.
	 * @param insertion The point at which the definition will be inserted
	 * @param insertVirtual If the resulting definition will be declared 'virtual'
	 * @param definition The definition to copy.
	 * @return Suitable copy for inserting in the target class.
	 */
	protected IASTDeclaration copyDefinitionFor(
			IASTCompositeTypeSpecifier source,
			IASTCompositeTypeSpecifier target,
			InsertionPoint insertion,
			boolean insertVirtual,
			IASTFunctionDefinition definition) {
		
		final IASTFunctionDefinition copy = nodeFactory().newFunctionDefinition(
				definition.getDeclSpecifier().copy(), 
				definition.getDeclarator().copy(), 
				definition.getBody().copy(CopyStyle.withLocations));
		
		final IASTName current = copy.getDeclarator().getName();
		final IASTName name = PullUpHelper.prepareNameForTarget(current, 
				target, insertion);
		copy.getDeclarator().setName(name);
		
		if (insertVirtual) {
			if (copy.getDeclSpecifier() instanceof ICPPASTDeclSpecifier) {
				ICPPASTDeclSpecifier spec = (ICPPASTDeclSpecifier) copy.getDeclSpecifier();
				spec.setVirtual(true);
			}
		}
		
		return PullUpHelper.getTemplateDeclaration(definition, copy);
	}
	
	
	
	protected IASTSimpleDeclaration definitionToDeclaration(IASTFunctionDefinition def, 
			boolean virtual, boolean abstr) {
		
		final ICPPASTDeclSpecifier declSpec = 
				(ICPPASTDeclSpecifier) def.getDeclSpecifier().copy(CopyStyle.withLocations);
		if (virtual) {
			declSpec.setVirtual(true);
		}
		
		final IASTSimpleDeclaration decl = nodeFactory().newSimpleDeclaration(declSpec);
		final IASTFunctionDeclarator declarator = def.getDeclarator().copy(CopyStyle.withLocations);
		if (abstr) {
			final IASTEqualsInitializer init = nodeFactory().newEqualsInitializer(
					nodeFactory().newLiteralExpression(
							IASTLiteralExpression.lk_integer_constant, "0")); //$NON-NLS-1$
			declarator.setInitializer(init);
		}
		decl.addDeclarator(declarator);
		return decl;
	}

	
	
	protected IASTSimpleDeclaration definitionToDeclaration(IASTFunctionDefinition def) {
		return this.definitionToDeclaration(def, false, false);
	}
	
	
	
	protected IASTSimpleDeclaration definitionToAbstractDeclaration(
			IASTFunctionDefinition def) {
		return this.definitionToDeclaration(def, true, true);
	}
}
