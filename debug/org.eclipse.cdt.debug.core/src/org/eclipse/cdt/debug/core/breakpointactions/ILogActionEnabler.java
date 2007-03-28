/*******************************************************************************
 * Copyright (c) 2007 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.breakpointactions;

/**
 * 
 * THIS INTERFACE IS PROVISIONAL AND WILL CHANGE IN THE FUTURE PLUG-INS USING
 * THIS INTERFACE WILL NEED TO BE REVISED TO WORK WITH FUTURE VERSIONS OF CDT.
 * 
 */

public interface ILogActionEnabler {

	String evaluateExpression(String expression) throws Exception;

}
