package org.eclipse.cdt.internal.ui.editor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public interface IPainter {
	
	/** Paint reasons */
	int SELECTION=		0;
	int TEXT_CHANGE=	1;
	int KEY_STROKE=		2;
	int MOUSE_BUTTON= 4;
	int INTERNAL=			8;
	
	
	void dispose();
	
	void paint(int reason);
	
	void deactivate(boolean redraw);
	
	void setPositionManager(IPositionManager manager);
}

