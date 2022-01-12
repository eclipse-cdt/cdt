/*******************************************************************************
 * Copyright (c) 2008, 2010 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.gnu.ui;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class GprofAppCalculator extends ProfAppCalculator {
	@Override
	protected String getOptionIdPattern() {
		return ".compiler.option.debugging.gprof"; //$NON-NLS-1$
	}
}
