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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.osgi.util.NLS;
import org.eclipse.text.edits.TextEditGroup;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;

import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.internal.ui.refactoring.IndexToASTNameHelper;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.pullup.actions.MoveAction;
import org.eclipse.cdt.internal.ui.refactoring.pullup.ui.TargetActions;
import org.eclipse.cdt.internal.ui.refactoring.pushdown.ui.PushDownMemberTableEntry;

public class PushDownRefactoring extends PullUpPushDownBase<PushDownMemberTableEntry> {

	public static final String ID =
			"org.eclipse.cdt.internal.ui.refactoring.pullup.PushDownRefactoring"; //$NON-NLS-1$
	
	
	private MoveAction changes;
	
	public PushDownRefactoring(ICElement element, ISelection selection, 
			ICProject project) {
		super(element, selection, project);
	}
	
	
	
	@Override
	protected RefactoringDescriptor getRefactoringDescriptor() {
		final Map<String, String> arguments = this.getArgumentMap();
		final RefactoringDescriptor rd = new PushDownRefactoringDescriptor(
				project.getProject().getName(), 
				"Push Down Member Refactoring", "Push Down", arguments); //$NON-NLS-1$ //$NON-NLS-2$
		return rd;
	}
	

	
	@Override
	protected void iterateInheritanceTree(InheritanceLevel predecessor, 
			ICPPClassType current, List<InheritanceLevel> result, int level) 
					throws OperationCanceledException, CoreException {
		
		// find all references of the current class
		final IIndexName[] names = this.getIndex().findNames(
				current, IIndex.FIND_REFERENCES);
		
		for (final IIndexName name : names) {
			final IASTTranslationUnit ast = PullUpHelper.getASTForIndexName(
					name, this.refactoringContext);
			
			final IASTName astName = IndexToASTNameHelper.findMatchingASTName(
					ast, name, this.getIndex());
			
			// check whether any reference is a BaseSpecifier, if so we found a subclass
			if (astName != null && astName.getPropertyInParent() == ICPPASTBaseSpecifier.NAME) {
				final ICPPASTBaseSpecifier baseSpec = (ICPPASTBaseSpecifier) astName.getParent();
				final ICPPASTCompositeTypeSpecifier typeSpec = (ICPPASTCompositeTypeSpecifier) baseSpec.getParent();
				
				final ICPPClassType clazz = (ICPPClassType) typeSpec.getName().resolveBinding();
				final InheritanceLevel lvl = new InheritanceLevel(predecessor, clazz, 
						null, level + 1);
				result.add(lvl);
				iterateInheritanceTree(lvl, clazz, result, level + 1);
				if (predecessor != null) {
					predecessor.addChild(lvl);
				}
			}
		}
	}
	
	
	
	@Override
	public PushDownInformation getInformation() {
		return (PushDownInformation) super.getInformation();
	}
	
	
	
	@Override
	protected PushDownInformation createInformationObject(CRefactoringContext context,
			ICPPClassType source, ICPPMember selected, List<InheritanceLevel> targets) 
					throws OperationCanceledException, CoreException {
		return new PushDownInformation(context, source, selected, targets);
	}
	
	
	
	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		super.checkInitialConditions(pm);
		if (this.initStatus.hasFatalError()) {
			return this.initStatus;
		}
		
