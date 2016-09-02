/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.IModules.ISymbolDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIFIleListSharedLibrariesInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;


/**
 * -file-list-shared-libraries [ @var{regexp}]
 * 
 * List the shared libraries in the program. The corresponding CLI command is
 * 'info shared'.
 * 
 * @since 5.1
 */
public class MIFileListSharedLibraries extends MICommand<MIFIleListSharedLibrariesInfo>
{
    /**
     * @since 1.1
     */
    public MIFileListSharedLibraries(ISymbolDMContext dmc) {
        super(dmc, "-file-list-shared-libraries"); //$NON-NLS-1$
    }
    
    @Override
    public MIFIleListSharedLibrariesInfo getResult(MIOutput out)  {
        return new MIFIleListSharedLibrariesInfo(out);
    }
}
