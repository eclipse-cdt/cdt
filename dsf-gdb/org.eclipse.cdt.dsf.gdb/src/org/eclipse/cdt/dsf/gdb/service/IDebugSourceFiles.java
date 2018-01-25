package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.service.IDsfService;

/**
 * Provides the ability to obtain the list of source files for the given debug
 * context. For GDB this is using the -file-list-exec-source-files command
 * 
 * @since 5.5
 */
public interface IDebugSourceFiles extends IDsfService {

	/**
	 * Data type for what is returned by
	 * {@link IDebugSourceFiles#getSources(IDMContext, DataRequestMonitor)}
	 */
	public interface IDebugSourceFileInfo {

		/**
		 * The name of the source file as it appears in the debug information. This may
		 * be relative, just the name, or absolute. Use {@link #getPath()} for the
		 * absolute path to the file name.
		 * 
		 * @return name of the file
		 */
		public String getName();

		/**
		 * The absolute path of the the file.
		 * 
		 * @return path to the file
		 */
		public String getPath();
	}

	/**
	 * Retrieves the list of sources data/files for the given context.
	 *
	 * @param context
	 *            execution context
	 * @param rm
	 *            Request completion monitor.
	 */
	void getSources(IDMContext context, DataRequestMonitor<IDebugSourceFileInfo[]> rm);
}
