package org.eclipse.cdt.managedbuilder.core.tests;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConvertManagedBuildObject;

public class ProjectConverter21 implements IConvertManagedBuildObject {

	public IBuildObject convert(IBuildObject buildObj, String fromId,
			String toId, boolean isConfirmed) {

		String tmpDir = System.getProperty("java.io.tmpdir");	//$NON-NLS-1$	
		
		File outputFile = new File(tmpDir + "/converterOutput21.txt");	//$NON-NLS-1$
		try {
			FileWriter out = new FileWriter(outputFile);			
			out.write("Converter for CDT 2.1 Project is invoked");	//$NON-NLS-1$
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
			System.out.println("Exception raised.");	//$NON-NLS-1$
		}
		return buildObj;
	}

}
