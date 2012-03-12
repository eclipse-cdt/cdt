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

public interface IConfigureOption {
	
	public final static int CATEGORY = 0;
	public final static int BIN = 1;
	public final static int STRING = 2;
	public final static int INTERNAL = 3;
	public final static int MULTIARG = 4;
	public final static int TOOL = 5;
	public final static int FLAG = 6;
	public final static int FLAGVALUE = 7;
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
