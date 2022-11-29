package health.medunited.service;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class MedicationDbLookup {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MedicationDbLookup.class);

    public static List<String> lookupMedicationByPZN_db1(String pznToLookup) {

        try {
            MedicationDbLookup instance = new MedicationDbLookup();
            InputStream is = instance.getFileAsIOStream("medicationDatabase-part1.csv");

            Scanner scanner = new Scanner(is);
            scanner.useDelimiter("[\n]");

            while(scanner.hasNext()) {
                String[] myArray = scanner.next().split(",");
                List<String> tableEntry1 = Arrays.asList(myArray);
                String pznFound = tableEntry1.get(1);

                if (pznFound.equals(pznToLookup)) {
                    System.out.println(tableEntry1);
                    return tableEntry1;
                }
            }
        } catch (Exception e) {
            log.error("Error while reading medicationDatabase-part1.csv", e);
        }
        return null;
    }

    public static List<String> lookupMedicationByPZN_db2(String pznToLookup) {

        try {
            MedicationDbLookup instance = new MedicationDbLookup();
            InputStream is = instance.getFileAsIOStream("medicationDatabase-part2.csv");

            Scanner scanner = new Scanner(is);
            scanner.useDelimiter("[\n]");

            while(scanner.hasNext()) {
                String[] myArray = scanner.next().split(",");
                List<String> tableEntry2 = Arrays.asList(myArray);
                String pznFound = tableEntry2.get(0);

                if (pznFound.equals(pznToLookup)) {
                    System.out.println(tableEntry2);
                    return tableEntry2;
                }
            }
        } catch (Exception e) {
            log.error("Error while reading medicationDatabase-part1.csv", e);
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
        return tableEntry.get(1).trim();
    }

    public static String getPharmaceuticalFormCode(List<String> tableEntry) {
        return tableEntry.get(7).trim();
    }

    public static String getPharmaceuticalFormText(List<String> tableEntry) {
        return tableEntry.get(8).trim();
    }

    public static String getManufacturer(List<String> tableEntry) {
        return tableEntry.get(9).trim();
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
