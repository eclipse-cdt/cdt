/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.core.build;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.resources.ACBuilder;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.internal.qt.core.Activator;
import org.eclipse.cdt.internal.qt.core.Messages;
import org.eclipse.cdt.qt.core.IQtBuildConfiguration;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

public class QtBuilder extends ACBuilder {

	public static final String ID = Activator.ID + ".qtBuilder"; //$NON-NLS-1$

	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		try {
			IConsole console = CCorePlugin.getDefault().getConsole();
			ConsoleOutputStream errStream = console.getErrorStream();

			ICBuildConfiguration cconfig = getBuildConfig().getAdapter(ICBuildConfiguration.class);
			IQtBuildConfiguration qtConfig = cconfig.getAdapter(QtBuildConfiguration.class);
			if (qtConfig == null) {
				// Qt hasn't been configured yet print a message and bale
				errStream.write(Messages.QtBuilder_0);
				return null;
			}
			IToolChain toolChain = qtConfig.getToolChain();

			ICommandLauncher launcher = new CommandLauncher();
			ConsoleOutputStream outStream = console.getOutputStream();
			ErrorParserManager epm = new ErrorParserManager(project, qtConfig.getBuildDirectory().toUri(),
					this, toolChain.getErrorParserIds());
			epm.setOutputStream(outStream);

			Path buildDir = qtConfig.getBuildDirectory();
			if (!buildDir.resolve("Makefile").toFile().exists()) { //$NON-NLS-1$
				// Need to run qmake
				List<String> arglist = new ArrayList<>();

				String config = qtConfig.getQmakeConfig();
				if (config != null) {
					arglist.add(config);
				}

				IFile projectFile = qtConfig.getBuildConfiguration().getProject().getFile(project.getName() + ".pro"); //$NON-NLS-1$
				arglist.add(projectFile.getLocation().toOSString());

				String[] env = null; // TODO

				launcher.execute(qtConfig.getQmakeCommand(),
						arglist.toArray(new String[arglist.size()]), env, qtConfig.getBuildDirectory(), monitor);
				StringBuffer msg = new StringBuffer();
				msg.append(qtConfig.getQmakeCommand().toString()).append(' ');
				for (String arg : arglist) {
					msg.append(arg).append(' ');
				}
				msg.append('\n');
				outStream.write(msg.toString());
				launcher.waitAndRead(outStream, errStream, monitor);
			}

			// run make
			// TODO obviously hardcoding here
			boolean isWin = Platform.getOS().equals(Platform.OS_WIN32);
			String make = isWin ? "C:/Qt/Tools/mingw492_32/bin/mingw32-make" : "make"; //$NON-NLS-1$ //$NON-NLS-2$
			ProcessBuilder procBuilder = new ProcessBuilder(make).directory(buildDir.toFile());
			if (isWin) {
				// Need to put the toolchain into env
				Map<String, String> env = procBuilder.environment();
				String path = env.get("PATH"); //$NON-NLS-1$
				path = "C:/Qt/Tools/mingw492_32/bin;" + path; //$NON-NLS-1$
				env.put("PATH", path); //$NON-NLS-1$
			}
			Process process = procBuilder.start();
			outStream.write("make\n"); //$NON-NLS-1$
			//console.monitor(process, null, buildDir);

			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);

			// clear the scanner info cache
			// TODO be more surgical about what to clear based on what was
			// built.
			//qtConfig.clearScannerInfoCache();

			outStream.write("Complete.\n");
			return new IProject[] { project };
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.ID, "Building " + project.getName(), e)); //$NON-NLS-1$
		}
	}

	private void monitorProcess(Process process, OutputStream outStream) {
		new Thread() {

		};
	}

}
