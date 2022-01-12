/*******************************************************************************
 * Copyright (c) 2020 Marc-Andre Laperle.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.msw.build;

/**
 * Constants related to Visual Studio versions.
 */
interface IVSVersionConstants {
	VSVersionNumber VS2017_BASE_VER = new VSVersionNumber(15);
	VSVersionNumber VS2019_BASE_VER = new VSVersionNumber(16);
}