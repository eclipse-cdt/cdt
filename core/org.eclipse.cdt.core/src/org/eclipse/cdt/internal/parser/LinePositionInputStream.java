package org.eclipse.cdt.internal.parser;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * An input stream that only observes the stream and remembers the position of new
 * lines
 */
public class LinePositionInputStream extends InputStream {
	
	private List fLinePositions;
	private BufferedInputStream buffered;
	
	private boolean fRRead;
	private boolean fAddLine;
	
	private int fCurrPosition;
	
	public LinePositionInputStream(InputStream inputStream) throws IOException {
		buffered = new BufferedInputStream(inputStream);
		fLinePositions= new ArrayList(30);
		fAddLine= true;
		fRRead= false;
		fCurrPosition= 0;
	}
	
	public int read() throws IOException {
	
		int ch = buffered.read();	
		if (fRRead && ch == '\n') {
			fRRead= false;
		} else {
			if (fAddLine) {
				fLinePositions.add(new Integer(fCurrPosition));
				fAddLine= false;		
			}
	
			if (ch == '\n' || ch == '\r') {
				fAddLine= true;
				fRRead= (ch == '\r');
			} else {
				fRRead= false;
			}	
		}
		fCurrPosition++;
		return ch;
	}
		
	public int getPosition(int line, int col) {
		line--;
		col--;
		if (line < fLinePositions.size()) {
			Integer lineStart= (Integer)fLinePositions.get(line);
			return lineStart.intValue() + col;
		}
		return -1;
	}
}