/*******************************************************************************
 * Copyright (c) 2009, 2015 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.core.configure;

import java.util.List;
import java.util.Map;

public interface IAConfiguration {
	IConfigureOption getOption(String name);

	String getId();

	boolean isDirty();

	void setDirty(boolean value);

	Map<String, IConfigureOption> getOptions();

	String getToolParameters(String name);

	List<String> getToolArgs(String name);

	void setOption(String name, String value);

	void setConfigToolDirectory(String configToolDirectory);

	String getConfigToolDirectory();

	IAConfiguration copy();

	IAConfiguration copy(String id);

	void setDefaultOptions();
}
