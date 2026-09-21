package co.wethinkcode.logisticsconnect;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import io.javalin.Javalin;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class IngestionServiceApp {

    private static List<modelRecords> records = new ArrayList<>();

    public static class modelRecords {
        private final String hubID;
        private final String hubName;
        private final String province;
        private final String flag;

        public modelRecords(String hubID, String province, String hubName, String flag){
            this.hubID = hubID;
            this.hubName = hubName;
            this.province = province;
            this.flag= flag;

        }

    }

    public static List<modelRecords> cleanFile(String filename){

        try(InputStream is= IngestionServiceApp.class.getResourceAsStream(filename);
        InputStreamReader isr = new InputStreamReader(is);
        CSVReader csvReader = new CSVReaderBuilder(isr).withSkipLines(1).build())
        {

            String [] nextRecord ;

            while ((nextRecord = csvReader.readNext()) != null){

                if (nextRecord.length < 4) continue;

                String inputHubID = nextRecord[0].trim().toUpperCase() ;
                String inputHubName= nextRecord[2].trim().toUpperCase() ;
                String inputProvince = nextRecord[1].trim().toUpperCase() ;
                String inputFlag = nextRecord[3].trim().toUpperCase() ;
                String baseHubName;
                String baseProvince;
                String baseFlag;


                switch (inputProvince){
                    case "GAUTENG":
                    case "WESTERN CAPE":
                    case "EASTERN CAPE":
                    case "NORTHERN CAPE":
                    case "MPUMALANGA":
                    case "LIMPOPO":
                    case "KWAZULU NATAL":
                    case "FREESTATE":
                    case "NORTH WEST":
                        baseProvince = nextRecord[1].trim();
                    default :
                        baseProvince = "null";

                }

                switch (inputHubName){
                    case "JOHANNESBURG CENTRAL":
                    case "CAPE TOWN PORT":
                    case "PRETORIA NORTH":
                    case "DURBAN HARBOUR":
                    case "BLOEMFONTEIN HUB":
                    case "PORT ELIZABETH HUB":
                    case "POLOKWANE HUB":
                    case "RUSTENBURG HUB":
                    case "NELSPRUIT HUB":
                    case "KIMBERLY HUB":
                        baseHubName= nextRecord[2].trim();
                    default:
                        baseHubName="null";
                }

                switch (inputFlag) {
                    case "Y":
                    case "YES":
                    case "TRUE":
                    case "1":
                        baseFlag= "true";
                        break;
                    case "N":
                    case "NO":
                    case "FALSE":
                    case "0":
                        baseFlag= "false";
                        break;
                    case "UNKNOWN":
                        baseFlag = "UNKNOWN";
                    case "N/A":
                        baseFlag = "N/A";
                    default:
                        baseFlag = "null";
                }
                records.add(new modelRecords(inputHubID, Character.toUpperCase(baseProvince.charAt(0)) + baseProvince.substring(1).toLowerCase(), Character.toUpperCase(baseHubName.charAt(0)) + baseHubName.substring(1).toLowerCase(Locale.ROOT), baseFlag));
            }

        }catch (Exception e ){
            e.printStackTrace();
        }
        return records;
    }

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7050);

        app.get("/health", ctx -> ctx.result("OK"));
        app.get("/hubs", ctx -> ctx.json(cleanFile("/hubs-global.csv")));
        // TODO: read and clean src/main/resources/hubs-global.csv (hubs, sorting centers, regional districts data —
        // trim whitespace, fix casing, normalize dates/booleans) and expose the
        // cleaned records here for the other services to consume.
    }
}
