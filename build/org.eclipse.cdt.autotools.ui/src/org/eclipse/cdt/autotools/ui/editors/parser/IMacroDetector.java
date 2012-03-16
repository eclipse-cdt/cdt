/*******************************************************************************
 * Copyright (c) 2008 Nokia Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ed Swartz (Nokia) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.autotools.ui.editors.parser;

/**
 * Clients implement this interface to detect whether a given identifier 
 * represents a known or potential macro in m4 or configure.ac text.
 * @author eswartz
 *
 */
public interface IMacroDetector {
	boolean isMacro(String identifier);
}
