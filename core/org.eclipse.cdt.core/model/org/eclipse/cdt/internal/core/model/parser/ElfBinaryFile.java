package org.eclipse.cdt.internal.core.model.parser;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.eclipse.cdt.core.IBinaryParser.IBinaryExecutable;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.IBinaryParser.IBinaryShared;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.cdt.utils.elf.AR;
import org.eclipse.cdt.utils.elf.Elf;
import org.eclipse.cdt.utils.elf.ElfHelper;
import org.eclipse.cdt.utils.elf.Elf.Attribute;
import org.eclipse.cdt.utils.elf.ElfHelper.Sizes;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.PlatformObject;

/**
 */
public class ElfBinaryFile extends PlatformObject implements IBinaryFile, IBinaryObject, IBinaryExecutable, IBinaryShared {
	IPath path;
	AR.ARHeader header;
	long timestamp;
	String soname;
	String[] needed;
	Sizes sizes;
	Attribute attribute;
	ArrayList symbols;

	public ElfBinaryFile(IPath p) throws IOException {
		this(p, null);
	}

	public ElfBinaryFile(IPath p, AR.ARHeader h) throws IOException {
		header = h;
		path = p;
		loadInformation();
		hasChanged();
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryFile#getFile()
	 */
	public IPath getPath() {
		return path;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#getBSS()
	 */
	public long getBSS() {
		Sizes sz = getSizes();
		if (sz != null) {
			return sizes.bss;
		}
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
		Sizes sz = getSizes();
		if (sz != null) {
			return sizes.data;
		}
		return 0;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#getText()
	 */
	public long getText() {
		Sizes sz = getSizes();
		if (sz != null) {
			return sizes.text;
		}
		return 0;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryShared#getSoName()
	 */
	public String getSoName() {
		if (hasChanged()) {
			try {
				loadInformation();
			} catch (IOException e) {
			}
		}
		if (soname != null) {
			return soname;
		}
		return "";
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#hasDebug()
	 */
	public boolean hasDebug() {
		Attribute attr = getAttribute();
		if (attr != null) {
			return attribute.hasDebug();
		}
		return false;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#isLittleEndian()
	 */
	public boolean isLittleEndian() {
		Attribute attr = getAttribute();
		if (attr != null) {
			return attribute.isLittleEndian();
		}
		return false;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryExecutable#getNeededSharedLibs()
	 */
	public String[] getNeededSharedLibs() {
		if (hasChanged()) {
			try {
				loadInformation();
			} catch (IOException e) {
			}
		}
		if (needed != null) {
			return needed;
		}
		return new String[0];
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryFile#getType()
	 */
	public int getType() {
		int type = 0;
		Attribute attr = getAttribute();
		if (attr != null) {
			switch (attribute.getType()) {
				case Attribute.ELF_TYPE_EXE :
					type = IBinaryFile.EXECUTABLE;
					break;

				case Attribute.ELF_TYPE_SHLIB :
					type = IBinaryFile.SHARED;
					break;

				case Attribute.ELF_TYPE_OBJ :
					type = IBinaryFile.OBJECT;
					break;

				case Attribute.ELF_TYPE_CORE :
					type = IBinaryFile.CORE;
					break;
			}
		}
		return type;
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
		return (ISymbol[]) symbols.toArray(new ISymbol[0]);
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryFile#getContents()
	 */
	public InputStream getContents() {
		InputStream stream = null;
		// Archive ?
		if (path != null && header != null) {
			try {
				stream = new ByteArrayInputStream(header.getObjectData());
			} catch (IOException e) {
			}
		} else if (path != null) {
			try {
				stream = new FileInputStream(path.toFile());
			} catch (IOException e) {
			}
		}
		if (stream == null) {
			stream = new ByteArrayInputStream(new byte[0]);
		}
		return stream;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#getName()
	 */
	public String getName() {
		if (header != null) {
			return header.getObjectName();
		}
		if (path != null) {
			return path.lastSegment().toString();
		}
		return "";
	}

	public String toString() {
		return getName();
	}

	protected Attribute getAttribute() {
		if (hasChanged()) {
			try {
				loadInformation();
			} catch (IOException e) {
			}
		}
		return attribute;
	}

	protected Sizes getSizes() {
		if (hasChanged()) {
			try {
				loadInformation();
			} catch (IOException e) {
			}
		}
		return sizes;
	}

	boolean hasChanged() {
		long modification = path.toFile().lastModified();
		boolean changed = modification != timestamp;
		timestamp = modification;
		return changed;
	}

	protected ElfHelper getElfHelper() throws IOException {
		// Archive ?
		if (header != null) {
			return new ElfHelper(header.getElf());
		} else if (path != null) {
			return new ElfHelper(path.toOSString());
		}
		throw new IOException("No file assiocated with Binary");
	}

	protected void loadInformation() throws IOException {
		ElfHelper helper = null;
		try {
			helper = getElfHelper();
			loadInformation(helper);
		} finally {
			if (helper != null) {
				helper.dispose();
			}
		}
	}

	private void loadInformation(ElfHelper helper) throws IOException {
		loadAttributes(helper);
		if (symbols != null) {
			symbols.clear();
			loadSymbols(helper);
			symbols.trimToSize();
		}
	}

	private void loadAttributes(ElfHelper helper) throws IOException {
		Elf.Dynamic[] sharedlibs = helper.getNeeded();
		needed = new String[sharedlibs.length];
		for (int i = 0; i < sharedlibs.length; i++) {
			needed[i] = sharedlibs[i].toString();
		}
		sizes = helper.getSizes();
		soname = helper.getSoname();
		attribute = helper.getElf().getAttributes();
	}

	private void loadSymbols(ElfHelper helper) throws IOException {
		Elf.Dynamic[] sharedlibs = helper.getNeeded();
		needed = new String[sharedlibs.length];
		for (int i = 0; i < sharedlibs.length; i++) {
			needed[i] = sharedlibs[i].toString();
		}
		sizes = helper.getSizes();
		soname = helper.getSoname();
		attribute = helper.getElf().getAttributes();

		addSymbols(helper.getExternalFunctions(), ISymbol.FUNCTION);
		addSymbols(helper.getLocalFunctions(), ISymbol.FUNCTION);
		addSymbols(helper.getExternalObjects(), ISymbol.VARIABLE);
		addSymbols(helper.getLocalObjects(), ISymbol.VARIABLE);
		symbols.trimToSize();
	}

	private void addSymbols(Elf.Symbol[] array, int type) {
		for (int i = 0; i < array.length; i++) {
			Symbol sym = new Symbol();
			sym.type = type;
			sym.name = array[i].toString();
			sym.addr = array[i].st_value;
			try {
				// This can fail if we use addr2line
				// but we can safely ignore the error.
				if (header == null) {
					sym.filename = array[i].getFilename();
					sym.startLine = array[i].getFuncLineNumber();
					sym.endLine = sym.startLine;
				}
			} catch (IOException e) {
				//e.printStackTrace();
			}
			symbols.add(sym);
		}
	}

}
