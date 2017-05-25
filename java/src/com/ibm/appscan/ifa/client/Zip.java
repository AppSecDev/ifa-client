/**
 * ©  Copyright HCL Technologies Ltd. 2017.  All Rights Reserved.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.ibm.appscan.ifa.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.naming.SizeLimitExceededException;

public class Zip {
	static final int BUFFER = 2048;

	//500MB max size - prevent zip bombs.
	/**
	 * Max file size of 500MB per file
	 */
	static final long MAX_SIZE=500*1024*1024;

	//Only allow up to 1024 files to be extracted.
	/**
	 * Max of 1024 entries allowed 
	 */
	static final int MAX_ENTRIES=10000;

	static private int m_count;

	/**
	 * Creates a zip file in the parent directory of f, or the current directory if f has no parent
	 * @param f The file to zip and encrypt
	 * @param parent_dir The target directory to save the returned file.
	 * @param c The {@link Crypto} to use for encryption
	 * @return the encrypted zip file
	 * @throws IOException 
	 * @throws SizeLimitExceededException if more than {@value #MAX_ENTRIES} files are added
	 * or if one of the files is larger than {@value #MAX_SIZE} bytes
	 */
	public static synchronized File makeZipFile(File f, File target) throws SizeLimitExceededException, IOException{
		if (isValidZipDir(f)){
			if (target==null){
				if (f.getParentFile()!=null){
					target=f.getParentFile();
				} 
			}
			if (!target.exists()){
				target.mkdirs();
			}
			File ret=new File(target,"out.zip");
			ZipOutputStream out = null;
			try {
				out = new ZipOutputStream(new BufferedOutputStream(new 
						FileOutputStream(ret)));
				
				addToZip(f,f, out);
				out.flush();
			} finally {
				if (out!=null){
					out.close();
				}
			}
			return ret;
		}
		return null;
	}

	private static void addToZip(File parent,File f,ZipOutputStream zip) throws IOException {
		if (f.isDirectory()){
			for (File file:f.listFiles()){
				addToZip(parent,file, zip);
			}
		} else {
			String name=parent.toURI().relativize(f.toURI()).toString();
			if (name==null || name.length()==0){
				name=f.getName();
			}
			zip.putNextEntry(new ZipEntry(name));
			BufferedInputStream input= null;
			try {
				input = new BufferedInputStream(new FileInputStream(f));
				
				int len = 0;
				byte[] buffer = new byte[BUFFER];
				while ((len = input.read(buffer)) != -1){
					zip.write(buffer, 0, len);
				}
			}
			finally {
				try {
					if (input!=null){
						input.close();
					}
				} finally {
					zip.flush();
					zip.closeEntry();
				}
			}
		}
	}

	/**
	 * 
	 * @param f The zipped and encrypted file
	 * @param target_dir The target directory to place the unzipped and decrypted file(s)
	 * @param c The {@link Crypto} to use
	 * @return The file list from the encrypted and zipped f
	 * @throws FileNotFoundException 
	 * @throws IOException if more than {@value #MAX_ENTRIES} files are in the zip
	 * or if one of the files is larger than {@value #MAX_SIZE} bytes
	
	 */
	public static ArrayList<File> getUnzippedFile(File f, File target_dir) throws FileNotFoundException, IOException {
		return getUnzippedFile(new FileInputStream(f), target_dir);
	}

	/**
	 * 
	 * @param f The inputstream containing the zipped and encrypted file
	 * @param target_dir The target directory to place the unzipped and decrypted file(s)
	 * @param c The {@link Crypto} to use
	 * @return The file list from the encrypted and zipped f
	 * @throws IOException if more than {@value #MAX_ENTRIES} files are in the zip
	 * or if one of the files is larger than {@value #MAX_SIZE} bytes
	 */
	public static ArrayList<File> getUnzippedFile(InputStream f, File target_dir) throws IOException {
		if (!target_dir.getAbsoluteFile().exists()){
			target_dir.getAbsoluteFile().mkdirs();
		}
		if (!target_dir.isDirectory()){
			target_dir=target_dir.getParentFile();
		}
		ArrayList<File>ret=new ArrayList<File>();
		ZipInputStream zip=null;
		try {
			BufferedOutputStream dest = null;
			zip = new ZipInputStream(f);
			
			int entry_count=0;
			ZipEntry  e = null;

			while((e=zip.getNextEntry())!=null) {
				String name=e.getName();
				if (name.contains("/")){
					name=name.replace("/", File.separator);
				}
				if (name.contains("\\")){
					name=name.replace("\\", File.separator);
				}
				//Make sure we are not path traversing here.
				isValidFileName(name, ".");

				int count;
				byte data[] = new byte[BUFFER];

				long total=0;
				try {	
						
						File file=new File(target_dir+File.separator+name);
						if (e.isDirectory()){
							file.mkdirs();
						} else {
							if (file.getParentFile()!=null){
								file.getParentFile().mkdirs();
							}
							dest = new BufferedOutputStream(new FileOutputStream(file), BUFFER);
							//Prevent zip bombs here. 
							//Check to make sure the current unzip size is less than MAX_SIZE
							while (total + BUFFER <= MAX_SIZE && (count = zip.read(data, 0, BUFFER)) != -1){
								dest.write(data, 0, count);
								total += count;
							}
							dest.flush();
						}
						ret.add(file);
						entry_count++;
						if (entry_count>=MAX_ENTRIES){
							throw new IllegalStateException(Messages.getMessage("err.too.many.files.in.zip", MAX_ENTRIES));
						}
						if (total > MAX_SIZE) {
							throw new IllegalStateException("File being unzipped is too big.");
						}
				} finally {
					if (dest!=null){
						dest.close();
					}
				}
			}
		} finally {
			if (zip!=null){
				zip.close();
			}
		}
		return ret;
	}

	private static boolean isValidFileName(String file_to_check, String targetDir) throws IOException{
		if (new File(file_to_check).getCanonicalPath().startsWith(new File(targetDir).getCanonicalPath())) {
			return true;
		} else {
			throw new IllegalStateException(Messages.getMessage("err.zip.pt", file_to_check));
		}
	}

	private static boolean isValidZipDir(File f) throws SizeLimitExceededException{
		m_count=0;
		isValidZipDir(f,f);
		return true;
	}

	private static void isValidZipDir(File given,File dir) throws SizeLimitExceededException{
		if (dir.isDirectory()){
			for (File f:dir.listFiles()){


				isValidZipDir(given, f);
			}
		} else{
			m_count++;
			if (dir.length()>MAX_SIZE){
				throw new SizeLimitExceededException(Messages.getMessage("err.zip.add.size", dir.getName(),MAX_SIZE));
			}
			if (m_count>MAX_ENTRIES){
				throw new SizeLimitExceededException(Messages.getMessage("err.zip.add.count", given.getPath(),MAX_ENTRIES));
			}
		}

	}
}
