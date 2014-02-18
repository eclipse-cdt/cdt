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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.index.IIndex;

import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassType;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.internal.ui.refactoring.pullup.actions.MoveAction;
import org.eclipse.cdt.internal.ui.refactoring.pullup.ui.MemberTableEntry;
import org.eclipse.cdt.internal.ui.refactoring.pullup.ui.TargetActions;

/**
 * Objects of this class gather information about input provided by the user. These 
 * information are reached through various steps performed by the PullUp- or 
 * PushDownRefactoring. Additionally, user input can be transformed into executable
 * {@link MoveAction MoveActions}
 * 
 * @author Simon Taddiken
 * @param <T> Type of the MemberTableEntry this information object manages.
 */
public abstract class Information<T extends MemberTableEntry> {

	/** The current RefactoringContext */
	protected final CRefactoringContext context;
	
	/** The source class from which members are to be moved */
	protected final ICPPClassType source;
	
	/** 
	 * The initially selected member. May be <code>null</code> if user selected 
	 * constructor or destructor. This is only needed for initially selecting a member
	 * within the GUI
	 */
	protected final ICPPMember member;
	
	/** List of possible target classes to which members can be moved */
	protected final List<InheritanceLevel> targets;
	
	/** 
	 * List of all members (attributes and methods, no constructors/destructor) of the 
	 * source class.
	 */
	protected final List<T> allMembers;
	
	/** Stores dependencies for a certain member once they have been calculated */
	private final Map<MemberTableEntry, Collection<T>> dependencyCache;
	
	/** reference to the index */
	protected final IIndex index;
	
	
	public Information(CRefactoringContext context, ICPPClassType source, 
			ICPPMember member, List<InheritanceLevel> targets) 
					throws OperationCanceledException, CoreException {
		// HINT: member is allowed to be null, e.g. if user selected a 
		//       constructor/destructor
		if (context == null || source == null || targets == null) {
			throw new NullPointerException();
		}
		this.context = context;
		this.source = source;
		this.member = member;
		this.targets = targets;
		this.allMembers = new ArrayList<T>();
		this.dependencyCache = new HashMap<MemberTableEntry, Collection<T>>();
		this.index = context.getIndex();
		this.findAllMembers(source, this.allMembers);
	}

	
	
	/**
	 * Gets a reference to the index.
	 * @return The index.
	 */
	public IIndex getIndex() {
		return this.index;
	}
	
	
	
	/**
	 * Finds all members that are declared in the provided source class. Each member 
	 * binding is converted to a {@link MemberTableEntry} using 
	 * {@link #toTableEntry(ICPPMember)} and returned within the specified list. 
	 * c
	 * @param source The class from which members should be listed.
	 * @param allMembers List into which the members are put.
	 */
	private final void findAllMembers(ICPPClassType source, 
			final List<T> allMembers) {
		
		for (final ICPPMethod mtd : source.getDeclaredMethods()) {
			if (PullUpHelper.isConstructor(mtd) || PullUpHelper.isDestructor(mtd)) {
				// skip constructors and destructor
				continue;
			}
			final T mte = this.toTableEntry(mtd);
			allMembers.add(mte);
		}
		for (final ICPPField field : source.getDeclaredFields()) {
			final T mte = this.toTableEntry(field);
			allMembers.add(mte);
		}
	}
	
	
	
	/**
	 * Finds the provided member binding in the source's declared members. Returns
	 * <code>null</code> if the provided binding is null or is not contained within
	 * the member declarations of the source class.
	 * 
	 * @param member The member to find the entry for.
	 * @return The MemberTableEntry for the provided member.
	 */
	public T findByMember(ICPPMember member) {
		if (member == null) {
			return null;
		}
		for (final T mte : this.allMembers) {
			if (PullUpHelper.bindingsEqual(this.index, member, mte.getMember())) {
				return mte;
			}
		}
		return null;
	}
	
	
	
	/**
	 * Calculates a collection of MemberTableEntries that need to be moved as well when
	 * moving the provided MemberTableEntry.
	 * 
	 * @param mte The member to find the dependencies for.
	 * @return Collection of dependent members.
	 */
	public final Collection<T> calculateDependencies(T mte) {
		if (this.dependencyCache.containsKey(mte)) {
			return this.dependencyCache.get(mte);
		}
		final Collection<T> result = new ArrayList<T>();
		this.calculateDependenciesInternal(this.source, result, mte.getMember());
		this.dependencyCache.put(mte, result);
		return result;
	}
	
	
	
	/**
	 * Internal method for calculating dependencies for a certain member. This method
	 * is responsible for actually finding the dependencies and gets called by 
	 * {@link #calculateDependencies(MemberTableEntry)}.
	 * 
	 * @param owner The {@link CPPClassType} in which the member to move was declared. 
	 * @param result Result collection into which dependencies are to be added.
	 * @param member The member to find dependencies for.
	 */
	protected abstract void calculateDependenciesInternal(final ICPPClassType owner,
			final Collection<T> result, ICPPMember member);
	
	
	
	/**
	 * Converts a member binding into a suitable {@link MemberTableEntry} instance.
	 * @param member The binding to convert.
	 * @return The binding as a MemberTableEntry
	 */
	protected abstract T toTableEntry(ICPPMember member);
	
	
	
	/**
	 * Converts the input settings made by the users into {@link MoveAction} instances 
	 * which are then responsible for checking whether all selected settings are possible
	 * to execute and then actually execute them.
	 * @return A move action for moving selected members
	 * @throws CoreException 
	 */
	public abstract MoveAction generateMoveAction() 
			throws CoreException;
	
	
	
	/**
	 * Gathers all {@link MemberTableEntry MemberTableEntries} where the user chose a 
	 * {@link TargetActions TargetAction} != {@link TargetActions#NONE}.
	 * @return List of members for which an action has been chosen.
	 */
	public List<T> getSelectedMembers() {
		final List<T> result = new ArrayList<T>(this.allMembers.size());
		for (final T mte : this.allMembers) {
			if (mte.getSelectedAction() != TargetActions.NONE) {
				result.add(mte);
			}
		}
		return result;
	}
	
	
	
	/**
	 * Gets the initially selected member. This might be <code>null</code> if the user 
	 * selected a constructor or destructor.
	 * @return The member that user selected when running the refactoring.
	 */
	public ICPPMember getSelectedMember() {
		return member;
	}
	
	
	
	/**
	 * Binding of the class from which methods are to be pulled up/pushed down.
	 * @return The source class
	 */
	public ICPPClassType getSource() {
		return this.source;
	}
	
	
	
	/**
	 * Possible target classes for the refactoring to which members can be 
	 * pulled up/pushed down.
	 * @return List of possible target classes.
	 */
	public List<InheritanceLevel> getTargets() {
		return this.targets;
	}
	
	
	
	/**
	 * Gets all members of the selected source class.
	 * @return All members of selected class.
	 */
	public List<T> getAllMembers() {
		return this.allMembers;
	}
}