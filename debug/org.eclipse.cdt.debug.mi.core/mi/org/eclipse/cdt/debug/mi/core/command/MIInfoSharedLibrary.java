package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfoSharedLibraryInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;

public class MIInfoSharedLibrary extends MICommand {

	public MIInfoSharedLibrary(String miVersion) {
		super(miVersion, "info sharedlibrary"); //$NON-NLS-1$
	}

	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIInfoSharedLibraryInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}

	public MIInfoSharedLibraryInfo getMIInfoSharedLibraryInfo() throws MIException {
		return (MIInfoSharedLibraryInfo) getMIInfo();
	}

}
