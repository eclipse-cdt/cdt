/*******************************************************************************
 * Copyright (c) 2010, 2015 Red Hat Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.core;

import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.autotools.core.AutotoolsPlugin;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.remote.core.RemoteCommandLauncher;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IMarkerResolution;

public class PkgconfigErrorResolution implements IMarkerResolution {

	private static class ConsoleOutputStream extends OutputStream {

		protected StringBuffer fBuffer;

		public ConsoleOutputStream() {
			fBuffer = new StringBuffer();
		}

		public synchronized String readBuffer() {
			String buf = fBuffer.toString();
			fBuffer.setLength(0);
			return buf;
		}

		@Override
		public synchronized void write(int c) {
			byte ascii[] = new byte[1];
			ascii[0] = (byte) c;
			fBuffer.append(new String(ascii));
		}

		@Override
		public synchronized void write(byte[] b, int off, int len) {
			fBuffer.append(new String(b, off, len));
		}
	}

	private static final String PKG_UPDATE_MSG = "UpdatePackage.msg"; //$NON-NLS-1$
	private String pkgName;

	public PkgconfigErrorResolution(String pkgconfigRequirement) {
		// Get the pkgconfig package name from the requirement message.
		Pattern p = Pattern.compile("(.*?)[\\s,>,<,=].*");
		Matcher m = p.matcher(pkgconfigRequirement);
		if (m.matches()) {
			pkgName = m.group(1);
		}
	}

	@Override
	public String getLabel() {
		return AutotoolsPlugin.getFormattedString(PKG_UPDATE_MSG, new String[] { pkgName });
	}

	@Override
	public void run(IMarker marker) {
		// We have a pkgconfig library missing requirement for "pkg".  Now, "pkg" does
		// not necessarily match the actual system package needed to be updated (e.g.
		// gtk+-2.0 is the name of the pkgconfig file for gtk2).
		// We can try and find the "pkg.pc" file and look at what real package provides
		// it.  Updating that package will update the actual package in question as well
		// as updating the pkgconfig info for "pkg".
		// Note, that we won't have any pkgconfig path settings from the configure call
		// so we can't handle the situation where the user doesn't have pkgconfig files
		// stored in the usual place.
		IPath pkgconfigPath = new Path("/usr/lib/pkgconfig").append(pkgName + ".pc"); //$NON-NLS-1$ //$NON-NLS-2$
		// Get a launcher for the config command
		RemoteCommandLauncher launcher = new RemoteCommandLauncher();
		IPath commandPath = new Path("rpm"); //$NON-NLS-1$
		String[] commandArgs = new String[] { "-q", //$NON-NLS-1$
				"--queryformat", //$NON-NLS-1$
				"%{NAME}", //$NON-NLS-1$
				"--whatprovides", //$NON-NLS-1$
				pkgconfigPath.toOSString() };
		try {
			// Use CDT launcher to run rpm to query the package that provides
			// the pkgconfig .pc file for the package in question.
			ConsoleOutputStream output = new ConsoleOutputStream();
			Process proc = launcher.execute(commandPath, commandArgs, null, new Path("."), new NullProgressMonitor());
			if (proc != null) {
				try {
					// Close the input of the process since we will never write to
					// it
					proc.getOutputStream().close();
				} catch (IOException e) {
				}
				if (launcher.waitAndRead(output, output, new NullProgressMonitor()) != ICommandLauncher.OK) {
					AutotoolsPlugin.logErrorMessage(launcher.getErrorMessage());
				} else {
					String result = output.readBuffer();
					if (!result.startsWith("error:")) //$NON-NLS-1$
						System.out.println("need to execute update of " + result);
				}
			}

		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

}
