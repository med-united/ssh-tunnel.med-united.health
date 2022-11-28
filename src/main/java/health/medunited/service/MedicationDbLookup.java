package health.medunited.service;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class MedicationDbLookup {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MedicationDbLookup.class);

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
            log.error("Error while reading medicationDatabase.csv", e);
        }
        return null;
    }

    public static String getMedicationName(List<String> tableEntry) {
        return tableEntry.get(5).trim();
    }

    public static String getQuantity(List<String> tableEntry) {
        return tableEntry.get(2).trim();
    }

    public static String getPackageSize(List<String> tableEntry) {
        return tableEntry.get(3).trim();
    }

    public static String getAVP(List<String> tableEntry) {
        return tableEntry.get(4).trim();
    }

    public static String getATC(List<String> tableEntry) {
        return tableEntry.get(6).trim();
    }

    public static String getComposition(List<String> tableEntry) {
        return tableEntry.get(7).trim();
    }

    public static String getPharmaceuticalFormCode(List<String> tableEntry) {
        return tableEntry.get(8).trim();
    }

    public static String getPharmaceuticalFormText(List<String> tableEntry) {
        return tableEntry.get(9).trim();
    }

    public static String getManufacturer(List<String> tableEntry) {
        return tableEntry.get(10).trim();
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
