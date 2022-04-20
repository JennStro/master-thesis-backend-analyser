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

    public String addPackageDeclaration(String code) {
        String[] tokens = simpleTokeniser(code);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("package ");
        stringBuilder.append(Analyser.PACKAGE_FOR_DEPENDENCIES);
        stringBuilder.append("; ");
        for (int i = 0; i < tokens.length; i++) {
            stringBuilder.append(tokens[i]).append(" ");
        }
        return stringBuilder.toString();
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

    public String replacePackageDeclaration(String code) {
        String[] tokens = simpleTokeniser(code);
        String package_keyword = "package";
        if (!tokens[0].equals(package_keyword)) {
            return addPackageDeclaration(code);
        }

        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equals(package_keyword)) {
                tokens[i+1] = Analyser.PACKAGE_FOR_DEPENDENCIES + ";";
            }
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            builder.append(tokens[i]).append(" ");
        }
        return builder.toString();
    }
}

