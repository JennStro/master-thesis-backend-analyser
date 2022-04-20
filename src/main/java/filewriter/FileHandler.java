package filewriter;

import master.thesis.backend.analyser.Analyser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileHandler {

    public void createNewFileAndWrite(String code) {
        String fileName = getClassName(code);
        try {
            File file = new File(Analyser.PATH_FOR_DEPENDENCIES + fileName + ".java");
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

    private String[] addPackageDeclaration(String[] tokens) {
        int LENGTH_OF_PACKAGE_DECLARATION = 2;
        String[] tokensWithPackageDeclaration = new String[tokens.length + LENGTH_OF_PACKAGE_DECLARATION];
        tokensWithPackageDeclaration[0] = "package";
        tokensWithPackageDeclaration[1] = Analyser.PACKAGE_FOR_DEPENDENCIES;
        for (int i = 0; i < tokens.length; i++) {
            tokensWithPackageDeclaration[i+LENGTH_OF_PACKAGE_DECLARATION] = tokens[i];
        }
        return tokensWithPackageDeclaration;
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

