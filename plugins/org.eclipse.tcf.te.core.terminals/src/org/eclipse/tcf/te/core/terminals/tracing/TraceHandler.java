/*******************************************************************************
 * Copyright (c) 2011, 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.core.terminals.tracing;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.tcf.te.core.terminals.activator.CoreBundleActivator;

/**
 * Helper class to handle tracing using the platforms debug capabilities.
 */
public class TraceHandler {
	/**
	 * The bundle identifier.
	 */
	private final String identifier;

	/**
	 * The tracer instance.
	 */
	private Tracer tracer = null;

	/**
	 * The tracer is responsible for writing the trace message to the desired
	 * output media.
	 */
	protected static class Tracer {

		/**
		 * The bundle identifier.
		 */
		private final String fIdentifier;

		/**
		 * The qualifier for the default &quot;&lt;bundle identifier&gt;/debugmode&quot;
		 * tracing slot.
		 */
		private final String fDebugModeQualifier;

		/**
		 * Constructor.
		 *
		 * @param identifier The bundle identifier. Must not be <code>null</code>.
		 */
		public Tracer(String identifier) {
			Assert.isNotNull(identifier);
			fIdentifier = identifier;

			// Initialize the debug mode qualifier
			fDebugModeQualifier = fIdentifier + "/debugmode"; //$NON-NLS-1$
		}

		/**
		 * Returns the value of the debug mode tracing slot.
		 * <p>
		 * If not set, or the value is not an {@link Integer}, the method returns <code>0</code>.
		 *
		 * @return The debug mode value.
		 */
		protected int getDebugMode() {
			try {
				String mode = Platform.getDebugOption(fDebugModeQualifier);
				if (mode != null && Integer.decode(mode).intValue() > 0) {
					return Integer.decode(mode).intValue();
				}
			} catch (NumberFormatException e) { /* ignored on purpose */ }

			return 0;
		}

		/**
		 * Check if the specified trace slot is enabled.
		 *
		 * @param slotId The name of the slot.
		 * @return <code>true</code> if the slot is defined and enabled, <code>false</code> otherwise.
		 */
		protected boolean isSlotEnabled(String slotId) {
			return fIdentifier != null ? Boolean.parseBoolean(Platform.getDebugOption(fIdentifier + "/" + slotId)) : false; //$NON-NLS-1$
		}

		/**
		 * Check if tracing is enabled for given mode and slot.
		 *
		 * @param debugMode The debug mode for the current debug.
		 * @param slotId The name of the slot.
		 *
		 * @return <code>true</code> if the debug should be written, <code>false</code> otherwise.
		 */
		protected final boolean isEnabled(int debugMode, String slotId) {
			return getDebugMode() < 0 ||
			(debugMode <= getDebugMode() &&
					(slotId == null || slotId.trim().length() == 0 || isSlotEnabled(slotId)));
		}

		/**
		 * Format the trace message.
		 *
		 * @param message The trace message.
		 * @param debugMode The debug mode.
		 * @param slotId The name of the slot.
		 * @param severity The severity. See {@link IStatus} for valid severity values.
		 * @param clazz The class that calls this tracer.
		 *
		 * @see IStatus
		 */
		protected String getFormattedDebugMessage(String message, int debugMode, String slotId, int severity, Object clazz) {
			StringBuffer debug = new StringBuffer();
			if (slotId != null || clazz != null) {
				if (clazz != null) {
					String name = clazz instanceof Class<?> ? ((Class<?>)clazz).getSimpleName() : clazz.getClass().getSimpleName();
					debug.append(name.trim().length() > 0 ? name.trim() : clazz instanceof Class<?> ? ((Class<?>)clazz).getName() : clazz.getClass().getName());
				}
				if (slotId != null) {
					debug.append(" at "); //$NON-NLS-1$
					debug.append(slotId);
				}
				if (debugMode >= 0) {
					debug.append(" (Mode "); //$NON-NLS-1$
					debug.append(debugMode);
					debug.append(')');
				}
				debug.append('\n');
				debug.append('\t');
			}
			debug.append(message);

			return debug.toString();
		}

		/**
		 * Write the trace message.
		 *
		 * @param message The trace message.
		 * @param debugMode The debug mode.
		 * @param slotId The name of the slot.
		 * @param severity The severity. See {@link IStatus} for valid severity values.
		 * @param clazz The class that calls this tracer.
		 *
		 * @see IStatus
		 */
		protected void write(String message, int debugMode, String slotId, int severity, Object clazz) {
			String formattedMessage = getFormattedDebugMessage(message, debugMode, slotId, severity, clazz);
			if (severity == IStatus.ERROR || severity == IStatus.WARNING) {
				System.err.println(formattedMessage);
			}
			else {
				System.out.println(formattedMessage);
			}
		}

