package com.rambris.jsontojava;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class App
{
    public static void main( String[] args ) throws IOException {
        var mapper = new ObjectMapper();
        var parser = new JsonParser(mapper);
        var json=Files.readString(Path.of(args[0]));
        var name = FilenameUtils.getBaseName(Path.of(args[0]).getFileName().toString());
        var java=parser.parseJson(json, name);
        System.out.println(java);
    }
}
