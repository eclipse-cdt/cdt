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

import org.eclipse.cdt.make.core.makefile.ISuffixesRule;

/**
 * .SUFFIXES
 * Prerequisites of .SUFFIXES shall be appended to the list of known suffixes and are
 * used in conjunction with the inference rules.
 *
 */
public class SuffixesRule extends SpecialRule implements ISuffixesRule {

	public SuffixesRule(Directive parent, String[] suffixes) {
		super(parent, new Target(MakeFileConstants.RULE_SUFFIXES), suffixes, new Command[0]);
	}

}
