package org.eclipse.cdt.build.core;

import java.util.Collection;

import org.eclipse.launchbar.core.target.ILaunchTarget;

public interface IToolChainManager {

	IToolChainType getToolChainType(String id);

	IToolChain getToolChain(String typeId, String name);

	Collection<IToolChain> getToolChainsSupporting(ILaunchTarget target);

}
