/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.ui.messages;

import java.util.Stack;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.ISystemThemeConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

/**
 * A message line. It distinguishs between "normal" messages and errors.
 * Setting an error message hides a currently displayed message until
 * <code>clearErrorMessage</code> is called.
 */
public class SystemMessageLine
       extends Composite
       implements ISystemMessageLine
{
	
	private Button moreButton;
	private Label image;
    private Text widget;
	private Stack messageStack = new Stack();
	private static final int ERROR = 3;
	private static final int WARNING = 2;
	private static final int INFO = 1;
	private static final int NONE = 0;
	
	private abstract class MyMessage {
		/**
		 * @return The Image of the message based on its type.
		 */
		Image getImage() {
			int type = getType();
			switch (type) {
				case ERROR:
					return JFaceResources.getImage(org.eclipse.jface.dialogs.Dialog.DLG_IMG_MESSAGE_ERROR);
				case WARNING:
					return JFaceResources.getImage(org.eclipse.jface.dialogs.Dialog.DLG_IMG_MESSAGE_WARNING);
				case INFO:
					return JFaceResources.getImage(org.eclipse.jface.dialogs.Dialog.DLG_IMG_MESSAGE_INFO);
				default:
					return JFaceResources.getImage(org.eclipse.jface.dialogs.Dialog.DLG_IMG_MESSAGE_INFO);
			}
		}
		
		Color getColor() {
			int type = getType();
			switch (type) {
				case ERROR:
					return getColor(ISystemThemeConstants.MESSAGE_ERROR_COLOR);
				case WARNING:
					return getColor(ISystemThemeConstants.MESSAGE_WARNING_COLOR);
				case INFO:
					return getColor(ISystemThemeConstants.MESSAGE_INFORMATION_COLOR);
				default: 
					return getColor(ISystemThemeConstants.MESSAGE_INFORMATION_COLOR);
			}
		}
		
		/**
		 * @param symbolicName the name of the color in the current theme's color registry.
		 * @return an SWT Color or null.
		 */
		private Color getColor(String symbolicName)
		{
			ColorRegistry registry = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();
			Color result = registry.get(symbolicName);
			return result;
		}
		
		boolean isError() {
			return getType() == ERROR;
		}
		
		/**
		 * @return The id of the message or null if there is none.
		 */
		abstract String getID();
		
		/**
		 * @return The full text of the message to be shown in the message line.
		 */
		abstract String getText();
		
		/**
		 * @return The tooltip for the message, to be shown when hovering over the message line.
		 */
		abstract String getTooltip();
		
		/**
		 * @return true if there is more text that can be shown in a message details pane.
		 */
		abstract boolean hasMore();

		/**
		 * @return The SystemMessage version of the message.
		 */
		abstract SystemMessage toSystemMessage();
		
		/**
		 * @return The type of the message. One of NONE, INFO, WARNING, or ERROR.
		 */
		abstract int getType();
		
		/**
		 * @return The data values associated with this message.
		 */
		abstract Object[] getData();
		
		/**
		 * @return true if the message resulted form a strange occurence.
		 */
		abstract boolean isStrange();
	}
	
	private class MySystemMessage extends MyMessage {
		
		private SystemMessage message = null;

		/**
		 * @param message
		 */
		MySystemMessage(SystemMessage message) {
			this.message = message;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.rse.ui.messages.SystemMessageLine.MyMessage#toSystemMessage()
		 */
		SystemMessage toSystemMessage() {
			return message;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.rse.ui.messages.SystemMessageLine.MyMessage#getID()
		 */
		String getID() {
			return message.getFullMessageID();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.rse.ui.messages.SystemMessageLine.MyMessage#getText()
		 */
		String getText() {
			return message.getLevelOneText();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.rse.ui.messages.SystemMessageLine.MyMessage#getTooltip()
		 */
		String getTooltip() {
			return message.getFullMessageID() + ": " + getText();
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.rse.ui.messages.SystemMessageLine.MyMessage#hasMore()
		 */
		boolean hasMore() {
			String text2 = message.getLevelTwoText();
			return (text2 != null) && (text2.length() > 0);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.rse.ui.messages.SystemMessageLine.MyMessage#wasStrange()
		 */
		boolean isStrange() {
			return message.getIndicator() == SystemMessage.UNEXPECTED;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.rse.ui.messages.SystemMessageLine.MyMessage#getData()
		 */
		Object[] getData() {
			Object[] result = message.getSubVariables();
			if (result == null) result = new Object[0];
			return result;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.rse.ui.messages.SystemMessageLine.MyMessage#getType()
		 */
		int getType() {
			int result = NONE;
			if (message != null) {
				switch (message.getIndicator()) {
					case SystemMessage.COMPLETION:
					case SystemMessage.INFORMATION:
					case SystemMessage.INQUIRY:
						result = INFO;
						break;
					case SystemMessage.ERROR:
					case SystemMessage.UNEXPECTED:
						result = ERROR;
						break;
					case SystemMessage.WARNING:
						result = WARNING;
						break;
					default: result = NONE;
				}
			}
			return result;
		}
	}
	
	private class MyImpromptuMessage extends MyMessage {
		
		private int type = NONE;
		private String text1 = "";
		private String text2 = null;
		
		/**
		 * @param type The type of the message.
		 * @param text1 The first-level text of the message.
		 */
		MyImpromptuMessage(int type, String text1) {
			this.type = type;
			this.text1 = text1;
		}
		
		/**
		 * @param type The type of the message.
		 * @param text1 The first-level text of the message.
		 * @param text2 the second-level text of the message.
		 */
		MyImpromptuMessage(int type, String text1, String text2) {
			this.type = type;
			this.text1 = text1;
			this.text2 = text2;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.rse.ui.messages.SystemMessageLine.MyMessage#toSystemMessage()
		 */
		public SystemMessage toSystemMessage() {
			String id = null;
			Object[] data = null;
			if (text2 == null) {
				id = isError() ? ISystemMessages.MSG_GENERIC_E : ISystemMessages.MSG_GENERIC_I;
				data = new Object[] {text1};
			} else {
				id = isError() ? ISystemMessages.MSG_GENERIC_E_HELP : ISystemMessages.MSG_GENERIC_I_HELP;
				data = new Object[] {text1, text2};
			}
			SystemMessage result = RSEUIPlugin.getPluginMessage(id, data);
			return result;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.rse.ui.messages.SystemMessageLine.MyMessage#getID()
		 */
		String getID() {
			return null;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.rse.ui.messages.SystemMessageLine.MyMessage#getText()
		 */
		String getText() {
			return text1;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.rse.ui.messages.SystemMessageLine.MyMessage#getTooltip()
		 */
		String getTooltip() {
			return text1;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.rse.ui.messages.SystemMessageLine.MyMessage#hasMore()
		 */
		boolean hasMore() {
			return text2 != null;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.rse.ui.messages.SystemMessageLine.MyMessage#wasStrange()
		 */
		boolean isStrange() {
			return false;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.rse.ui.messages.SystemMessageLine.MyMessage#getData()
		 */
		Object[] getData() {
			return new Object[0];
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.rse.ui.messages.SystemMessageLine.MyMessage#getType()
		 */
		int getType() {
			return type;
		}
	}
	
	/**
	 * Creates a new message line as a child of the given parent.
	 */
	public SystemMessageLine(Composite parent)
	{
		super(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 5;
		layout.marginHeight = 2;
		layout.marginWidth = 3;
		setLayout(layout);
		
		image = new Label(this, SWT.NONE);
		image.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		
		//  this is a read-only text field so it is tab enabled and readable by a screen reader.
		widget = new Text(this, SWT.READ_ONLY | SWT.SINGLE);
		widget.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));
		widget.setBackground(parent.getBackground());
		
		moreButton = new Button(this, SWT.NONE);
		moreButton.setImage(RSEUIPlugin.getDefault().getImage(ISystemIconConstants.ICON_SYSTEM_HELP_ID));
		moreButton.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		moreButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				if (e.getSource().equals(moreButton)) {
					MyMessage message = getTopMessage();
					if (message != null) {
						SystemMessage m = message.toSystemMessage();
						Shell shell = getShell();
						SystemMessageDialog dialog = new SystemMessageDialog(shell, m);
						dialog.openWithDetails();
					}
				}
			}
		});	
		// add accessibility information to the "more" button
		moreButton.setToolTipText(SystemResources.RESID_MSGLINE_TIP);
		moreButton.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			public void getName(AccessibleEvent e) {
				getHelp(e);
			}
			public void getHelp(AccessibleEvent e) {
				e.result = moreButton.getToolTipText();
			}
		});
		
		addControlListener(new ControlListener() {
			public void controlMoved(ControlEvent e) {
			}
			public void controlResized(ControlEvent e) {
				adjustText();
				layout();
			}
		});
        addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				widget.dispose();
				moreButton.dispose();
				image.dispose();
			}
		});
        
		showTopMessage(); 
			
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.messages.ISystemMessageLine#clearErrorMessage()
	 */
	public void clearErrorMessage() {
		MyMessage message = getTopMessage();
		if (message != null && message.isError()) {
			popMessage();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.messages.ISystemMessageLine#clearMessage()
	 */
	public void clearMessage() {
		MyMessage message = getTopMessage();
		if (message != null && !message.isError()) {
			popMessage();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.messages.ISystemMessageLine#getErrorMessage()
	 */
	public String getErrorMessage() {
		MyMessage message = getTopMessage();
		if (message != null && message.isError()) return message.getText();
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.messages.ISystemMessageLine#getMessage()
	 */
	public String getMessage() {
		MyMessage message = getTopMessage();
		if (message != null && !message.isError()) return message.getText();
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.messages.ISystemMessageLine#getSystemErrorMessage()
	 */
	public SystemMessage getSystemErrorMessage() {
		MyMessage message = getTopMessage();
		if (message != null && message.isError()) return message.toSystemMessage();
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.messages.ISystemMessageLine#setErrorMessage(java.lang.String)
	 */
	public void setErrorMessage(String message) {
		MyMessage temp = new MyImpromptuMessage(ERROR, message);
		pushMessage(temp);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.messages.ISystemMessageLine#setErrorMessage(org.eclipse.rse.services.clientserver.messages.SystemMessage)
	 */
	public void setErrorMessage(SystemMessage message) {
		MyMessage temp = new MySystemMessage(message);
		pushMessage(temp);
		logMessage(message);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.messages.ISystemMessageLine#setErrorMessage(java.lang.Throwable)
	 */
	public void setErrorMessage(Throwable throwable)
	{
		SystemMessage message = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_ERROR_UNEXPECTED);
		message.makeSubstitution(throwable);
		setErrorMessage(message);
	}
	
    /**
	 * Set the message text. If the message line currently displays an error,
	 * the message is stored and will be shown after a call to clearErrorMessage.
	 */
	public void setMessage(String message) {
		MyMessage temp = new MyImpromptuMessage(INFO, message);
		pushMessage(temp);
	}

	/**
	 * Set the non-error message text, using a SystemMessage object. 
	 * If the message line currently displays an error,
	 * the message is stored and will be shown after a call to clearMessage.
	 * The SystemMessage text is always shown as a "non-error". 
	 */
	public void setMessage(SystemMessage message) {
		MyMessage temp = new MySystemMessage(message);
		if (temp.isError()) {
			temp = new MyImpromptuMessage(NONE, message.getLevelOneText(), message.getLevelTwoText());
		}
		pushMessage(temp);
	}
	
	/**
	 * Pushes a new message onto the stack and shows it.
	 * @param message The MyMessage to push on the stack.
	 */
	private void pushMessage(MyMessage message) {
		messageStack.push(message);
		showTopMessage();
	}
	
	/**
	 * Pops a message off the message stack and shows the new top message.
	 */
	private void popMessage() {
		if (!messageStack.isEmpty()) messageStack.pop();
		showTopMessage();
	}
	
	/**
	 * Retrieves the top MyMessage from the stack
	 * @return A MyMessage or null if the stack is empty.
	 */
	private MyMessage getTopMessage() {
		if (messageStack.isEmpty()) return null;
		return (MyMessage) messageStack.peek();
	}
	
	/**
	 * Shows the top message on the stack. If the stack is empty it will "show" nothing.
	 */
	private void showTopMessage() {
		MyMessage message = getTopMessage();
		setIcon(message);
		setText(message);
		setMoreButton(message);
		layout();
	}

	/**
	 * Sets the icon field of the widget to the appropriate symbol depending on the
	 * type of message.
	 * @param message the message used to determine the icon type.
	 */
	private void setIcon(MyMessage message) {
		Image t = (message == null) ? null : message.getImage();
		image.setImage(t);
	}

	/**
	 * Write the text from a MyMessage to the widget.
	 * @param message the message from which to get the text.
	 */
	private void setText(MyMessage message) {
		String text = "";
		String toolTip = null;
		Color color = null;
		if (message != null) {
			text = message.getText();
			toolTip = message.getTooltip();
			color = message.getColor();
		}
		widget.setToolTipText(toolTip);
		widget.setForeground(color);
		widget.setText(text);
		widget.setData(text);
		adjustText();
	}

	/**
	 * Hide or show the "more" button. If the message has second level text then
	 * the more button is shown.
	 */
	private void setMoreButton(MyMessage message) {
		boolean visible = message != null && message.hasMore();
		moreButton.setVisible(visible);
	}
	
	/**
	 * Adjusts the text in the widget. The full text is stored in the data field of the
	 * Text widget. The partial text is shown if the width of the containing control 
	 * is too small to hold it.
	 */
	private void adjustText() {
		GC gc = new GC(widget);
		int maxWidth = getSize().x;
		maxWidth -= moreButton.getSize().x;
		maxWidth -= image.getSize().x;
		maxWidth -= 17; // a guess at the padding between controls
		maxWidth = (maxWidth >= 0) ? maxWidth : 0;
		String text = (String) widget.getData();
		if (text != null) {
			if (gc.stringExtent(text).x > maxWidth) {
				StringBuffer head = new StringBuffer(text);
				int n = head.length();
				head.append("...");
				while (n > 0) {
					text = head.toString();
					if (gc.stringExtent(text).x <= maxWidth) break;
					head.deleteCharAt(--n);
				}
				if (n == 0) text = "";
			}
			widget.setText(text);
		}
		gc.dispose();
	}

	/**
	 * Logs a message in the appropriate log according to the current preferences.
	 * @param message The SystemMessage to be logged. 
	 */
	private void logMessage(SystemMessage message) {
		MyMessage m = new MySystemMessage(message);
		Object[] data = m.getData();
		for (int i = 0; i < data.length; i++) {
			Object object = data[i];
			StringBuffer buffer = new StringBuffer(200);
			buffer.append(m.getID());
			buffer.append(": SUB#");
			buffer.append(Integer.toString(i));
			buffer.append(":");
			buffer.append(object.toString());
			logMessage(m.getType(), buffer.toString(), false);
		}
		logMessage(m.getType(), m.getID(), m.isStrange());
	}
	
	/**
	 * Sends a text message to the log.
	 * @param type The type of the message - NONE, INFO, WARNING or ERROR.
	 * @param text The text to log.
	 * @param stackTrace If true then generate a stack trace in the log. Ignored if the 
	 * type is not ERROR.
	 */
	private void logMessage(int type, String text, boolean stackTrace) {
		switch (type) {
			case ERROR:
				Exception e = stackTrace ? new Exception("Stack Trace") : null;
				SystemBasePlugin.logError(text, e);
				break;
			case WARNING:
				SystemBasePlugin.logWarning(text);
				break;
			case INFO:
			case NONE:
			default:
				SystemBasePlugin.logInfo(text);
		}
	}
	
}