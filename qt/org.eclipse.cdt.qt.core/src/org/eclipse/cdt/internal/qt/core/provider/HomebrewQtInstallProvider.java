package org.eclipse.cdt.internal.qt.core.provider;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.cdt.internal.qt.core.QtInstall;
import org.eclipse.cdt.qt.core.IQtInstall;
import org.eclipse.cdt.qt.core.IQtInstallProvider;
import org.eclipse.core.runtime.Platform;

/**
 * QtInstall provider for qt out of Homebrew. Unfortunately they don't put it on the path so we have
 * to look where they put it.
 */
public class HomebrewQtInstallProvider implements IQtInstallProvider {

	@Override
	public Collection<IQtInstall> getInstalls() {
		if (Platform.getOS().equals(Platform.OS_MACOSX)) {
			Path qmakePath = Paths.get("/usr/local/opt/qt5/bin/qmake"); //$NON-NLS-1$
			if (Files.exists(qmakePath)) {
				return Arrays.asList(new QtInstall(qmakePath));
			}
		}
		return Collections.emptyList();
	}

}
