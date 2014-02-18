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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.internal.ui.refactoring.pullup.actions.MoveAction;
import org.eclipse.cdt.internal.ui.refactoring.pullup.actions.MoveActionGroup;
import org.eclipse.cdt.internal.ui.refactoring.pullup.actions.MoveFieldAction;
import org.eclipse.cdt.internal.ui.refactoring.pullup.actions.PushDownActions;
import org.eclipse.cdt.internal.ui.refactoring.pullup.ui.TargetActions;
import org.eclipse.cdt.internal.ui.refactoring.pushdown.ui.PushDownMemberTableEntry;
import org.eclipse.cdt.internal.ui.refactoring.pushdown.ui.TargetTableEntry;


public class PushDownInformation extends Information<PushDownMemberTableEntry> {

	private Map<InheritanceLevel, Collection<PushDownMemberTableEntry>> mandatories;
	
	
	
	public PushDownInformation(CRefactoringContext context, ICPPClassType source, 
			ICPPMember member, List<InheritanceLevel> targets) 
					throws OperationCanceledException, CoreException {
		super(context, source, member, targets);
	}

	
	
	private void addMandatory(InheritanceLevel lvl, PushDownMemberTableEntry mte) {
		// lazy initialization HACK because this method gets called by the super 
		// constructor
		if (this.mandatories == null) {
			this.mandatories = new HashMap<InheritanceLevel, Collection<PushDownMemberTableEntry>>();			
		}
		Collection<PushDownMemberTableEntry> entries = 
				this.mandatories.get(lvl);
		if (entries == null) {
			entries = new HashSet<PushDownMemberTableEntry>();
			this.mandatories.put(lvl, entries);
		}
		entries.add(mte);
	}
	
	
	
	/**
	 * Determines whether it is mandatory to insert a member's declaration in the provided
	 * target class. This is the case if the member is currently referenced within that 
	 * target class.
	 * 
	 * @param mte The member to check
	 * @param target The target class to check.
	 * @return Whether the member is referenced in the target class and hence must be 
	 * 			pushed there
	 */
	public boolean isMandatory(PushDownMemberTableEntry mte, InheritanceLevel target) {
		if (this.mandatories == null) {
			return false;
		}
		final Collection<PushDownMemberTableEntry> entries = this.mandatories.get(target);
		return entries != null && entries.contains(mte); 
	}
	
	
	
	/**
	 * Determines whether there is at least one member of the source class which 
	 * is referenced in the target class.
	 * 
	 * @param target The target class to check.
	 * @return Whether the target references a member of the source class.
	 */
	public boolean hasMandatoryChild(InheritanceLevel target) {
		if (this.mandatories == null) {
			return false;
		}
		final Collection<PushDownMemberTableEntry> entries = this.mandatories.get(target);
		return entries != null && !entries.isEmpty();
	}
	
	
	
	@Override
	protected PushDownMemberTableEntry toTableEntry(ICPPMember member) {
		final PushDownMemberTableEntry mte = new PushDownMemberTableEntry(member);
		
		for (final InheritanceLevel lvl : this.targets) {
			// If a member is referenced within a subclass it is mandatory to push
			// it down
			if (PullUpHelper.isReferencedIn(this.index, this.context, member, 
					Collections.<ICPPMember>emptyList(), lvl.getClazz())) {
				this.addMandatory(lvl, mte);
			}
		}
		
		return mte;
	}

	
	
	public Map<InheritanceLevel, List<TargetTableEntry>> generateTree() {
		final Map<InheritanceLevel, List<TargetTableEntry>> result = 
				new HashMap<InheritanceLevel, List<TargetTableEntry>>();
		for (final InheritanceLevel target : this.targets) {
			final List<TargetTableEntry> ttes = new ArrayList<TargetTableEntry>();
			for (final PushDownMemberTableEntry mte : this.getSelectedMembers()) {
				final boolean mandatory = this.isMandatory(mte, target);
				final String defaultAction = mandatory 
						? TargetActions.EXISTING_DEFINITION 
						: TargetActions.NONE;
				
				final TargetActions supported = TargetTableEntry.PER_CLASS_TARGET_ACTIONS;
				final TargetTableEntry tte = new TargetTableEntry(target, mte, 
						supported, defaultAction);
				ttes.add(tte);
			}
			result.put(target, ttes);
		}
		return result;
	}
	
	
	
	@Override
	protected void calculateDependenciesInternal(ICPPClassType owner, 
			Collection<PushDownMemberTableEntry> result, ICPPMember member) {
		
		final List<ICPPMember> referenced = new ArrayList<ICPPMember>();
		PullUpHelper.findReferencedMembers(this.index,  this.context, owner, member, 
				referenced);
		
		// all referenced members that are not visible in target class are required
		for (final ICPPMember reference : referenced) {
			if (reference.getVisibility() < ICPPASTVisibilityLabel.v_private) {
				// 'reference' is at least protected, so it is visible in target class
				continue;
			}
			
			final PushDownMemberTableEntry mte = this.findByMember(reference);
			if (!result.contains(mte)) {
				result.add(mte);
			}
		}
		
		// all members which definitions reference the selected member are required
		final Collection<ICPPMember> referencing = PullUpHelper.findReferencingMembers(
				this.index, this.context, member);
		for (final ICPPMember reference : referencing) {

			final PushDownMemberTableEntry mte = this.findByMember(reference);
			if (!result.contains(mte)) {
				result.add(mte);
			}
		}
	}
	
	
	
	public void resetPerClassActions() {
		for (final PushDownMemberTableEntry mte : this.getSelectedMembers()) {
			mte.getActionsPerClass().clear();
		}
	}
	


	@Override
	public MoveAction generateMoveAction() {
		final MoveActionGroup group = new MoveActionGroup(this);
		
		for (final PushDownMemberTableEntry mte : this.getSelectedMembers()) {
			if (mte.getMember() instanceof ICPPMethod) {
				final ICPPMethod method = (ICPPMethod) mte.getMember();
				group.addAction(PushDownActions.forAction(
					mte.getSelectedAction(), 
					this.context, 
					group, 
					method, 
					method.getClassOwner(), 
					mte.getTargetVisibility().getVisibilityLabelValue()));
				
				for (final Entry<InheritanceLevel, String> action : mte.getActionsPerClass().entrySet()) {
					group.addAction(PushDownActions.forAction(
						action.getValue(), 
						this.context, 
						group, 
						method, 
						action.getKey().getClazz(), 
						mte.getTargetVisibility().getVisibilityLabelValue()));
				}
			} else if (mte.getMember() instanceof ICPPField) {
				final ICPPField field = (ICPPField) mte.getMember();
				for (final InheritanceLevel target : mte.getActionsPerClass().keySet()) {
					group.addAction(new MoveFieldAction(group, this.context, field, 
							target.getClazz(), mte.getTargetVisibility().getVisibilityLabelValue()));
				}
			}
		}
		return group;
	}
}
