package ru.skbbank.getmypfrinfonow;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootApplication
public class GetMyPfrInfoNowApplication {
    private static final String CSV_FILE_PATH = "";
    private static final String CSV_FILE_RESULT_PATH = "";
    private static final String KONTUR_FOCUS_API_KEY = "3208d29d15c507395db770d0e65f3711e40374df";
    private static final String KONTUR_FOCUS_API_URL = "https://focus-api.kontur.ru/api3/egrDetails";
    private static final String PARSED_FIELD = "pfrRegNumber";

    public static void main(String[] args) throws IOException {

        Unirest.config().proxy("proxy.url", 8080);

        try (
                Reader reader = Files.newBufferedReader(Paths.get(CSV_FILE_PATH));
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

                BufferedWriter writer = Files.newBufferedWriter(Paths.get(CSV_FILE_RESULT_PATH));
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                    .withHeader("claim_id", "inn", "pfr"))
        ) {
            for (CSVRecord csvRecord : csvParser) {
                String claimId = csvRecord.get(0);
                String inn = csvRecord.get(1);

                System.out.println("Record No - " + csvRecord.getRecordNumber());
                System.out.println("---------------");
                System.out.println("claimId : " + claimId);
                System.out.println("inn : " + inn);
                System.out.println("---------------\n\n");

                HttpResponse<String> response = Unirest.get(KONTUR_FOCUS_API_URL)
                        .header("Accept", "application/json")
                        .queryString("key", KONTUR_FOCUS_API_KEY)
                        .queryString("inn", inn)
                        .asString();

                System.out.println(response.getBody());

                JSONArray jArray = new JSONArray(response.getBody());
                JSONObject obj = new JSONObject(jArray.get(0).toString());

                String pfrRegNumber;
                if (inn.length() == 10) {
                    pfrRegNumber = obj.getJSONObject("UL").getString(PARSED_FIELD);
                } else if (inn.length() == 12) {
                    pfrRegNumber = obj.getJSONObject("IP").getString(PARSED_FIELD);
                } else {
                     pfrRegNumber = "none";
                }

                csvPrinter.printRecord(claimId, inn, pfrRegNumber);
            }
            csvPrinter.flush();
        }

        SpringApplication.run(GetMyPfrInfoNowApplication.class, args);

    }

}
