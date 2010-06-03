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

/**
 * Interface for checker with parameters, if checker implements this interface
 * method would be called on initialization so checker has a chance to set
 * default values for its parameters. It is recommended to use
 * {@link AbstractCheckerWithProblemPreferences} insted of implementing it
 * directly.<p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same.
 * </p>
 * 
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ICheckerWithPreferences {
	/**
	 * Implement this method to set default parameters for checkers with
	 * parameters.
	 * 
	 * @param problem
	 *        - instance of problem working copy
	 */
	void initPreferences(IProblemWorkingCopy problem);
}
