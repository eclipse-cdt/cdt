package org.eclipse.cdt.internal.qt.core.build;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

import org.eclipse.cdt.internal.qt.core.QtPlugin;
import org.eclipse.core.runtime.Platform;

public class QtInstall {

	private final Path qmakePath;
	private String spec;

	public QtInstall(Path qmakePath) {
		this.qmakePath = qmakePath;
	}

	public Path getQmakePath() {
		return qmakePath;
	}

	public Path getLibPath() {
		return qmakePath.resolve("../lib"); //$NON-NLS-1$
	}

	public boolean supports(String os, String arch) {
		switch (getSpec()) {
		case "macx-clang": //$NON-NLS-1$
			return Platform.OS_MACOSX.equals(os) && Platform.ARCH_X86_64.equals(arch);
		}
		return false;
	}

	public String getSpec() {
		if (spec == null) {
			try {
				Process proc = new ProcessBuilder(getQmakePath().toString(), "-query", "QMAKE_XSPEC").start(); //$NON-NLS-1$ //$NON-NLS-2$
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
					String line = reader.readLine();
					if (line != null) {
						spec = line.trim();
					}
				}
			} catch (IOException e) {
				QtPlugin.log(e);
			}
		}
		return spec;
	}

}
