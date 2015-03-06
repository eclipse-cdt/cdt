/*******************************************************************************
 * Copyright (c) 2003, 2015 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following Wind River employees contributed to the Terminal component
 * that contains this file: Chris Thew, Fran Litterio, Stephen Lamb,
 * Helmut Haigermoser and Ted Williams.
 *
 * Contributors:
 * Michael Scharf (Wind River) - split into core, view and connector plugins
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 * Martin Oberhuber (Wind River) - [206892] State handling: Only allow connect when CLOSED
 * Martin Oberhuber (Wind River) - [206883] Serial Terminal leaks Jobs
 * Martin Oberhuber (Wind River) - [208145] Terminal prints garbage after quick disconnect/reconnect
 * Martin Oberhuber (Wind River) - [207785] NPE when trying to send char while no longer connected
 * Michael Scharf (Wind River) - [209665] Add ability to log byte streams from terminal
 * Ruslan Sychev (Xored Software) - [217675] NPE or SWTException when closing Terminal View while connection establishing
 * Michael Scharf (Wing River) - [196447] The optional terminal input line should be resizeable
 * Martin Oberhuber (Wind River) - [168197] Replace JFace MessagDialog by SWT MessageBox
 * Martin Oberhuber (Wind River) - [204796] Terminal should allow setting the encoding to use
 * Michael Scharf (Wind River) - [237398] Terminal get Invalid Thread Access when the title is set
 * Martin Oberhuber (Wind River) - [240745] Pressing Ctrl+F1 in the Terminal should bring up context help
 * Michael Scharf (Wind River) - [240098] The cursor should not blink when the terminal is disconnected
 * Anton Leherbauer (Wind River) - [335021] Middle mouse button copy/paste does not work with the terminal
 * Max Stepanov (Appcelerator) - [339768] Fix ANSI code for PgUp / PgDn
 * Pawel Piech (Wind River) - [333613] "Job found still running" after shutdown
 * Martin Oberhuber (Wind River) - [348700] Terminal unusable after disconnect
 * Simon Bernard (Sierra Wireless) - [351424] [terminal] Terminal does not support del and insert key
 * Martin Oberhuber (Wind River) - [265352][api] Allow setting fonts programmatically
 * Martin Oberhuber (Wind River) - [378691][api] push Preferences into the Widget
 * Anton Leherbauer (Wind River) - [433751] Add option to enable VT100 line wrapping mode
 * Anton Leherbauer (Wind River) - [434294] Incorrect handling of function keys with modifiers
 * Martin Oberhuber (Wind River) - [434294] Add Mac bindings with COMMAND
 * Anton Leherbauer (Wind River) - [434749] UnhandledEventLoopException when copying to clipboard while the selection is empty
 * Martin Oberhuber (Wind River) - [436612] Restore Eclipse 3.4 compatibility by using Reflection
 * Anton Leherbauer (Wind River) - [458398] Add support for normal/application cursor keys mode
 * Anton Leherbauer (Wind River) - [420928] Terminal widget leaks memory
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.emulator;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.SocketException;
import java.nio.charset.Charset;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.SWTKeySupport;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm.internal.terminal.control.ICommandInputField;
import org.eclipse.tm.internal.terminal.control.ITerminalListener;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;
import org.eclipse.tm.internal.terminal.control.impl.ITerminalControlForText;
import org.eclipse.tm.internal.terminal.control.impl.TerminalMessages;
import org.eclipse.tm.internal.terminal.control.impl.TerminalPlugin;
import org.eclipse.tm.internal.terminal.preferences.ITerminalConstants;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.tm.internal.terminal.textcanvas.PipedInputStream;
import org.eclipse.tm.internal.terminal.textcanvas.PollingTextCanvasModel;
import org.eclipse.tm.internal.terminal.textcanvas.TextCanvas;
import org.eclipse.tm.internal.terminal.textcanvas.TextLineRenderer;
import org.eclipse.tm.terminal.model.ITerminalTextData;
import org.eclipse.tm.terminal.model.ITerminalTextDataSnapshot;
import org.eclipse.tm.terminal.model.TerminalTextDataFactory;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.keys.IBindingService;

/**
 *
 * This class was originally written to use nested classes, which unfortunately makes
 * this source file larger and more complex than it needs to be.  In particular, the
 * methods in the nested classes directly access the fields of the enclosing class.
 * One day we should pull the nested classes out into their own source files (but still
 * in this package).
 *
 * @author Chris Thew <chris.thew@windriver.com>
 */
public class VT100TerminalControl implements ITerminalControlForText, ITerminalControl, ITerminalViewControl
{
    protected final static String[] LINE_DELIMITERS = { "\n" }; //$NON-NLS-1$

    /**
     * This field holds a reference to a TerminalText object that performs all ANSI
     * text processing on data received from the remote host and controls how text is
     * displayed using the view's StyledText widget.
     */
    private final VT100Emulator			  fTerminalText;
    private Display                   fDisplay;
    private TextCanvas                fCtlText;
    private Composite                 fWndParent;
    private Clipboard                 fClipboard;
    private KeyListener               fKeyHandler;
    private final ITerminalListener         fTerminalListener;
    private String                    fMsg = ""; //$NON-NLS-1$
    private TerminalFocusListener     fFocusListener;
    private ITerminalConnector		  fConnector;
    private final ITerminalConnector[]      fConnectors;
	private final boolean fUseCommonPrefs;
	private boolean connectOnEnterIfClosed	= true;

