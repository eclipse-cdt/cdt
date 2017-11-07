package org.eclipse.cdt.build.gcc.core;

import java.nio.file.Path;

import org.eclipse.cdt.core.build.IToolChainProvider;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;

/**
 * The Clang toolchain. There's little different from the GCC toolchain other
 * than the toolchain type and name.
 * 
 * @author dschaefer
 *
 */
public class ClangToolChain extends GCCToolChain {

	private static final String TYPE_ID = "org.eclipse.cdt.build.clang"; //$NON-NLS-1$

	public ClangToolChain(IToolChainProvider provider, Path pathToToolChain, String arch,
			IEnvironmentVariable[] envVars) {
		super(provider, pathToToolChain, arch, envVars);
	}

	@Override
	public String getTypeId() {
		return TYPE_ID;
	}

}
