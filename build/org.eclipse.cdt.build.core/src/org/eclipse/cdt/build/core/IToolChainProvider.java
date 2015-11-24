package org.eclipse.cdt.build.core;

import java.util.Collection;

public interface IToolChainProvider {

	Collection<IToolChain> getToolChains();

}
