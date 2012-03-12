/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.automake;

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
