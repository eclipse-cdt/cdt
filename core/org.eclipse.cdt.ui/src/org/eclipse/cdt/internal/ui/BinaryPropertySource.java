package org.eclipse.cdt.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.FilePropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.ui.*;

public class BinaryPropertySource extends FilePropertySource {
	
	private final static String ELF_CPU= "CElementProperties.elf_cpu";
	private final static String ELF_TEXT= "CElementProperties.elf_text";
	private final static String ELF_DATA= "CElementProperties.elf_data";
	private final static String ELF_BSS= "CElementProperties.elf_bss";
	private final static String ELF_TYPE= "CElementProperties.elf_type";
	private final static String ELF_HAS_DEBUG= "CElementProperties.elf_has_debug";
	private final static String ELF_SONAME= "CElementProperties.elf_soname";
	private final static String ELF_NEEDED= "CElementProperties.elf_needed";
	
	private IBinary binary;
	
	// Property Descriptors
	static private IPropertyDescriptor[] fgPropertyDescriptors;
	
	/**
	 * Get a PropertyDescriptor that defines the binary properties of an Elf
	 * @return the PropertyDescriptor
	 */
	private static IPropertyDescriptor[] getInitialPropertyDescriptor() {
		// cpu name
		String cpuName= CUIPlugin.getResourceString(ELF_CPU);
		PropertyDescriptor cpuDescriptor= new PropertyDescriptor(ICElementPropertyConstants.P_ELF_CPU, cpuName);
		cpuDescriptor.setAlwaysIncompatible(true);

		// elf text
		String textName = CUIPlugin.getResourceString(ELF_TEXT);
		PropertyDescriptor textDescriptor= new PropertyDescriptor(ICElementPropertyConstants.P_ELF_TEXT, textName);
		textDescriptor.setAlwaysIncompatible(true);
		
		// elf data
		String dataName = CUIPlugin.getResourceString(ELF_DATA);
		PropertyDescriptor dataDescriptor= new PropertyDescriptor(ICElementPropertyConstants.P_ELF_DATA, dataName);
		dataDescriptor.setAlwaysIncompatible(true);

		// elf bss
		String bssName = CUIPlugin.getResourceString(ELF_BSS);
		PropertyDescriptor bssDescriptor= new PropertyDescriptor(ICElementPropertyConstants.P_ELF_BSS, bssName);
		bssDescriptor.setAlwaysIncompatible(true);

		// elf type
		String typeName = CUIPlugin.getResourceString(ELF_TYPE);
		PropertyDescriptor typeDescriptor= new PropertyDescriptor(ICElementPropertyConstants.P_ELF_TYPE, typeName);
		bssDescriptor.setAlwaysIncompatible(true);

		// elf needed
		String neededName = CUIPlugin.getResourceString(ELF_NEEDED);
		PropertyDescriptor neededDescriptor= new PropertyDescriptor(ICElementPropertyConstants.P_ELF_NEEDED, neededName);
		bssDescriptor.setAlwaysIncompatible(true);

		// elf soname
		String sonameName = CUIPlugin.getResourceString(ELF_SONAME);
		PropertyDescriptor sonameDescriptor= new PropertyDescriptor(ICElementPropertyConstants.P_ELF_SONAME, sonameName);
		bssDescriptor.setAlwaysIncompatible(true);

		// elf debug
		String debugName = CUIPlugin.getResourceString(ELF_HAS_DEBUG);
		PropertyDescriptor debugDescriptor= new PropertyDescriptor(ICElementPropertyConstants.P_ELF_HAS_DEBUG, debugName);
		bssDescriptor.setAlwaysIncompatible(true);

		return new IPropertyDescriptor[] { cpuDescriptor, textDescriptor, dataDescriptor,
			bssDescriptor, typeDescriptor, sonameDescriptor, debugDescriptor, neededDescriptor };
	}
	
	public BinaryPropertySource(IBinary bin) {
		super(bin.getFile());
		this.binary= bin;
	}

	/**
	 * @see IPropertySource#getPropertyDescriptors
	 */
	public IPropertyDescriptor[] getPropertyDescriptors() {
		if (fgPropertyDescriptors == null)
			initializeBinaryDescriptors();
		return fgPropertyDescriptors;
	}

	/**
	 * @see IPropertySource#getPropertyValue
	 */	
	public Object getPropertyValue(Object name) {
		if (element != null) {
			Object returnValue = super.getPropertyValue(name);

			if(returnValue != null)
				return returnValue;
		}

		if (name.equals(ICElementPropertyConstants.P_ELF_CPU)) {
			return binary.getCPU();
		} else if (name.equals(ICElementPropertyConstants.P_ELF_TEXT)) {
			return Long.toString(binary.getText());
		} else if (name.equals(ICElementPropertyConstants.P_ELF_DATA)) {
			return Long.toString(binary.getData());
		} else if (name.equals(ICElementPropertyConstants.P_ELF_BSS)) {
			return Long.toString(binary.getBSS());
		} else if (name.equals(ICElementPropertyConstants.P_ELF_SONAME)) {
			return binary.getSoname();
		} else if (name.equals(ICElementPropertyConstants.P_ELF_HAS_DEBUG)) {
			 if (binary.hasDebug()) {
				return "true";//$NON-NLS-1$
			} else {
				return "false";//$NON-NLS-1$
			}
		} else if (name.equals(ICElementPropertyConstants.P_ELF_NEEDED)) {
			String[] needed = binary.getNeededSharedLibs();
			String need = "";
			for (int i = 0; i < needed.length; i++) {
				need += " " + needed[i];
			}
			return need.trim();
		} else if (name.equals(ICElementPropertyConstants.P_ELF_TYPE)) {
			if (binary.isObject()) {
				return "object";
			} else if (binary.isExecutable()) {
				return "executable";
			} else if (binary.isSharedLib()) {
				return "shared library";
			} else if (binary.isCore()) {
				return "core file";
			}
		}
		return null;
	}

	/**
	 * Return the Property Descriptors for the file type.
	 */
	private void initializeBinaryDescriptors() {
		IPropertyDescriptor[] superDescriptors = super.getPropertyDescriptors();
		int superLength = superDescriptors.length;
		IPropertyDescriptor[] binDescriptors = getInitialPropertyDescriptor();
		int binLength = binDescriptors.length;
		fgPropertyDescriptors = new IPropertyDescriptor[superLength + binLength];
		System.arraycopy(superDescriptors, 0, fgPropertyDescriptors, 0, superLength);
		System.arraycopy(binDescriptors, 0, fgPropertyDescriptors, superLength, binLength);
	}
}
