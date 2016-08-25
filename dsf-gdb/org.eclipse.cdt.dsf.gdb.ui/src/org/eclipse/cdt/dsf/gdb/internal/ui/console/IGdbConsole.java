/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.ui.console.IConsole;

public interface IGdbConsole extends IConsole {
	ILaunch getLaunch();
    void resetName();
    void setInvertedColors(boolean enable);

}
