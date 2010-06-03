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

import org.eclipse.cdt.codan.internal.core.CheckersRegistry;

/**
 * Problem Profile contains tree of categories and problems. For the user
 * the profile is quick way to switch between problem sets depending on the
 * task he is doing (i.e. find real bugs, vs doing code style report)
 * User can set different profiles for different projects.
 * Profiles can have different categories and different problem sets,
 * problems with the same id can have different severities/enablement in
 * different profiles.
 * Category tree can have few reference to a same problem, but only instance of
 * Problem
 * with the same id can exist in the same profile (i.e. two category can have
 * same problem listed in both,
 * but they both should point to the same problem instance).
 * 
 * To obtain read-only profile use method
 * {@link CheckersRegistry#getResourceProfile},
 * {@link CheckersRegistry#getDefaultProfile()} or
 * {@link CheckersRegistry#getWorkspaceProfile()}
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IProblemProfile extends IProblemElement {
	/**
	 * @return root category in profile
	 */
	IProblemCategory getRoot();

	/**
	 * Find and return problem by id if it contained in this profile
	 * 
	 * @param id
	 *        - problem id
	 * @return problem instance
	 */
	IProblem findProblem(String id);

	/**
	 * Find and return category by id if it is contained in this profile
	 * 
	 * @param id
	 *        - category id
	 * @return category instance
	 */
	IProblemCategory findCategory(String id);

	/**
	 * Get all problems defined in this profile (if problem duplicated in a
	 * category tree, it returns only one instance of each)
	 * 
	 * @return array of problems defined in profile
	 */
	IProblem[] getProblems();
}
