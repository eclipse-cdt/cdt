/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

public abstract class AbstractChecker implements IChecker {
	String name;

	public AbstractChecker() {
	}

	public boolean enabledInContext(IResource res) {
		return true;
	}

	public void reportProblem(String id, IFile file, int lineNumber,
			String message) {
		ErrorReporter.reportProblem(id, file, lineNumber, message);
	}
}
