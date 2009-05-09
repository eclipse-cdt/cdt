/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model;

/**
 * Problem Profile contains tree of categories and problems. Profiles can have
 * different categories and different problems set, problems with the same id
 * can have different severities/enablement in different profiles. To obtain
 * profile use class {@link CheckersRegisry#getResourceProfile,
 * CheckersRegisry#getDefaultProfile() or CheckersRegisry#getWorkspaceProfile()}
 * .
 * 
 */
public interface IProblemProfile extends IProblemElement {
	/**
	 * @return root category in profile
	 */
	IProblemCategory getRoot();

	/**
	 * Find and return problem by id
	 * 
	 * @param id
	 *            - problem id
	 * @return problem instance
	 */
	IProblem findProblem(String id);

	/**
	 * Find and return category by id
	 * 
	 * @param id
	 *            - category id
	 * @return category instance
	 */
	IProblemCategory findCategory(String id);

	/**
	 * Get all defined problems
	 * 
	 * @return array of problems defined in profile
	 */
	IProblem[] getProblems();
}
