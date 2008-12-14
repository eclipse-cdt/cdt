/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - initial API and implementation
 * Anna Dushistova  (MontaVista) - [258631][api] ITerminalService should be public API
 *******************************************************************************/

package org.eclipse.rse.services.terminals;

import org.eclipse.rse.services.shells.IHostShell;

/**
 * Interface representing a terminal connection through Streams.
 *
 * Rather than the underlying {@link IBaseShell}, an ITerminalShell connection
 * adds methods that describe the presentation of the data transmitted over its
 * Streams, as well as methods like {@link #setTerminalSize(int, int)} to change
 * the behavior of the presentation of this data. An instance of ITerminalShell
 * is typically obtained from an {@link ITerminalService}.
 *
 * In RSE, a single remote shell instance can only either support the streamed
 * ITerminalShell interface or the listener-based {@link IHostShell} interface,
 * but not both. Note, though, that with the capabilities that an ITerminalShell
 * has, it is always possible to adapt it to an IHostShell; this is typically
 * not possible the other way round. We therefore recommend extenders of RSE
 * that used to subclass IHostShell to move to the new IBaseShell /
 * ITerminalShell APIs eventually, if they can.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 *              Clients must subclass the provided {@link AbstractTerminalShell}
 *              or {@link TerminalShellDecorator} classes rather than
 *              implementing this interface directly.
 *
 * @see IBaseShell
 * @see ITerminalService
 * @see AbstractTerminalShell
 * @see TerminalShellDecorator
 * @since org.eclipse.rse.services 3.1
 */
public interface ITerminalShell extends IBaseShell {

	/**
	 * Get the Terminal Type that's expected on this connection.
	 *
	 * The terminal type may be specified by the client when constructing a
	 * concrete instance of an ITerminalShell, or a remote side may actually
	 * expect a particular terminal type to be present.
	 *
	 * @return the terminal type expected by the remote side to properly render
	 *         the Streams associated with this Terminal, or <code>null</code>
	 *         if the ITerminalShell does not know what kind of Terminal Type is
	 *         expected.
	 */
	public String getPtyType();

	/**
	 * Return the default encoding that the terminal service had specified when
	 * creating this terminal connection, or that's known from the remote side
	 * to be expected. This is not necessarily known or accurate, and may be
	 * <code>null</code>.
	 *
	 * TODO I'm not actually sure if this method is a good idea. Perhaps we
	 * should use the IAdaptable mechanism for dealing with encodings, since our
	 * shells basically deal with binary data only.
	 *
	 * @return the specified default encoding, or <code>null</code> if
	 *         unknown.
	 */
	public String getDefaultEncoding();

    /**
	 * Notify the remote site that the size of the terminal has changed. There
	 * is no guarantee that the remote side is actually capable of changing the
	 * Terminal size.
	 *
	 * @param newWidth
	 * @param newHeight
	 */
	void setTerminalSize(int newWidth, int newHeight);

	/**
	 * Test if local echo is needed on this terminal connection. Clients are
	 * expected to return <code>false</code> if in doubt.
	 *
	 * @return <code>true</code> if a local echo is needed.
	 */
	boolean isLocalEcho();

}
