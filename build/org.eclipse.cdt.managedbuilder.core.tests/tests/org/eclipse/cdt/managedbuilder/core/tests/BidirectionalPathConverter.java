/*******************************************************************************
 * Copyright (c) 2007, 2011 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core.tests;

import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IReverseOptionPathConverter;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class BidirectionalPathConverter extends OneDirectionalPathConverter implements IReverseOptionPathConverter {

	@Override
	public String convertToOptionValue(ICSettingEntry entry, IOption option, ITool tool) {
		String name = entry.getName();
		IPath path = new Path(name);

		if (PREFIX.isPrefixOf(path))
			return path.removeFirstSegments(PREFIX.segmentCount()).makeAbsolute().toString();
		else if (!path.isAbsolute())
			path = new Path("../" + path.toString());
		return path.toString();
	}

}
