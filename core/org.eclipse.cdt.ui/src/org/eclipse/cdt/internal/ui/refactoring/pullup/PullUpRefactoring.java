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

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.pullup.actions.MoveAction;
import org.eclipse.cdt.internal.ui.refactoring.pullup.ui.PullUpMemberTableEntry;
import org.eclipse.cdt.internal.ui.refactoring.pullup.ui.TargetActions;

/**
 * Refactoring implementation for moving methods and fields up the inheritance tree. 
 * 
 * @author Simon Taddiken
 */
public class PullUpRefactoring extends PullUpPushDownBase<PullUpMemberTableEntry> {
	
	
	public static final String ID =
			"org.eclipse.cdt.internal.ui.refactoring.pullup.PullUpRefactoring"; //$NON-NLS-1$
	
	/**
	 *  Changes will be generated after checking final conditions and will be executed 
	 *  during collection of modifications.
	 */
	private MoveAction changes;
	
	
	public PullUpRefactoring(ICElement element, ISelection selection, 
			ICProject project) {
		super(element, selection, project);
		this.name = Messages.PullUpRefactoring_name;
	}
	
	
	
	@Override
	protected RefactoringDescriptor getRefactoringDescriptor() {
		// TODO: getRefactoringDescriptor
		return null;
	}

	
	
	/**
	 * Recursively creates a collection of all base classes of the provided class. The 
	 * result is sorted in 'inheritance tree order' (see below). Each entry in the result 
	 * is assigned an inheritance level which specifies the distance from this class to 
	 * any super class. Consider the following inheritance scenario:
	 * <pre>
	 *   A   B
	 *    \ /
	 *     C   D
	 *      \ /
	 *       E
	 *       |
	 *       F
	 * </pre>
	 * 
	 * Calling <code>getBaseClasses(F)</code> will yield the following list:
	 * <table>
	 * <tr>
	 *   <th>Index</th>
	 *   <th>Class</th>
	 *   <th>Level</th>
	 * </tr>
	 * <tr>
	 *   <td>0</td>
	 *   <td>E</td>
	 *   <td>1</td>
	 * </tr>
	 * <tr>
	 *   <td>1</td>
	 *   <td>C</td>
	 *   <td>2</td>
	 * </tr>
	 * <tr>
	 *   <td>2</td>
	 *   <td>A</td>
	 *   <td>3</td>
	 * </tr>
	 * <tr>
	 *   <td>3</td>
	 *   <td>B</td>
	 *   <td>3</td>
	 * </tr>
	 * <tr>
	 *   <td>4</td>
	 *   <td>D</td>
	 *   <td>2</td>
	 * </tr>
	 * </table>
	 * 
	 * @param predecessor Predecessor of current inheritance path
	 * @param current Current class
	 * @param result List of inheritance levels
	 * @param level The current level
	 */
	@Override
	protected void iterateInheritanceTree(InheritanceLevel predecessor, 
			ICPPClassType current, List<InheritanceLevel> result, int level) {
		
		for (final ICPPBase base : current.getBases()) {
			if (base.getBaseClass() instanceof ICPPClassType) {
				final ICPPClassType baseClass = (ICPPClassType) base.getBaseClass();
				final InheritanceLevel newLevel  = 
						new InheritanceLevel(predecessor, baseClass, base, level);
				result.add(newLevel);
				this.iterateInheritanceTree(newLevel, baseClass, result, level + 1);
				if (predecessor != null) {
					predecessor.addChild(newLevel);
				}
			}
		}
	}
	
	
	
	@Override
	public PullUpInformation getInformation() {
		return (PullUpInformation) super.getInformation();
	}
	
	
	
