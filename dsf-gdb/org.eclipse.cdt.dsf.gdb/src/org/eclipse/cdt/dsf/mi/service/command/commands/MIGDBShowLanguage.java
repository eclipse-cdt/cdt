/*******************************************************************************
 * Copyright (c) 2013  AdaCore and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Philippe Gil (AdaCore) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIGDBShowLanguageInfo;

/**
 * 
 *     -gdb-show language
 *     
 * @since 4.3
 * 
 */
public class MIGDBShowLanguage extends MIGDBShow<MIGDBShowLanguageInfo>
{
    public MIGDBShowLanguage(IDMContext ctx) {
        super(ctx, new String[] {"language"}); //$NON-NLS-1$
    }

    @Override
	public MIGDBShowLanguageInfo getResult(MIOutput miResult) {
		return new MIGDBShowLanguageInfo(miResult);
	}
}