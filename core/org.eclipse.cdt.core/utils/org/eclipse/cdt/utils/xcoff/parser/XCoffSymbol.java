/*
 * Created on Jul 6, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.cdt.utils.xcoff.parser;

import java.io.IOException;

import org.eclipse.cdt.utils.Addr2line;
import org.eclipse.cdt.utils.BinaryObjectAdapter;
import org.eclipse.cdt.utils.Symbol;
import org.eclipse.core.runtime.IPath;


/**
 * @author DInglis
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class XCoffSymbol extends Symbol {

	/**
	 * @param binary
	 * @param name
	 * @param type
	 * @param addr
	 * @param size
	 * @param sourceFile
	 * @param startLine
	 * @param endLine
	 */
	public XCoffSymbol(BinaryObjectAdapter binary, String name, int type, long addr, long size, IPath sourceFile, int startLine,
			int endLine) {
		super(binary, name, type, addr, size, sourceFile, startLine, endLine);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param binary
	 * @param name
	 * @param type
	 * @param addr
	 * @param size
	 */
	public XCoffSymbol(BinaryObjectAdapter binary, String name, int type, long addr, long size) {
		super(binary, name, type, addr, size);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.Symbol#getLineNumber(long)
	 */
	public int getLineNumber(long offset) {
		int line = -1;
		Addr2line addr2line = ((XCOFFBinaryObject)binary).getAddr2line(true);
		if (addr2line != null) {
			try {
				return addr2line.getLineNumber(getAddress() + offset);
			} catch (IOException e) {
				// ignore
			}
		}
		return line;
	}
	
}
