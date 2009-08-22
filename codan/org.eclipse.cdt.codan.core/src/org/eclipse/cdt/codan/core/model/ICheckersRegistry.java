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

import java.util.Iterator;

import org.eclipse.core.resources.IResource;

/**
 * @author Alena
 * 
 */
public interface ICheckersRegistry {
	public abstract Iterator<IChecker> iterator();

	public abstract void addChecker(IChecker checker);

	public abstract void addProblem(IProblem p, String category);

	public abstract void addCategory(IProblemCategory p, String category);

	public abstract void addRefProblem(IChecker c, IProblem p);

	/**
	 * @return
	 */
	public abstract IProblemProfile getDefaultProfile();

	/**
	 * @return
	 */
	public abstract IProblemProfile getWorkspaceProfile();

	/**
	 * @param element
	 * @return
	 */
	public abstract IProblemProfile getResourceProfile(IResource element);

	/**
	 * @param element
	 * @return
	 */
	public abstract IProblemProfile getResourceProfileWorkingCopy(
			IResource element);

	/**
	 * Set profile for resource. This method is called by UI, and should not be
	 * called by clients directly
	 * 
	 * @param resource
	 *            - resource
	 * @param profile
	 *            - problems profile
	 */
	public abstract void updateProfile(IResource resource,
			IProblemProfile profile);
}