	@Override
	protected PullUpInformation createInformationObject(
			CRefactoringContext context, ICPPClassType source, ICPPMember selected, 
			List<InheritanceLevel> targets) throws OperationCanceledException, CoreException {
		return new PullUpInformation(context, source, selected, targets);
	}
	
	
	
	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) 
			throws CoreException, OperationCanceledException {
		super.checkInitialConditions(pm);
		
		if (this.initStatus.hasFatalError()) {
			return this.initStatus;
		}
		
		if (this.getInformation().getTargets().isEmpty()) {
			this.initStatus.addFatalError(NLS.bind(Messages.PullUpRefactoring_noBaseClass, 
					this.getInformation().getSource().getName()));
		}
		return this.initStatus;
	}
	
	
	
	@Override
	protected RefactoringStatus checkFinalConditions(IProgressMonitor subProgressMonitor,
			CheckConditionsContext checkContext) throws CoreException, OperationCanceledException {
		
		final RefactoringStatus status = super.checkFinalConditions(
				subProgressMonitor, checkContext);
		
		
		final ICPPClassType target = 
				this.getInformation().getSelectedTarget().getClazz();
		final boolean isPureAbstract = PullUpHelper.isPureAbstract(target);
		
		// check for multiple inheritance of the selected target base class
		// collect all inheritance paths that lead to the selected target class
		final List<InheritanceLevel> targetLevels = new ArrayList<InheritanceLevel>();
		for (final InheritanceLevel lvl : this.getInformation().getTargets()) {
			if (lvl != this.getInformation().getSelectedTarget() && target == lvl.getClazz()) {
				targetLevels.add(lvl);
			}
		}
		
		// As c++ allows for visibility reduction when inheriting from classes it is
		// possible that members might not be visible in this class when moved up the 
		// inheritance tree
		targetLevels.add(this.getInformation().getSelectedTarget());
		boolean visible = false;
		boolean virtual = false;
		for (final InheritanceLevel targetLevel : targetLevels) {
			visible |= targetLevel.computeMaxVisibility() <= ICPPASTVisibilityLabel.v_protected;
			virtual |= targetLevel.isVirtual();
		}

		if (!visible) {
			status.addWarning(Messages.PullUpRefactoring_membersNotVisible);
		}
		
		if (targetLevels.size() > 1 && !virtual) {
			// at least two inheritance paths lead to the selected target class and 
			// target class is not inherited virtually on any
			status.addWarning(Messages.PullUpRefactoring_diamond);
		}
		final Collection<PullUpMemberTableEntry> selected = this.getInformation().getSelectedMembers();
		final List<ICPPMember> ignoreList = new ArrayList<ICPPMember>(selected.size());
		
		// collect a list of all members that are to be removed from their source classes
		for (final PullUpMemberTableEntry mte : selected) {
			if (mte.getSelectedAction() == TargetActions.PULL_UP || 
					mte.getSelectedAction() == TargetActions.REMOVE_METHOD) {
				ignoreList.add(mte.getMember());
			}
		}
		
		for (final PullUpMemberTableEntry mte : selected) {
			
			// check whether member is referenced in source class besides its declaration
			final boolean stillReferenced = PullUpHelper.isReferencedIn(
					this.getIndex(),
					this.refactoringContext, mte.getMember(), ignoreList, 
					mte.getMember().getClassOwner());
			
			final Collection<PullUpMemberTableEntry> dependencies = this.getInformation().calculateDependencies(mte);
			final String mteName = PullUpHelper.getMemberString(mte.getMember());

			// if member is referenced, check whether it is visible
			if (stillReferenced && 
					mte.getTargetVisibility().getVisibilityLabelValue() > ICPPASTVisibilityLabel.v_protected) {
				status.addError(NLS.bind(Messages.PullUpRefactoring_memberStillReferenced, 
						mteName));
			}
			
			if (!this.getInformation().doPullIntoPureAbstract()) {
				// definitions should not be pulled up into pure abstract
				if (isPureAbstract && mte.getSelectedAction() == TargetActions.PULL_UP) {
					
					final IASTName name = PullUpHelper.findName(this.getIndex(), 
							this.refactoringContext, mte.getMember(), 
							IIndex.FIND_DECLARATIONS_DEFINITIONS);
					RefactoringStatusContext c = this.getStatusContext(name);
					
					// TODO: would be even better if we check whether the member has 
					//		 a definition right here to avoid superfluous warnings 
					// TODO: memo to self: what does above todo mean?
					if (mte.getMember() instanceof ICPPMethod) {
						status.addWarning(NLS.bind(
								Messages.PullUpRefactoring_targetIsAbstract, mteName), c);
						mte.setSelectedAction(TargetActions.DECLARE_VIRTUAL);
					} else {
						status.addError(NLS.bind(
								Messages.PullUpRefactoring_canNotPullUpField, mteName), c);
					}
				}
			}
			
			// check whether all dependencies are pulled up
			if (mte.getSelectedAction() == TargetActions.PULL_UP) {
				for (final PullUpMemberTableEntry depend : dependencies) {
					if (depend.getSelectedAction() == TargetActions.NONE) {
						final IASTName name = PullUpHelper.findName(this.getIndex(), 
								this.refactoringContext, depend.getMember(), IIndex.FIND_DECLARATIONS);
						final RefactoringStatusContext c = this.getStatusContext(name);
						final String dependName = PullUpHelper.getMemberString(depend.getMember());
						status.addError(NLS.bind(
								Messages.PullUpRefactoring_requiredDependency, 
								mteName, dependName), c);
					}
				}
			}
		}
		
		// check refactoring independent post conditions
		this.changes = this.getInformation().generateMoveAction();
		this.changes.isPossible(status, subProgressMonitor);

		return status;
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
