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

import org.eclipse.cdt.make.core.makefile.gnu.IIntermediateRule;
import org.eclipse.cdt.make.internal.core.makefile.Command;
import org.eclipse.cdt.make.internal.core.makefile.Directive;
import org.eclipse.cdt.make.internal.core.makefile.SpecialRule;
import org.eclipse.cdt.make.internal.core.makefile.Target;

/**
 * .INTERMEDIATE
 *   The targets which `.INTERMEDIATE' depends on are treated as intermediate files.
 *   `.INTERMEDIATE' with no prerequisites has no effect.
 */
public class IntermediateRule extends SpecialRule implements IIntermediateRule {

	public IntermediateRule(Directive parent, String[] reqs) {
		super(parent, new Target(GNUMakefileConstants.RULE_INTERMEDIATE), reqs, new Command[0]);
	}

}
