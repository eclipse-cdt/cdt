/*
 * Copyright (c) 2014, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core.index;

/**
 * Represents a QMake environment similarly to IQMakeEnv but it has an explicit init method which is called to notify
 * that the IQMakeEnv is used for active listening on change of QMakeEnvInfo and therefore it should initialize
 * its listener for such changes.
 *
 * Note that it is expected that IQMakeEnv2 does complete initialization in init method - not in the constructor
 * i.e. the IQMakeEnv2 instance should start listening on possible changes of its IQMakeEnvInfo.
 */
public interface IQMakeEnv2 extends IQMakeEnv {

	/**
	 * Notifies that this IQMakeEnv is used.
	 * This method should not use any workspace-lock or sync-locks that might call QMake-related structures.
	 */
	void init();

}
