package org.eclipse.cdt.managedbuilder.core.tests;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConvertManagedBuildObject;

public class ProjectConverter implements IConvertManagedBuildObject {

	public IBuildObject convert(IBuildObject buildObj, String fromId,
			String toId, boolean isConfirmed) {

		String tmpDir = System.getProperty("java.io.tmpdir");
		
		File outputFile = new File(tmpDir + "/converterOutput.txt");
		try {
			FileWriter out = new FileWriter(outputFile);
			out.write("---------- Start-------");
			out.write("Converter for the build object : '" + buildObj.getName() + "' is invoked.");
			out.write("From Id : " + fromId );
			out.write("To Id : " + toId);
			out.write("---------- End-------");
		} catch (IOException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
			System.out.println("Exception raised.");
		}
		
	
	return buildObj;
	}

}
