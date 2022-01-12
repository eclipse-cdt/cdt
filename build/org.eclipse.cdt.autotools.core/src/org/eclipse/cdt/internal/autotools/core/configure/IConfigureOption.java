/*******************************************************************************
 * Copyright (c) 2009, 2015 Red Hat Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.core.configure;

import java.util.List;

import org.eclipse.cdt.autotools.core.IAutotoolsOption;

public interface IConfigureOption {

	int CATEGORY = IAutotoolsOption.CATEGORY;
	int BIN = IAutotoolsOption.BIN;
	int STRING = IAutotoolsOption.STRING;
	int INTERNAL = IAutotoolsOption.INTERNAL;
	int MULTIARG = IAutotoolsOption.MULTIARG;
	int TOOL = IAutotoolsOption.TOOL;
	int FLAG = IAutotoolsOption.FLAG;
	int FLAGVALUE = IAutotoolsOption.FLAGVALUE;
	/**
	 * @since 2.0
	 */
	int ENVVAR = IAutotoolsOption.ENVVAR;

	String getName();

	String getParameter();

	List<String> getParameters();

	boolean isParmSet();

	String getDescription();

	String getToolTip();

	void setValue(String value);

	IConfigureOption copy(AutotoolsConfiguration cfg);

	String getValue();

	boolean isCategory();

	boolean isMultiArg();

	boolean isFlag();

	boolean isFlagValue();

	int getType();
}
