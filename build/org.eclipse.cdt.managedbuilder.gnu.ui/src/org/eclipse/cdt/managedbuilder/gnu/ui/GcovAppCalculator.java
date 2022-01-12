/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 *
 *   *Based on implementation of GprofAppCalculator*
 *
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    lufimtse :  Leo Ufimtsev lufimtse@redhat.com
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.gnu.ui;

/**
 * @since 8.3
 */
public class GcovAppCalculator extends ProfAppCalculator {
	@Override
	protected String getOptionIdPattern() {
		return ".compiler.option.debugging.codecov"; //$NON-NLS-1$
	}
}
