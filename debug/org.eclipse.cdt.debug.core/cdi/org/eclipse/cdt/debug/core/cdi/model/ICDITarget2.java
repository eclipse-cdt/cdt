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

package org.eclipse.cdt.debug.core.cdi.model;


public interface ICDITarget2 extends ICDITarget {


	/** Returns a list of global variables for the targeted process
	 * @return list of globals for the targeted process
	 */
	ICDIGlobalVariableDescriptor[] getGlobalVariables();

}