    PipedInputStream fInputStream;
	private static final String defaultEncoding = Charset.defaultCharset().name();
	private String fEncoding = defaultEncoding;
	private InputStreamReader fInputStreamReader;

	private ICommandInputField fCommandInputField;

	private volatile TerminalState fState;

	private final ITerminalTextData fTerminalModel;

	private final EditActionAccelerators editActionAccelerators = new EditActionAccelerators();

	private boolean fApplicationCursorKeys;

	/**
	 * Listens to changes in the preferences
	 */
	private final IPropertyChangeListener fPreferenceListener=new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			if(event.getProperty().equals(ITerminalConstants.PREF_BUFFERLINES)
					|| event.getProperty().equals(ITerminalConstants.PREF_INVERT_COLORS)) {
				updatePreferences();
			}
		}
	};
	private final IPropertyChangeListener fFontListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(ITerminalConstants.FONT_DEFINITION)) {
				onTerminalFontChanged();
			}
		}
	};

	/**
	 * Is protected by synchronize on this
	 */
	volatile private Job fJob;

	private PollingTextCanvasModel fPollingTextCanvasModel;

	public VT100TerminalControl(ITerminalListener target, Composite wndParent, ITerminalConnector[] connectors) {
		this(target, wndParent, connectors, false);
	}

	/**
	 * Instantiate a Terminal widget.
	 * @param target Callback for notifying the owner of Terminal state changes.
	 * @param wndParent The Window parent to embed the Terminal in.
	 * @param connectors Provided connectors.
	 * @param useCommonPrefs If <code>true</code>, the Terminal widget will pick up settings
	 *    from the <code>org.eclipse.tm.terminal.TerminalPreferencePage</code> Preference page.
	 *    Otherwise, clients need to maintain settings themselves.
	 * @since 3.2
	 */
	public VT100TerminalControl(ITerminalListener target, Composite wndParent, ITerminalConnector[] connectors, boolean useCommonPrefs) {
		fConnectors=connectors;
		fUseCommonPrefs = useCommonPrefs;
		fTerminalListener=target;
		fTerminalModel=TerminalTextDataFactory.makeTerminalTextData();
		fTerminalModel.setMaxHeight(1000);
		fInputStream=new PipedInputStream(8*1024);
		fTerminalText = new VT100Emulator(fTerminalModel, this, null);
		try {
			// Use Default Encoding as start, until setEncoding() is called
			setEncoding(null);
		} catch (UnsupportedEncodingException e) {
			// Should never happen
			e.printStackTrace();
			// Fall back to local Platform Default Encoding
			fEncoding = defaultEncoding;
			fInputStreamReader = new InputStreamReader(fInputStream);
			fTerminalText.setInputStreamReader(fInputStreamReader);
		}

		setupTerminal(wndParent);
	}

	public void setEncoding(String encoding) throws UnsupportedEncodingException {
		if (encoding == null) {
			// TODO better use a standard remote-to-local encoding?
			encoding = "ISO-8859-1"; //$NON-NLS-1$
			// TODO or better use the local default encoding?
			// encoding = defaultEncoding;
		}
		fInputStreamReader = new InputStreamReader(fInputStream, encoding);
		// remember encoding if above didn't throw an exception
		fEncoding = encoding;
		fTerminalText.setInputStreamReader(fInputStreamReader);
	}

	public String getEncoding() {
		return fEncoding;
	}

	public ITerminalConnector[] getConnectors() {
		return fConnectors;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl#copy()
	 */
	public void copy() {
		copy(DND.CLIPBOARD);
	}

	private void copy(int clipboardType) {
		String selection = getSelection();
		if (selection.length() > 0) {
			Object[] data = new Object[] { selection };
			Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
			fClipboard.setContents(data, types, clipboardType);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl#paste()
	 */
	public void paste() {
		paste(DND.CLIPBOARD);
// TODO paste in another thread.... to avoid blocking
//		new Thread() {
//			public void run() {
//				for (int i = 0; i < strText.length(); i++) {
//					sendChar(strText.charAt(i), false);
//				}
//
//			}
//		}.start();
	}

	private void paste(int clipboardType) {
		TextTransfer textTransfer = TextTransfer.getInstance();
		String strText = (String) fClipboard.getContents(textTransfer, clipboardType);
		pasteString(strText);
	}

	/**
	 * @param strText the text to paste
	 */
	public boolean pasteString(String strText) {
		if(!isConnected())
			return false;
		if (strText == null)
			return false;
		if (!fEncoding.equals(defaultEncoding)) {
			sendString(strText);
		} else {
			// TODO I do not understand why pasteString would do this here...
			for (int i = 0; i < strText.length(); i++) {
				sendChar(strText.charAt(i), false);
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl#selectAll()
	 */
	public void selectAll() {
		getCtlText().selectAll();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl#sendKey(char)
	 */
	public void sendKey(char character) {
		Event event;
		KeyEvent keyEvent;

		event = new Event();
		event.widget = getCtlText();
		event.character = character;
		event.keyCode = 0;
		event.stateMask = 0;
		event.doit = true;
		keyEvent = new KeyEvent(event);

		fKeyHandler.keyPressed(keyEvent);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl#clearTerminal()
	 */
	public void clearTerminal() {
		// The TerminalText object does all text manipulation.

		getTerminalText().clearTerminal();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl#getClipboard()
	 */
	public Clipboard getClipboard() {
		return fClipboard;
	}

	/**
	 * @return non null selection
	 */
	public String getSelection() {
		String txt= fCtlText.getSelectionText();
		if(txt==null)
			txt=""; //$NON-NLS-1$
		return txt;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl#setFocus()
	 */
	public void setFocus() {
		getCtlText().setFocus();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl#isEmpty()
	 */
	public boolean isEmpty() {
		return getCtlText().isEmpty();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl#isDisposed()
	 */
	public boolean isDisposed() {
		return getCtlText().isDisposed();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl#isConnected()
	 */
	public boolean isConnected() {
		return fState==TerminalState.CONNECTED;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl#disposeTerminal()
	 */
	public void disposeTerminal() {
		Logger.log("entered."); //$NON-NLS-1$
		if(fUseCommonPrefs) {
			TerminalPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(fPreferenceListener);
			JFaceResources.getFontRegistry().removeListener(fFontListener);
		}
		disconnectTerminal();
		fClipboard.dispose();
		fPollingTextCanvasModel.stopPolling();
		getTerminalText().dispose();
	}

	public void connectTerminal() {
		Logger.log("entered."); //$NON-NLS-1$
		if(getTerminalConnector()==null)
			return;
		fTerminalText.resetState();
		fApplicationCursorKeys = false;
		if(fConnector.getInitializationErrorMessage()!=null) {
			showErrorMessage(NLS.bind(
					TerminalMessages.CannotConnectTo,
					fConnector.getName(),
					fConnector.getInitializationErrorMessage()));
			// we cannot connect because the connector was not initialized
			return;
		}
		// clean the error message
		setMsg(""); //$NON-NLS-1$
		getTerminalConnector().connect(this);
		waitForConnect();
	}

	public ITerminalConnector getTerminalConnector() {
		return fConnector;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl#disconnectTerminal()
	 */
	public void disconnectTerminal() {
		Logger.log("entered."); //$NON-NLS-1$

		//Disconnect the remote side first
		if (getState()!=TerminalState.CLOSED) {
			if(getTerminalConnector()!=null) {
				getTerminalConnector().disconnect();
			}
		}

        //Ensure that a new Job can be started; then clean up old Job.
        Job job;
        synchronized(this) {
            job = fJob;
            fJob = null;
        }
        if (job!=null) {
            job.cancel();
            // Join job to avoid leaving job running after workbench shutdown (333613).
            // Interrupt to be fast enough; cannot close fInputStream since it is re-used (bug 348700).
            Thread t = job.getThread();
            if(t!=null) t.interrupt();
            try {
                job.join();
            } catch (InterruptedException e) {}
        }
	}

	private void waitForConnect() {
		Logger.log("entered."); //$NON-NLS-1$

		// TODO Eliminate the nested dispatch loop
		do {
			if (!fDisplay.readAndDispatch())
				fDisplay.sleep();
		} while (getState()==TerminalState.CONNECTING);
		
		if (getCtlText().isDisposed()) {
			disconnectTerminal();
			return;
		}
		if (getMsg().length() > 0) {
			showErrorMessage(getMsg());
			disconnectTerminal();
			return;
		}
		if (getCtlText().isFocusControl()) {
			if (getState() == TerminalState.CONNECTED)
				fFocusListener.captureKeyEvents(true);
		}
		startReaderJob();
	}

	private synchronized void startReaderJob() {
		if(fJob==null) {
			fJob=new Job("Terminal data reader") { //$NON-NLS-1$
				protected IStatus run(IProgressMonitor monitor) {
					IStatus status=Status.OK_STATUS;
					try {
						while(true) {
							while(fInputStream.available()==0 && !monitor.isCanceled()) {
								try {
									fInputStream.waitForAvailable(500);
								} catch (InterruptedException e) {
									Thread.currentThread().interrupt();
								}
							}
							if(monitor.isCanceled()) {
								//Do not disconnect terminal here because another reader job may already be running
								status=Status.CANCEL_STATUS;
								break;
							}
							try {
								// TODO: should block when no text is available!
								fTerminalText.processText();
							} catch (Exception e) {
								disconnectTerminal();
								status=new Status(IStatus.ERROR,TerminalPlugin.PLUGIN_ID,e.getLocalizedMessage(),e);
								break;
							}
						}
					} finally {
						// clean the job: start a new one when the connection gets restarted
						// Bug 208145: make sure we do not clean an other job that's already started (since it would become a Zombie)
						synchronized (VT100TerminalControl.this) {
							if (fJob==this) {
								fJob=null;
							}
						}
					}
					return status;
				}

			};
			fJob.setSystem(true);
			fJob.schedule();
		}
	}

	private void showErrorMessage(String message) {
		String strTitle = TerminalMessages.TerminalError;
		// [168197] Replace JFace MessagDialog by SWT MessageBox
		//MessageDialog.openError( getShell(), strTitle, message);
		MessageBox mb = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
		mb.setText(strTitle);
		mb.setMessage(message);
		mb.open();
	}

	protected void sendString(String string) {
		try {
			// Send the string after converting it to an array of bytes using the
			// platform's default character encoding.
			//
			// TODO: Find a way to force this to use the ISO Latin-1 encoding.
			// TODO: handle Encoding Errors in a better way

			getOutputStream().write(string.getBytes(fEncoding));
			getOutputStream().flush();
		} catch (SocketException socketException) {
			displayTextInTerminal(socketException.getMessage());

			String strMsg = TerminalMessages.SocketError
					+ "!\n" + socketException.getMessage(); //$NON-NLS-1$
			showErrorMessage(strMsg);

			Logger.logException(socketException);

			disconnectTerminal();
		} catch (IOException ioException) {
			showErrorMessage(TerminalMessages.IOError + "!\n" + ioException.getMessage());//$NON-NLS-1$

			Logger.logException(ioException);

			disconnectTerminal();
		}
	}

	public Shell getShell() {
		return getCtlText().getShell();
	}

	protected void sendChar(char chKey, boolean altKeyPressed) {
		try {
			int byteToSend = chKey;
			OutputStream os = getOutputStream();
			if (os==null) {
				// Bug 207785: NPE when trying to send char while no longer connected
				Logger.log("NOT sending '" + byteToSend + "' because no longer connected"); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				if (altKeyPressed) {
					// When the ALT key is pressed at the same time that a character is
					// typed, translate it into an ESCAPE followed by the character.  The
					// alternative in this case is to set the high bit of the character
					// being transmitted, but that will cause input such as ALT-f to be
					// seen as the ISO Latin-1 character '�', which can be confusing to
					// European users running Emacs, for whom Alt-f should move forward a
					// word instead of inserting the '�' character.
					//
					// TODO: Make the ESCAPE-vs-highbit behavior user configurable.

					byte[] bytesToSend = String.valueOf(chKey).getBytes(fEncoding);
					StringBuilder b = new StringBuilder("sending ESC"); //$NON-NLS-1$
					for (int i = 0; i < bytesToSend.length; i++) {
						if (i != 0) b.append(" +"); //$NON-NLS-1$
						b.append(" '" + bytesToSend[i] + "'"); //$NON-NLS-1$ //$NON-NLS-2$
					}
					Logger.log(b.toString());
					os.write('\u001b');
					os.write(bytesToSend);
				} else {
					byte[] bytesToSend = String.valueOf(chKey).getBytes(fEncoding);
					StringBuilder b = new StringBuilder("sending"); //$NON-NLS-1$
					for (int i = 0; i < bytesToSend.length; i++) {
						if (i != 0) b.append(" +"); //$NON-NLS-1$
						b.append(" '" + bytesToSend[i] + "'"); //$NON-NLS-1$ //$NON-NLS-2$
					}
					Logger.log(b.toString());
					os.write(bytesToSend);
				}
				os.flush();
			}
		} catch (SocketException socketException) {
			Logger.logException(socketException);

			displayTextInTerminal(socketException.getMessage());

			String strMsg = TerminalMessages.SocketError
					+ "!\n" + socketException.getMessage(); //$NON-NLS-1$

			showErrorMessage(strMsg);
			Logger.logException(socketException);

			disconnectTerminal();
		} catch (IOException ioException) {
			Logger.logException(ioException);

			displayTextInTerminal(ioException.getMessage());

			String strMsg = TerminalMessages.IOError + "!\n" + ioException.getMessage(); //$NON-NLS-1$

			showErrorMessage(strMsg);
			Logger.logException(ioException);

			disconnectTerminal();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl#setupTerminal(org.eclipse.swt.widgets.Composite)
	 */
	public void setupTerminal(Composite parent) {
		Assert.isNotNull(parent);
		boolean wasDisposed = true;
		TerminalState oldState = fState;
		fState = TerminalState.CLOSED;
		if (fClipboard != null && !fClipboard.isDisposed()) {
			// terminal was not disposed (DnD)
			wasDisposed = false;
			fClipboard.dispose();
			fPollingTextCanvasModel.stopPolling();
		}
		if (fWndParent != null && !fWndParent.isDisposed()) {
			// terminal widget gets a new parent (DnD)
			fWndParent.dispose();
		}
		setupControls(parent);
		setCommandInputField(fCommandInputField);
		setupListeners();
		if (fUseCommonPrefs && wasDisposed) {
			updatePreferences();
			onTerminalFontChanged();
			TerminalPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(fPreferenceListener);
			JFaceResources.getFontRegistry().addListener(fFontListener);
		}
		setupHelp(fWndParent, TerminalPlugin.HELP_VIEW);
		
		if (!wasDisposed) {
			fState = oldState;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.control.ITerminalViewControl#updatePreferences()
	 */
	private void updatePreferences() {
		int bufferLineLimit = Platform.getPreferencesService().getInt(TerminalPlugin.PLUGIN_ID, ITerminalConstants.PREF_BUFFERLINES, 0, null);
		boolean invert = Platform.getPreferencesService().getBoolean(TerminalPlugin.PLUGIN_ID, ITerminalConstants.PREF_INVERT_COLORS, false, null);
		setBufferLineLimit(bufferLineLimit);
		setInvertedColors(invert);
	}

	private void onTerminalFontChanged() {
		// set the font for all
		setFont(ITerminalConstants.FONT_DEFINITION);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.control.ITerminalViewControl#setFont(java.lang.String)
	 */
	public void setFont(String fontName) {
		Font font=JFaceResources.getFont(fontName);
		getCtlText().setFont(font);
		if(fCommandInputField!=null) {
			fCommandInputField.setFont(font);
		}
		// Tell the TerminalControl singleton that the font has changed.
		fCtlText.updateFont(fontName);
		getTerminalText().fontChanged();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl#onFontChanged()
	 */
	public void setFont(Font font) {
		getCtlText().setFont(font);
		if(fCommandInputField!=null) {
			fCommandInputField.setFont(font);
		}

		// Tell the TerminalControl singleton that the font has changed.
		fCtlText.onFontChange();
		getTerminalText().fontChanged();
	}
	public Font getFont() {
		return getCtlText().getFont();
	}
	public Control getControl() {
		return fCtlText;
	}
	public Control getRootControl() {
		return fWndParent;
	}
	protected void setupControls(Composite parent) {
		fWndParent=new Composite(parent,SWT.NONE);
		GridLayout layout=new GridLayout();
		layout.marginWidth=0; layout.marginHeight=0; layout.verticalSpacing=0;
		fWndParent.setLayout(layout);

		ITerminalTextDataSnapshot snapshot=fTerminalModel.makeSnapshot();
		// TODO how to get the initial size correctly!
		snapshot.updateSnapshot(false);
		fPollingTextCanvasModel=new PollingTextCanvasModel(snapshot);
		fCtlText=new TextCanvas(fWndParent,fPollingTextCanvasModel,SWT.NONE,new TextLineRenderer(fCtlText,fPollingTextCanvasModel));

		fCtlText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		fCtlText.addResizeHandler(new TextCanvas.ResizeListener() {
			public void sizeChanged(int lines, int columns) {
				fTerminalText.setDimensions(lines, columns);
			}
		});
		fCtlText.addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				// update selection used by middle mouse button paste
				if (e.button == 1 && getSelection().length() > 0) {
					copy(DND.SELECTION_CLIPBOARD);
				}
			}
		});

		fDisplay = getCtlText().getDisplay();
		fClipboard = new Clipboard(fDisplay);
	}

	protected void setupListeners() {
		fKeyHandler = new TerminalKeyHandler();
		fFocusListener = new TerminalFocusListener();

		getCtlText().addKeyListener(fKeyHandler);
		getCtlText().addFocusListener(fFocusListener);

	}

	/**
	 * Setup all the help contexts for the controls.
	 */
	protected void setupHelp(Composite parent, String id) {
		Control[] children = parent.getChildren();

		for (int nIndex = 0; nIndex < children.length; nIndex++) {
			if (children[nIndex] instanceof Composite) {
				setupHelp((Composite) children[nIndex], id);
			}
		}

		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, id);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl#displayTextInTerminal(java.lang.String)
	 */
	public void displayTextInTerminal(String text) {
		writeToTerminal("\r\n"+text+"\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	private void writeToTerminal(String text) {
		try {
			getRemoteToTerminalOutputStream().write(text.getBytes(fEncoding));
		} catch (UnsupportedEncodingException e) {
			// should never happen!
			e.printStackTrace();
		} catch (IOException e) {
			// should never happen!
			e.printStackTrace();
		}

	}

	public OutputStream getRemoteToTerminalOutputStream() {
		if(Logger.isLogEnabled()) {
			return new LoggingOutputStream(fInputStream.getOutputStream());
		} else {
			return fInputStream.getOutputStream();
		}
	}
	protected boolean isLogCharEnabled() {
		return TerminalPlugin.isOptionEnabled(Logger.TRACE_DEBUG_LOG_CHAR);
	}

	public OutputStream getOutputStream() {
		if(getTerminalConnector()!=null)
			return getTerminalConnector().getTerminalToRemoteStream();
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl#setMsg(java.lang.String)
	 */
	public void setMsg(String msg) {
		fMsg = msg;
	}

	public String getMsg() {
		return fMsg;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl#getCtlText()
	 */
	protected TextCanvas getCtlText() {
		return fCtlText;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl#getTerminalText()
	 */
	public VT100Emulator getTerminalText() {
		return fTerminalText;
	}
	protected class TerminalFocusListener implements FocusListener {
		private IContextActivation terminalContextActivation = null;
		private IContextActivation editContextActivation = null;

		protected TerminalFocusListener() {
			super();
		}

		public void focusGained(FocusEvent event) {
			// Disable all keyboard accelerators (e.g., Control-B) so the Terminal view
			// can see every keystroke.  Without this, Emacs, vi, and Bash are unusable
			// in the Terminal view.
			if (getState() == TerminalState.CONNECTED)
				captureKeyEvents(true);

			IContextService contextService = (IContextService) PlatformUI
					.getWorkbench().getAdapter(IContextService.class);
			editContextActivation = contextService
					.activateContext("org.eclipse.tm.terminal.EditContext"); //$NON-NLS-1$
		}

		public void focusLost(FocusEvent event) {
			// Enable all keybindings.
			captureKeyEvents(false);

			// Restore the command context to its previous value.

			IContextService contextService = (IContextService) PlatformUI
					.getWorkbench().getAdapter(IContextService.class);
			contextService.deactivateContext(editContextActivation);
		}

		protected void captureKeyEvents(boolean capture) {
			IBindingService bindingService = (IBindingService) PlatformUI
					.getWorkbench().getAdapter(IBindingService.class);
			IContextService contextService = (IContextService) PlatformUI
					.getWorkbench().getAdapter(IContextService.class);

			boolean enableKeyFilter = !capture;
			if (bindingService.isKeyFilterEnabled() != enableKeyFilter)
				bindingService.setKeyFilterEnabled(enableKeyFilter);

			if (capture && terminalContextActivation == null) {
				// The above code fails to cause Eclipse to disable menu-activation
				// accelerators (e.g., Alt-F for the File menu), so we set the command
				// context to be the Terminal view's command context.  This enables us to
				// override menu-activation accelerators with no-op commands in our
				// plugin.xml file, which enables the Terminal view to see absolutely _all_
				// key-presses.
				terminalContextActivation = contextService
						.activateContext("org.eclipse.tm.terminal.TerminalContext"); //$NON-NLS-1$

			} else if (!capture && terminalContextActivation != null) {
				contextService.deactivateContext(terminalContextActivation);
				terminalContextActivation = null;
			}
		}
	}

	protected class TerminalKeyHandler extends KeyAdapter {
		public void keyPressed(KeyEvent event) {
			//TODO next 2 lines are probably obsolete now
			if (getState()==TerminalState.CONNECTING)
				return;

			//TODO we should no longer handle copy & paste specially.
			//Instead, we should have Ctrl+Shift always go to local since there is no escape sequence for this.
			//On Mac, Command+Anything already goes always to local.
			//Note that this decision means that Command will NOT be Meta in Emacs on a Remote.
			int accelerator = SWTKeySupport.convertEventToUnmodifiedAccelerator(event);
			if (editActionAccelerators.isCopyAction(accelerator)) {
				copy();
				return;
			}
			if (editActionAccelerators.isPasteAction(accelerator)) {
				paste();
				return;
			}

			// We set the event.doit to false to prevent any further processing of this
			// key event.  The only reason this is here is because I was seeing the F10
			// key both send an escape sequence (due to this method) and switch focus
			// to the Workbench File menu (forcing the user to click in the Terminal
			// view again to continue entering text).  This fixes that.

			event.doit = false;

			char character = event.character;
			int modifierKeys = event.stateMask & SWT.MODIFIER_MASK;
			boolean ctrlKeyPressed = (event.stateMask & SWT.CTRL) != 0;
			boolean onlyCtrlKeyPressed = modifierKeys == SWT.CTRL;
			boolean macCmdKeyPressed = (event.stateMask & SWT.COMMAND) != 0;

			// To fix SPR 110341, we consider the Alt key to be pressed only when the
			// Control key is _not_ also pressed.  This works around a bug in SWT where,
			// on European keyboards, the AltGr key being pressed appears to us as Control
			// + Alt being pressed simultaneously.
			boolean altKeyPressed = (event.stateMask & SWT.ALT) != 0 && !ctrlKeyPressed;

			//if (!isConnected()) {
			if (fState==TerminalState.CLOSED) {
				// Pressing ENTER while not connected causes us to connect.
				if (character == '\r' && isConnectOnEnterIfClosed()) {
					connectTerminal();
					return;
				}

				// Ignore all other keyboard input when not connected.
				// Allow other key handlers (such as Ctrl+F1) do their work
				event.doit = true;
				return;
			}

			// Manage the Del key
			if (event.keyCode == 0x000007f) {
				sendString("\u001b[3~"); //$NON-NLS-1$
				return;
			}
			
			// TODO Linux tty is usually expecting a DEL (^?) character
			// but this causes issues with some telnet servers and
			// serial connections. Workaround: stty erase ^H
			//if (event.keyCode == SWT.BS) {
			//	sendChar(SWT.DEL, altKeyPressed);
			//	return;
			//}

			// If the event character is NUL ('\u0000'), then a special key was pressed
			// (e.g., PageUp, PageDown, an arrow key, a function key, Shift, Alt,
			// Control, etc.).  The one exception is when the user presses Control-@,
			// which sends a NUL character, in which case we must send the NUL to the
			// remote endpoint.  This is necessary so that Emacs will work correctly,
			// because Control-@ (i.e., NUL) invokes Emacs' set-mark-command when Emacs
			// is running on a terminal.  When the user presses Control-@, the keyCode
			// is 50.
			// On a Mac, the Cmd key is always used for local commands.

			if (macCmdKeyPressed || (character == '\u0000' && event.keyCode != 50)) {
				// A special key was pressed.  Figure out which one it was and send the
				// appropriate ANSI escape sequence.
				//
				// IMPORTANT: Control will not enter this method for these special keys
				// unless certain <keybinding> tags are present in the plugin.xml file
				// for the Terminal view.  Do not delete those tags.

				String escSeq = null;
				boolean anyModifierPressed = modifierKeys != 0;
				boolean onlyMacCmdKeyPressed = modifierKeys == SWT.COMMAND;

				switch (event.keyCode) {
				case 0x1000001: // Up arrow.
					if (!anyModifierPressed)
						escSeq = fApplicationCursorKeys ? "\u001bOA" : "\u001b[A"; //$NON-NLS-1$ //$NON-NLS-2$
					break;

				case 0x1000002: // Down arrow.
					if (!anyModifierPressed)
						escSeq = fApplicationCursorKeys ? "\u001bOB" : "\u001b[B"; //$NON-NLS-1$ //$NON-NLS-2$
					break;

				case 0x1000003: // Left arrow.
					if (onlyCtrlKeyPressed) {
						escSeq = "\u001b[1;5D"; //$NON-NLS-1$
					} else if (!anyModifierPressed) {
						escSeq = fApplicationCursorKeys ? "\u001bOD" : "\u001b[D"; //$NON-NLS-1$ //$NON-NLS-2$
					} else if (onlyMacCmdKeyPressed) {
						// Cmd-Left is "Home" on the Mac
						escSeq = fApplicationCursorKeys ? "\u001bOH" : "\u001b[H"; //$NON-NLS-1$ //$NON-NLS-2$
					}
					break;

				case 0x1000004: // Right arrow.
					if (onlyCtrlKeyPressed) {
						escSeq = "\u001b[1;5C"; //$NON-NLS-1$
					} else if (!anyModifierPressed) {
						escSeq = fApplicationCursorKeys ? "\u001bOC" : "\u001b[C"; //$NON-NLS-1$ //$NON-NLS-2$
					} else if (onlyMacCmdKeyPressed) {
						// Cmd-Right is "End" on the Mac
						escSeq = fApplicationCursorKeys ? "\u001bOF" : "\u001b[F"; //$NON-NLS-1$ //$NON-NLS-2$
					}
					break;

				case 0x1000005: // PgUp key.
					if (!anyModifierPressed)
						escSeq = "\u001b[5~"; //$NON-NLS-1$
					break;
					
				case 0x1000006: // PgDn key.
					if (!anyModifierPressed)
						escSeq = "\u001b[6~"; //$NON-NLS-1$
					break;

				case 0x1000007: // Home key.
					if (!anyModifierPressed)
						escSeq = fApplicationCursorKeys ? "\u001bOH" : "\u001b[H"; //$NON-NLS-1$ //$NON-NLS-2$
					break;

				case 0x1000008: // End key.
					if (!anyModifierPressed)
						escSeq = fApplicationCursorKeys ? "\u001bOF" : "\u001b[F"; //$NON-NLS-1$ //$NON-NLS-2$
					break;

				case 0x1000009: // Insert.
					if (!anyModifierPressed)
						escSeq = "\u001b[2~"; //$NON-NLS-1$
					break;

				case 0x100000a: // F1 key.
					if (!anyModifierPressed)
						escSeq = "\u001bOP"; //$NON-NLS-1$
					break;

				case 0x100000b: // F2 key.
					if (!anyModifierPressed)
						escSeq = "\u001bOQ"; //$NON-NLS-1$
					break;

				case 0x100000c: // F3 key.
					if (!anyModifierPressed)
						escSeq = "\u001bOR"; //$NON-NLS-1$
					break;

				case 0x100000d: // F4 key.
					if (!anyModifierPressed)
						escSeq = "\u001bOS"; //$NON-NLS-1$
					break;

				case 0x100000e: // F5 key.
					if (!anyModifierPressed)
						escSeq = "\u001b[15~"; //$NON-NLS-1$
					break;

				case 0x100000f: // F6 key.
					if (!anyModifierPressed)
						escSeq = "\u001b[17~"; //$NON-NLS-1$
					break;

				case 0x1000010: // F7 key.
					if (!anyModifierPressed)
						escSeq = "\u001b[18~"; //$NON-NLS-1$
					break;

				case 0x1000011: // F8 key.
					if (!anyModifierPressed)
						escSeq = "\u001b[19~"; //$NON-NLS-1$
					break;

				case 0x1000012: // F9 key.
					if (!anyModifierPressed)
						escSeq = "\u001b[20~"; //$NON-NLS-1$
					break;

				case 0x1000013: // F10 key.
					if (!anyModifierPressed)
						escSeq = "\u001b[21~"; //$NON-NLS-1$
					break;

				case 0x1000014: // F11 key.
					if (!anyModifierPressed)
						escSeq = "\u001b[23~"; //$NON-NLS-1$
					break;

				case 0x1000015: // F12 key.
					if (!anyModifierPressed)
						escSeq = "\u001b[24~"; //$NON-NLS-1$
					break;

				default:
					// Ignore other special keys.  Control flows through this case when
					// the user presses SHIFT, CONTROL, ALT, and any other key not
					// handled by the above cases.
					break;
				}

				if (escSeq == null) {
					// Any unmapped key should be handled locally by Eclipse
					event.doit = true;
					processKeyBinding(event, accelerator);
				} else
					sendString(escSeq);

				// It's ok to return here, because we never locally echo special keys.

				return;
			}

			Logger.log("stateMask = " + event.stateMask); //$NON-NLS-1$

			if (onlyCtrlKeyPressed) {
				switch (character) {
				case ' ':
					// Send a NUL character -- many terminal emulators send NUL when
					// Control-Space is pressed.  This is used to set the mark in Emacs.
					character = '\u0000';
					break;
				case '/':
					// Ctrl+/ is undo in emacs
					character = '\u001f';
					break;
				}
			}

			//TODO: At this point, Ctrl+M sends the same as Ctrl+Shift+M .
			//This is undesired. Fixing this here might make the special Ctrl+Shift+C
			//handling unnecessary further up.
			sendChar(character, altKeyPressed);

			// Special case: When we are in a TCP connection and echoing characters
			// locally, send a LF after sending a CR.
			// ISSUE: Is this absolutely required?

			if (character == '\r' && getTerminalConnector() != null
					&& isConnected()
					&& getTerminalConnector().isLocalEcho()) {
				sendChar('\n', false);
			}

			// Now decide if we should locally echo the character we just sent.  We do
			// _not_ locally echo the character if any of these conditions are true:
			//
			// o This is a serial connection.
			//
			// o This is a TCP connection (i.e., m_telnetConnection is not null) and
			//   the remote endpoint is not a TELNET server.
			//
			// o The ALT (or META) key is pressed.
			//
			// o The character is any of the first 32 ISO Latin-1 characters except
			//   Control-I or Control-M.
			//
			// o The character is the DELETE character.

			if (getTerminalConnector() == null
					|| getTerminalConnector().isLocalEcho() == false || altKeyPressed
					|| (character >= '\u0001' && character < '\t')
					|| (character > '\t' && character < '\r')
					|| (character > '\r' && character <= '\u001f')
					|| character == '\u007f') {
				// No local echoing.
				return;
			}

			// Locally echo the character.

			StringBuffer charBuffer = new StringBuffer();
			charBuffer.append(character);

			// If the character is a carriage return, we locally echo it as a CR + LF
			// combination.

			if (character == '\r')
				charBuffer.append('\n');

			writeToTerminal(charBuffer.toString());
		}

		/*
		 * Process given event as Eclipse key binding.
		 */
		private void processKeyBinding(KeyEvent event, int accelerator) {
			IBindingService bindingService = (IBindingService) PlatformUI
					.getWorkbench().getAdapter(IBindingService.class);
			KeyStroke keyStroke = SWTKeySupport.convertAcceleratorToKeyStroke(accelerator);
			Binding binding = bindingService.getPerfectMatch(KeySequence.getInstance(keyStroke));
			if (binding != null) {
				ParameterizedCommand cmd = binding.getParameterizedCommand();
				if (cmd != null) {
					IHandlerService handlerService = (IHandlerService) PlatformUI
							.getWorkbench().getAdapter(IHandlerService.class);
					Event cmdEvent = new Event();
					cmdEvent.type = SWT.KeyDown;
					cmdEvent.display = event.display;
					cmdEvent.widget = event.widget;
					cmdEvent.character = event.character;
					cmdEvent.keyCode = event.keyCode;
					////Bug - KeyEvent.keyLocation was introduced in Eclipse 3.6
					////Use reflection for now to remain backward compatible down to Eclipse 3.4
					//cmdEvent.keyLocation = event.keyLocation;
					try {
						Field f1 = event.getClass().getField("keyLocation"); //$NON-NLS-1$
						Field f2 = cmdEvent.getClass().getField("keyLocation"); //$NON-NLS-1$
						f2.set(cmdEvent, f1.get(event));
					} catch(NoSuchFieldException nsfe) {
						/* ignore, this is Eclipse 3.5 or earlier */
					} catch(Throwable t) {
						t.printStackTrace();
					}
					cmdEvent.stateMask = event.stateMask;
					event.doit = false;
					try {
						handlerService.executeCommand(cmd, cmdEvent);
					} catch (ExecutionException e) {
						TerminalPlugin.getDefault().getLog().log(
								new Status(IStatus.ERROR,TerminalPlugin.PLUGIN_ID,e.getLocalizedMessage(),e));
					} catch (Exception e) {
						// ignore other exceptions from cmd execution
					}
				}
			}
		}

	}

	public void setTerminalTitle(String title) {
		fTerminalListener.setTerminalTitle(title);
	}


	public TerminalState getState() {
		return fState;
	}


	public void setState(TerminalState state) {
		fState=state;
		fTerminalListener.setState(state);
		// enable the (blinking) cursor if the terminal is connected
		runAsyncInDisplayThread(new Runnable() {
			public void run() {
				if(fCtlText!=null && !fCtlText.isDisposed()) {
					if (isConnected()) {
						fCtlText.setCursorEnabled(true);
						fPollingTextCanvasModel.startPolling();
					} else {
						fCtlText.setCursorEnabled(false);
						// Stop capturing all key events
						fFocusListener.captureKeyEvents(false);
						fPollingTextCanvasModel.stopPolling();
					}
				}
			}
		});
	}
	/**
	 * @param runnable run in display thread
	 */
	private void runAsyncInDisplayThread(Runnable runnable) {
		if(Display.findDisplay(Thread.currentThread())!=null)
			runnable.run();
		else if(PlatformUI.isWorkbenchRunning() && PlatformUI.getWorkbench().getDisplay() != null && !PlatformUI.getWorkbench().getDisplay().isDisposed())
			PlatformUI.getWorkbench().getDisplay().asyncExec(runnable);
		// else should not happen and we ignore it...
	}

	public String getSettingsSummary() {
		if(getTerminalConnector()!=null)
			return getTerminalConnector().getSettingsSummary();
		return ""; //$NON-NLS-1$
	}

	public void setConnector(ITerminalConnector connector) {
		fConnector=connector;

	}
	public ICommandInputField getCommandInputField() {
		return fCommandInputField;
	}

	public void setCommandInputField(ICommandInputField inputField) {
		if(fCommandInputField!=null)
			fCommandInputField.dispose();
		fCommandInputField=inputField;
		if(fCommandInputField!=null)
			fCommandInputField.createControl(fWndParent, this);
		if(fWndParent.isVisible())
			fWndParent.layout(true);
	}

	public int getBufferLineLimit() {
		return fTerminalModel.getMaxHeight();
	}

	public void setBufferLineLimit(int bufferLineLimit) {
		if(bufferLineLimit<=0)
			return;
		synchronized (fTerminalModel) {
			if(fTerminalModel.getHeight()>bufferLineLimit)
				fTerminalModel.setDimensions(bufferLineLimit, fTerminalModel.getWidth());
			fTerminalModel.setMaxHeight(bufferLineLimit);
		}
	}

	public boolean isScrollLock() {
		return fCtlText.isScrollLock();
	}

	public void setScrollLock(boolean on) {
		fCtlText.setScrollLock(on);
	}

	public void setInvertedColors(boolean invert) {
		fCtlText.setInvertedColors(invert);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl#setConnectOnEnterIfClosed(boolean)
	 */
	public final void setConnectOnEnterIfClosed(boolean on) {
		connectOnEnterIfClosed = on;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl#isConnectOnEnterIfClosed()
	 */
	public final boolean isConnectOnEnterIfClosed() {
		return connectOnEnterIfClosed;
	}

	public void setVT100LineWrapping(boolean enable) {
		getTerminalText().setVT100LineWrapping(enable);
	}

	public boolean isVT100LineWrapping() {
		return getTerminalText().isVT100LineWrapping();
	}

	public void enableApplicationCursorKeys(boolean enable) {
		fApplicationCursorKeys = enable;
	}
	
}
