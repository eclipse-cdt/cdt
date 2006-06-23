/*******************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser;


/**
 * @author jcamelon
 *
 */
public interface ISourceElementCallbackDelegate
{
	public void acceptElement( ISourceElementRequestor requestor );
	public void enterScope( ISourceElementRequestor requestor );
	public void exitScope( ISourceElementRequestor requestor );
}
