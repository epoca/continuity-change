package it.epocaricerca.standalone.continuityChange.util;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

public class FileUtils {

	public static int countLines(String filename) throws IOException {
	    LineNumberReader reader  = new LineNumberReader(new FileReader(filename));
	    int cnt = 0;
	    String lineRead = "";
	    while ((lineRead = reader.readLine()) != null) {}

	    cnt = reader.getLineNumber(); 
	    reader.close();
	    return cnt-1;
	}
}
