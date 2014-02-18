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
package org.eclipse.cdt.internal.ui.refactoring.pullup;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.FileStatusContext;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;

import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.resources.ResourceLookup;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.internal.ui.refactoring.pullup.ui.MemberTableEntry;

/**
 * Abstract implementation for pulling up/pushing down member declarations.
 * 
 * @author Simon Taddiken
 * @param <T> Type of the member table entries used for displaying in the gui
 */
public abstract class PullUpPushDownBase<T extends MemberTableEntry> 
		extends CRefactoring {

	private Information<T> information;
	
	
	public PullUpPushDownBase(ICElement element, ISelection selection, 
			ICProject project) {
		super(element, selection, project);
	}
	
	
	
	public CRefactoringContext getRefactoringContext() {
		return this.refactoringContext;
	}
	
	
	
	public Information<T> getInformation() {
		return this.information;
	}
	
	
	
	protected List<InheritanceLevel> getTargetClasses(ICPPClassType current) 
			throws CoreException {
		final List<InheritanceLevel> result = new ArrayList<InheritanceLevel>();
		this.iterateInheritanceTree(null, current, result, 1);
		return result;
	}
	
	
	
	protected void iterateInheritanceTree(InheritanceLevel predecessor, 
			ICPPClassType current, List<InheritanceLevel> result, int level) 
					throws OperationCanceledException, CoreException {
		
		// default: do nothing
	}

	

	/**
	 * Finds the binding of the selected member within the current selection. If no 
	 * member is selected directly, this method traverses the parents to find an 
	 * enclosing function declaration.
	 * 
	 * @param selection Text selection in current document.
	 * @param ast AST of current document.
	 * @param pm Progress monitor
	 * @return The found declarator or <code>null</code> if none exists.
	 * @throws CoreException 
	 * @throws OperationCanceledException 
	 */
	private final ICPPMember findSelectedMember(IASTNode selected, 
			IASTTranslationUnit ast, IProgressMonitor pm)
					throws OperationCanceledException, CoreException {
		
		if (selected == null) {
			return null;
		} else if (selected instanceof IASTName) {
			IASTName name = (IASTName) selected;
			while (name.getParent() instanceof IASTName) {
				name = (IASTName) selected.getParent();
			}
			final IBinding binding = name.resolveBinding();
			if (!(binding instanceof ICPPMember)) {
				// selection within a method?
				return this.findWithinDeclaration(selected);
			}
			
			return (ICPPMember) binding;
		} else {
			// selection within a method?
			return this.findWithinDeclaration(selected);
		}
	}
	
	
	
	private ICPPMember findWithinDeclaration(IASTNode selected) {
		final IBinding binding = CPPVisitor.findEnclosingFunction(selected);
		if (binding instanceof ICPPMember) {
			return (ICPPMember) binding;
		}
		return null;
	}
	
	
	
	/**
	 * Creates a {@link RefactoringStatusContext} object which highlights the provided
	 * node within its file.
	 * 
	 * @param node The node to highlight.
	 * @return The context object to use with {@link RefactoringStatus} or 
	 * 			<code>null</code> if no location can be resolved from the passed node.
	 */
	public RefactoringStatusContext getStatusContext(IASTNode node) {
		if (node == null) {
			return null;
		}
		final IPath nodePath = new Path(node.getTranslationUnit().getFilePath());
		final IFile file = ResourceLookup.selectFileForLocation(nodePath, 
				this.project.getProject());
		if (file == null) {
			return null;
		}
		final IRegion region = new Region(node.getFileLocation().getNodeOffset(), 
				node.getFileLocation().getNodeLength());
		return new FileStatusContext(file, region);
	}
	
	
	
	protected abstract Information<T> createInformationObject(CRefactoringContext context,
			ICPPClassType source, ICPPMember selected, List<InheritanceLevel> targets) 
					throws CoreException;
	
	
	
	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) 
			throws CoreException, OperationCanceledException {
		
		final SubMonitor sm = SubMonitor.convert(pm, 10);

		try {
			final RefactoringStatus status = super.checkInitialConditions(sm.newChild(6));
			if (status.hasFatalError()) {
				return status;
			}
			
			if (this.selectedRegion == null) {
				this.initStatus.addFatalError(Messages.PullUpRefactoring_noSelection);
				return this.initStatus;
			}
			
			final IASTTranslationUnit ast = this.getAST(this.tu, sm);
			final IASTNodeSelector nodeSelector = ast.getNodeSelector(null);
			final IASTNode selected = nodeSelector.findEnclosingNode(
					this.selectedRegion.getOffset(), this.selectedRegion.getLength());
			
			final ICPPMember selectedMember = this.findSelectedMember(selected, ast, sm);
			
			if (selectedMember == null) {
				this.initStatus.addFatalError(Messages.PullUpRefactoring_invalidSelection);
				return this.initStatus;
			}
			
			final ICPPClassType clazz = selectedMember.getClassOwner();

			this.information = this.createInformationObject(this.refactoringContext, 
					clazz, selectedMember, this.getTargetClasses(clazz));
			
			return this.initStatus;
		} finally {
			sm.done();
		}
	}
}
