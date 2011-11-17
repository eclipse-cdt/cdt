/*******************************************************************************
 * Copyright (c) 2008, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.gnu.ui;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionApplicability;
import org.eclipse.cdt.managedbuilder.core.ITool;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class ProfAppCalculator implements IOptionApplicability {

	protected static final String COMPILER_PATTERN = ".compiler."; //$NON-NLS-1$

	protected String getOptionIdPattern() {
		return ".compiler.option.debugging.prof"; //$NON-NLS-1$
	}

	@Override
	public boolean isOptionEnabled(IBuildObject configuration,
			IHoldsOptions holder, IOption option) {
		return true;
	}

	@Override
	public boolean isOptionUsedInCommandLine(IBuildObject configuration,
			IHoldsOptions holder, IOption option) {

		if (! (configuration instanceof IConfiguration))
			return false; // not probable.

		IConfiguration cfg = (IConfiguration)configuration;
	outer:
		for (ITool t : cfg.getFilteredTools()){
			if (t.getId().indexOf(COMPILER_PATTERN) < 0)
				continue;
			for (IOption op : t.getOptions()) {
				if (op.getId().indexOf(getOptionIdPattern()) < 0)
					continue;
				try {
					if (op.getBooleanValue() != option.getBooleanValue())
						cfg.setOption(holder, option, op.getBooleanValue());
				} catch (BuildException e) {}
				break outer;
			}
		}
		return true;
	}

	@Override
	public boolean isOptionVisible(IBuildObject configuration,
			IHoldsOptions holder, IOption option) {
		return false;
	}

}
