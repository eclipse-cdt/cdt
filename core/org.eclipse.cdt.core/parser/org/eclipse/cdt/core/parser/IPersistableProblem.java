/*******************************************************************************
 * Copyright (c) 2007, 2009 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
