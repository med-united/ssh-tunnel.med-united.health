package health.medunited.service;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class MedicationDbLookup {

    public static List<String> lookupMedicationByPZN(String pznToLookup) {
        boolean found = false;

        try {
            MedicationDbLookup instance = new MedicationDbLookup();
            InputStream is = instance.getFileAsIOStream("medicationDatabase.csv");

            Scanner scanner = new Scanner(is);
            scanner.useDelimiter("[\n]");

            while(scanner.hasNext() && !found) {
                String[] myArray = scanner.next().split(",");
                List<String> tableEntry = Arrays.asList(myArray);
                // System.out.println(tableEntry);
                String pznFound = tableEntry.get(1);

                if (pznFound.equals(pznToLookup)) {
                    return tableEntry;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static String getMedicationName(List<String> tableEntry) {
        return tableEntry.get(5);
    }

    public static String getQuantity(List<String> tableEntry) {
        return tableEntry.get(2);
    }

    public static String getNorm(List<String> tableEntry) {
        return tableEntry.get(3);
    }

    public static String getAVP(List<String> tableEntry) {
        return tableEntry.get(4);
    }

    public static String getATC(List<String> tableEntry) {
        return tableEntry.get(6);
    }

    public static String getComposition(List<String> tableEntry) {
        return tableEntry.get(7);
    }

    public InputStream getFileAsIOStream(final String fileName) {
        InputStream ioStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream(fileName);

        if (ioStream == null) {
            throw new IllegalArgumentException(fileName + " is not found");
        }
        return ioStream;
    }
}
