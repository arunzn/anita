package com.mbrdi.anita.basic.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class FileUtil {
	
	public static String readFile(File templateFile) {
		try {
			return new Scanner(templateFile).useDelimiter("\\Z").next();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	
	public static String readFile(String fileWithPath) {
		return readFile(new File(fileWithPath));
	}
	
	public static String replaceText(String content, String replace_me, String replace_with) {
		return content.replace(replace_me, replace_with!= null ? replace_with : "");
	}

}
