/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core;

import org.eclipse.cdt.core.settings.model.ICMultiItemsHolder;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildProperty;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyValue;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public interface IMultiConfiguration extends IConfiguration, ICMultiItemsHolder {
	
	boolean getParallelDef();
	void    setParallelDef(boolean def);
	
	int     getParallelNumber();
	void    setParallelNumber(int num);
	
	boolean getInternalBuilderParallel();

	boolean isInternalBuilderEnabled();
	boolean canEnableInternalBuilder(boolean v);
	void    enableInternalBuilder(boolean v);
	
	void    setOutputPrefixForPrimaryOutput(String pref);
	String  getToolOutputPrefix();
	
	IBuildProperty getBuildProperty(String id);
	void    setBuildProperty(String id, String val);
	IBuildPropertyValue[] getSupportedValues(String id);
}
