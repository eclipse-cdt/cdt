package org.eclipse.cdt.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.ui.views.properties.FilePropertySource;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class BinaryPropertySource extends FilePropertySource implements IPropertySource {
	
	private final static String ELF_CPU= "CElementProperties.elf_cpu"; //$NON-NLS-1$
	private final static String ELF_TEXT= "CElementProperties.elf_text"; //$NON-NLS-1$
	private final static String ELF_DATA= "CElementProperties.elf_data"; //$NON-NLS-1$
	private final static String ELF_BSS= "CElementProperties.elf_bss"; //$NON-NLS-1$
	private final static String ELF_TYPE= "CElementProperties.elf_type"; //$NON-NLS-1$
	private final static String ELF_HAS_DEBUG= "CElementProperties.elf_has_debug"; //$NON-NLS-1$
	private final static String ELF_SONAME= "CElementProperties.elf_soname"; //$NON-NLS-1$
	private final static String ELF_NEEDED= "CElementProperties.elf_needed"; //$NON-NLS-1$
	
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
		cpuDescriptor.setCategory(ICElementPropertyConstants.P_BINARY_FILE_CATEGORY);

		// elf text
		String textName = CUIPlugin.getResourceString(ELF_TEXT);
		PropertyDescriptor textDescriptor= new PropertyDescriptor(ICElementPropertyConstants.P_ELF_TEXT, textName);
		textDescriptor.setAlwaysIncompatible(true);
		textDescriptor.setCategory(ICElementPropertyConstants.P_BINARY_FILE_CATEGORY);
		
		// elf data
		String dataName = CUIPlugin.getResourceString(ELF_DATA);
		PropertyDescriptor dataDescriptor= new PropertyDescriptor(ICElementPropertyConstants.P_ELF_DATA, dataName);
		dataDescriptor.setAlwaysIncompatible(true);
		dataDescriptor.setCategory(ICElementPropertyConstants.P_BINARY_FILE_CATEGORY);
		
		// elf bss
		String bssName = CUIPlugin.getResourceString(ELF_BSS);
		PropertyDescriptor bssDescriptor= new PropertyDescriptor(ICElementPropertyConstants.P_ELF_BSS, bssName);
		bssDescriptor.setAlwaysIncompatible(true);
		bssDescriptor.setCategory(ICElementPropertyConstants.P_BINARY_FILE_CATEGORY);
		
		// elf type
		String typeName = CUIPlugin.getResourceString(ELF_TYPE);
		PropertyDescriptor typeDescriptor= new PropertyDescriptor(ICElementPropertyConstants.P_ELF_TYPE, typeName);
		typeDescriptor.setAlwaysIncompatible(true);
		typeDescriptor.setCategory(ICElementPropertyConstants.P_BINARY_FILE_CATEGORY);
		
		// elf needed
		String neededName = CUIPlugin.getResourceString(ELF_NEEDED);
		PropertyDescriptor neededDescriptor= new PropertyDescriptor(ICElementPropertyConstants.P_ELF_NEEDED, neededName);
		neededDescriptor.setAlwaysIncompatible(true);
		neededDescriptor.setCategory(ICElementPropertyConstants.P_BINARY_FILE_CATEGORY);
		
		// elf soname
		String sonameName = CUIPlugin.getResourceString(ELF_SONAME);
		PropertyDescriptor sonameDescriptor= new PropertyDescriptor(ICElementPropertyConstants.P_ELF_SONAME, sonameName);
		sonameDescriptor.setAlwaysIncompatible(true);
		sonameDescriptor.setCategory(ICElementPropertyConstants.P_BINARY_FILE_CATEGORY);
		
		// elf debug
		String debugName = CUIPlugin.getResourceString(ELF_HAS_DEBUG);
		PropertyDescriptor debugDescriptor= new PropertyDescriptor(ICElementPropertyConstants.P_ELF_HAS_DEBUG, debugName);
		debugDescriptor.setAlwaysIncompatible(true);
		debugDescriptor.setCategory(ICElementPropertyConstants.P_BINARY_FILE_CATEGORY);
		
		return new IPropertyDescriptor[] { cpuDescriptor, textDescriptor, dataDescriptor,
			bssDescriptor, typeDescriptor, sonameDescriptor, debugDescriptor, neededDescriptor };
	}
	
	public BinaryPropertySource(IBinary bin) {
		super((IFile)bin.getResource());
		binary= bin;
	}

	/**
	 * @see IPropertySource#getPropertyDescriptors
	 */
	public IPropertyDescriptor[] getPropertyDescriptors() {
		if (fgPropertyDescriptors == null) {
			initializeBinaryDescriptors();
		}
		return fgPropertyDescriptors;
	}

	/**
	 * @see IPropertySource#getPropertyValue
	 */	
	public Object getPropertyValue(Object name) {
		if (element != null) {
			Object returnValue = super.getPropertyValue(name);
			if(returnValue != null) {
				return returnValue;
			}
		}
		if (name.equals(IBasicPropertyConstants.P_TEXT)) {
			return binary.getElementName();
		} else if (name.equals(ICElementPropertyConstants.P_ELF_CPU)) {
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
			}
			return "false";//$NON-NLS-1$
		} else if (name.equals(ICElementPropertyConstants.P_ELF_NEEDED)) {
			String[] needed = binary.getNeededSharedLibs();
			String need = ""; //$NON-NLS-1$
			for (int i = 0; i < needed.length; i++) {
				need += " " + needed[i]; //$NON-NLS-1$
			}
			return need.trim();
		} else if (name.equals(ICElementPropertyConstants.P_ELF_TYPE)) {
			if (binary.isObject()) {
				return "object"; //$NON-NLS-1$
			} else if (binary.isExecutable()) {
				return "executable"; //$NON-NLS-1$
			} else if (binary.isSharedLib()) {
				return "shared library"; //$NON-NLS-1$
			} else if (binary.isCore()) {
				return "core file"; //$NON-NLS-1$
			}
		}
		return null;
	}

	/**
	 * Return the Property Descriptors for the file type.
	 */
	private void initializeBinaryDescriptors() {
		if (element != null) {
			IPropertyDescriptor[] superDescriptors = super.getPropertyDescriptors();
			int superLength = superDescriptors.length;
			IPropertyDescriptor[] binDescriptors = getInitialPropertyDescriptor();
			int binLength = binDescriptors.length;
			fgPropertyDescriptors = new IPropertyDescriptor[superLength + binLength];
			System.arraycopy(superDescriptors, 0, fgPropertyDescriptors, 0, superLength);
			System.arraycopy(binDescriptors, 0, fgPropertyDescriptors, superLength, binLength);
		} else {
			fgPropertyDescriptors = getInitialPropertyDescriptor();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.IPropertySource#getEditableValue()
	 */
	public Object getEditableValue() {
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.IPropertySource#isPropertySet(java.lang.Object)
	 */
	public boolean isPropertySet(Object id) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.IPropertySource#resetPropertyValue(java.lang.Object)
	 */
	public void resetPropertyValue(Object id) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.IPropertySource#setPropertyValue(java.lang.Object, java.lang.Object)
	 */
	public void setPropertyValue(Object id, Object value) {
	}

}
