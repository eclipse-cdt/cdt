/*******************************************************************************
 * Copyright (c) 2008, 2016 others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stefan Sprenger
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIFileListExecSourceFilesInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * -file-list-exec-source-files
 * 
 *   (gdb)
 *    -file-list-exec-source-files
 *    ^done,files=[
 *    {file=foo.c,fullname=/home/foo.c},
 *    {file=/home/bar.c,fullname=/home/bar.c},
 *    {file=gdb_could_not_find_fullpath.c}]
     (gdb)
 * 
 * @since 5.0
 * 
 */
public class MIFileListExecSourceFiles <V extends MIFileListExecSourceFilesInfo> extends MICommand<V> 
{
    public MIFileListExecSourceFiles(IMIContainerDMContext dmc) {
    	this(dmc, null);
    }

    public MIFileListExecSourceFiles(IMIContainerDMContext dmc, String file) {
        super(dmc, "-file-list-exec-source-files", null, null); //$NON-NLS-1$
    }
    
    @Override
    public MIFileListExecSourceFilesInfo getResult(MIOutput output) {
        return new MIFileListExecSourceFilesInfo(output);
    }
    
}
