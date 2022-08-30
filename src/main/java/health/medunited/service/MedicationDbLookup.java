package health.medunited.service;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class MedicationDbLookup {

    public static List<String> lookupMedicationByPZN(String PZNtoLookup) {
        boolean found = false;

        try {
            MedicationDbLookup instance = new MedicationDbLookup();
            InputStream is = instance.getFileAsIOStream("medicationDatabase.csv");
            // instance.printFileContent(is);

            Scanner scanner = new Scanner(is);
            scanner.useDelimiter("[\n]");

            while(scanner.hasNext() && !found) {
                String[] myArray = scanner.next().split(",");
                List<String> tableEntry = Arrays.asList(myArray);
                // System.out.println(tableEntry);
                String PZNFound = tableEntry.get(1);

                if (PZNFound.equals(PZNtoLookup)) {
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

    public InputStream getFileAsIOStream(final String fileName)
    {
        InputStream ioStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream(fileName);

        if (ioStream == null) {
            throw new IllegalArgumentException(fileName + " is not found");
        }
        return ioStream;
    }

    public void printFileContent(InputStream is) throws IOException
    {
        try (InputStreamReader isr = new InputStreamReader(is);
             BufferedReader br = new BufferedReader(isr);)
        {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            is.close();
        }
    }

}
