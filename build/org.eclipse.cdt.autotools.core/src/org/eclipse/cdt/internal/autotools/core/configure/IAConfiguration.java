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
