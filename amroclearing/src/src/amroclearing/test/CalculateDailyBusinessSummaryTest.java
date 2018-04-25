package amroclearing.test;

import amroclearing.InputHeaderMapping;
import amroclearing.CalculateDailyBusinessSummary;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(JUnit4.class)
public class CalculateDailyBusinessSummaryTest {

    CalculateDailyBusinessSummary calculateDailyBusinessSummary;
    private static String baseFileLocation = "";
    private static String descriptionFile = "";
    private static String inputFile = "";
    private static List<InputHeaderMapping> inputHeaderMapSubset = new ArrayList<>();
    private List<String[]> parsedRowsSubset = new ArrayList<>();
    private static List<String> inputHeadersAllColumns = new ArrayList<>();
    private static List<String[]> parsedInputRowsAllColumns = new ArrayList<>();


    @Before
    public void setUp() {
        calculateDailyBusinessSummary = new CalculateDailyBusinessSummary();
        baseFileLocation = "D:" + File.separator + "csvFile" + File.separator + "test";
        File baseFolderForTest = new File(baseFileLocation);
        if (!baseFolderForTest.exists()) baseFolderForTest.mkdir();

        descriptionFile = "System A File Specification.csv";
        inputFile = baseFileLocation+ File.separator+ "input test1.csv";

        inputHeaderMapSubset=new ArrayList<>();
        inputHeaderMapSubset.add(new InputHeaderMapping("RECORD CODE", 1, 3, 3));
        inputHeaderMapSubset.add(new InputHeaderMapping("CLIENT TYPE", 4, 7, 4));
        inputHeaderMapSubset.add(new InputHeaderMapping("CLIENT NUMBER", 8, 11, 4));


        String[] row1 = {"315", "CL  ", "4321"};
        String[] row2 = {"315", "CL  ", "4321"};
        String[] row3 = {"315", "CL  ", "1234"};

        parsedRowsSubset= new ArrayList<>();
        parsedRowsSubset.add(row1);
        parsedRowsSubset.add(row2);
        parsedRowsSubset.add(row3);


        String[] headers = {"RECORD CODE", "CLIENT TYPE", "CLIENT NUMBER", "ACCOUNT NUMBER", "SUBACCOUNT NUMBER",
                "OPPOSITE PARTY CODE", "PRODUCT GROUP CODE", "EXCHANGE CODE", "SYMBOL", "EXPIRATION DATE", "CURRENCY CODE",
                "MOVEMENT CODE", "BUY SELL CODE", "QUANTTTY LONG SIGN", "QUANTITY LONG", "QUANTITY SHORT SIGN",
                "QUANTITY SHORT", "EXCH/BROKER FEE / DEC", "EXCH/BROKER FEE / DC", "EXCH/BROKER FEE CUR CODE",
                "CLEARING FEE / DEC", "CLEARING FEE D C", "CLEARING FEE CUR CODE", "COMMISSION", "COMMISSION D C",
                "COMMISSION CUR CODE", "TRANSACTION DATE", "FUTURE REFERENCE", "TICKET NUMBER", "EXTERNAL NUMBER",
                "TRANSACTION PRICE / DEC", "TRADER INITIALS", "OPPOSITE TRADER ID", "OPEN CLOSE CODE"};

        inputHeadersAllColumns = Arrays.asList(headers);


        String[] rown1 = {"315", "CL  ", "4321", "0002", "0001", "SGXDC ", "FU", "SGX ", "NK    ", "20100910", "JPY", "01", "B", " "
                , "0000000001", " ", "0000000000", "000000000060", "D", "USD", "000000000030", "D", "USD", "000000000000", "D",
                "JPY", "20100820", "001238", "0     ", "688032", "000092500000000", "      ", "       ", "O"};
        String[] rown2 = {"315", "CL  ", "4321", "0002", "0001", "SGXDC ", "FU", "SGX ", "NK    ", "20100910", "JPY", "01", "B", " ",
                "0000000001", " ", "0000000000", "000000000060", "D", "USD", "000000000030", "D", "USD", "000000000000", "D", "JPY",
                "20100820", "001240", "0     ", "688058", "000092500000000", "      ", "       ", "O"};
        String[] rown3 = {"315", "CL  ", "1234", "0002", "0001", "SGXDC ", "FU", "SGX ", "NK    ", "20100910", "JPY", "01", "B", " ",
                "0000000001", " ", "0000000000", "000000000060", "D", "USD", "000000000030", "D", "USD", "000000000000", "D", "JPY",
                "20100820", "001240", "0     ", "688058", "000092500000000", "      ", "       ", "O"};

        parsedInputRowsAllColumns= new ArrayList<>();
        parsedInputRowsAllColumns.add(rown1);
        parsedInputRowsAllColumns.add(rown2);
        parsedInputRowsAllColumns.add(rown3);

    }

    @Test
    public void testCreateDirectoryIfMissing() {
        File testFolder = new File(baseFileLocation + File.separator + "testFolder1");
        calculateDailyBusinessSummary.createDirectoryIfMissing(baseFileLocation + File.separator + "testFolder1");
        Assert.assertTrue(testFolder.exists());
        if (testFolder.exists()) testFolder.delete();
    }

