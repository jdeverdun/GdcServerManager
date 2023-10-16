package model;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import settings.WindowManager;
import tools.DicomUtils;
import tools.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DicomFile extends File {
    public static String GENERIC_ACQDATE = "1111111111";
    protected String dic_unique_key;
    protected String patientId;
    protected String patientName;
    protected String acquisitionDate;
    public DicomFile(String pathname) throws IOException {
        super(pathname);
        checkFileExists();
        // read small amount of the header
        Attributes dinfo = DicomUtils.readDicomHeader(this, Tag.ProtocolName+1);
        extractRequiredData(dinfo);
    }



    public DicomFile(String pathname, Attributes dinfo) throws IOException {
        super(pathname);
        checkFileExists();
        extractRequiredData(dinfo);
    }
    protected void extractRequiredData(Attributes dinfo) throws IOException {
        this.patientName = StringUtils.removeAllSpecialChars(dinfo.getString(Tag.PatientName));
        this.patientId = StringUtils.removeAllSpecialChars(dinfo.getString(Tag.PatientID));
        this.setAcquisitionDate(dinfo);
        if(patientName == null || patientId == null || this.dic_unique_key.equals("_"))
            throw new IOException("Patient ID / Name or SeriesDescription - Protocol could not be retrieved in DICOM "+this.toPath());
    }



    private void checkFileExists() throws IOException {
        if(!Files.exists(this.toPath())){
            String msg = "File "+this.toPath()+" not found.";
            WindowManager.mwLogger.log(Level.SEVERE, msg);
            throw new IOException(msg);
        }
    }

    @Override
    public String toString() {
        return ""+getAbsolutePath()+" | pname="+this.patientName+" | pid="+this.patientId+" | date="+this.acquisitionDate+" | key="+this.dic_unique_key;
    }

    public String getDic_unique_key() {
        return dic_unique_key;
    }

    public void setDic_unique_key(String dic_unique_key) {
        this.dic_unique_key = dic_unique_key;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getAcquisitionDate() {
        return acquisitionDate;
    }

    public void setAcquisitionDate(String acquisitionDate) {
        this.acquisitionDate = acquisitionDate;
    }

    /**
     * Check all potential acquisitiondate fields
     * @param dinfo
     */
    public void setAcquisitionDate(Attributes dinfo) {
        String acqdate = dinfo.getString(Tag.AcquisitionDate);
        String acqTime = dinfo.getString(Tag.AcquisitionTime);
        String acqdatetime = dinfo.getString(Tag.AcquisitionDateTime);
        String studyDate = dinfo.getString(Tag.StudyDate);
        String studyTime = dinfo.getString(Tag.StudyTime);

        acqTime = acqTime == null ? "00":acqTime.substring(0,2);
        studyTime = studyTime == null ? "00":studyTime.substring(0,2);
        if(acqdate != null){

            setAcquisitionDate(acqdate+acqTime);
            return;
        }
        if(acqdatetime != null){
            setAcquisitionDate(acqdatetime.substring(0,10));
            return;
        }
        if(studyDate != null){
            setAcquisitionDate(studyDate+studyTime);
            return;
        }
        try {
            dinfo = DicomUtils.readDicomHeader(this);
            String framerefuid = dinfo.getString(Tag.FrameOfReferenceUID);
            String pattern = "^2[0-9][0-9][0-9]....+";

            if(framerefuid != null){
                Pattern p = Pattern.compile(pattern);
                for(String part:framerefuid.split("\\.")){

                    Matcher m = p.matcher(part);
                    if(m.find()){
                        setAcquisitionDate(part.substring(0,9));
                    }

                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        WindowManager.mwLogger.log(Level.WARNING,"AcquisitionDate could not be found ... setting generic date "+GENERIC_ACQDATE);
        setAcquisitionDate(GENERIC_ACQDATE);

    }
}
