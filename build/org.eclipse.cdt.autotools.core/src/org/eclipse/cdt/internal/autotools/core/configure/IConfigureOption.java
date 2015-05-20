/*******************************************************************************
 * Copyright (c) 2009 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.core.configure;

import java.util.ArrayList;

import org.eclipse.cdt.autotools.core.IAutotoolsOption;

public interface IConfigureOption {
	
	public final static int CATEGORY = IAutotoolsOption.CATEGORY;
	public final static int BIN = IAutotoolsOption.BIN;
	public final static int STRING = IAutotoolsOption.STRING;
	public final static int INTERNAL = IAutotoolsOption.INTERNAL;
	public final static int MULTIARG = IAutotoolsOption.MULTIARG;
	public final static int TOOL = IAutotoolsOption.TOOL;
	public final static int FLAG = IAutotoolsOption.FLAG;
	public final static int FLAGVALUE = IAutotoolsOption.FLAGVALUE;
	/**
	 * @since 1.5
	 */
	public final static int VARIABLE = IAutotoolsOption.VARIABLE;
	public String getName();
	public String getParameter();
	public ArrayList<String> getParameters();
	public boolean isParmSet();
	public String getDescription();
	public String getToolTip();
	public void setValue(String value);
	public IConfigureOption copy(AutotoolsConfiguration cfg);
	public String getValue();
	public boolean isCategory();
	public boolean isMultiArg();
	public boolean isFlag();
	public boolean isFlagValue();
	public int getType();
}
