package test;

import java.io.File;

import daemon.tools.nifti.Nifti_Reader;
import ij.ImagePlus;

public class Test {
	public static void main(String[] args){
		Nifti_Reader imp = new Nifti_Reader(new File("C:/Users/Analyse/Documents/MATLAB/Jeremy_these/DATA/aslani_et_moi/spm_segment_snmat/f002695891-0013-00002-000041-01.nii"));
		System.out.println(imp.getNSlices());
	}
}
