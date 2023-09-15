/*******************************************************************************
 * Copyright (c) 2023 John Dallaway and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * John Dallaway - Initial API and implementation (#361)
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.macros;

import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariableStatus;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.utils.IGnuToolFactory;
import org.eclipse.core.runtime.Status;

/**
 * A CDT build variable defining the GNU tool prefix for use by
 * an {@link org.eclipse.cdt.utils.IGnuToolFactory} implementation.
 *
 * @since 9.6
 */
public abstract class AbstractGnuToolPrefixMacro implements IBuildMacro {

	public static final String MACRO_NAME = IGnuToolFactory.GNU_TOOL_PREFIX_VARIABLE;

	@Override
	public final String getName() {
		return MACRO_NAME;
	}

	@Override
	public final int getValueType() {
		return IBuildMacro.VALUE_TEXT;
	}

	@Override
	public final int getMacroValueType() {
		return getValueType();
	}

	@Override
	public abstract String getStringValue() throws BuildMacroException;

	@Override
	public final String[] getStringListValue() throws BuildMacroException {
		throw new BuildMacroException(
				new CdtVariableException(ICdtVariableStatus.TYPE_MACRO_NOT_STRINGLIST, MACRO_NAME, null, MACRO_NAME));
	}

	protected String getStringValue(IOption option) throws BuildMacroException {
		try {
			return option.getStringValue();
		} catch (BuildException e) {
			throw new BuildMacroException(Status.error("Error getting macro value: " + MACRO_NAME, e)); //$NON-NLS-1$
		}
	}

}
