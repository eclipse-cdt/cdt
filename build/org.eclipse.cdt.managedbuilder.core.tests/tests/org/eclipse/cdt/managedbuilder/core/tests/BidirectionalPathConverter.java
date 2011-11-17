/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	public String convertToOptionValue(ICSettingEntry entry, IOption option,
			ITool tool) {
		String name = entry.getName();
		IPath path = new Path(name);

		if(PREFIX.isPrefixOf(path))
			return path.removeFirstSegments(PREFIX.segmentCount()).makeAbsolute().toString();
		else if (!path.isAbsolute())
			path = new Path("../" + path.toString());
		return path.toString();
	}

}
