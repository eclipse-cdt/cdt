/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License 2.0 
 * which accompanies this distribution, and is available at 
 * https://www.eclipse.org/legal/epl-2.0/ 
 * 
 * Contributors: 
 * Tobias Schwarz (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.tests.testsubsystem.interfaces;

import org.eclipse.rse.core.subsystems.ISubSystem;

/**
 * Interface for the test subsystem.
 */
public interface ITestSubSystem extends ISubSystem, ITestSubSystemNodeContainer {
	// only for internal use
}