		if (this.getInformation().getTargets().isEmpty()) {
			this.initStatus.addFatalError(NLS.bind(Messages.PushDownRefactoring_noSubclasses, 
					this.getInformation().getSource().getName()));
		}
		return this.initStatus;
	}
	
	
	
	@Override
	protected RefactoringStatus checkFinalConditions(IProgressMonitor subProgressMonitor,
			CheckConditionsContext checkContext) throws CoreException, OperationCanceledException {
		
		final RefactoringStatus status = super.checkFinalConditions(
				subProgressMonitor, checkContext);
		
		if (status.hasFatalError()) {
			return status;
		}
		
		final Collection<PushDownMemberTableEntry> selected = 
				this.getInformation().getSelectedMembers();
		
		// collect members that will be removed from their current class
		final Collection<PushDownMemberTableEntry> toRemove = 
				new ArrayList<PushDownMemberTableEntry>(selected.size());
		
		
		for (final PushDownMemberTableEntry mte : selected) {
			final Collection<PushDownMemberTableEntry> dependencies = 
					this.getInformation().calculateDependencies(mte);
			final String mteName = PullUpHelper.getMemberString(mte.getMember());
			
			// check whether all dependencies are pushed down
			if (mte.getSelectedAction() == TargetActions.PUSH_DOWN) {
				
				// collect members that will be removed
				toRemove.add(mte);
				
				// hence, check all classes to which the member's definition is pushed
				for (final Entry<InheritanceLevel, String> e : mte.getActionsPerClass().entrySet()) {
					if (e.getValue() == TargetActions.EXISTING_DEFINITION) {
						// definition is pushed to this class, so check dependencies
						
						for (final PushDownMemberTableEntry depend : dependencies) {
							// action of dependency for current target class
							final String dependAction = depend.getActionsPerClass().get(e.getKey());
							
							if (dependAction == null || dependAction == TargetActions.NONE) {
								final String dependName = PullUpHelper.getMemberString(depend.getMember());
								final Object[] bindings = { mteName, dependName, e.getKey().getClazz().getName() };
								status.addError(
									NLS.bind(Messages.PushDownRefactoring_requiredDependency, bindings));
							}
						}
					}
				}
			}
		}
		
		// now, check all call sites of members that will be removed
		for (final PushDownMemberTableEntry mte : toRemove) {
			final Collection<IASTName> callSites = PullUpHelper.findReferences(
					this.getIndex(), mte.getMember(), this.refactoringContext);
			
			for (final IASTName reference : callSites) {
				this.inspectCallSite(mte, reference, 
						toRemove, status);
			}
			
		}
		this.changes = this.getInformation().generateMoveAction();
		this.changes.isPossible(status, subProgressMonitor);
		return status;
	}

	
	
	/**
	 * Checks whether the passed reference is called as member of the class from which it
	 * is being removed. If so, an error will be appended to the provided refactoring
	 * status.
	 * @param mte The binding of the member which is currently investigated
	 * @param reference One reference of that member
	 * @param toRemove Collection of members which will be removed from the source 
	 * 		class. References within any of these members will be excluded
	 * @param status RefactoringStatus to fill with error information.
	 * @throws CoreException 
	 * @throws OperationCanceledException 
	 */
	private void inspectCallSite(PushDownMemberTableEntry mte, IASTName reference, 
			Collection<PushDownMemberTableEntry> toRemove, RefactoringStatus status) 
					throws OperationCanceledException, CoreException {
		
		final ICPPMember member = mte.getMember();
		
		// first, check whether this reference occurs within a definition which is removed
		// anyway. In this case -> no error
		final IASTFunctionDefinition enclosingDef = CPPVisitor.findAncestorWithType(
				reference, IASTFunctionDefinition.class);
		
		final IBinding fnctBinding = enclosingDef.getDeclarator().getName().resolveBinding();
		// Check whether this reference occurs within a method which is removed anyway
		for (final PushDownMemberTableEntry removedMTE : toRemove) {
			final IBinding bnd = removedMTE.getMember();
			if (PullUpHelper.bindingsEqual(this.getIndex(), bnd, fnctBinding)) {
				return;
			}
		}
		
		
		final String currentClassName = ((ICPPMember) fnctBinding).getClassOwner().getName();
		final IASTFunctionCallExpression call = CPPVisitor.findAncestorWithType(reference, 
				IASTFunctionCallExpression.class);
		
		if (call == null) {
			return;
		}
		final IASTExpression ce = call.getFunctionNameExpression();
		if (ce instanceof IASTFieldReference) {
			final IASTFieldReference fr = (IASTFieldReference) ce;
			final IASTExpression owner = fr.getFieldOwner();
			
			if (owner instanceof IASTIdExpression) {
				final IASTIdExpression id = (IASTIdExpression) owner;
				final IType exprType = id.getExpressionType();
				
				if (!canBeRemoved(mte, exprType)) {
					final Object[] msgs = {
							PullUpHelper.getMemberString(member),
							currentClassName
					};
					final RefactoringStatusContext c = this.getStatusContext(call);
					status.addError(NLS.bind(
							Messages.PushDownRefactoring_memberIsReferenced, 
							msgs), c);
				}
			}
		}
		return;
	}
	
	
	
	private boolean canBeRemoved(PushDownMemberTableEntry mte, IType exprType) {
		if (!(exprType instanceof ICPPClassType)) {
			return false;
		}
		
		final ICPPClassType exprClassType = (ICPPClassType) exprType;
		// check whether exprType is super class of any target class
		for (final InheritanceLevel target : mte.getActionsPerClass().keySet()) {
			final ICPPClassType targetClass = target.getClazz();
			if (PullUpHelper.isSubClass(targetClass, exprClassType)) {
				return false;
			}
		}
		
		return true;
	}
	
	
	
	@Override
	protected void collectModifications(IProgressMonitor pm, ModificationCollector collector)
			throws CoreException, OperationCanceledException {
		final TextEditGroup editGroup = new TextEditGroup(""); //$NON-NLS-1$
		assert this.changes != null;
		this.changes.run(collector, editGroup, pm);
		this.changes = null;
	}
}
