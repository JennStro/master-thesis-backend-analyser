package filewriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class FileHandler {

    private ArrayList<File> files = new ArrayList<>();
    private File directoryForDependencies;

    public FileHandler() {
        String userDirectory = System.getProperty("user.dir");
        directoryForDependencies = new File(userDirectory +"/directoryForDependencies");
        directoryForDependencies.mkdirs();
    }

    public File getDirectoryForDependencies() {
        return directoryForDependencies;
    }

    public void createNewFileAndWrite(String code) {
        String fileName = getClassName(code);
        try {
            File file = new File(directoryForDependencies.getAbsolutePath() + "/" + fileName + ".java");
            files.add(file);
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(code);
            fileWriter.close();
        } catch (IOException e) {
            System.out.println("Could not create file");
            e.printStackTrace();
        }
    }

    public void deleteFilesInDependencyDirectory() {
        for (File file : files) {
            System.out.println("Deleted: " + file.getAbsolutePath() + " " + file.delete());
        }
    }

    private String[] simpleTokeniser(String code) {
        return code.split(" ");
    }

    public String getClassName(String code) {
        String[] tokens = simpleTokeniser(code);
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equals("class")) {
                return tokens[i+1];
            }
        }
        return "";
    }

}

