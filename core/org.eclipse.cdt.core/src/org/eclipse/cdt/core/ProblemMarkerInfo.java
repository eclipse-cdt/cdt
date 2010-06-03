/*******************************************************************************
 * Copyright (c) 2006, 2009 Siemens AG.
 * All rights reserved. This content and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Norbert Ploett - Initial implementation
 *******************************************************************************/

package org.eclipse.cdt.core;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public  class ProblemMarkerInfo {
		
		public IResource file;
		public int lineNumber;
		public String description;
		public int severity;
		public String variableName;
		public IPath externalPath ;

		public ProblemMarkerInfo(IResource file, int lineNumber, String desciption, int severity, String variableName) {
			this.file = file;
			this.lineNumber = lineNumber;
			this.description = desciption;
			this.severity = severity;
			this.variableName = variableName;
			this.externalPath = null ;
		}


		public ProblemMarkerInfo(IResource file, int lineNumber, String description, int severity, String variableName, IPath externalPath) {
			super();
			this.file = file;
			this.lineNumber = lineNumber;
			this.description = description;
			this.severity = severity;
			this.variableName = variableName;
			this.externalPath = externalPath;
		}
		
}