package org.eclipse.cdt.internal.core.model.parser;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.eclipse.cdt.core.model.IBinaryParser.IBinaryExecutable;
import org.eclipse.cdt.core.model.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.model.IBinaryParser.IBinaryShared;
import org.eclipse.cdt.core.model.IBinaryParser.ISymbol;
import org.eclipse.cdt.utils.elf.AR;
import org.eclipse.cdt.utils.elf.Elf;
import org.eclipse.cdt.utils.elf.ElfHelper;
import org.eclipse.cdt.utils.elf.Elf.Attribute;
import org.eclipse.cdt.utils.elf.ElfHelper.Sizes;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;

/**
 */
public class ElfBinaryFile extends PlatformObject implements IBinaryFile,
	IBinaryObject, IBinaryExecutable, IBinaryShared {

	IFile file;
	String objectName;
	long timestamp;
	String soname;
	String[] needed;
	Sizes sizes;
	Attribute attribute;
	ArrayList symbols;
	
	public class ElfSymbol implements ISymbol {
	
		String filename;
		int lineno;
		String name;
		int type;

		public ElfSymbol (Elf.Symbol symbol, int t) throws IOException {
			filename = symbol.getFilename();
			name = symbol.toString();
			lineno = symbol.getFuncLineNumber();
			type = t;
		}
		
		/**
		 * @see org.eclipse.cdt.core.model.IBinaryParser.ISymbol#getFilename()
		 */
		public String getFilename() {
			return filename;
		}

		/**
		 * @see org.eclipse.cdt.core.model.IBinaryParser.ISymbol#getLineNumber()
		 */
		public int getLineNumber() {
			return lineno;
		}

		/**
		 * @see org.eclipse.cdt.core.model.IBinaryParser.ISymbol#getName()
		 */
		public String getName() {
			return name;
		}

		/**
		 * @see org.eclipse.cdt.core.model.IBinaryParser.ISymbol#getType()
		 */
		public int getType() {
			return type;
		}

	}
	
	public ElfBinaryFile(IFile f) throws IOException {
		this(f, null, null);
	}

	public ElfBinaryFile(IFile f, String n) throws IOException {
		this(f, n, null);
	}

	public ElfBinaryFile(IFile f, String n, Elf elf) throws IOException {
		file = f;
		objectName = n;
		if (elf != null) {
			loadAttributes(new ElfHelper(elf));
		} else {
			loadAttributes();
		}
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryFile#getFile()
	 */
	public IFile getFile() {
		return  file;
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
				case Attribute.ELF_TYPE_EXE:
					type = IBinaryFile.EXECUTABLE;
				break;

				case Attribute.ELF_TYPE_SHLIB:
					type = IBinaryFile.SHARED;
				break;

				case Attribute.ELF_TYPE_OBJ:
					type = IBinaryFile.OBJECT;
				break;
				
				case Attribute.ELF_TYPE_CORE:
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
		return (ISymbol[])symbols.toArray(new ISymbol[0]);
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryFile#getContents()
	 */
	public InputStream getContents() {
		InputStream stream = null;
		// Archive ?
		if (file != null && objectName != null) {
			IPath location = file.getLocation();
			if (location != null) {
				AR ar = null;
				try {	
					ar = new AR(file.getLocation().toOSString());
					AR.ARHeader[] headers = ar.getHeaders();
					for (int i = 0; i < headers.length; i++) {
						if (objectName.equals(headers[i].getObjectName())) {
							stream = new ByteArrayInputStream(headers[i].getObjectData());
							break;
						}
					}
				} catch (IOException e) {
				}
				if (ar != null) {
					ar.dispose();
				}
			}
		} else if (file != null && file.exists()) {
			try {
				stream = file.getContents();
			} catch (CoreException e) {
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
		return objectName;
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
		long modification = file.getModificationStamp();
		boolean changed = modification != timestamp;
		timestamp = modification;
		return changed;
	}

	protected ElfHelper getElfHelper() throws IOException {
		// Archive ?
		if (file != null && file.exists() && objectName != null) {
			ElfHelper helper = null;
			AR ar = null;
			try {	
				ar = new AR(file.getLocation().toOSString());
				AR.ARHeader[] headers = ar.getHeaders();
				for (int i = 0; i < headers.length; i++) {
					if (objectName.equals(headers[i].getObjectName())) {
						helper = new ElfHelper(headers[i].getElf());
						break;
					}
				}
			} finally {
				if (ar != null) {
					ar.dispose();
				}
			}
			return helper;
		} else if (file != null && file.exists()) {
			IPath path = file.getLocation();
			if (path == null) {
				path = new Path("");
			}
			return new ElfHelper(path.toOSString());
		}
		throw new IOException("No file assiocated with Binary");
	}

	protected void loadInformation() throws IOException {
		loadAttributes();
		if (symbols != null) {
			symbols.clear();
			loadSymbols();
			symbols.trimToSize();
		}
	}
	
	protected void loadAttributes() throws IOException {
		ElfHelper helper = getElfHelper();
		loadAttributes(helper);
		helper.dispose();
	}

	protected void loadAttributes(ElfHelper helper) throws IOException {
		Elf.Dynamic[] sharedlibs = helper.getNeeded();
		needed = new String[sharedlibs.length];
		for (int i = 0; i < sharedlibs.length; i++) {
			needed[i] = sharedlibs[i].toString();
		}
		sizes = helper.getSizes();
		soname = helper.getSoname();
		attribute = helper.getElf().getAttributes();
	}

	protected void loadSymbols() throws IOException {
		ElfHelper helper = getElfHelper();
		loadSymbols(helper);
		helper.dispose();
	}

	protected void loadSymbols(ElfHelper helper) throws IOException {
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

	protected void addSymbols(Elf.Symbol[] array, int type) {
		for (int i = 0; i < array.length; i++) {
			try {
				ISymbol sym = new ElfSymbol(array[i], type);
				symbols.add(sym);
			} catch (IOException e) {
			}
		}
	}

}
