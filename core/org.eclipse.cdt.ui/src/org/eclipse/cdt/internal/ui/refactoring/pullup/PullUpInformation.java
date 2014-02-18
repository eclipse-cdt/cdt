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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.internal.ui.refactoring.pullup.actions.MoveAction;
import org.eclipse.cdt.internal.ui.refactoring.pullup.actions.MoveActionGroup;
import org.eclipse.cdt.internal.ui.refactoring.pullup.actions.MoveFieldAction;
import org.eclipse.cdt.internal.ui.refactoring.pullup.actions.PullUpActions;
import org.eclipse.cdt.internal.ui.refactoring.pullup.ui.PullUpMemberTableEntry;
import org.eclipse.cdt.internal.ui.refactoring.pullup.ui.SubClassTreeEntry;
import org.eclipse.cdt.internal.ui.refactoring.pullup.ui.TargetActions;


/**
 * Specialization of the information object for pulling up members. Contains some
 * additional user input data and generates {@link MoveAction} instances from the 
 * selected members and actions.
 * 
 * @author Simon Taddiken
 */
 public class PullUpInformation extends Information<PullUpMemberTableEntry> {

	/** 
	 * Whether method stubs are to be inserted in non-abstract classes which inherit from
	 * the selected target class.
	 */
	private boolean insertMethodStubs;
	
	/**
	 * Whether methods can be pulled up into abstract base classes without generating an
	 * error.
	 */
	private boolean pullIntoPureAbstract;
	
	/**
	 * The selected target class to pull the members up to
	 */
	protected InheritanceLevel selectedTarget;
	
	/** Collection of members which will be removed from other sub classes */
	protected final Set<PullUpMemberTableEntry> additionalRemoves;
	
	
	
	public PullUpInformation(CRefactoringContext context,
			ICPPClassType source, ICPPMember selected, 
			List<InheritanceLevel> targets) throws OperationCanceledException, CoreException {
		super(context, source, selected, targets);
		this.additionalRemoves = new HashSet<PullUpMemberTableEntry>();
	}
	
	
	
	/**
	 * The returned list will contain all members selected within the gui + all members
	 * that are marked to be removed from their sub classes.
	 */
	@Override
	public List<PullUpMemberTableEntry> getSelectedMembers() {
		final List<PullUpMemberTableEntry> result = super.getSelectedMembers();
		result.addAll(this.additionalRemoves);
		return result;
	}
	
	
	
	/**
	 * Toggles a member to be removed from its class.
	 * 
	 * @param scte The selected member.
	 * @param doRemove Whether it should be removed or not
	 */
	public void toggleRemove(SubClassTreeEntry scte, boolean doRemove) {
		if (doRemove) {
			this.additionalRemoves.add(scte.getMember());
		} else {
			this.additionalRemoves.remove(scte.getMember());
		}
	}
	
	
	
	
	public void resetAdditionalRemoves() {
		this.additionalRemoves.clear();
	}

	
	
	@Override
	protected void calculateDependenciesInternal(final ICPPClassType owner,
			final Collection<PullUpMemberTableEntry> result, ICPPMember member) {
		
		final List<ICPPMember> referenced = new ArrayList<ICPPMember>();
		PullUpHelper.findReferencedMembers(this.index, this.context, owner, 
				member, referenced);
		
		for (final ICPPMember reference : referenced) {
			final PullUpMemberTableEntry mte = this.findByMember(reference);
			result.add(mte);
		}
	}
	
	
	
	@Override
	public MoveAction generateMoveAction() throws OperationCanceledException, CoreException {
		// Group all actions into single MoveAction
		final MoveActionGroup group = new MoveActionGroup(this);
		
		for (final PullUpMemberTableEntry mte : this.getSelectedMembers()) {
			final int v = mte.getTargetVisibility().getVisibilityLabelValue();
			if (mte.getMember() instanceof ICPPField) {
				group.addAction(new MoveFieldAction(group, this.context, mte.getMember(), 
						this.selectedTarget.getClazz(), v));
			} else {
				group.addAction(PullUpActions.forAction(
						mte.getSelectedAction(), 
						this.context, 
						group, 
						(ICPPMethod) mte.getMember(), 
						this.selectedTarget.getClazz(), 
						mte.getTargetVisibility().getVisibilityLabelValue()));
				
			}
		}
		// Add MoveActions to declare method stubs 
		this.findMethodStubTargets(group);
		return group;
	}
	
	
	
	/**
	 * Generates the tree structure which will be shown to the user to select members 
	 * that are to be removed from their classes.
	 * @return Map of members to their classes.
	 */
	public Map<InheritanceLevel, List<SubClassTreeEntry>> generateTree() {
		final Map<InheritanceLevel, List<SubClassTreeEntry>> result = 
				new HashMap<InheritanceLevel, List<SubClassTreeEntry>>();
		
		// this levels just serves to collect direct children
		final InheritanceLevel root = new InheritanceLevel(null, null, null, -1);
		try {
			PullUpHelper.findSubClasses(this.context, this.selectedTarget.getClazz(), 
					false, root);
		} catch (CoreException e) {
			e.printStackTrace();
			CUIPlugin.log(e);
		}
		
		generateTree(root, result);

		
		return result;
	}
	
	
	
	
	private void generateTree(InheritanceLevel lvl, 
			Map<InheritanceLevel, List<SubClassTreeEntry>> target) {
		for (final InheritanceLevel child : lvl.getChildren()) {
			if (PullUpHelper.bindingsEqual(this.index, child.getClazz(), this.source)) {
				// method will be removed here anyway
				continue;
			}
			
			final List<SubClassTreeEntry> list = new ArrayList<SubClassTreeEntry>();
			
			for (final ICPPMember member : child.getClazz().getDeclaredMethods()) {
				if (!(member instanceof ICPPMethod) || PullUpHelper.isConstructor(member) 
						|| PullUpHelper.isDestructor(member)) {
					// TODO: fields not supported yet
					continue;
				}
				// TODO: precheck all members with matching declarations of the ones
				//		 that are pushed up
				final PullUpMemberTableEntry mte = PullUpMemberTableEntry.forRemoval(
						(ICPPMethod) member);
				mte.setSelectedAction(TargetActions.REMOVE_METHOD);
				list.add(new SubClassTreeEntry(mte, child));
			}
			if (!list.isEmpty()) {
				target.put(child, list);
			}
			generateTree(child, target);
		}
	}

	
	
	/**
	 * Creates {@link MoveAction} instances which insert method stubs to some target 
	 * classes. The set of targets are the classes that are derived from the 
	 * {@link #getSelectedTarget() selected target}. On each inheritance path 
	 * for each method which is to be declared virtual, a method stub is created within 
	 * the first non-abstract class found on the path.
	 * 
	 * @param group Group into which the generated actions are added.
	 * @throws OperationCanceledException
	 * @throws CoreException
	 */
	private void findMethodStubTargets(MoveActionGroup group) 
			throws OperationCanceledException, CoreException {
		
		if (this.insertMethodStubs) {
			// this levels just serves to collect direct children
			final InheritanceLevel root = new InheritanceLevel(null, null, null, -1);
			
			PullUpHelper.findSubClasses(this.context, this.selectedTarget.getClazz(), false, root);
			
			for (final PullUpMemberTableEntry mte : this.getSelectedMembers()) {
				if (!(mte.getMember() instanceof ICPPMethod) || 
						mte.getSelectedAction() != TargetActions.DECLARE_VIRTUAL) {
					// stubs are only needed for virtual methods
					continue;
				}
				
				final ICPPMethod method = (ICPPMethod) mte.getMember();
				final Queue<InheritanceLevel> q = new ArrayDeque<InheritanceLevel>();
				q.addAll(root.getChildren()); // add first level
				
				// search inheritance tree in level order
				while (!q.isEmpty()) {
					final InheritanceLevel next = q.poll();
					final boolean isAbstract = PullUpHelper.isPureAbstract(next.getClazz());
					
					if (!isAbstract && !PullUpHelper.checkContains(next.getClazz(), method)) {
						// yay, we found a class to insert the stub to
						group.addAction(PullUpActions.forAction(
								TargetActions.METHOD_STUB, 
								this.context, 
								group, 
								method, 
								next.getClazz(), 
								method.getVisibility()));
					} else if (isAbstract) {
						// no success on this level, so search the next level
						q.addAll(next.getChildren());
					}
				}
			}
		}
	}
	
	
	
	@Override
	protected PullUpMemberTableEntry toTableEntry(ICPPMember member) {
		return new PullUpMemberTableEntry(member);
	}
	
	

	/**
	 * Sets whether method stubs should be created in non-abstract subtypes of the
	 * target class. If <code>true</code>, {@link #generateMoveAction()} will return a
	 * action which will create those stubs too.
	 * @param insertMethodStubds Whether to create method stubs
	 */
	public void setDoInsertMethodStubs(boolean insertMethodStubds) {
		this.insertMethodStubs = insertMethodStubds;
	}



	/**
	 * Gets the target class which the user selected in the GUI.
	 * 
	 * @return The target class for the refactoring
	 */
	public InheritanceLevel getSelectedTarget() {
		return this.selectedTarget;
	}



	/**
	 * Sets the target class for the refactoring.
	 * 
	 * @param selectedTarget The target class.
	 */
	public void setSelectedTarget(InheritanceLevel selectedTarget) {
		this.selectedTarget = selectedTarget;
	}
	
	
	
	/**
	 * Whether methods can be pulled up into pure abstract base classes without generating
	 * an error.
	 * 
	 * @return Whether methods can be pulled up into pure abstract base classes.
	 */
	public boolean doPullIntoPureAbstract() {
		return this.pullIntoPureAbstract;
	}
	
	
	
	/**
	 * Sets whether methods can be pulled up into pure abstract base classes without 
	 * generating an error.
	 * 
	 * @param pullIntoPureAbstract
	 */
	public void setPullIntoPureAbstract(boolean pullIntoPureAbstract) {
		this.pullIntoPureAbstract = pullIntoPureAbstract;
	}
}