package org.eclipse.cdt.utils.coff.parser;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.cdt.utils.Addr2line;
import org.eclipse.cdt.utils.CPPFilt;
import org.eclipse.cdt.utils.CygPath;
import org.eclipse.cdt.utils.coff.Coff;
import org.eclipse.cdt.utils.coff.PE;
import org.eclipse.cdt.utils.coff.PE.Attribute;
import org.eclipse.core.runtime.IPath;

/**
 */
public class BinaryObject extends BinaryFile implements IBinaryObject {

	PE.Attribute attribute;
	ArrayList symbols;
	int type = IBinaryFile.OBJECT;

	public BinaryObject(IPath p) throws IOException {
		super(p);
		loadInformation();
		hasChanged();
	}

	public void setType(int t) {
		type = t;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryFile#getType()
	 */
	public int getType() {
		return type;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#getBSS()
	 */
	public long getBSS() {
		return 0;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#getCPU()
	 */
	public String getCPU() {
		Attribute attr = getAttribute();
		if (attr != null) {
			return attribute.getCPU();
		}
		return "";
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#getData()
	 */
	public long getData() {
		return 0;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#getName()
	 */
	public String getName() {
		return getPath().lastSegment().toString();
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#getSymbols()
	 */
	public ISymbol[] getSymbols() {
		if (hasChanged() || symbols == null) {
			if (symbols == null) {
				symbols = new ArrayList(5);
			}
			try {
				loadInformation();
			} catch (IOException e) {
			}
		}
		return (ISymbol[])symbols.toArray(new ISymbol[0]);
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#getText()
	 */
	public long getText() {
		return 0;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#hasDebug()
	 */
	public boolean hasDebug() {
		Attribute attr = getAttribute();
		if (attr != null) {
			return attr.hasDebug();
		}
		return false;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#isLittleEndian()
	 */
	public boolean isLittleEndian() {
		Attribute attr = getAttribute();
		if (attr != null) {
			return attr.isLittleEndian();
		}
		return false;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryExecutable#getNeededSharedLibs()
	 */
	public String[] getNeededSharedLibs() {
		return new String[0];
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryShared#getSoName()
	 */
	public String getSoName() {
		return "";
	}

	protected PE getPE() throws IOException {
		return new PE(getPath().toOSString());
	}

	protected PE.Attribute getAttribute() {
		if (hasChanged()) {
			try {
				loadInformation(); 
			} catch (IOException e) {
			}
		}
		return attribute;
	}

	protected void loadInformation() throws IOException {
		PE pe = getPE();
		loadInformation(pe);
		pe.dispose();
	}

	private void loadInformation(PE pe) throws IOException {
		loadAttribute(pe);
		if (symbols != null) {
			symbols.clear();
			loadSymbols(pe);
			symbols.trimToSize();
		}
	}

	private void loadAttribute(PE pe) throws IOException {
		attribute = pe.getAttribute();
	}

	private void loadSymbols(PE pe) throws IOException {
		Addr2line addr2line = getAddr2Line();
		CPPFilt cppfilt = getCPPFilt();
		CygPath cygpath = getCygPath();

		Coff.Symbol[] peSyms = pe.getSymbols();
		byte[] table = pe.getStringTable();
		addSymbols(peSyms, table, addr2line, cppfilt, cygpath);

		if (addr2line != null) {
			addr2line.dispose();
		}
		if (cppfilt != null) {
			cppfilt.dispose();
		}
		if (cygpath != null) {
			cygpath.dispose();
		}
		symbols.trimToSize();
	}

	protected void addSymbols(Coff.Symbol[] peSyms, byte[] table, Addr2line addr2line, CPPFilt cppfilt, CygPath cygpath) {
		for (int i = 0; i < peSyms.length; i++) {
			if (peSyms[i].isFunction() || peSyms[i].isPointer() ||peSyms[i].isArray()) {
				String name = peSyms[i].getName(table);
				if (name == null || name.trim().length() == 0 ||
				    !Character.isJavaIdentifierStart(name.charAt(0))) {
					continue;
				}
				Symbol sym = new Symbol(this);
				sym.type = peSyms[i].isFunction() ? ISymbol.FUNCTION : ISymbol.VARIABLE;
				sym.addr = peSyms[i].n_value;

				sym.name = name;
				if (cppfilt != null) {
					try {
						sym.name = cppfilt.getFunction(sym.name);
					} catch (IOException e1) {
					}
				}

				sym.filename = null;
				sym.startLine = 0;
				sym.endLine = 0;
				if (addr2line != null) {
					try {
						sym.filename =  addr2line.getFileName(sym.addr);
						if (cygpath != null)
							sym.filename =  cygpath.getFileName(sym.filename);
						sym.startLine = addr2line.getLineNumber(sym.addr);
					} catch (IOException e) {
					}
				}
				addSymbol(sym);
			}
		}
	}

	protected void addSymbol(Symbol sym) {
		symbols.add(sym);
	}

}
