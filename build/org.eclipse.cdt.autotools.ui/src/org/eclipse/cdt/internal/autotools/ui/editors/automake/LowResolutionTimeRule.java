/*******************************************************************************
 * Copyright (c) 2000, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import org.eclipse.cdt.make.core.makefile.gnu.ILowResolutionTimeRule;

/**
 * .LOW_RESOLUTION_TIME'
 *  If you specify prerequisites for `.LOW_RESOLUTION_TIME', `make'
 *  assumes that these files are created by commands that generate low
 *  resolution time stamps.
 */
public class LowResolutionTimeRule extends SpecialRule implements ILowResolutionTimeRule {

	public LowResolutionTimeRule(Directive parent, String[] reqs) {
		super(parent, new Target(GNUMakefileConstants.RULE_LOW_RESOLUTION_TIME), reqs, new Command[0]);
	}

}
