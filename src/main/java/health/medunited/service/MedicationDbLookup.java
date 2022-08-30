package health.medunited.service;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class MedicationDbLookup {

    public static List<String> lookupMedicationByPZN(String PZNtoLookup) {
        boolean found = false;

        try {
            File database = new File("/deployments/src/main/resources/medicationDatabase.csv");
            Scanner scanner = new Scanner(database);
            scanner.useDelimiter("[\n]");

            while(scanner.hasNext() && !found) {
                String[] myArray = scanner.next().split(",");
                List<String> tableEntry = Arrays.asList(myArray);
//                System.out.println(tableEntry);
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

}
