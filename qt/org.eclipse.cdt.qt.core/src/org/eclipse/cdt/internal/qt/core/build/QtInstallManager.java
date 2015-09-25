package org.eclipse.cdt.internal.qt.core.build;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class QtInstallManager {

	public static final QtInstallManager instance = new QtInstallManager();

	private List<QtInstall> installs;

	public List<QtInstall> getInstalls() {
		if (installs == null) {
			installs = new ArrayList<>();
			// TODO hack to get going
			File qtDir = new File(System.getProperty("user.home"), "Qt/5.5");
			if (qtDir.isDirectory()) {
				for (File dir : qtDir.listFiles()) {
					Path qmakePath = dir.toPath().resolve("bin/qmake");
					if (qmakePath.toFile().canExecute()) {
						installs.add(new QtInstall(qmakePath));
					}
				}
			}
		}

		return installs;
	}

}
