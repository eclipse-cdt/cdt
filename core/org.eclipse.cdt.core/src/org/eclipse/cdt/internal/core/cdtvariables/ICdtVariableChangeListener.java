/*******************************************************************************
 * Copyright (c) 2007, 2013 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.cdtvariables;

/**
 * Listener to listen to Build Variables changes events.
 */
public interface ICdtVariableChangeListener {
	void variablesChanged(VariableChangeEvent event);
}
