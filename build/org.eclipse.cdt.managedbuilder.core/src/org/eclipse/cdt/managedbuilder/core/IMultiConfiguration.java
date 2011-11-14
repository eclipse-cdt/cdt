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
import org.eclipse.cdt.newmake.core.IMakeCommonBuildInfo;

/**
 * This class is to combine multiple configurations to one to support
 * selection of multiple configurations on property pages.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IMultiConfiguration extends IConfiguration, ICMultiItemsHolder {
	
	/**
	 * Check if the configuration's builder is operating in parallel mode.
	 * @return {@code true} if parallel mode is enabled, {@code false} otherwise.
	 */
	boolean getParallelDef();
	/**
	 * Set parallel execution mode for the configuration's builder.
	 * @see Builder#setParallelBuildOn(boolean)
	 * 
	 * @param parallel - the flag to enable or disable parallel mode.
	 */
	void setParallelDef(boolean parallel);

	/**
	 * Returns maximum number of parallel threads/jobs used by the configuration's builder.
	 * Note that this function can return negative value to indicate  "optimal" number.
	 * 
	 * @see #setParallelDef(boolean)
	 * @see Builder#getParallelizationNum()
	 * 
	 * @return - maximum number of parallel threads or jobs used by the builder or negative number.
	 *    For exact interpretation see table in {@link IMakeCommonBuildInfo#getParallelizationNum()}
	 */
	int getParallelNumber();
	/**
	 * Sets maximum number of parallel threads/jobs to be used by builder.
	 * Note that the number will be set only if the builder is in "parallel"
	 * mode.
	 * 
	 * @param jobs - maximum number of jobs or threads. If the number is 0
	 *    or negative, negative "optimal" number will be set, see
	 *    {@link Builder#getOptimalParallelJobNum()}.
	 */
	void setParallelNumber(int jobs);
	
	/**
	 * returns the Internal Builder parallel mode
	 * if true, internal builder will work in parallel mode 
	 * otherwise it will use only one thread
	 * @return boolean
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
