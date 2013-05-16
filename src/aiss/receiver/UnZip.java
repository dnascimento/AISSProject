package aiss.receiver;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import aiss.interf.AISSInterface;

public class UnZip {
    List<String> fileList;


    /**
     * Unzip it
     * 
     * @param zipFile input zip file
     * @param output zip file output folder
     */
    public void unZipIt(byte[] data, File outDirectory) {

        byte[] buffer = new byte[1024];

        try {
            // get the zip file content
            ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(data));
            // get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(outDirectory, fileName);
                System.out.println("file unzip : " + newFile.getAbsoluteFile());
                // create all non exists folders
                // else you will hit FileNotFoundException for compressed folder
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
            AISSInterface.logreceiver.append("UNZIP - Data unzipped: "
                    + outDirectory.getAbsolutePath() + "\n");
            System.out.println("Done");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


}
