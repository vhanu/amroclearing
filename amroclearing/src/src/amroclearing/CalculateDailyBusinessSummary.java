package amroclearing;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


import com.google.common.base.Joiner;

import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.CSVFormat;
import sun.net.www.protocol.http.logging.HttpLogFormatter;

import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class CalculateDailyBusinessSummary {

    private static final String baseFileLocation = "D:" + File.separator + "csvFile";
    private static Logger logger = null;

    static {
        SimpleFormatter simpleFormatter = new HttpLogFormatter();
        logger = Logger.getLogger(CalculateDailyBusinessSummary.class.getName());
        try {
            String folderPath = baseFileLocation + File.separator + "log";
            File logFolder = new File(folderPath);
            if (!logFolder.exists()) {
                logFolder.mkdir();
            }
            FileHandler handler = new FileHandler((folderPath + File.separator + "log.txt"), true);
            handler.setFormatter(simpleFormatter);
            logger.addHandler(handler);
        } catch (IOException ee) {
            ee.printStackTrace();
        }

    }

    public static void main(String[] args) {
        logger.info("\n Application started");
        logger.info("----------------------------------------------------------------------");
        logger.info("Checking resources and creating folders");

        if(!softwareSetup(baseFileLocation)){
            logger.severe("System exiting, resources not set properly. " +
                    "Check log file for details "+baseFileLocation+File.separator+"log.txt");
            logger.info("----------------------------------------------------------------------\n");
            System.exit(0);
        }

        String descriptionFile = baseFileLocation + File.separator + "System A File Specification.csv";
        String inputFile = baseFileLocation + File.separator + "input.csv";

        logger.info("\n Starting to build daily summary report");

        List<InputHeaderMapping> inputHeaderMappings =
                readCsvFieldDescriptionFileForHeaders(descriptionFile);

        List<String[]> parsedInputRows = readInputFile(inputFile,inputHeaderMappings);



        String intermediateOutFileName = baseFileLocation + File.separator + "output" +
                File.separator + "intermediateOutput_" + getDateString() + ".csv";
        generateIntermediateCsvFile(intermediateOutFileName, inputHeaderMappings, parsedInputRows);

        String outPutFileName = baseFileLocation + File.separator + "output" +
                File.separator + "output_" + getDateString() + ".csv";
        generateOutPutCsvFile(outPutFileName,inputHeaderMappings, parsedInputRows);

        logger.info("All tasks successfully completed. Log file, Intermediate file and Output file written.");
        logger.info("----------------------------------------------------------------------\n");

    }

    /**
     * This method checks the availability of the resources folder location, description file, input file.
     * If the resources are available then it creates the required folders for the log and output files.
     * @return, returns true if software setup is complete
     */
    public static boolean softwareSetup(String baseFileLocation){

        if(!new File(baseFileLocation).exists()){
            logger.severe("Resources folder not found. It is configured to be "+baseFileLocation);
            return false;
        }
        if(!new File(baseFileLocation+File.separator+"System A File Specification.csv").exists()){
            logger.severe("Description file i.e., "+baseFileLocation+File.separator+"'System A File Specification.csv'"
                    +" not found in "+baseFileLocation);
            logger.severe("The system will exit. \n Please add the file in the "+baseFileLocation);
            return false;
        }

        if(!new File(baseFileLocation+File.separator+"input.csv").exists()){
            logger.severe("Description file i.e., "+baseFileLocation+File.separator+"'input.csv'"
                    +" not found in "+baseFileLocation);
            logger.severe("The system will exit. \n Please add the file in the "+baseFileLocation);
            return false;
        }

        //Create folder for Log file once
        createDirectoryIfMissing(baseFileLocation+File.separator+"log");

        //Create folder for output files once
        createDirectoryIfMissing(baseFileLocation+File.separator+"output");

        return true;
    }

    /**
     * This method reads the description file and populates the header and the length of input fields and data.
     * Rules should be followed for preparing the description file
     * Header should be removed
     * The last two rows ie for filler data and total length count should be removed
     * The startIndex, EndIndex and the Length data should contain only numbers
     * The file should be places in the same location as the input file
     * here it is defined by the baseFileLocation variable.
     *
     * @param fileName, the description file
     * @return the list of header for input csv based on the description file
     */
    public static List<InputHeaderMapping> readCsvFieldDescriptionFileForHeaders(String fileName) {
        List<InputHeaderMapping> mapOfInputFields = new ArrayList<>();
        try {
            Reader in = new FileReader(fileName);

            Iterable<CSVRecord> records = CSVFormat.newFormat(',').withHeader(InputHeaderMapping.Headers.class).parse(in);

            for (CSVRecord record : records) {
                String fieldName = record.get(InputHeaderMapping.Headers.FIELDNAME);
                String length = record.get(InputHeaderMapping.Headers.LENGTH).trim();
                Integer intLength = Integer.parseInt(length);
                String startIndex = record.get(InputHeaderMapping.Headers.STARTINDEX).trim();
                Integer intStartIndex = Integer.parseInt(startIndex);
                String endIndex = record.get(InputHeaderMapping.Headers.ENDINDEX).trim();
                Integer intEndIndex = Integer.parseInt(endIndex);

                InputHeaderMapping inputHeaderMapping = new InputHeaderMapping(fieldName, intStartIndex, intEndIndex, intLength);
                mapOfInputFields.add(inputHeaderMapping);

            }

        } catch (IOException ee) {
            ee.printStackTrace();
            logger.severe("There is issue in reading the description file. Place a well formatted file in the proper place " + baseFileLocation);
            logger.severe("System exiting.");
            logger.info("----------------------------------------------------------------------\n");
            System.exit(0);
        }
        logger.info("Description file read completed. Input headers extracted");
        return mapOfInputFields;
    }


    /**
     * This method reads the input file and collects the rows
     * @param inputFile The data file to be read
     * @param inputHeaderMappings, Header description for reading the input file
     * @return, the list of parsed rows from the data file.
     */
    public static List<String[]> readInputFile(String inputFile, List<InputHeaderMapping> inputHeaderMappings ) {
        List<String[]> parsedInputRows = new ArrayList<>();

        try {
            logger.info("Reading input data file");
            Reader in = new FileReader(inputFile);
            Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);

            for (CSVRecord record : records) {
                String[] row = parseAndPopulateFieldsInList(record.get(0),inputHeaderMappings);
                parsedInputRows.add(row);
            }
        } catch
                (Exception ee) {
            ee.printStackTrace();
        }

        logger.info("Input data file read successfully");
        return parsedInputRows;
    }

    /**
     * This method populates each intermediate output rows to add in list from the input file.
     *
     * @param record is each row data in the input file.
     */
    public static String[] parseAndPopulateFieldsInList(String record, List<InputHeaderMapping> inputHeaderMap) {

        String[] column = new String[(getInputHeaders(inputHeaderMap)).size()];
        int i = 0;
        for (InputHeaderMapping indicesMapping : inputHeaderMap) {
            column[i] = record.substring(indicesMapping.getStartIndex() - 1, indicesMapping.getEndIndex());
            i++;
        }
        return column;
    }

    /**
     * This method generates an intermediate output file in the location defined as baseFileLocation.
     * The generated output file has the translated data into the fields defined by the description file.
     */
    public static void generateIntermediateCsvFile(String fileName,List<InputHeaderMapping> headerAndIndicesMap,
                                                   List<String[]> parsedInputRows) {
        logger.info("Generating intermediate csv file.");
        generateCsvFiles(fileName, getInputHeaders(headerAndIndicesMap), parsedInputRows);
        logger.info("Intermediate csv file generated as." + fileName);
    }

    /**
     * This method generates the required output file int the location defined by baseFileLocation
     */

    public static void generateOutPutCsvFile(String fileName, List<InputHeaderMapping> headerAndIndicesMap, List<String[]> parsedInputRows) {

        logger.info("Business logic calculation started for daily summary.");
        List<String> outputHeaders= getInputHeaders(headerAndIndicesMap);

        List<String[]> finalOutPutRows = populateRowsForDailySummary(parsedInputRows, outputHeaders);

        List<String> fileHeaders = new ArrayList<>();
        fileHeaders.add("Client Information");
        fileHeaders.add("Product Information");
        fileHeaders.add("Total Transaction Amount");

        logger.info("Writing final output file " + fileName);
        generateCsvFiles(fileName, fileHeaders, finalOutPutRows);
    }

    /**
     * This method applies the business logic and populates the output rows.
     * @param parsedInputRows are the rows parsed based on the description file
     * @param inputHeaders headers from the description file.
     * @return the parsed rows for the final csv writing
     */
    public static List<String[]> populateRowsForDailySummary(List<String[]> parsedInputRows, List<String> inputHeaders) {
        logger.info("Business logic calculation ongoing.");
        String clientInformation;
        String productInformation;
        Integer totalTransactionAmount;

        HashMap<String, Integer> tempOutPutRows = new HashMap<>();
        List<String[]> finalOutPutRows = new ArrayList<>();

        for (String[] row : parsedInputRows) {
            //The client information contains CLIENT TYPE, CLIENT NUMBER, ACCOUNT NUMBER, SUBACCOUNT NUMBER
            clientInformation = row[inputHeaders.indexOf("CLIENT TYPE")] + "" + row[inputHeaders.indexOf("CLIENT NUMBER")] + ""
                    + row[inputHeaders.indexOf("ACCOUNT NUMBER")] + "" + row[inputHeaders.indexOf("SUBACCOUNT NUMBER")];

            //The productInformation contains EXCHANGE CODE, PRODUCT GROUP CODE, SYMBOL, EXPIRATION DATE
            productInformation = row[inputHeaders.indexOf("EXCHANGE CODE")] + "" + row[inputHeaders.indexOf("PRODUCT GROUP CODE")] + ""
                    + row[inputHeaders.indexOf("SYMBOL")] + "" + row[inputHeaders.indexOf("EXPIRATION DATE")];

            //The total transaction contains sum of QUANTITY LONG - QUANTITY SHORT, for same client, same product, same day
            Integer qtyLong = Integer.parseInt(row[inputHeaders.indexOf("QUANTITY LONG")]);
            Integer qtyShort = Integer.parseInt(row[inputHeaders.indexOf("QUANTITY SHORT")]);
            totalTransactionAmount = (qtyLong - qtyShort);

            String mapKey = Joiner.on('~').join(clientInformation, productInformation);
            if (tempOutPutRows.containsKey(mapKey)) {
                tempOutPutRows.put(mapKey, tempOutPutRows.get(mapKey) + totalTransactionAmount);
            } else {
                tempOutPutRows.put(mapKey, totalTransactionAmount);
            }
        }

        for (String cpInfo : tempOutPutRows.keySet()) {
            String[] cpInformation = cpInfo.split("~");
            String[] column = {cpInformation[0], cpInformation[1], "" + tempOutPutRows.get(cpInfo)};
            finalOutPutRows.add(column);
        }
        logger.info("Business logic calculation for daily summary completed.");
        return finalOutPutRows;
    }

    /**
     * This method writes the csv file
     * @param fileName the filename with absolute path to write the csv file
     * @param headers, the csv header to write
     * @param rowsToWrite, the rows to write in csv
     * @return true when the file is written
     */
    public static boolean generateCsvFiles(String fileName, List<String> headers, List<String[]> rowsToWrite) {
        boolean result = false;
        logger.info("Writing to csv file with name " + fileName);
        if((new File(fileName)).exists()){
            (new File(fileName)).delete();
        }

        try {

            BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName));
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withDelimiter(',').withAllowMissingColumnNames());
            csvPrinter.printRecord(headers);
            for (String[] row : rowsToWrite) {
                csvPrinter.printRecord(Arrays.asList(row));
            }
            csvPrinter.flush();
            result = true;
        } catch (Exception ee) {
            ee.printStackTrace();
            logger.severe("There was some error while writing the file " + fileName + " \n " +
                    "Please check that the folder exists and the folder has read and write access. System exiting.");
            logger.info("----------------------------------------------------------------------\n");
            result= false;
            System.exit(0);
        }
        logger.info("Csv file " + fileName + " written.");
        return result;
    }

    /**
     * Returns a string of date to append in the output file name so that the older summary files are retained.
     * The string returns the date string in format Year Month Day Hour Minutes Seconds without spaces.
     * @return the dateString to make the outfiles unique
     */
    public static String getDateString() {
        return LocalDateTime.now().withNano(0).format(DateTimeFormatter.ofPattern("yyyyMMddhhmmss"));
    }

    /**
     * This method returns the fieldNames for the input headers
     * @param inputHeaderMappings, the List holding the input header details
     * @return list of the field name for the input headers
     */
    public static List<String> getInputHeaders(List<InputHeaderMapping> inputHeaderMappings) {
        List<String> inputHeaders = new ArrayList<>();
        inputHeaderMappings.forEach((inputHeaderMapping)-> inputHeaders.add(inputHeaderMapping.getFieldName()));
        return inputHeaders;

    }

    /**
     * This method checks if the required folder are available or not, if the folder are not available then it creates the folder.
     * @param folder, folder name to check and create if not exist.
     * @return true if successful
     */
    public static boolean createDirectoryIfMissing(String folder) {
        File folderToCheck = new File(folder);
        boolean result = false;
        try {
            if (!folderToCheck.exists()) {
                folderToCheck.mkdir();
            }
            result = true;
        } catch (Exception ee) {
            ee.printStackTrace();
            result= false;
            logger.severe("Creating folder " + folder + " unsuccessful. \n Please check issue " +
                    " and create the folder manually with read write permission.");
            logger.severe("System exiting");
            System.exit(0);
        }

        return result;
    }

}
