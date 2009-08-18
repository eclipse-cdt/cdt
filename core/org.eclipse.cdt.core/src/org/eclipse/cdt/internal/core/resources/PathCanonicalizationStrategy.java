/*******************************************************************************
 * Copyright (c) 2009 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.resources;

import java.io.File;
import java.io.IOException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.internal.core.pdom.PDOMManager;

public abstract class PathCanonicalizationStrategy {

	public static String getCanonicalPath(File file) {
		PathCanonicalizationStrategy strategy =
			((PDOMManager) CCorePlugin.getIndexManager()).getPathCanonicalizationStrategy();
		return strategy.getCanonicalPathInternal(file);
	}

	public static PathCanonicalizationStrategy getStrategy(boolean canonicalize) {
		if (canonicalize) {
			return new PathCanonicalizationStrategy() {
				@Override
				protected String getCanonicalPathInternal(File file) {
					try {
						return file.getCanonicalPath();
					} catch (IOException e) {
						return file.getAbsolutePath();
					}
				}
			};
		} else {
			return new PathCanonicalizationStrategy() {
				@Override
				protected String getCanonicalPathInternal(File file) {
					return file.getAbsolutePath();
				}
			};
		}
	}

	protected abstract String getCanonicalPathInternal(File file);
}
