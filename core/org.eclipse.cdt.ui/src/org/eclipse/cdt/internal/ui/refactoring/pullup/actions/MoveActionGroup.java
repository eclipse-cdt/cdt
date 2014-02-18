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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.text.edits.TextEditGroup;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.index.IIndex;

import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.pullup.Information;
import org.eclipse.cdt.internal.ui.refactoring.pullup.ui.MemberTableEntry;

/**
 * Groups the execution of a list of {@link MoveAction MoveActions}. When running this
 * action, all child actions are run. When this task is completed, some post processing of 
 * the children's results is done.
 * 
 * @author Simon Taddiken
 */
public class MoveActionGroup implements MoveAction {
	
	/** Actions to be executed within this group */
	protected final List<MoveAction> actions;
	
	/** 
	 * Maps visibilities to their nodes in target class. The nodes may not be part
	 * of the target class' AST because they may have been created during execution of
	 * any MoveAction
	 */
	protected final Map<Integer, ICPPASTVisibilityLabel> labels;
	
	/** 
	 * List of declarators to be removed after all child MoveField actions have 
	 * been executed 
	 */
	protected final List<IASTDeclarator> removeLater;
	
	/** Current refactoring settings */
	protected final Information<? extends MemberTableEntry> information;
	
	/** 
	 * Holds a collection of nodes that have already been removed from one source during
	 * one pass of executing the modifications
	 */
	protected final Collection<IASTNode> removed;
	

	
	/**
	 * Creates a new MoveActionGroup
	 * @param information Current refactoring information.
	 */
	public MoveActionGroup(Information<? extends MemberTableEntry> information) {
		this.information = information;
		this.actions = new ArrayList<MoveAction>();
		this.labels = new HashMap<Integer, ICPPASTVisibilityLabel>();
		this.removeLater = new ArrayList<IASTDeclarator>();
		this.removed = new HashSet<IASTNode>();
	}
	
	
	
	/**
	 * Convenience method for accessing the index.
	 * @return A reference to the index.
	 */
	public IIndex getIndex() {
		return this.information.getIndex();
	}
	
	
	
	/**
	 * Removes the provided node if it has not been removed yet. 
	 * @param node The node to remove.
	 * @param rewrite The rewriter instance to perform the removal
	 * @param editGroup Editgroup for text changes
	 */
	public void performRemove(IASTNode node, ASTRewrite rewrite, TextEditGroup editGroup) {
		if (this.removed.add(node)) {
			rewrite.remove(node, editGroup);
		}
	}
	
	
	
	/**
	 * Adds an action to be executed within this group.
	 * 
	 * @param action The action
	 */
	public void addAction(MoveAction action) {
		this.actions.add(action);
	}
	
	
	
	/**
	 * Calls {@link #isPossible(RefactoringStatus, IProgressMonitor)} for each action in 
	 * this group. Even if one child action is not possible, checks for all other
	 * actions are performed.
	 * 
	 * @return <code>true</code> iff <tt>isPossible()</tt> for all child actions return 
	 * 			<code>true</code>.
	 */
	@Override
	public boolean isPossible(RefactoringStatus status, IProgressMonitor pm) {
		boolean result = true;
		final SubMonitor sm = SubMonitor.convert(pm, this.actions.size());
		
		try {
			for (final MoveAction action : this.actions) {
				result &= action.isPossible(status, sm);
				sm.worked(1);
			}
			return result;
		} finally {
			sm.done();
		}
	}
	
	
	
	@Override
	public void run(ModificationCollector mc, TextEditGroup editGroup, IProgressMonitor pm) 
			throws CoreException {
		
		final SubMonitor sm = SubMonitor.convert(pm, this.actions.size());
		
		// run all actions
		for (final MoveAction action : this.actions) {
			if (sm.isCanceled()) {
				throw new OperationCanceledException();
			}
			action.run(mc, editGroup, sm);
			sm.worked(1);
		}
		
		// Remove attribute declarators or declarations
		// group declarators to be removed by their declarations
		final Map<IASTSimpleDeclaration, Collection<IASTDeclarator>> map = 
				new HashMap<IASTSimpleDeclaration, Collection<IASTDeclarator>>();
		for (final IASTDeclarator declarator : this.removeLater) {
			if (sm.isCanceled()) {
				throw new OperationCanceledException();
			}
			final IASTSimpleDeclaration decl = (IASTSimpleDeclaration) declarator.getParent();
			Collection<IASTDeclarator> c = map.get(decl);
			if (c == null) {
				c = new HashSet<IASTDeclarator>();
				map.put(decl, c);
			}
			c.add(declarator);
		}
		
		
		final ICPPNodeFactory nf = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
		for (final Entry<IASTSimpleDeclaration, Collection<IASTDeclarator>> e : map.entrySet()) {
			if (sm.isCanceled()) {
				throw new OperationCanceledException();
			}
			final IASTSimpleDeclaration replace = this.createReplacement(nf, e.getKey(), e.getValue());
			final IASTTranslationUnit target = e.getKey().getTranslationUnit();
			final ASTRewrite targetRw = mc.rewriterForTranslationUnit(target);
			if (replace == null) {
				// remove the whole declaration
				targetRw.remove(e.getKey(), editGroup);
			} else {
				targetRw.replace(e.getKey(), replace, editGroup);
			}
		}
		
		sm.done();
	}
	
	
	
	/**
	 * Recreates a simple declaration from a given one, but leaves out the specified
	 * declarators.
	 * 
	 * @param nf Nodefactory for creation of the new node
	 * @param decl The declaration to copy.
	 * @param remove The declarators that will not be contained within the result.
	 * @return A new declaration node or <code>null</code> of all declarators of the 
	 * 			declaration are to be removed
	 */
	private final IASTSimpleDeclaration createReplacement(ICPPNodeFactory nf,
			IASTSimpleDeclaration decl, Collection<IASTDeclarator> remove) {
		if (remove.size() == decl.getDeclarators().length) {
			return null;
		}
		final IASTSimpleDeclaration simple = nf.newSimpleDeclaration(
				decl.getDeclSpecifier().copy());
		for (final IASTDeclarator declarator : decl.getDeclarators()) {
			if (remove.contains(declarator)) {
				simple.addDeclarator(declarator.copy());
			}
		}
		return simple;
	}
}
