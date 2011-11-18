/*******************************************************************************
 * Copyright (c) 2007, 2011 Intel Corporation and others.
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
import org.eclipse.cdt.managedbuilder.internal.core.Builder;

/**
 * This class is to combine multiple configurations to one to support
 * selection of multiple configurations on property pages.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IMultiConfiguration extends IConfiguration, ICMultiItemsHolder {
	
	/**
	 * Check if all configurations' builders are operating in parallel mode.
	 * @return {@code true} if parallel mode is enabled for all configurations,
	 *    {@code false} otherwise.
	 */
	boolean getParallelDef();
	/**
	 * Set same parallel execution mode for all configurations' builders.
	 * @see Builder#setParallelBuildOn(boolean)
	 * 
	 * @param parallel - the flag to enable or disable parallel mode.
	 */
	void setParallelDef(boolean parallel);

	/**
	 * Returns maximum number of parallel threads/jobs used by the configurations' builders.
	 * @see #setParallelDef(boolean)
	 * 
	 * @return - maximum number of parallel threads or jobs used by each builder or 0 if the numbers
	 *    don't match. 
	 */
	int getParallelNumber();

	/**
	 * Sets maximum number of parallel threads/jobs to be used by each builder.
	 * 
	 * @param jobs - maximum number of jobs or threads, see for more details
	 *    {@link Builder#getOptimalParallelJobNum()}.
	 */
	void setParallelNumber(int jobs);
	
	/**
	 * Check if all configurations' internal builders are operating in parallel mode.
	 * @return {@code true} if parallel mode is enabled for all configurations,
	 *    {@code false} otherwise.
	 * 
	 * @deprecated since CDT 9.0. Use {@link #getParallelDef()}
	 */
	@Deprecated
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
