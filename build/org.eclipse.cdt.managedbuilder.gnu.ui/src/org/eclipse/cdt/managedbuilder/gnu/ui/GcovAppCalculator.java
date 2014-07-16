/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 *
 *   *Based on implementation of GprofAppCalculator*
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
