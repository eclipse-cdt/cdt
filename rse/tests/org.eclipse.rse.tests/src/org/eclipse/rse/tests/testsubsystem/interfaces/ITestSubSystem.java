/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
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
