/*******************************************************************************
 * Copyright (c) 2010 CodeSourcery and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * CodeSourcery - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MITargetDownloadInfo;

/**
 * This command downloads a file to a remote target.
 * 
 * @since 3.0
 */
public class MITargetDownload extends MICommand<MITargetDownloadInfo> {

    public MITargetDownload(ICommandControlDMContext ctx) {
        super(ctx, "-target-download"); //$NON-NLS-1$
    }

    public MITargetDownload(ICommandControlDMContext ctx, String file) {
        super(ctx, "-target-download", null, new String[] { file }); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.dsf.mi.service.command.commands.MICommand#getResult(org.eclipse.cdt.dsf.mi.service.command.output.MIOutput)
     */
    @Override
    public MIInfo getResult( MIOutput MIresult ) {
        return new MITargetDownloadInfo( MIresult );
    }
}
