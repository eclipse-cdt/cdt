/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - initial API and implementation
 *******************************************************************************/

package org.eclipse.rse.tests.subsystems.files;

import org.eclipse.rse.subsystems.files.ftp.FTPFileSubSystemConfiguration;

public class FTPWindowsFileSubSystemConfiguration extends FTPFileSubSystemConfiguration {

	public FTPWindowsFileSubSystemConfiguration() {
		super();
		setIsUnixStyle(false);
	}

}
