/*******************************************************************************
 * Copyright (c) 2007, 2009 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IPersistableProblem extends IProblem {
	/**
	 * Returns the marker type associated to this problem, if it gets persisted into a marker.
	 * @return the type of the marker which would be associated to the problem.
	 */
	String getMarkerType();
}
