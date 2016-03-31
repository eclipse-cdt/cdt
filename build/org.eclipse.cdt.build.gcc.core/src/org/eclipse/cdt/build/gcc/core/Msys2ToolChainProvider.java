package org.eclipse.cdt.build.gcc.core;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainProvider;

public class Msys2ToolChainProvider implements IToolChainProvider {

	@Override
	public Collection<IToolChain> getToolChains() {
		return Collections.emptyList();
	}

}
