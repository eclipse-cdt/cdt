/*******************************************************************************
 * Copyright (c) 2007 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.cdi.event;

import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;

/**
 * 
 * Notifies that a breakpoint problem has occurred.
 */
public interface ICDIBreakpointProblemEvent {

	/** The problem type is a string that identifies specific
	 * kinds of breakproblems.
	 * @return problem type name
	 */
	String getProblemType();
	
	/** The CDI breakpoint that has the problem
	 * @return the cdi breakpoint that has the problem
	 */
	ICDIBreakpoint getBreakpoint();
	
	/** A description of the problem.
	 * This will be presented in the problems view.
	 * @return a description of the problem
	 */
	String getDescription();
	
	/** The severity code maps to the IMarker.SEVERITY_XXX constants.
	 * @return severity code.
	 */
	int getSeverity();
	
	/** Indicated that existing problems of this type and at this
	 * breakpoint's location will be removed, no new ones will
	 * be added.
	 * @return only remove existing problems
	 */
	boolean removeOnly();
	
	/** Indicated that any existing problems of this type
	 * and at this breakpoint's location will be removed
	 * before the new problem is added.
	 * @return remove any existing markers
	 */
	boolean removeExisting();
	
}
