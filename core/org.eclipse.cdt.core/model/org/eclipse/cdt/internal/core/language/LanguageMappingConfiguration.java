/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.language;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class LanguageMappingConfiguration {

	private Map fProjectMappings;

	public LanguageMappingConfiguration() {
		fProjectMappings = new TreeMap();
	}
	
	public Map getProjectMappings() {
		return Collections.unmodifiableMap(fProjectMappings);
	}

	public void setProjectMappings(Map projectMappings) {
		fProjectMappings = projectMappings;
	}

	public void addProjectMapping(String contentType, String language) {
		fProjectMappings.put(contentType, language);
	}

	public void removeProjectMapping(String contentType) {
		fProjectMappings.remove(contentType);
	}

}
