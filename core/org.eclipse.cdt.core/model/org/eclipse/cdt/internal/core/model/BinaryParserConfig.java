/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.AbstractCExtension;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.settings.model.ICConfigExtensionReference;
import org.eclipse.cdt.core.settings.model.util.CExtensionUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

/*
 * BinaryParserConfig
 */
public class BinaryParserConfig {
	private IBinaryParser parser;
	private final String id;
	private final ICConfigExtensionReference ref;

	public BinaryParserConfig(IBinaryParser parser, String id) {
		this.parser = parser;
		this.id = id;
		this.ref = null;
	}

	public BinaryParserConfig(ICConfigExtensionReference ref) {
		this.ref = ref;
		this.id = ref.getID();
	}

	public String getId() {
		return id;
	}

	public IBinaryParser getBinaryParser() throws CoreException {
		if (parser == null) {
			AbstractCExtension cExtension = null;
			IConfigurationElement el = CExtensionUtil.getFirstConfigurationElement(ref, "cextension", false); //$NON-NLS-1$
			cExtension = (AbstractCExtension) el.createExecutableExtension("run"); //$NON-NLS-1$
			cExtension.setExtensionReference(ref);
			cExtension.setProject(ref.getConfiguration().getProjectDescription().getProject());
			parser = (IBinaryParser) cExtension;
		}
		return parser;
	}
}
