/*******************************************************************************
 * Copyright (c) 2009, 2011 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.core.runtime.CoreException;

/**
 * TODO - this interface assumes configuration description. CDT project
 * can be without configurations, in that case IProject should be passed
 * to startup somehow. Perhaps another interface "IPConsoleParser" could
 * be created when needed?
 *
 * @since 5.4
 */
public interface ICConsoleParser extends IConsoleParser {
	public void startup(ICConfigurationDescription cfgDescription) throws CoreException;
}
