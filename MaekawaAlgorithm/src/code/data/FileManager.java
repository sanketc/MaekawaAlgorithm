package code.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import code.common.Globals;

/**
 * Handles file operation.
 * @author Sanket Chandorkar
 */
public class FileManager {

	private String dataFile = null;
	
	public static final String DATA_FILE_NAME = "dataFile.txt";
	
	public FileManager(String serverId){
		/* Create folder data/<server_ID> */
		File folder = new File(Globals.DATA + File.separator + serverId);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		
		/* Initialize file name */
		dataFile = folder + File.separator + DATA_FILE_NAME;
		
		/* Create file for the first time if does not exists. */
		File file = new File(dataFile);
		if(!file.exists()){
			Globals.logMsg("Creating new dataFile : " + dataFile);
			try {
				file.createNewFile();
			} catch (IOException e) {
				System.out.println("Error: Creating new datafile : " + dataFile);
				System.out.println("Exiting application now !!");
				System.exit(Globals.SYS_FAILURE);
			}
		}
	}
	
	public void appendRecord(Record record) throws IOException, FileNotFoundException {
		File file = new File(dataFile);
		if(!file.exists())
			throw new FileNotFoundException();
		
		PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(dataFile, true /* append */)));
		pw.println(record);
		pw.close();
	}
	
	public boolean checkHealth() {
		File file = new File(dataFile);
		return file.exists();
	}
}