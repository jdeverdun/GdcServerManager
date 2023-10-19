package tools;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import model.DICOM;


import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import settings.SystemSettings;

import exceptions.DicomException;

public class DicomTools {

	/**

    Modify metadata dicom
    not vr= SQ
    @param file : input File
    @param tagChooser: choosing tag
    @param vr : vr of Tag choosing
    @param newString : new String
    exemple:
    **/
	public static void changementTag(File file, File output, int tagChooser, VR vr, String newString ) throws IOException{

		DicomInputStream dis = new DicomInputStream(file);
		Attributes attributes = dis.readDataset();
		dis.close();

		boolean change = false;
		attributes.setString(tagChooser, vr, newString);
		change = true;

		if(change){
			/*FileOutputStream fos = new FileOutputStream( output );//new File(file.getParent()+ File.separator + file.getName() +"_a.dcm"));
			BufferedOutputStream bos = new BufferedOutputStream(fos);*/
			DicomOutputStream dos = new DicomOutputStream(output);
			attributes.writeTo(dos);
			dos.close();
		}
	}
	
	public static String getTag(File fi, String tag) throws FileNotFoundException, DicomException{
		FileInputStream fis = new FileInputStream(fi.getAbsolutePath());
		String header = new DICOM(fis).getInfo(fi.toPath());
		if(header == null)
			throw new DicomException("Empty DICOM header");
		try {
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (header==null) return null;
		int index1 = header.indexOf(tag);
		if (index1==-1) return null;
		//IJ.log(hdr.charAt(index1+11)+"   "+hdr.substring(index1,index1+20));
		if (header.charAt(index1+11)=='>') {
			// ignore tags in sequences
			index1 = header.indexOf(tag, index1+10);
			if (index1==-1) return null;
		}
		index1 = header.indexOf(":", index1);
		if (index1==-1) return null;
		int index2 = header.indexOf("\n", index1);
		String value = header.substring(index1+1, index2);
		return value;
	}
	
	/**
	 * Anonymise un dicom grace a son nom et a sa date de naissance format [prenom[1:2]_nom[1:3]_datenaissance]
	 * @param dicomPath
	 * @param oldName
	 * @param birthdate
	 * @return
	 * @throws IOException 
	 */
	public static String anonymize(Path dicomPath,Path output, String oldName,String birthdate) throws IOException {
		String[] parts = oldName.split("_");
		String prenom = "";
		String nom = "";
		for(int i = 0; i<parts.length; i++){
			if(parts[i].length()>2){
				prenom = parts[i].substring(0, 2);
				break;
			}
		}
		for(int i = parts.length-1; i>=0; i--){
			if(parts[i].length()>2){
				nom = parts[i].substring(0, Math.min(3, parts[i].length()));
				break;
			}
		}
		String code = prenom+"_"+nom+"_"+birthdate;
		//System.out.println(oldName+"\n"+code);
		DicomTools.changementTag(dicomPath.toFile(), output.toFile(), Tag.PatientName, VR.PN, code);
		return code;
	}
	/*
	public static void main(String[] args){
		

		try {
			DicomTools.changementTag(new File("C:\\Users\\Analyse\\Desktop\\test\\IM000004"), Tag.PatientName , VR.PN, "piane" );
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/

}
