package tools;


import model.DicomFile;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.w3c.dom.Attr;
import settings.WindowManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class DicomUtils {
    static AtomicInteger progress = new AtomicInteger();
    public static Attributes readDicomHeader(File dicomFile) throws IOException {
        DicomInputStream dis = new DicomInputStream(dicomFile);
        Attributes attributes = dis.readDataset();
        dis.close();
        return(attributes);
    }

    public static void main(String[] args){
        File fi = new File("C:\\Users\\arngd\\Desktop\\bidstest\\out\\sourcedata\\sub-003410219\\ses-2020072317\\ENC_DIFF_smsRESOLVE_AXIAL_2mm_ENC_DIFF_smsRESOLVE_AXIAL_2mm_ADC//IM0001");
        try {
            Attributes attr = readDicomHeader(fi);
            System.out.println(attr.getFloat(Tag.PixelSpacing,-1.0f));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static Attributes readDicomHeader(File dicomFile, int stopTag) throws IOException {
        DicomInputStream dis = new DicomInputStream(dicomFile);
        Attributes attributes = dis.readDataset(stopTag);

        dis.close();
        return(attributes);
    }

    public static boolean isDicomFile(File file) {
        try {
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(128);
            byte[] buffer = new byte[4];
            raf.read(buffer);
            raf.close();
            return new String(buffer).equals("DICM");
        } catch (Exception e) {
            return false;
        }
    }

    public static ArrayList<DicomFile> filterDicomFiles(List<Path> files) {
        ArrayList<DicomFile> pfiles = new ArrayList<DicomFile>();
        for(Path path:files){
            if(isDicomFile(path.toFile())){
                try {
                    pfiles.add(new DicomFile(""+path));
                    progress.getAndIncrement();
                } catch (IOException e) {
                    WindowManager.mwLogger.log(Level.WARNING,"File "+path+" couldn't be loaded ... skipped.",e);
                }

            }
        }
        return(pfiles);
    }



    public static HashMap<String,DicomFile> uniqueDicomsDICkeyInList(ArrayList<DicomFile> files){
        HashMap<String,DicomFile> output = new HashMap<>();
        for(DicomFile file : files){
            output.put(file.getDic_unique_key(),file);
        }
        return(output);
    }

    public static HashMap<String, String> uniquePatientsInList(ArrayList<DicomFile> files) {
        HashMap<String,String> output = new HashMap<>();
        for(DicomFile file : files){
            output.put(file.getPatientId(),file.getPatientName());
        }
        return(output);
    }


}
