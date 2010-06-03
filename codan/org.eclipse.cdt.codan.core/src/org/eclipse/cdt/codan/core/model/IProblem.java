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

import org.eclipse.cdt.codan.core.param.IProblemPreference;

/**
 * Interface representing code analysis problem type. For example
 * "Null Pointer Dereference" is a problem. It has user visible Name and Message
 * (translatable), as well as some other parameters, changeable by user such as
 * enablement, severity and so on. Same problem cannot have two severities
 * determined by runtime. If it is the case - two Problems should be created
 * (i.e. one for error and one for warning). All of problem attributes are
 * defined in a checker extension point.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same.
 * </p>
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IProblem extends IProblemElement {
	/**
	 * Name of the problem - user visible "title", not the message
	 * 
	 * @return title of the problem
	 */
	String getName();

	/**
	 * Unique problem id. Should be qualified by plugin name to maintain
	 * uniqueness.
	 * 
	 * @return unique problem id
	 */
	String getId();

	/**
	 * Is enabled in current context (usually within profile)
	 * 
	 * @return true if enabled
	 */
	boolean isEnabled();

	/**
	 * Get current severity
	 * 
	 * @return severity
	 */
	CodanSeverity getSeverity();

	/**
	 * Message pattern, java patter like 'Variable {0} is never used here'
	 * 
	 * @return pattern
	 */
	String getMessagePattern();

	/**
	 * Get root preference descriptor or null if not defined (used by ui to
	 * generate user controls for changing parameters)
	 * 
	 * @return root preference or null
	 */
	public IProblemPreference getPreference();

	/**
	 * Get short description of a problem
	 * 
	 * @return description
	 */
	public String getDescription();

	/**
	 * Return marker id for the problem
	 * 
	 * @return marker id
	 */
	public String getMarkerType();
}
