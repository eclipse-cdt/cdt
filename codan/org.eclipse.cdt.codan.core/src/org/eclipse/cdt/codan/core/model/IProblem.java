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
 * Interface representing code analysis problem
 * 
 */
public interface IProblem extends IProblemElement {
	/**
	 * Name of the problem - user visible "title", not the message
	 */
	String getName();

	/**
	 * Unique problem id. Should be qualified by plugin name to maintain
	 * uniqueness.
	 * 
	 * @return
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

	public Object getParameter(Object key);

	/**
	 * Get root paramterInfo - contains description of types of all the
	 * parameters or null if not defined
	 * 
	 * @return
	 */
	public IProblemParameterInfo getParameterInfo();
}
