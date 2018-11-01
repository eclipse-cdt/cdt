/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.internal.useractions.ui.compile;

import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.internal.useractions.ui.ISystemSubstitutor;

/**
 * This is the callback from SystemCmdSubstVarList that is used to substitute
 *  a particular substitution variable into the given compile command, for the
 *  given remote object.
 */
public interface ISystemCompileCommandSubstitutor extends ISystemSubstitutor {
	/**
	 * Reset the connection so one instance can be re-used
	 */
	public void setConnection(IHost connection);
}
