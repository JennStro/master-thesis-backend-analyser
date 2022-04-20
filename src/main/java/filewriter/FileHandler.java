package filewriter;

import master.thesis.backend.analyser.Analyser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileHandler {

    public void createNewFileAndWrite(String code) {
        String fileName = getClassName(code);
        try {
            File file = new File(Analyser.FILE_PATH_FOR_DEPENDENCIES + fileName + ".java");
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(code);
            fileWriter.close();
        } catch (IOException e) {
            System.out.println("Uh oh could not read file");
            e.printStackTrace();
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

