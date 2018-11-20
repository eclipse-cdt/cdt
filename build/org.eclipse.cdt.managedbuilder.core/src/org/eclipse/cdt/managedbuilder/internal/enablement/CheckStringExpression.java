/*******************************************************************************
 * Copyright (c) 2005, 2016 Intel Corporation and others.
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
 * Miwako Tokugawa (Intel Corporation) - bug 222817 (OptionCategoryApplicability)
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.enablement;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.cdt.managedbuilder.internal.macros.OptionContextData;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;

public class CheckStringExpression implements IBooleanExpression {
	public static final String NAME = "checkString"; //$NON-NLS-1$

	public static final String STRING = "string"; //$NON-NLS-1$
	public static final String VALUE = "value"; //$NON-NLS-1$
	public static final String IS_REGEX = "isRegex"; //$NON-NLS-1$

	private String fString;
	private String fValue;
	private boolean fIsRegex;

	public CheckStringExpression(IManagedConfigElement element) {
		fString = element.getAttribute(STRING);
		if (fString == null)
			fString = ""; //$NON-NLS-1$

		fValue = element.getAttribute(VALUE);
		if (fValue == null)
			fValue = ""; //$NON-NLS-1$

		fIsRegex = OptionEnablementExpression.getBooleanValue(element.getAttribute(IS_REGEX));
	}

	@Override
	public boolean evaluate(IResourceInfo rcInfo, IHoldsOptions holder, IOption option) {

		IBuildMacroProvider provider = ManagedBuildManager.getBuildMacroProvider();
		IEnvironmentVariableProvider env = ManagedBuildManager.getEnvironmentVariableProvider();
		String delimiter = env.getDefaultDelimiter();
		try {
			String resolvedString = provider.resolveValue(fString, " ", //$NON-NLS-1$
					delimiter, IBuildMacroProvider.CONTEXT_OPTION, new OptionContextData(option, holder));

			String resolvedValue = provider.resolveValue(fValue, " ", //$NON-NLS-1$
					delimiter, IBuildMacroProvider.CONTEXT_OPTION, new OptionContextData(option, holder));

			if (fIsRegex) {
				Pattern pattern = Pattern.compile(resolvedValue);
				Matcher matcher = pattern.matcher(resolvedString);
				return matcher.matches();
			}
			return resolvedString.equals(resolvedValue);
		} catch (BuildMacroException e) {
		}
		return false;
	}

	@Override
	public boolean evaluate(IResourceInfo rcInfo, IHoldsOptions holder, IOptionCategory category) {

		IBuildMacroProvider provider = ManagedBuildManager.getBuildMacroProvider();
		IEnvironmentVariableProvider env = ManagedBuildManager.getEnvironmentVariableProvider();
		String delimiter = env.getDefaultDelimiter();
		try {
			String resolvedString = provider.resolveValue(fString, " ", //$NON-NLS-1$
					delimiter, IBuildMacroProvider.CONTEXT_OPTION, new OptionContextData(category, holder));

			String resolvedValue = provider.resolveValue(fValue, " ", //$NON-NLS-1$
					delimiter, IBuildMacroProvider.CONTEXT_OPTION, new OptionContextData(category, holder));

			if (fIsRegex) {
				Pattern pattern = Pattern.compile(resolvedValue);
				Matcher matcher = pattern.matcher(resolvedString);
				return matcher.matches();
			}
			return resolvedString.equals(resolvedValue);
		} catch (BuildMacroException e) {
		}
		return false;
	}

}
