package co.wethinkcode.logisticsconnect;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import io.javalin.Javalin;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class IngestionServiceApp {

    private static List<modelRecords> records = new ArrayList<>();

    public static class modelRecords {
        private final String hubID;
        private final String hubName;
        private final String province;
        private final String district;

        public modelRecords(String hubID, String hubName, String province, String district){
            this.hubID = hubID;
            this.hubName = hubName;
            this.province = province;
            this.district= district;

        }

    }

    public static List<modelRecords> cleanFile(String filename){

        try(InputStream is= IngestionServiceApp.class.getResourceAsStream(filename);
        InputStreamReader isr = new InputStreamReader(is);
        CSVReader csvReader = new CSVReaderBuilder(isr).withSkipLines(1).build())
        {


        }catch (Exception e ){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7050);

        app.get("/health", ctx -> ctx.result("OK"));

        // TODO: read and clean src/main/resources/hubs-global.csv (hubs, sorting centers, regional districts data —
        // trim whitespace, fix casing, normalize dates/booleans) and expose the
        // cleaned records here for the other services to consume.
    }
}
