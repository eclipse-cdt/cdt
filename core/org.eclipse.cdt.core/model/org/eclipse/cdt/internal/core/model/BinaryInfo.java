package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.cdt.core.model.ICElement;

import org.eclipse.cdt.utils.elf.Elf;
import org.eclipse.cdt.utils.elf.ElfHelper;

class BinaryInfo extends CFileInfo {

	String [] needed;
	ElfHelper.Sizes sizes;
	Elf.Attribute attribute;
	String soname; 
	Map hash;
	ElfHelper elfHelper = null;

	public BinaryInfo(CElement element) {
		super(element);
		needed = new String[0];
		sizes = null;
		attribute = null;
		soname = "";
		hash = new HashMap();
	}

	public boolean isBinary() {
		return true;
	}

	public ICElement [] getChildren() {
		initChildren();
		return super.getChildren();
	}

	public String getCPU() {
		init();
		String cpu = null;
		if (attribute != null)
			cpu = attribute.getCPU();
		return (cpu == null) ? "" : cpu;
	}

	public boolean isSharedLib() {
		init();
		if (attribute != null)
			return attribute.getType() == attribute.ELF_TYPE_SHLIB;
		return false;
	}

	public boolean isExecutable() {
		init();
		if (attribute != null)
			return attribute.getType() == attribute.ELF_TYPE_EXE;
		return false;
	}

	public boolean isObject() {
		init();
		if (attribute != null)
			return attribute.getType() == attribute.ELF_TYPE_OBJ;
		return false;
	}

	public boolean hasDebug () {
		init();
		if (attribute != null)
			return attribute.hasDebug();
		return false;
	}

	public String [] getNeededSharedLibs() {
		init();
		return needed;
	}

	public long getText() {
		init();
		if (sizes != null) {
			return sizes.text;
		}
		return 0;
	}

	public long getData() {
		init();
		if (sizes != null) {
			return sizes.data;
		}
		return 0;
	}

	public long getBSS() {
		init();
		if (sizes != null) {
			return sizes.bss;
		}
		return 0;
	}

	public String getSoname() {
		init();
		return soname;
	}

	public boolean isLittleEndian() {
		init();
		if (attribute != null) {
			return attribute.isLittleEndian();
		}
		return false;
	}
	
	private void addFunction(Elf.Symbol [] symbol, boolean external) {
		for (int i = 0; i < symbol.length; i++) {
			ICElement parent = getElement();
			String filename = null;
			try {
				filename = symbol[i].getFilename();
			} catch (IOException e) {
				//e.printStackTrace();
			}
			Function function = null;

			// Addr2line returns the funny "??" when it can find the file.
			if (filename != null && !filename.equals("??")) {
				TranslationUnit tu = null;
				IPath path = new Path(filename);
				if (hash.containsKey(path)) {
					tu = (TranslationUnit)hash.get(path);
				} else {
					tu = new TranslationUnit(parent, path);
					hash.put(path, tu);
					addChild(tu);
				}
				function = new Function(tu, symbol[i].toString());
				tu.addChild(function);
			} else {
				function = new Function(parent, symbol[i].toString());
				addChild(function);
			}
			if (function != null)
				if (!external)
					function.getFunctionInfo().setAccessControl(IConstants.AccStatic);
		}
	}

	private void addVariable(Elf.Symbol[] symbol, boolean external) {
		for (int i = 0; i < symbol.length; i++) {
			String filename = null;
			try {
				filename = symbol[i].getFilename();
			} catch (IOException e) {
				//e.printStackTrace();
			}
			ICElement parent = getElement();
			Variable variable = null;
			// Addr2line returns the funny "??" when it can not find the file.
			if (filename != null && !filename.equals("??")) {
				TranslationUnit tu = null;
				IPath path = new Path(filename);
				if (hash.containsKey(path)) {
					tu = (TranslationUnit)hash.get(path);
				} else {
					tu = new TranslationUnit(parent, path);
					hash.put(path, tu);
					addChild(tu);
				}
				variable = new Variable(tu, symbol[i].toString());
				tu.addChild(variable);
			} else {
				variable = new Variable(parent, symbol[i].toString());
				addChild(variable);
			}
			if (variable != null)
				if (!external)
					variable.getVariableInfo().setAccessControl(IConstants.AccStatic);
		}
	}

	protected void init() {
		if (hasChanged()) {
			loadInfo();
		}
	}

	protected void initChildren() {
		if (hasChanged() || !isStructureKnown()) {
			removeChildren();
			loadInfoChildren();
		}
	}
	

	protected ElfHelper getElfHelper() throws IOException {
		if (elfHelper != null) {
			return elfHelper;
		}
		CFile file = (CFile)getElement();
		if (file != null) {
			IPath path = ((CFile)getElement()).getLocation();
			if (path == null)
				path = new Path("");
			return new ElfHelper(path.toOSString());
		}
		throw new IOException("No file assiocated with Binary");
	}

	protected void loadInfo() {
		try {
			ElfHelper helper = this.getElfHelper();
			Elf.Dynamic[] sharedlibs = helper.getNeeded();
			needed = new String[sharedlibs.length];
			for (int i = 0; i < sharedlibs.length; i++) {
				needed[i] = sharedlibs[i].toString();
			}

			sizes = helper.getSizes();
			soname = helper.getSoname();
			attribute = helper.getElf().getAttributes();
			helper.dispose();
		} catch (IOException e) {
			//e.printStackTrace();
		}
	}

	protected void loadInfoChildren() {
		try {
			setIsStructureKnown(true);
			ElfHelper helper = this.getElfHelper();
			addFunction(helper.getExternalFunctions(), true);
			addFunction(helper.getLocalFunctions(), false);
			addVariable(helper.getExternalObjects(), true);
			addVariable(helper.getLocalObjects(), false);

			Elf.Dynamic[] sharedlibs = helper.getNeeded();
			needed = new String[sharedlibs.length];
			for (int i = 0; i < sharedlibs.length; i++) {
				needed[i] = sharedlibs[i].toString();
			}

			sizes = helper.getSizes();
			soname = helper.getSoname();
			
			attribute = helper.getElf().getAttributes();
			helper.dispose();
		} catch (IOException e) {
			//e.printStackTrace();
		}
	}
}
