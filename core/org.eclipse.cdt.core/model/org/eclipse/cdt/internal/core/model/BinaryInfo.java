package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;

import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryExecutable;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.IBinaryParser.IBinaryShared;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

class BinaryInfo extends OpenableInfo {

	int type;
	boolean debug;
	String cpu =  "";
	String shared[] = new String[0];
	long text;
	long data;
	long bss;
	String soname = "";
	boolean littleE;

	public BinaryInfo(CElement element) {
		super(element);
	}

	public String getCPU() {
		return cpu;
	}

	public boolean isSharedLib() {
		return type == IBinaryObject.SHARED;
	}

	public boolean isExecutable() {
		return type == IBinaryObject.EXECUTABLE;
	}

	public boolean isObject() {
		return type == IBinaryObject.OBJECT;
	}

	public boolean isCore() {
		return type == IBinaryObject.CORE;
	}

	public boolean hasDebug() {
		return debug;
	}

	public String[] getNeededSharedLibs() {
		return shared;
	}

	public long getText() {
		return text;
	}

	public long getData() {
		return data;
	}

	public long getBSS() {
		return bss;
	}

	public String getSoname() {
		return soname;
	}

	public boolean isLittleEndian() {
		return littleE;
	}

	protected void loadInfo() {
		loadInfo(getBinaryObject());
	}

	protected void loadInfo(IBinaryObject bin) {
		if (bin != null) {
			type = bin.getType();
			cpu =  bin.getCPU();
			debug = bin.hasDebug();
			if (isExecutable()) {
				IBinaryExecutable exec = (IBinaryExecutable) bin;
				shared = exec.getNeededSharedLibs();
			}
			text = bin.getText();
			data = bin.getData();
			bss = bin.getBSS();
			if (isSharedLib()) {
				IBinaryShared shared = (IBinaryShared) bin;
				soname = shared.getSoName();
			}
			littleE = bin.isLittleEndian();
		}
	}
	
	private IBinaryObject getBinaryObject() {
		IBinaryObject binary = null;
		IProject project = getElement().getCProject().getProject();
		IBinaryParser parser = CModelManager.getDefault().getBinaryParser(project);
		if (parser != null) {
			try {
				IPath path = getElement().getUnderlyingResource().getLocation();
				IBinaryFile bfile = parser.getBinary(path);
				if (bfile instanceof IBinaryObject) {
					binary = (IBinaryObject) bfile;
				}
			} catch (CModelException e) {
			} catch (IOException e) {
			}
		}
		return binary;
	}

}
