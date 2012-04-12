/*******************************************************************************
 * Copyright (c) 2012, 2012 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.language.settings.providers;

import java.net.URI;

/**
 * Interface for console parsers able to track current working directory for build.
 *
 * @since 5.4
 */
public interface IWorkingDirectoryTracker {
	/**
	 * Returns current working directory for the current build command as determined from
	 * build output.
	 *
	 * @return URI of current working directory or {@code null}.
	 */
	public URI getWorkingDirectoryURI();
}
