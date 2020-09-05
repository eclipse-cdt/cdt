/*******************************************************************************
 * Copyright (c) 2013-2020 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.core.internal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/** Parses the error output stream of cmake and reports errors and warnings as problem markers.<p
 * As long as cmake keeps printing the output we are interested in to the standard error stream,
 * attaching an instance of this class to cmake's standard <b>output</b>
 * stream will have no positive effect.
 * </p>
 * <p>
 * NOTE: There is no way properly handle output emitted by the cmake
 * {@code MESSAGE(NOTICE "text")} or {@code MESSAGE("text")} command
 * since that output is arbitrary text w/o any indication of the message type
 * nor filename/line-number information.
 * </p>
 *
 * @author Martin Weber
 */
/* package */ class CMakeErrorParser implements AutoCloseable {

	private static final String START_LOG = "CMake Debug Log"; //$NON-NLS-1$
	private static final String START_STATUS = "-- "; //$NON-NLS-1$

	/** matches the Start of a message, also ending the previous message */
	private static final Pattern PTN_MSG_START;

	private static Map<String, MessageHandler> handlersByMessageStart = new HashMap<>();

	/** the handler for the message we are currently gathering output for or <code>null</code> */
	private MessageHandler currentHandler;
	private final ICMakeExecutionMarkerFactory markerFactory;
	private final StringBuilder buffer;

	static {
		// setup regex to match the start of a message...
		StringBuilder ptnbuf = new StringBuilder("^"); //$NON-NLS-1$

		String ignoredMessages = String.join("|", START_LOG, START_STATUS); //$NON-NLS-1$
		ptnbuf.append(ignoredMessages);

		List<MessageHandler> markerHandlers = Arrays.asList(new MhDeprError(), new MhDeprWarning(), new MhErrorDev(),
				new MhError(), new MhInternalError(), new MhWarningDev(), new MhWarning());
		ptnbuf.append('|');
		for (Iterator<MessageHandler> it = markerHandlers.iterator(); it.hasNext();) {
			MessageHandler h = it.next();
			handlersByMessageStart.put(h.getMessageStart(), h);
			ptnbuf.append(Pattern.quote(h.getMessageStart()));
			if (it.hasNext()) {
				ptnbuf.append('|');
			}
		}

		PTN_MSG_START = Pattern.compile(ptnbuf.toString());
	}

	////////////////////////////////////////////////////////////////////
	/**
	 * @param markerFactory
	 *          the object responsible for creating problem marker objects for the project being built
	 */
	public CMakeErrorParser(ICMakeExecutionMarkerFactory markerFactory) {
		this.markerFactory = Objects.requireNonNull(markerFactory);
		buffer = new StringBuilder(512);
	}

	/** Adds text from the output stream to parse.
	 *
	 * @param input
	 * text from the output stream to parse
	 */
	public void addInput(CharSequence input) {
		buffer.append(input);
		processBuffer(false);
	}

	/** Closes this parser. Any remaining buffered input will be parsed.
	 */
	@Override
	public void close() {
		// process remaining bytes
		processBuffer(true);
		buffer.delete(0, buffer.length());
	}

	private void processBuffer(boolean isEOF) {
		Matcher matcher = PTN_MSG_START.matcher(""); //$NON-NLS-1$
		while (true) {
			matcher.reset(currentHandler == null ? buffer
					: buffer.subSequence(currentHandler.getMessageStart().length(), buffer.length()));
			//			System.err.println("-###\nr" + buffer.toString() + "\n###-");
			if (matcher.find()) {
				// first or second message arrived in buffer
				String handlerId = matcher.group();
				MessageHandler newHandler = handlersByMessageStart.get(handlerId);
				if (currentHandler == null) {
					// first message arrived in buffer
					if (newHandler != null) {
						// got handler for first message:
						// discard leading junk
						buffer.delete(0, matcher.start());
						currentHandler = newHandler;
					} else {
						// no handler for first message:
						// message is to be ignored, remove msg start to advance
						buffer.delete(0, matcher.end());
					}
					continue; // proceed with follow-up messages
					//					return; // wait for more input
				} else {
					// second message arrived in buffer
					// extract first message from buffer and process it...
					int end = matcher.start() + currentHandler.getMessageStart().length();
					String message = buffer.substring(0, end);
					processMessage(currentHandler, message.trim());
					currentHandler = newHandler;
					buffer.delete(0, end); // delete processed message
					continue; // proceed with follow-up messages
				}
			} else {
				// NO message arrived in buffer
				if (currentHandler != null && isEOF) {
					// first message is in buffer:
					// take buffer content as first message and process it...
					processMessage(currentHandler, buffer.toString().trim());
				}
				return; // wait for more input
			}
		}
	}

	/**
	 * @param handler
	 *          message handler
	 * @param fullMessage
	 *          the complete message, including the string the message starts with
	 */
	private void processMessage(MessageHandler handler, String fullMessage) {
		try {
			handler.processMessage(markerFactory, fullMessage);
		} catch (CoreException e) {
			Activator.getPlugin().getLog()
					.log(new Status(IStatus.WARNING, Activator.getId(), "CMake output error parsing failed", e)); //$NON-NLS-1$
		}
	}

	////////////////////////////////////////////////////////////////////
	// inner classes
	////////////////////////////////////////////////////////////////////
	/**
	 * Marker creator base class. Extracts the source-file name and line-number of errors from the output stream.<p>
	 * Message matching regexes are taken from cmake code in
	 * cmMessenger.cxx#printMessagePreamble: <code>
	 * <pre>
	if (t == cmake::FATAL_ERROR) {
	msg << "CMake Error";
	} else if (t == cmake::INTERNAL_ERROR) {
	msg << "CMake Internal Error (please report a bug)";
	} else if (t == cmake::LOG) {
	msg << "CMake Debug Log";
	} else if (t == cmake::DEPRECATION_ERROR) {
	msg << "CMake Deprecation Error";
	} else if (t == cmake::DEPRECATION_WARNING) {
	msg << "CMake Deprecation Warning";
	} else if (t == cmake::AUTHOR_WARNING) {
	msg << "CMake Warning (dev)";
	} else if (t == cmake::AUTHOR_ERROR) {
	msg << "CMake Error (dev)";
	} else {
	msg << "CMake Warning";
	 * </pre>
	 *
	 * <code><br>
	 * NOTE: We cannot properly handle output emitted by the cmake MESSAGE(NOTICE ...) command since
	 * the output is arbitrary text w/o any indication of the message type nor filename/line-number information.
	 * </p>
	 *
	 * @author Martin Weber
	 */
	private static abstract class MessageHandler {

		/** patterns used to extract file-name and line number information */
		private static final Pattern[] PTN_LOCATION;

		/** Name of the named-capturing group that holds a file name. */
		private static final String GRP_FILE = "File"; //$NON-NLS-1$
		/** Name of the named-capturing group that holds a line number. */
		private static final String GRP_LINE = "Lineno"; //$NON-NLS-1$

		static {
			PTN_LOCATION = new Pattern[] {
					Pattern.compile("(?m)^ at (?<" + GRP_FILE + ">.+):(?<" + GRP_LINE + ">\\d+).*$"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					Pattern.compile(
							"(?s)^: Error in cmake code at.(?<" + GRP_FILE + ">.+):(?<" + GRP_LINE + ">\\d+).*$"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					Pattern.compile("(?m)^ in (?<" + GRP_FILE + ">.+):(?<" + GRP_LINE + ">\\d+).*$"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					Pattern.compile("(?m)^ in (?<" + GRP_FILE + ">.+?):.*$"), //$NON-NLS-1$ //$NON-NLS-2$
					Pattern.compile("(?m)^:\\s.+$"), }; //$NON-NLS-1$
		}

		/**
		 * Gets the string the error message is supposed to start with.
		 */
		abstract String getMessageStart();

		/**
		 * @return the severity of the problem, see {@link IMarker} for acceptable severity values
		 */
		abstract int getSeverity();

		/**
		 * Creates the {@link IMarker marker object} that reflects the message.
		 *
		 * @param markerFactory
		 *          the object responsible for creating problem marker objects for the project being built
		 * @param fullMessage
		 *          the complete message, including the string the message starts with
		 * @throws CoreException
		 */
		public void processMessage(ICMakeExecutionMarkerFactory markerFactory, String fullMessage)
				throws CoreException {
			String content = fullMessage.substring(getMessageStart().length());
			// mandatory attributes for the marker
			Map<String, Object> attributes = new HashMap<>(3);
			attributes.put(IMarker.LOCATION, getClass().getSimpleName());

			// filename is normally project source root relative but may be absolute FS path
			String filename = null;
			for (Pattern ptn : PTN_LOCATION) {
				final Matcher matcher = ptn.matcher(content);
				// try to extract filename and/or line number from message
				if (matcher.find()) {
					try {
						filename = matcher.group(GRP_FILE);
					} catch (IllegalArgumentException expected) {
						// no file name in message
					}
					// attach additional info to marker...
					try {
						String lineno = matcher.group(GRP_LINE);
						Integer lineNumber = Integer.parseInt(lineno);
						attributes.put(IMarker.LINE_NUMBER, lineNumber);
					} catch (IllegalArgumentException expected) {
						// no line number in message
					}
					break;
				}
			}
			markerFactory.createMarker(fullMessage, getSeverity(), filename, attributes);
		}
	} // MessageHandler

	////////////////////////////////////////////////////////////////////
	private static class MhDeprError extends MessageHandler {
		private static final String START_DERROR = "CMake Deprecation Error"; //$NON-NLS-1$

		@Override
		String getMessageStart() {
			return START_DERROR;
		}

		@Override
		int getSeverity() {
			return IMarker.SEVERITY_ERROR;
		}

		@Override
		public String toString() {
			return super.toString() + ": " + getMessageStart(); //$NON-NLS-1$
		}
	}

	private static class MhDeprWarning extends MessageHandler {
		private static final String START_DWARNING = "CMake Deprecation Warning"; //$NON-NLS-1$

		@Override
		String getMessageStart() {
			return START_DWARNING;
		}

		@Override
		int getSeverity() {
			return IMarker.SEVERITY_WARNING;
		}

		@Override
		public String toString() {
			return super.toString() + ": " + getMessageStart(); //$NON-NLS-1$
		}
	}

	private static class MhError extends MessageHandler {
		private static final String START_ERROR = "CMake Error"; //$NON-NLS-1$

		@Override
		String getMessageStart() {
			return START_ERROR;
		}

		@Override
		int getSeverity() {
			return IMarker.SEVERITY_ERROR;
		}

		@Override
		public String toString() {
			return super.toString() + ": " + getMessageStart(); //$NON-NLS-1$
		}
	}

	private static class MhErrorDev extends MessageHandler {
		private static final String START_ERROR_DEV = "CMake Error (dev)"; //$NON-NLS-1$

		@Override
		String getMessageStart() {
			return START_ERROR_DEV;
		}

		@Override
		int getSeverity() {
			return IMarker.SEVERITY_ERROR;
		}

		@Override
		public String toString() {
			return super.toString() + ": " + getMessageStart(); //$NON-NLS-1$
		}
	}

	private static class MhInternalError extends MessageHandler {
		private static final String START_IERROR = "CMake Internal Error (please report a bug)"; //$NON-NLS-1$

		@Override
		String getMessageStart() {
			return START_IERROR;
		}

		@Override
		int getSeverity() {
			return IMarker.SEVERITY_ERROR;
		}

		@Override
		public String toString() {
			return super.toString() + ": " + getMessageStart(); //$NON-NLS-1$
		}
	}

	private static class MhWarning extends MessageHandler {
		private static final String START_WARNING = "CMake Warning"; //$NON-NLS-1$

		@Override
		String getMessageStart() {
			return START_WARNING;
		}

		@Override
		int getSeverity() {
			return IMarker.SEVERITY_WARNING;
		}

		@Override
		public String toString() {
			return super.toString() + ": " + getMessageStart(); //$NON-NLS-1$
		}
	}

	private static class MhWarningDev extends MessageHandler {
		private static final String START_WARNING_DEV = "CMake Warning (dev)"; //$NON-NLS-1$

		@Override
		String getMessageStart() {
			return START_WARNING_DEV;
		}

		@Override
		int getSeverity() {
			return IMarker.SEVERITY_WARNING;
		}

		@Override
		public String toString() {
			return super.toString() + ": " + getMessageStart(); //$NON-NLS-1$
		}
	}
}
