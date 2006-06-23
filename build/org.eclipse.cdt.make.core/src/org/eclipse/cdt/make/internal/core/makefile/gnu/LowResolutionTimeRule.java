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
package org.eclipse.cdt.make.internal.core.makefile.gnu;

import org.eclipse.cdt.make.core.makefile.gnu.ILowResolutionTimeRule;
import org.eclipse.cdt.make.internal.core.makefile.Command;
import org.eclipse.cdt.make.internal.core.makefile.Directive;
import org.eclipse.cdt.make.internal.core.makefile.SpecialRule;
import org.eclipse.cdt.make.internal.core.makefile.Target;

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
