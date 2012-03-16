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
import java.util.Map;

public interface IAConfiguration {
	public IConfigureOption getOption(String name);
	public String getId();
	public boolean isDirty();
	public void setDirty(boolean value);
	public Map<String, IConfigureOption> getOptions();
	public String getToolParameters(String name);
	public ArrayList<String> getToolArgs(String name);
	public void setOption(String name, String value);
	public void setConfigToolDirectory(String configToolDirectory);
	public String getConfigToolDirectory();
	public IAConfiguration copy();
	public IAConfiguration copy(String id);
	public void setDefaultOptions();
}
