package org.eclipse.cdt.internal.qt.core.build;

import java.io.IOException;

public interface IConsoleService {

	// TODO add error parsers
	void monitor(Process process) throws IOException;

	void writeOutput(String msg) throws IOException;

	void writeError(String msg) throws IOException;

}
