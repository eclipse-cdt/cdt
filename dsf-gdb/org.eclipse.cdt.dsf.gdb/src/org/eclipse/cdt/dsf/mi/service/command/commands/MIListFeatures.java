/*******************************************************************************
 * Copyright (c) 2010 CodeSourcery and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Prus (CodeSourcery) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIListFeaturesInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;


/**
 * -list-features
 * 
 * Returns a list of particular features of the MI protocol that this
 * version of gdb implements.  A feature can be a command, or a new field
 * in an output of some command, or even an important bugfix.  While a
 * frontend can sometimes detect presence of a feature at runtime, it is
 * easier to perform detection at debugger startup.
 * 
 * The command returns a list of strings, with each string naming an
 * available feature.  Each returned string is just a name, it does not
 * have any internal structure.  
 * @since 4.0
 */
public class MIListFeatures extends MICommand<MIListFeaturesInfo> {

	public MIListFeatures(ICommandControlDMContext ctx) {
		super(ctx, "-list-features"); //$NON-NLS-1$
	}

	@Override
	public MIListFeaturesInfo getResult(MIOutput out) {
		return new MIListFeaturesInfo(out);
	}
}
