package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MiSourceFilesInfo;

/**
 * 
 * -file-list-exec-source-files
 * 
 * Returns the list of source files for the current execution context. It
 * outputs both filename and full (absolute path) file name of a source file.
 * 
 * @since 5.5
 */
public class MiFileListExecSourceFiles extends MICommand<MiSourceFilesInfo> {

	public MiFileListExecSourceFiles(IDMContext ctx) {
		super(ctx, "-file-list-exec-source-files"); //$NON-NLS-1$
	}

	@Override
	public MiSourceFilesInfo getResult(MIOutput out) {
		return new MiSourceFilesInfo(out);
	}
}
