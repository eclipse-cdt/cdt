package org.eclipse.cdt.utils.ui.controls;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.swt.widgets.*;

public interface IHyperlinkListener {

public void linkActivated(Control linkLabel);
public void linkEntered(Control linkLabel);
public void linkExited(Control linkLabel);
}
