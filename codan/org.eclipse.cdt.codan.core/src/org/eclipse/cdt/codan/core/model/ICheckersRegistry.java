/*******************************************************************************
 * Copyright (c) 2009, 2010 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.resources.IResource;

/**
 * This interface an API to add/remove checker and problems programmatically,
 * get problem profiles and change problem default settings
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * 
 */
public interface ICheckersRegistry extends Iterable<IChecker> {
	/**
	 * Iterator for registered checkers
	 * 
	 * @return iterator for registered checkers
	 */
	@Override
	public Iterator<IChecker> iterator();

	/**
	 * Add a checker
	 * 
	 * @param checker instance
	 */
	public void addChecker(IChecker checker);

	/**
	 * Add problem p into a category defined by a category id into default
	 * profile, category must exists in default profile
	 * 
	 * @param p
	 *        - problem
	 * @param categoryId
	 *        - category id
	 */
	public void addProblem(IProblem p, String categoryId);

	/**
	 * Add subcategory category into parent category with the id of
	 * parentCategoryId, if parent does not exist in the default profile or it
	 * is a null - it will be added to the root
	 * 
	 * @param category
	 *        - new category
	 * @param parentCategoryId
	 *        - parent category id
	 */
	public abstract void addCategory(IProblemCategory category, String parentCategoryId);

	/**
	 * Add problem reference to a checker, i.e. claim that checker can produce
	 * this problem. If checker does not claim any problems it cannot be
	 * enabled.
	 * 
	 * @param c
	 *        - checker
	 * @param p
	 *        - problem
	 */
	public void addRefProblem(IChecker c, IProblem p);

	/**
	 * Return collection of problem that this checker can produce
	 * 
	 * @param checker
	 * @return collection of problems
	 */
	public Collection<IProblem> getRefProblems(IChecker checker);

	/**
	 * Default profile is kind of "Installation Default".
	 * Always the same, comes from defaults in checker extensions or APIs added
	 * 
	 * @return default profile
	 */
	public IProblemProfile getDefaultProfile();

	/**
	 * Get workspace profile. User can change setting for workspace profile.
	 * 
	 * @return workspace profile
	 */
	public IProblemProfile getWorkspaceProfile();

	/**
	 * Get resource profile. For example given project can have different
	 * profile than a workspace.
	 * 
	 * @param element
	 *        - resource
	 * @return resource profile
	 */
	public IProblemProfile getResourceProfile(IResource element);

	/**
	 * Returns profile working copy for given resource element. (If profile is
	 * not specified for given element it will search for parent resource and so
	 * on). If you planning on editing it this method should be used instead of
	 * getResourceProfile. You have to save your changes after updating a
	 * working copy, using {@link #updateProfile(IResource, IProblemProfile)}
	 * method.
	 * 
	 * @noreference This method is not intended to be referenced by clients.
	 * @param element
	 * @return resource profile
	 */
	public IProblemProfile getResourceProfileWorkingCopy(IResource element);

	/**
	 * Set profile for resource.
	 * 
	 * @noreference This method is not intended to be referenced by clients.
	 * @param resource
	 *        - resource
	 * @param profile
	 *        - problems profile
	 */
	public void updateProfile(IResource resource, IProblemProfile profile);
}