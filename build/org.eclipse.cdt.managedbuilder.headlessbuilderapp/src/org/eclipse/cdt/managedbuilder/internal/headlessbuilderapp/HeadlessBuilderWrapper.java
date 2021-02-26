/*******************************************************************************
 * Copyright (c) 2019, 2020 Kichwa Coders Canada Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Kichwa Coders Canada, Inc - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.headlessbuilderapp;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.managedbuilder.internal.core.HeadlessBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.BundleException;

/**
 * Wraps original {@code CodeFormatterApplication} for better error diagnostic message if user does not specify workspace location.
 *
 * @author Jonah Graham <jonah@kichwacoders.com>
 * @since 1.0
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class HeadlessBuilderWrapper implements IApplication {
	/**
	 * Deals with the messages in the properties file (cut n' pasted from a generated class).
	 */
	private final static class Messages extends NLS {
		private static final String BUNDLE_NAME = "org.eclipse.cdt.managedbuilder.internal.headlessbuilderapp.messages";//$NON-NLS-1$
		public static String CommandLineUsage;
		public static String WorkspaceRequired;
		static {
			NLS.initializeMessages(BUNDLE_NAME, Messages.class);
		}

		/**
		 * Bind the given message's substitution locations with the given string values.
		 *
		 * @param message
		 *            the message to be manipulated
		 * @return the manipulated String
		 */
		public static String bind(String message) {
			return bind(message, null);
		}

		/**
		 * Bind the given message's substitution locations with the given string values.
		 *
		 * @param message
		 *            the message to be manipulated
		 * @param binding
		 *            the object to be inserted into the message
		 * @return the manipulated String
		 */
		public static String bind(String message, Object binding) {
			return bind(message, new Object[] { binding });
		}

		/**
		 * Bind the given message's substitution locations with the given string values.
		 *
		 * @param message
		 *            the message to be manipulated
		 * @param binding1
		 *            An object to be inserted into the message
		 * @param binding2
		 *            A second object to be inserted into the message
		 * @return the manipulated String
		 */
		public static String bind(String message, Object binding1, Object binding2) {
			return bind(message, new Object[] { binding1, binding2 });
		}

		/**
		 * Bind the given message's substitution locations with the given string values.
		 *
		 * @param message
		 *            the message to be manipulated
		 * @param bindings
		 *            An array of objects to be inserted into the message
		 * @return the manipulated String
		 */
		public static String bind(String message, Object[] bindings) {
			return MessageFormat.format(message, bindings);
		}
	}

	@Override
	public Object start(IApplicationContext context) throws Exception {
		String[] arguments = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
		List<String> args = Arrays.asList(arguments);
		if (args.isEmpty() || args.contains("-help") || args.contains("--help")) { //$NON-NLS-1$ //$NON-NLS-2$
			showUsage(context);
			return IApplication.EXIT_OK;
		}
		try {
			// Try to see if the workspace is available. If it is not available we'll
			// get a NoClassDefFoundError wrapping a ClassNotFoundException which
			// has a BundleException in it, whose cause was the original
			// IllegalStateException raised by
			// org.eclipse.core.internal.runtime.DataArea.assertLocationInitialized()
			ResourcesPlugin.getWorkspace();
		} catch (NoClassDefFoundError noClassError) {
			if (noClassError.getCause() instanceof ClassNotFoundException) {
				ClassNotFoundException classNotFoundException = (ClassNotFoundException) noClassError.getCause();
				if (classNotFoundException.getException() instanceof BundleException) {
					BundleException bundleException = (BundleException) classNotFoundException.getException();
					if (bundleException.getCause() instanceof IllegalStateException) {
						System.err.println(Messages.bind(Messages.WorkspaceRequired));
						showUsage(context);
						return 1;
					}
				}
			}
			throw noClassError;
		}

		// Workspace is available, so launch the original Headless Builder
		Object res = new HeadlessBuilder().start(context);
		if (res == HeadlessBuilder.SHOW_USAGE) {
			showUsage(context);
			return HeadlessBuilder.ERROR;
		}
		return res;
	}

	private void showUsage(IApplicationContext context) {
		String binaryName = "eclipse"; //$NON-NLS-1$ // TODO: How to lookup the name of the eclipse binary, might be branded!
		System.out.println(NLS.bind(Messages.CommandLineUsage, binaryName));
	}

	@Override
	public void stop() {
		// do nothing
	}
}