		/**
		 * Trace the given message with the given debug mode and slot.
		 *
		 * @param message The trace message.
		 * @param debugMode The debug mode.
		 * @param slotId The name of the slot.
		 * @param severity The severity. See {@link IStatus} for valid severity values.
		 * @param clazz The class that calls this tracer.
		 *
		 * @see IStatus
		 */
		public final void trace(String message, int debugMode, String slotId, int severity, Object clazz) {
			if (isEnabled(debugMode, slotId)) {
				write(message, debugMode, slotId, severity, clazz);
			}
		}
	}

	/**
	 * Constructor.
	 * <p>
	 * Initializes the tracing handler with the given bundle identifier.
	 *
	 * @param identifier The bundle identifier or <code>null</code>.
	 */
	public TraceHandler(String identifier) {
		this.identifier = identifier != null ? identifier : CoreBundleActivator.getUniqueIdentifier();
		Assert.isNotNull(this.identifier);
	}

	/**
	 * Returns the identifier.
	 */
	protected final String getIdentifier() {
		return identifier;
	}

	/**
	 * Returns the tracer instance. Create a new tracer instance
	 * on first invocation.
	 *
	 * @return The tracer instance.
	 */
	protected Tracer getTracer() {
		if (tracer == null) {
			tracer = new Tracer(identifier);
		}
		return tracer;
	}

	/**
	 * Return the current debug mode.
	 */
	public final int getDebugMode() {
		return getTracer().getDebugMode();
	}

	/**
	 * Check whether a trace slot is enabled. The debug mode defaults
	 * to 0.
	 *
	 * @param slotId The name of the slot.
	 *
	 * @return <code>true</code> if the slot is enabled, <code>false</code> otherwise.
	 */
	public final boolean isSlotEnabled(String slotId) {
		return isSlotEnabled(0, slotId);
	}

	/**
	 * Check whether a trace slot is enabled with the given debug mode.
	 *
	 * @param debugMode The debug mode
	 * @param slotId The name of the slot.
	 *
	 * @return <code>true</code> if the slot is enabled, <code>false</code> otherwise.
	 */
	public final boolean isSlotEnabled(int debugMode, String slotId) {
		return getTracer().isEnabled(debugMode, slotId);
	}

	/**
	 * Trace the given message.
	 * <p>
	 * The message severity will be {@link IStatus#INFO} and the message will be
	 * traced unconditionally.
	 *
	 * @param message The message.
	 * @param clazz The class that calls this tracer or <code>null</code>.
	 */
	public final void trace(String message, Object clazz) {
		getTracer().trace(message, 0, null, IStatus.INFO, clazz);
	}

	/**
	 * Trace the given message.
	 * <p>
	 * The message severity will be {@link IStatus#INFO}.
	 *
	 * @param message The message.
	 * @param debugMode The minimum debug mode that has to be set to write out the message.
	 * @param clazz The class that calls this tracer or <code>null</code>.
	 */
	public final void trace(String message, int debugMode, Object clazz) {
		getTracer().trace(message, debugMode, null, IStatus.INFO, clazz);
	}

	/**
	 * Trace the given message.
	 * <p>
	 * The message severity will be {@link IStatus#INFO} and the debug mode
	 * will default to <code>0</code>.
	 *
	 * @param message The message.
	 * @param slotId The slot that has to be enabled to write out the message.
	 * @param clazz The class that calls this tracer or <code>null</code>.
	 */
	public final void trace(String message, String slotId, Object clazz) {
		getTracer().trace(message, 0, slotId, IStatus.INFO, clazz);
	}

	/**
	 * Trace the given message.
	 *
	 * @param message The message.
	 * @param debugMode The minimum debug mode that has to be set to write out the message.
	 * @param slotId The slot that has to be enabled to write out the message.
	 * @param severity The severity. See {@link IStatus} for valid severity values.
	 * @param clazz The class that calls this tracer or <code>null</code>.
	 *
	 * @see IStatus
	 */
	public final void trace(String message, int debugMode, String slotId, int severity, Object clazz) {
		getTracer().trace(message, debugMode, slotId, severity, clazz);
	}

}