    @Test
    public void testSoftwareSetUp() {
        calculateDailyBusinessSummary.softwareSetup(baseFileLocation);
        File testDescriptionFile = new File(baseFileLocation + File.separator + "System A File Specification.csv");
        File testInputFile = new File(baseFileLocation + File.separator + "Input.csv");
        File logFolder = new File(baseFileLocation + File.separator + "log");
        File outputFolder = new File(baseFileLocation + File.separator + "output");

        Assert.assertTrue(testDescriptionFile.exists());
        Assert.assertTrue(testInputFile.exists());
        Assert.assertTrue(logFolder.exists());
        Assert.assertTrue(outputFolder.exists());

        //Deleting the log and output folder created inside the test folder during unit test
        if (logFolder.exists()) logFolder.delete();
        if (outputFolder.exists()) outputFolder.delete();

    }

    @Test
    public void testGetDateStringReturnsAValidString() {
        String actualDateString = calculateDailyBusinessSummary.getDateString();
        //Just checking that it returns a valid string, no need to check the date returned by Java
        Assert.assertTrue(!actualDateString.isEmpty());
    }

    @Test
    public void testReadCsvFieldDescriptionFileForHeaders() {
        List<InputHeaderMapping> inputHeaderMappings1 = new ArrayList<>();
        String fileName = baseFileLocation + File.separator + descriptionFile.replace(".csv", " test1.csv");
        inputHeaderMappings1 = calculateDailyBusinessSummary.readCsvFieldDescriptionFileForHeaders(fileName);
        Assert.assertTrue(inputHeaderMappings1.size() > 0);

        String[] actualFieldName = new String[inputHeaderMappings1.size()];
        String[] expectedInputField = {"RECORD CODE", "CLIENT TYPE", "CLIENT NUMBER", "ACCOUNT NUMBER", "SUBACCOUNT NUMBER", "OPPOSITE PARTY CODE"};

        int j = 0;
        for (InputHeaderMapping im : inputHeaderMappings1) {
            actualFieldName[j] = im.getFieldName();
            j++;
        }
        Assert.assertArrayEquals(expectedInputField, actualFieldName);
    }

    @Test
    public void testReadInputFile() {
        List<String[]> expectedResult = new ArrayList<>();
        String[] row1 = {"315", "CL  ", "4321"};
        String[] row2 = {"315", "CL  ", "4321"};
        String[] row3 = {"315", "CL  ", "1234"};

        expectedResult.add(row1);
        expectedResult.add(row2);
        expectedResult.add(row3);

        List<String[]> listOfInputRows = calculateDailyBusinessSummary.readInputFile(inputFile, inputHeaderMapSubset);
        int j = 0;
        for (String[] str : listOfInputRows) {
            Assert.assertArrayEquals(str, expectedResult.get(j));
            j++;
        }
    }

    @Test
    public void testParseAndPopulateFieldsInList() {
        //String record, List<InputHeaderMapping> headerAndIndicesMap
        String record = "315CL  432100020001SGXDC FUSGX NK    20100910JPY01B 0000000001 " +
                "0000000000000000000060DUSD000000000030DUSD000000000000DJPY201008200012380  " +
                "   688032000092500000000             O\n";

        String[] columns = calculateDailyBusinessSummary.parseAndPopulateFieldsInList(record, inputHeaderMapSubset);
        String[] expectedColumns = {"315", "CL  ", "4321"};
        Assert.assertArrayEquals(expectedColumns, columns);
    }

    @Test
    public void testGenerateIntermediateCsvFile() {
        String fileName = baseFileLocation + File.separator + "intermediateOutput_" + calculateDailyBusinessSummary.getDateString() + ".csv";
        calculateDailyBusinessSummary.generateIntermediateCsvFile(fileName, inputHeaderMapSubset, parsedRowsSubset);
        File file = new File(fileName);
        Assert.assertTrue(file.exists());
        if (file.exists()) file.delete();
    }

    @Test
    public void testPopulateBusinessSummary() {
        List<String[]> actualFinalRows = calculateDailyBusinessSummary.populateRowsForDailySummary(parsedInputRowsAllColumns, inputHeadersAllColumns);
        List<String[]> expectedFinalRows = new ArrayList<>();

        String[] outRow1 = {"CL  432100020001", "SGX FUNK    20100910", "2"};
        String[] outRow2 = {"CL  123400020001", "SGX FUNK    20100910", "2"};
        expectedFinalRows.add(outRow1);
        expectedFinalRows.add(outRow2);

        Assert.assertArrayEquals(expectedFinalRows.get(0), outRow1);
        Assert.assertArrayEquals(expectedFinalRows.get(1), outRow2);

    }

    @Test
    public void testCsvFileGeneration() {
        String fileName = baseFileLocation + File.separator + "testFile_" + calculateDailyBusinessSummary.getDateString() + ".csv";
        calculateDailyBusinessSummary.generateCsvFiles(fileName, inputHeadersAllColumns, parsedInputRowsAllColumns);
        Assert.assertTrue((new File(fileName)).exists());

        if ((new File(fileName)).exists()) {
            (new File(fileName)).delete();
        }

    }

}

