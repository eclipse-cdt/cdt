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
 * Interface for checker with parameters, if checker implements this
 * interface method would be called on initialization so checker has
 * a chance to set default values for its parameters
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICheckerWithParameters {
	/**
	 * Implement this method to set default parameters for checkers with parameters. 
	 * @param problem - instance of problem working copy
	 */
	void initParameters(IProblemWorkingCopy problem);
}
