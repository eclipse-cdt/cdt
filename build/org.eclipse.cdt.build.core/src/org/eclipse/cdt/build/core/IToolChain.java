package org.eclipse.cdt.build.core;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.parser.IExtendedScannerInfo;
import org.eclipse.core.resources.IResource;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.osgi.service.prefs.Preferences;

public interface IToolChain {

	IToolChainType getType();

	String getName();

	boolean supports(ILaunchTarget target);

	IExtendedScannerInfo getScannerInfo(String command, List<String> args, List<String> includePaths,
			IResource resource, Path buildDirectory) throws IOException;

	Collection<CConsoleParser> getConsoleParsers();

	void setEnvironment(Map<String, String> env);

	void save(Preferences properties);

}
