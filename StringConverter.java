package com.kered.demosample;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringConverter {
	public static final String TAG = "StringConverter";
	/* D:\\doc\\Programmer\\UI翻譯 */
	public static String TRANSLATION_PROJECT_FOLDER = "D:\\doc\\Programmer\\UI翻譯";
	
	
	
	private static File folder;
	private static File destFolder;
	public static void main(String[] args) {
		TRANSLATION_PROJECT_FOLDER = Paths.get(".").toAbsolutePath().normalize().toString();
		folder = new File(TRANSLATION_PROJECT_FOLDER);
		destFolder = createDestFolder(TRANSLATION_PROJECT_FOLDER);
		listFilesForFolder(folder);
	}

	public static File createDestFolder(String path){
		File folder = new File(TRANSLATION_PROJECT_FOLDER + File.separator + "Dest");
		if(!folder.exists()){
			/* create folder */
			if(folder.mkdir()){
				DKLog.d(TAG, "Make DIR ok!");
			}
		}
		return folder;
	}
	
	public static void listFilesForFolder(final File folder) {
	    for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	            listFilesForFolder(fileEntry);
	        } else {
	        	DKLog.d(TAG, fileEntry.getParent() + "\\" + fileEntry.getName());	           
	            if(fileEntry.getName().equals("Localizable.strings")){
	            	/*  get /<localize>/Localizable.strings */
	            	converter(fileEntry);
	            	break;
	            }
	            
	        }
	    }
	}	
	
	private static File createTargetFolder(File file){
		File localizeFolder = null;
		try {
			String parent = file.getParentFile().getName();
			DKLog.d(TAG, parent);
			/* de.lproj */
			String folderName = parent.substring(0, 2);
			//DKLog.d(TAG, folderName);
			/* de */
			
			localizeFolder = new File(destFolder.getAbsolutePath() + File.separator + "values-" + folderName);
			if(!localizeFolder.exists()){
				localizeFolder.mkdirs();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return localizeFolder;
	}
	
	public static void converter(File file){
		/* D:\swapub_doc\Programmer\UI翻譯\de.lproj\Localizable.strings */
		File targetFolder = createTargetFolder(file);
		BufferedReader reader = null;
		
		if(targetFolder != null) {
			try {
				/* create file : strings_real.xml */
				File converterFile = new File(targetFolder.getAbsolutePath() + File.separator + "strings_real.xml");
				if(!converterFile.exists()){
					converterFile.createNewFile();
				}
				
				/* write xml header */
				PrintWriter writer = new PrintWriter(converterFile.getAbsolutePath(), "UTF-8");	
				writer.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
				writer.println("<resources>");
				
				/* write xml converter */
			    reader = new BufferedReader(new FileReader(file));
			    String text = null;
			    
		    	/* skip 8 lines */
		    	for(int i = 0 ; i < 8 ; i++){
		    		text = reader.readLine();
		    	}
		    	
			    while ((text = reader.readLine()) != null) {
			    	/* init string variable number start from %1$s ...%n$d or %n$s */
					int stringVarNum = 1;
					if(text.startsWith("\"") && text.contains("=")){
						
				    	while(!text.endsWith("\";")){
				    		/* multiple line */
				    		if(text.endsWith(". ")){
				    			text = replaceLast(text, ". ", ". \\n");
				    		}
				    		
				    		text = text + reader.readLine();			    		
				    	}
						
						/* CASE 1: split "TempImageView_Title" = "Security Deposit"; */
						String split[] = text.split("= ");
					
						if((split[0].contains("��") && split[0].length() <= "��".length()) || 
								(split[0].contains(" ") && split[0].length() <= " ".length()) ||
								(split[0].length() < 5)){
							continue;
						}
						
						/*
						 * format:
						 * split[0] = "WelcomeView_Nav_Title"
						 * split[1] = "Welcome to Swapub"
						 * */
						
						/* Regulars */
						String key = split[0];
						key = key.substring(key.indexOf("\""), key.lastIndexOf("\"") + 1);
						if(key.contains("'") || key.contains(" ") || key.contains("&")){
							key = key.toLowerCase();
						}
						key = key.replace("'", ""); // remove '
						key = key.replace(" ", "_"); // replace space to _
						key = key.replace("&", "and"); // replace & to &amp;
						

						
						String value = split[1];
						/* for trace */
//						if(key.contains("TempImageView_Step_2")){
//							DKLog.d(TAG, value);
//							DKLog.d(TAG, String.valueOf(value.indexOf("\"") + 1));
//							DKLog.d(TAG, String.valueOf(value.lastIndexOf(";")));
//						}											
						value = value.substring(value.indexOf("\"") + 1, value.length() - 2);		
						value = value.replace("\\r", "\\n"); // replace \r to \n
						value = value.replace("'", "\\'"); // replace ' to \'
						value = value.replace("\"", "\\\""); // replace " to \"
						value = value.replace("\\\\\"", "\\\""); // replace \\" to \"
						value = value.replace("&", "&amp;"); // replace & to &amp;
						value = value.replace("<", "&lt;"); // replace < to &lt;
						value = value.replace(">", "&gt;"); // replace > to &gt;
						value = value.replace("  ", "\u0020\u0020");// replace double space to \u0020\u0020;
						
						if(getStringOccurrences(value, "%") <= 1){ // replace > to &gt;
							value = value.replace("%@", "%s");
						} else	{
							value = value.replace("%", "%%");
							while(value.indexOf("%%") != -1){
								value = value.replaceFirst("%%", String.format("%%%d\\$", stringVarNum++));						
							}
							value = value.replace("$@", "$s");
						}						
						
						/* single line END */
						if(value.endsWith("\";")){
							value = value.substring(0, value.length() - "\";".length());
						}else if(value.isEmpty()){
							value = key.replace("\"", "");
						}
						writer.println("    <string name=" + key + ">" + value + "</string>");						
						
					} else if (text.startsWith("//")){
						/* CASE 2: comment */
						text = text.substring(text.indexOf("//") + 2);
						writer.println("    <!-- " + text + " -->");
					} 
					
			    }
			    
				/* write xml ender */
				writer.println("</resources>");
			    writer.close();
			} catch (FileNotFoundException e) {
			    e.printStackTrace();
			} catch (IOException e) {
			    e.printStackTrace();
			} finally {
			    try {
			        if (reader != null) {
			            reader.close();
			        }
			    } catch (IOException e) {
			    }
			}	
		}
	}
	
	private static String replaceLast(String string, String substring, String replacement) {
	  int index = string.lastIndexOf(substring);
	  if (index == -1){
	    return string;
	  }
	  return string.substring(0, index) + replacement
	          + string.substring(index+substring.length());
	}
	
	private static int getStringOccurrences(String str, String findStr){
		Pattern p = Pattern.compile(findStr);
		Matcher m = p.matcher(str);
		int count = 0;
		while (m.find()){
		    count +=1;
		}
		return count;
	}
	
}
