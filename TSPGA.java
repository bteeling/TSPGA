
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

public class TSPGA {

    public static ArrayList<City> cityList;
    public static double biggestX, biggestY, smallestX, smallestY, circleRadius;
    public static int bestMemberI;
    public static KeyboardInputClass input = new KeyboardInputClass();
    public static City startCity;
    public static boolean fill, haveData = false;
    public static char showCityNums;

    public static void main(String[] args) {
        int popSize, nCN, scenario;
        double crossRate, mutationRate;
        boolean specifiedStart;
        System.out.println("Brandon Teeling: TSP Genetic Algorithm");
        while (true) {
            specifiedStart = false;
            if (!haveData) {
                cityList = new ArrayList();
                biggestX = 0;
                biggestY = 0;
                smallestX = 999999999;
                smallestY = 999999999;
                if (input.getCharacter(true, 'Y', "YN", 1, "Randomly generate cities? (Y/N. Default = Y)") == 'Y') {
                    generateCities();
                } else {
                    if (getTextFile()) {
                        haveData = true;
                    }//end of if
                }//end of if and else
            } else {
                setRangesAndCircleProperties();
                ImageConstruction citiesImage;
                showCityNums = input.getCharacter(true, 'Y', "YN", 1, "Show city numbers? (Y/N. Default = Y)\nWARNING: not recommended for 150 or more cities");
                citiesImage = getCitiesImage(false, null);
                citiesImage.displayImage(false, "window", true);
                for (int i = 0; i < cityList.size(); i++) {
                    City city = cityList.get(i);
                    System.out.println("city " + city.getLabel() + ": " + city.getX() + ", " + city.getY());
                }//end of loop
                if (input.getCharacter(true, 'N', "YN", 1, "Would you like to specify a starting city? (Y/N. Default = N)") == 'Y') {
                    specifiedStart = true;
                    startCity = cityList.get(input.getInteger(true, 1, 1, cityList.size(), "City number to start at? (1-" + cityList.size() + ". Default = 1)") - 1);
                }//end of if
                scenario = input.getInteger(true, 1, 1, 2, "Which scenario would you like to run?\n1. Single run (shows best path after each set of generations)\n2. Multiple runs (show results of multiple runs and generate a bell curve)\n(1-2. Default = 1)");
                popSize = input.getInteger(true, 20, 1, 999999999, "Population size? (Default = 20)");
                crossRate = input.getDouble(true, 1, 0, 1, "Crossover rate? (0-1. .8-1 is recommended. Default = 1)");
                mutationRate = input.getDouble(true, .5, 0, 1, "Mutation rate? (0-1. Default = .5)");
                nCN = input.getInteger(true, 1, 1, 2, "Create generation 0 (Enter 1 or 2):\n1. Randomly (Default)\n2. Next best neighbor");
                if (scenario == 1) {
                    oneRun(popSize, crossRate, mutationRate, specifiedStart, nCN, citiesImage);
                } else {
                    oneSetOfRuns(popSize, crossRate, mutationRate, specifiedStart, nCN, 50000, 600);
                }//end of if and else
                switch (input.getInteger(true, 1, 1, 3, "(1-3. Default = 1)\n1. Continue with current city data.\n2. Get new city data\n3. Exit")) {
                    case 1://continue with city data
                        break;
                    case 2://get new city data
                        haveData = false;
                        break;
                    case 3://exit
                        citiesImage.closeDisplay();
                        System.exit(0);
                }//end of switch
            }//end of if and else
        }//end of loop
    }//end of method

    public static void oneSetOfRuns(int popSize, double crossRate, double mutationRate, boolean specifiedStart, int nCN, int defaultGens, double defaultTargetDistance) {
        LinkedQueue bellCurves = new LinkedQueue();
        ImageConstruction image = null;
        int runs, generations, binSize, minBinSize;
        double targetDistance;
        int[][] bins;
        Object[] newParams;
        runs = input.getInteger(true, 10, 1, 999999999, "Number of runs? (1-999999999. Default = 10)");
        generations = input.getInteger(true, defaultGens, 1, 999999999, "Max number of generations per run? (1-999999999. Default = " + defaultGens + ")");
        minBinSize = (int) (generations / 13.333333333333333333333333333333333333);
        binSize = input.getInteger(true, minBinSize, minBinSize, generations, "Histogram bin size? (Minimun size of " + Integer.toString(minBinSize) + " due to window width limitations. Max size of " + generations + ". Default = " + minBinSize + ")");
        targetDistance = input.getDouble(true, defaultTargetDistance, 1, 999999999, "Target distance? Choose a near optimal distance for good bell curve. (1-999999999. Default = " + defaultTargetDistance + ")");
        while (true) {
            int minBinRange = 0, maxBinRange = 0, labels = 13, genNum, highestGenNum = 0;
            if (!bellCurves.isEmpty()) {
                if (input.getCharacter(true, 'Y', "YN", 1, "Close oldest bell curve? (Y/N. Default = Y)") == 'Y') {
                    image = (ImageConstruction) bellCurves.dequeue();
                    image.closeDisplay();
                    while (!bellCurves.isEmpty()) {
                        if (input.getCharacter(true, 'N', "YN", 1, "Close another? (Y/N. Default = N)") == 'Y') {
                            image = (ImageConstruction) bellCurves.dequeue();
                            image.closeDisplay();
                        } else {
                            break;
                        }//end of if and else
                    }//end of loop
                }//end of if
            }//end of if
            if (generations % binSize == 0) {
                bins = new int[generations / binSize][2];
            } else {
                bins = new int[(generations / binSize) + 1][2];
            }//end of if and else
            labels += bins.length;
            for (int i = 0; i < bins.length; i++) {//fills in the ranges for the bins
                if (maxBinRange + binSize > generations) {
                    bins[i][0] = generations;
                } else {
                    bins[i][0] = maxBinRange + binSize;
                    maxBinRange += binSize;
                }//end of if and else
            }//end of loop
            int[] genNums = new int[runs];//will hold genNum that each run terminated at
            Member curBest;
            Member[] curGen;
            for (int i = 0; i < runs; i++) {
                genNum = 0;
                curGen = getGeneration0(popSize, specifiedStart, nCN);
                curBest = curGen[bestMemberI];
                while (curBest.getDistance() > targetDistance && genNum < generations) {//each run terminates when the target value is reached
                    curGen = getNextGeneration(curGen, popSize, crossRate, mutationRate, specifiedStart);
                    genNum++;
                    curBest = curGen[bestMemberI];
                }//end of loop
                genNums[i] = genNum;
                System.out.print("Run " + (i + 1) + " Generations: " + genNum + " ");
                if (genNum == generations) {
                    System.out.println("(Did not reach target distance of " + targetDistance + ")");
                } else {
                    System.out.println("");
                    if (genNum > highestGenNum) {
                        highestGenNum = genNum;
                    }//end of if
                }//end of if and else
                curBest.displayMember();
            }//end of loop
            Arrays.sort(genNums);
            System.out.println("Generation counts:");
            for (int i = 0; i < runs; i++) {
                System.out.println(genNums[i]);
                for (int j = 0; j < bins.length; j++) {//finds the bin that run falls into
                    if (genNums[i] > minBinRange && genNums[i] <= bins[j][0]) {
                        bins[j][1]++;
                        break;
                    } else {
                        minBinRange = bins[j][0];
                    }//end of if and else
                }//end of nested loop
            }//end of loop
            image = getBellCurve(generations, runs, bins, binSize, labels);
            bellCurves.enqueue(image);
            image.displayImage(false, "window", true);
            System.out.print("Current GA parameters:\nPopulation size: " + popSize + "\nCrossover rate: " + crossRate + "\nMutation rate: " + mutationRate + "\nSpecify start city: " + specifiedStart + "\nNext closest neighbor: ");
            if (nCN == 2) {
                System.out.println(true);
            } else {
                System.out.println("false");
            }//end of if and else
            if (input.getCharacter(true, 'Y', "YN", 1, "Another set of runs?(Y/N. Default = Y)") == 'Y') {
                if (input.getCharacter(true, 'N', "YN", 1, "Change bell curve parameters? (Number of runs, max generations per run, histogram bin size, target distance)\n(Y/N. (Default = N)") == 'Y') {
                    newParams = changeBellCurveParams(new Object[]{runs, generations, binSize, targetDistance});
                    runs = (int) newParams[0];
                    generations = (int) newParams[1];
                    binSize = (int) newParams[2];
                    targetDistance = (double) newParams[3];
                }//end of if
                if (input.getCharacter(true, 'N', "YN", 1, "Change genetic algorithm  parameters? (Population size, crossover/mutation rate, specify start city, initialization of starting generation)\n(Y/N. (Default = N)") == 'Y') {
                    newParams = changeGAParams(new Object[]{popSize, crossRate, mutationRate, specifiedStart, nCN});
                    popSize = (int) newParams[0];
                    crossRate = (double) newParams[1];
                    mutationRate = (double) newParams[2];
                    specifiedStart = (boolean) newParams[3];
                    nCN = (int) newParams[4];
                }//end of if
            } else {
                while (!bellCurves.isEmpty()) {
                    image = (ImageConstruction) bellCurves.dequeue();
                    image.closeDisplay();
                }//end of loop
                return;
            }//end of if and else
        }//end of loop
    }//end of method

    public static ImageConstruction getBellCurve(int generations, int runs, int[][] bins, int binSize, int labels) {
        int colorNum;
        double rightMostOfWindow = generations * 1.4, topMostOfWindow, leftMost, rightMost, bottomMost, topMost, boxCornersIncrementer;
        int yAxisMax;
        int[][] colors = new int[][]{
            {255, 0, 0},
            {255, 0, 255},
            {0, 0, 255},
            {0, 255, 255},
            {0, 255, 0},
            {255, 255, 0},
            {255, 128, 0}
        };
        double maxRunNum = 0;
        String[] labelText = new String[labels];
        labelText[0] = "Generations";
        String intToParse1;
        String intToParse2;
        int minRange = 0;
        for (int i = 0; i < bins.length; i++) {
            intToParse1 = Integer.toString(bins[i][0]);
            intToParse2 = Integer.toString(minRange);
            labelText[i + 1] = "(" + intToParse2 + " - " + intToParse1 + ")";
            minRange = bins[i][0];
        }//end of loop
        for (int i = 0; i < bins.length; i++) {//finds the number of runs in the bin with the most runs
            if (bins[i][1] > maxRunNum) {
                maxRunNum = bins[i][1];
            }//end of if    
        }//end of loop
        yAxisMax = (int) maxRunNum;
        while (true) {
            yAxisMax++;
            if (yAxisMax % 10 == 0) {
                break;
            }//end of if
        }//end of loop
        labelText[bins.length + 1] = "# of Runs";
        double incrementer = yAxisMax / 10;
        int cur = 0;
        for (int i = bins.length + 2; i < labelText.length; i++) {
            labelText[i] = Integer.toString(cur);
            cur += incrementer;
        }//end of loop
        topMostOfWindow = yAxisMax * 1.4;
        leftMost = generations * .2;
        rightMost = generations * 1.2;
        bottomMost = (double) yAxisMax * .2;
        topMost = (double) yAxisMax * 1.2;;
        int width = 500;
        if (bins.length > 5) {//will add width to the window for every bin more than 10
            for (int i = 5; i < bins.length; i++) {
                width += 100;
            }//end of loop
        }//end of if
        ImageConstruction myImage = new ImageConstruction(500, width, 0, rightMostOfWindow, 0, (double) yAxisMax * 1.4, 1);
        myImage.displaySetup();
        myImage.imageOut.text = new String[labels];
        myImage.imageOut.textLineCount = myImage.imageOut.text.length;
        myImage.imageOut.textPosition = new int[myImage.imageOut.textLineCount][2];
        myImage.insertText(rightMost / 2, bottomMost / 4, labelText[0], 0);
        myImage.insertText(leftMost / 4, topMostOfWindow / 1.1, labelText[bins.length + 1], bins.length + 1);
        int incrementer2 = 0;
        for (int i = 1; i < bins.length + 1; i++) {
            myImage.insertText(leftMost + incrementer2, bottomMost / 1.25, labelText[i], i);
            incrementer2 += binSize;
        }//end of loop
        for (int i = bins.length + 2; i < labelText.length; i++) {
            myImage.insertText(leftMost / 1.6, bottomMost + Double.parseDouble(labelText[i]), labelText[i], i);
            myImage.insertLine(leftMost, bottomMost + Double.parseDouble(labelText[i]), rightMost, bottomMost + Double.parseDouble(labelText[i]), 255, 255, 255);
        }//end of loop
        colorNum = (int) (Math.random() * 7);
        boxCornersIncrementer = 0;
        for (int i = 0; i < bins.length; i++) {
            if (bins[i][0] == generations) {
                myImage.insertBox(leftMost + boxCornersIncrementer, bins[i][1] + bottomMost, rightMost, bottomMost, colors[colorNum][0], colors[colorNum][1], colors[colorNum][2], true);
            } else {
                myImage.insertBox(leftMost + boxCornersIncrementer, bins[i][1] + bottomMost, leftMost + binSize + boxCornersIncrementer, bottomMost, colors[colorNum][0], colors[colorNum][1], colors[colorNum][2], true);
                boxCornersIncrementer += binSize;
            }//end of if and else
            if (colorNum == 6) {
                colorNum = 0;
            } else {
                colorNum++;
            }//end of if and else
        }//end of loop
        myImage.insertLine(leftMost, bottomMost, leftMost, topMost, 255, 255, 255);
        return myImage;
    }//end of method

    public static void oneRun(int popSize, double crossRate, double mutationRate, boolean specifiedStart, int nCN, ImageConstruction citiesImage) {
        while (true) {
            ImageConstruction lastCitiesImage, curCitiesImage;
            int gens = 0, lastGenWithImprovement = 0;
            boolean doneASetOfGens = false;
            double bestAfterLastSetOfGens = 0, bestDistanceGen0, defaultTargetDistance;
            char showEachGen;
            Member curBest;
            Member[] curGen = getGeneration0(popSize, specifiedStart, nCN);
            Object[] newParams;
            curBest = curGen[bestMemberI];
            bestDistanceGen0 = curBest.getDistance();
            citiesImage.closeDisplay();
            curCitiesImage = getCitiesImage(true, curGen[bestMemberI]);
            curCitiesImage.displayImage(false, "window", true);;
            System.out.println("Generation " + gens + ": Best Distance = " + curBest.getDistance());
            if (input.getCharacter(true, 'N', "YN", 1, "Show best solution genome? (Y/N. Default = N)") == 'Y') {
                curBest.displayMember();
            }//end of if
            if (input.getCharacter(true, 'N', "YN", 1, "Show population of current generation? (Y/N. Default = N)") == 'Y') {
                for (int i = 0; i < curGen.length; i++) {
                    System.out.print("Member " + (i + 1));
                    if (i == bestMemberI) {
                        System.out.println(" (best)");
                    } else {
                        System.out.println("");
                    }//end of if and else
                    curGen[i].displayMember();
                    System.out.println("------------------------------------------------------------------------------");
                }//end of loop
            }//end of if
            int generations = input.getInteger(true, 10000, 1, 999999999, "Number of generations? (1-999999999. Default = 10000)");
            while (true) {
                showEachGen = input.getCharacter(true, 'N', "YN", 1, "Show best member from each generation? (Y/N. Default = N)");
                lastCitiesImage = curCitiesImage;
                for (int i = 0; i < generations; i++) {
                    curGen = getNextGeneration(curGen, popSize, crossRate, mutationRate, specifiedStart);
                    gens++;
                    if (curBest.getDistance() > curGen[bestMemberI].getDistance()) {
                        lastGenWithImprovement = gens;
                    }//end of if
                    curBest = curGen[bestMemberI];
                    if (showEachGen == 'Y') {
                        System.out.println("Generation " + gens + ": Best Distance = " + curBest.getDistance());
                    }//end of if
                }//end of loop
                System.out.println("Generation " + gens + ": Best Distance = " + curBest.getDistance());
                System.out.println("Initial best distance: " + bestDistanceGen0);
                if (doneASetOfGens) {
                    System.out.println("Best distance after last generation set: " + bestAfterLastSetOfGens);
                }//end of if
                System.out.println("Last distance improvement: generation " + lastGenWithImprovement);
                doneASetOfGens = true;
                bestAfterLastSetOfGens = curBest.getDistance();
                curCitiesImage = getCitiesImage(true, curGen[bestMemberI]);
                curCitiesImage.displayImage(false, "window", true);
                if (input.getCharacter(true, 'N', "YN", 1, "Show best solution genome? (Y/N. Default = N)") == 'Y') {
                    curBest.displayMember();
                }//end of if
                if (input.getCharacter(true, 'N', "YN", 1, "Show population of current generation? (Y/N. Default = N)") == 'Y') {
                    for (int i = 0; i < curGen.length; i++) {
                        System.out.print("Member " + (i + 1));
                        if (i == bestMemberI) {
                            System.out.println(" (best)");
                        } else {
                            System.out.println("");
                        }//end of if and else
                        curGen[i].displayMember();
                        System.out.println("------------------------------------------------------------------------------");
                    }//end of loop
                }//end of if
                if (input.getCharacter(true, 'Y', "YN", 1, "Create more generations?(Y/N. Default = Y)") == 'Y') {
                    generations = input.getInteger(true, 10000, 1, 999999999, "Number of generations? (1-999999999. Default = 10000)");
                    lastCitiesImage.closeDisplay();
                } else {
                    curCitiesImage.closeDisplay();
                    lastCitiesImage.closeDisplay();
                    break;
                }//end of if and else
            }//end of loop
            if (input.getCharacter(true, 'N', "YN", 1, "Do another run with different parameters? (Y/N. Default = N)") == 'Y') {
                newParams = changeGAParams(new Object[]{popSize, crossRate, mutationRate, specifiedStart, nCN});
                popSize = (int) newParams[0];
                crossRate = (double) newParams[1];
                mutationRate = (double) newParams[2];
                specifiedStart = (boolean) newParams[3];
                nCN = (int) newParams[4];
            } else {
                System.out.print("Current parameters:\nPopulation size: " + popSize + "\nCrossover rate: " + crossRate + "\nMutation rate: " + mutationRate + "\nSpecify start city: " + specifiedStart + "\nNext closest neighbor: ");
                if (nCN == 2) {
                    System.out.println(true);
                } else {
                    System.out.println("false");
                }//end of if and else
                if (input.getCharacter(true, 'Y', "YN", 1, "Do a set of multiple runs with current parameters? (Y/N. Default = Y)") == 'Y') {
                    defaultTargetDistance = (curBest.getDistance() + (bestDistanceGen0 * .05));//best attained distance + 5% of gen0bestdistance
                    oneSetOfRuns(popSize, crossRate, mutationRate, specifiedStart, nCN, (int) (lastGenWithImprovement * 1.2), defaultTargetDistance);
                    return;
                } else {
                    return;
                }//end of if and else
            }//end of if     
        }//end of loop
    }//end of method

    public static boolean getTextFile() {
        City city;
        TextFileClass textFile = new TextFileClass();
        System.out.println("File should be formatted as follows:\ncitynumber Xcoordinate Ycoordinate\n(a space sperates each city characteristic, each city on its own line)");
        System.out.println("Example: \n1 14 75\n" + "2 94 65\n" + "3 44 92....");
        textFile.getFileName("Specify the text file to be read");
        if (textFile.fileName.length() > 0) {
            textFile.getFileContents();
        }//end of if
        if (textFile.text[0] != null) {
            for (int i = 0; true; i++) {//trims the spaces from the beginnings and ends of the data
                if (textFile.text[i] != null) {
                    textFile.text[i] = textFile.text[i].trim();
                } else {
                    break;
                }//end of if else
            }//end of loop
            String curString;
            String curSubstring;
            String curChar;
            double[] curCity;
            int cnt;
            for (int i = 0; i < textFile.text.length; i++) {//going through the lines in the file
                curCity = new double[3];
                curString = textFile.text[i];
                if (textFile.text[i] == null) {
                    break;
                } else {
                    cnt = 0;
                    curSubstring = "";
                    for (int j = 0; j < curString.length(); j++) {//going through the characters in a line
                        curChar = curString.substring(j, j + 1);
                        if (curChar.equals(" ")) {
                            curCity[cnt] = Double.parseDouble(curSubstring);
                            cnt++;
                            curSubstring = "";
                        } else {
                            curSubstring += curChar;
                        }//end of if and else
                        if (j == curString.length() - 1) {//end of string reached
                            curCity[cnt] = Double.parseDouble(curSubstring);
                            cnt = 0;
                            city = new City((int) curCity[0], curCity[1], curCity[2]);
                            cityList.add(i, city);//add the last coordinate to curCity then add cur city to city cityList
                        }//end of if
                    }//end of loop
                }//end of if and else
            }//end of loop
            return true;
        } else {
            return false;
        }//end of if and else
    }//end of method

    public static ImageConstruction getCitiesImage(boolean havePath, Member member) {
        ImageConstruction myImage = new ImageConstruction(750, 750, smallestX - circleRadius, biggestX + circleRadius, smallestY - circleRadius, biggestY + circleRadius, 1);
        myImage.displaySetup();
        String[] labels = null;
        int[][] cityColors = {{255, 0, 0}, {255, 255, 0}};
        int cityColorNum = 1;
        if (showCityNums == 'Y') {
            cityColorNum = 0;
            labels = new String[cityList.size()];
            for (int i = 0; i < labels.length; i++) {
                labels[i] = Integer.toString(i + 1);
            }//end of loop
            myImage.imageOut.text = new String[cityList.size()];
            myImage.imageOut.textLineCount = myImage.imageOut.text.length;
            myImage.imageOut.textPosition = new int[myImage.imageOut.textLineCount][2];
        }//end of if
        City curCity = null;
        for (int i = 0; i < cityList.size(); i++) {
            curCity = cityList.get(i);
            myImage.insertCircle(curCity.getX(), curCity.getY(), circleRadius, cityColors[cityColorNum][0], cityColors[cityColorNum][1], cityColors[cityColorNum][2], fill);
            if (showCityNums == 'Y') {
                myImage.insertText(curCity.getX() - circleRadius, curCity.getY() - circleRadius, labels[i], i);
            }//end of if
        }//end of loop
        if (havePath) {
            startCity = member.getGenome()[0];
            myImage.insertCircle(startCity.getX(), startCity.getY(), circleRadius, 0, 0, 255, fill);
            City[] genome = member.getGenome();
            City prevCity = null;
            prevCity = cityList.get(genome[0].getLabel() - 1);//cities are located at the index # before their label (city 24 located in index 23 of cityList)
            for (int i = 1; i < genome.length; i++) {
                curCity = cityList.get(genome[i].getLabel() - 1);
                myImage.insertLine(prevCity.getX(), prevCity.getY(), curCity.getX(), curCity.getY(), 0, 255, 0);
                prevCity = curCity;
            }//end of loop 
        }//end of if 
        return myImage;
    }//end of method

    public static void generateCities() {
        int cityNum = input.getInteger(true, 25, 3, 10000, "Number of cities? (3-10000. Default = 25)");
        double randX;
        double randY;
        for (int i = 0; i < cityNum; i++) {
            randX = Math.random() * 100;
            randY = Math.random() * 100;
            cityList.add(i, new City(i + 1, randX, randY));
        }//end of loop
        haveData = true;
    }//end of method

    public static void setRangesAndCircleProperties() {
        double x;
        double y;
        for (int i = 0; i < cityList.size(); i++) {
            x = cityList.get(i).getX();
            y = cityList.get(i).getY();
            if (x > biggestX) {
                biggestX = x;
            }//end of if
            if (y > biggestY) {
                biggestY = y;
            }//end of if
            if (x < smallestX) {
                smallestX = x;
            }//end of if
            if (y < smallestY) {
                smallestY = y;
            }//end of if 
        }//end of loop
        circleRadius = (((biggestX - smallestX) + (biggestY - smallestY)) / 2) / cityList.size();
        fill = true;
        if (cityList.size() < 100) {
            circleRadius = 1;
        }//end of if
        if (cityList.size() > 500) {
            fill = false;
        }//end of if
    }//end of method
    //**************************************************************************
    //Method:      getGeneration0
    //Description: Creates generation 0 (the starting generation). Does so either randomly or using next closest neighbor.
    //Parameters:  popSize        Size of the population.
    //             specifiedStart Indicates whether or not a starting city was specified (true or false).
    //             nCN            Indicates whether or not to use next closest neighbor (1 means no, 2 means yes).
    //Returns:     Member[] Array containing the members of the generation.
    //Calls:       None
    //Globals:     cityList, startCity, bestMemberI

    private static Member[] getGeneration0(int popsize, boolean specifiedStart, int nCN) {
        City city = null;
        Member[] gen0 = new Member[popsize];
        City[] curGenome;
        int curGenomeCnt;
        double curBestDist = 0, curDist = 0;
        ArrayList<City> temp = new ArrayList();
        for (int i = 0; i < popsize; i++) {
            for (int j = 0; j < cityList.size(); j++) {//copy cityList to temp
                temp.add(j, cityList.get(j));
            }//end of loop  
            curGenome = new City[cityList.size() + 1];
            if (!specifiedStart) {
                startCity = temp.remove((int) (Math.random() * temp.size()));//randomly pick a specifiedStart city if one has not been specified  
            } else {
                temp.remove(startCity.getLabel() - 1);
            }//end of if and else
            curGenome[0] = startCity;
            curGenomeCnt = 1;
            while (!temp.isEmpty()) {
                if (nCN == 2) {
                    city = getNextClosestNeighbor(curGenome[curGenomeCnt - 1], temp);
                    Iterator<City> it = temp.iterator();
                    City x;
                    while (it.hasNext()) {//find the city in temp and remove it
                        x = it.next();
                        if (x.getLabel() == city.getLabel()) {
                            temp.remove(x);
                            break;
                        }//end of if
                    }//end of loop
                } else {
                    city = temp.remove((int) (Math.random() * temp.size()));
                }//end of if and else
                curGenome[curGenomeCnt] = city;
                curGenomeCnt++;
            }//end of loop
            curGenome[curGenomeCnt] = startCity;//ending where it started
            if (specifiedStart && nCN == 2) {//all genomes will be the same if specifiedStart city has been specified and next closest neighboris used
                for (int j = 0; j < gen0.length; j++) {//copy the genome to every member in gen0
                    gen0[j] = new Member(curGenome);
                }//end of loop
                bestMemberI = 0;
                break;
            } else {
                gen0[i] = new Member(curGenome);
            }//end of else
            curDist = gen0[i].getDistance();
            if (i == 0) {
                curBestDist = curDist;
            } else if (curDist < curBestDist) {
                curBestDist = curDist;
                bestMemberI = i;
            }//end of if and else
        }//end of loop
        startCity = gen0[bestMemberI].getGenome()[0];
        return gen0;
    }//end of method

    public static City getNextClosestNeighbor(City city1, ArrayList<City> list) {
        City closestCity = list.get(0), nextCity;
        double x1 = city1.getX(), y1 = city1.getY(), x2, y2, distance, bestDistance = Math.sqrt((Math.pow((closestCity.getX() - x1), 2) + Math.pow((closestCity.getY() - y1), 2)));
        for (int i = 1; i < list.size(); i++) {
            nextCity = list.get(i);
            if (nextCity.getLabel() != city1.getLabel()) {
                x2 = nextCity.getX();
                y2 = nextCity.getY();
                distance = Math.sqrt((Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2)));
                if (distance < bestDistance) {
                    bestDistance = distance;
                    closestCity = nextCity;
                }//end of if
            }//end of if
        }//end of loop
        return closestCity;
    }//end of method

    public static Member[] getNextGeneration(Member[] gen, int popSize, double crossRate, double mutationRate, boolean specifiedStart) {
        Member[] nextGen = new Member[popSize], children;// = new Member[2];
        Member parent1 = new Member(new City[0]), parent2 = new Member(new City[0]);
        double num1, num2, begOfRange, curWorstDist = 0, curDist, curBestDist;
        double[] normDistances = normalizeDistances(gen);
        int curWorstI = 0, curBestI = 0;
        for (int i = 0; i < popSize; i += 2) {//fills in nextGeneration doing crossover
            if (i != popSize - 1) {//if there's more than 1 spot remaining in nextGeneration
                num1 = Math.random();

                begOfRange = 0;
                for (int j = 0; j < popSize; j++) {//choosing parent 1
                    if (begOfRange < num1 && num1 <= normDistances[j]) {
                        parent1 = gen[j];
                        break;
                    } else {
                        begOfRange = normDistances[j];
                    }//end of if and else
                }//end of nested loop
                begOfRange = 0;
                while (true) {
                    num2 = Math.random();
                    for (int j = 0; j < popSize; j++) {//choosing parent 2
                        if (begOfRange < num2 && num2 <= normDistances[j]) {
                            parent2 = gen[j];
                            break;
                        } else {
                            begOfRange = normDistances[j];
                        }//end of if and else
                    }//end of nested loop
                    if (parent1.getDistance() != parent2.getDistance()) {
                        break;
                    }//end of if
                }//end of loop
                if (Math.random() <= crossRate) {
                    children = crossover(parent1.getGenome(), parent2.getGenome(), specifiedStart);
                    nextGen[i] = children[0];
                    nextGen[i + 1] = children[1];
                } else {
                    nextGen[i] = parent1;
                    nextGen[i + 1] = parent2;
                }//end of if and else
                if (Math.random() <= mutationRate) {//doing mutation as they go into nextGen
                    nextGen[i] = mutation(nextGen[i].getGenome(), specifiedStart);
                }//end of if
                if (Math.random() <= mutationRate) {
                    nextGen[i + 1] = mutation(nextGen[i + 1].getGenome(), specifiedStart);
                }//end of if
            } else {//chooses a member from last generation based on distance for the last spot in next generation
                begOfRange = 0;
                num1 = Math.random();
                for (int j = 0; j < popSize; j++) {//choosing member to put in last spot
                    if (begOfRange < num1 && num1 <= normDistances[j]) {
                        nextGen[i] = gen[j];
                        break;
                    } else {
                        begOfRange = normDistances[j];
                    }//end of if and else
                }//end of nested loop
            }//end of if and else
        }//end of loop should have next generation before mutation and saving best
        curBestDist = nextGen[0].getDistance();
        for (int i = 1; i < gen.length; i++) {//finding index the best member
            curDist = nextGen[i].getDistance();
            if (curDist < curBestDist) {
                curBestDist = curDist;
                curBestI = i;
            }//end of if
        }//end of loop
        if (nextGen[curBestI].getDistance() <= gen[bestMemberI].getDistance()) {//best in next gen is already better or the same as best from last gen
            bestMemberI = curBestI;
        } else {//find the worst member and replace it with the best member from last gen
            curWorstDist = 0;
            for (int i = 0; i < gen.length; i++) {
                curDist = nextGen[i].getDistance();
                if (curDist > curWorstDist) {
                    curWorstDist = curDist;
                    curWorstI = i;
                }//end of if 
            }//end of loop
            nextGen[curWorstI] = gen[bestMemberI];//replacing worst in current gen with best from last gen
            bestMemberI = curWorstI;
        }//end of if and else
        return nextGen;
    }//end of method

    public static Member mutation(City[] genome, boolean specifiedStart) {
        City[] genomeCopy = Arrays.copyOf(genome, genome.length);
        ArrayList<City> genomeList = new ArrayList(), displaced = new ArrayList();
        for (int i = 0; i < genome.length - 1; i++) {
            genomeList.add(genomeCopy[i]);
        }//end of loop
        Random getInt = new Random();
        int i1, i2, i3, start, end = genome.length - 1, x;
        if (!specifiedStart) {
            start = 0;
        } else {
            start = 1;
        }//end of if and else
        i1 = getInt.nextInt(end - 1) + start;
        i2 = getInt.nextInt(end - 1) + start;
        if (i2 < i1) {
            x = i1;
            i1 = i2;
            i2 = x;
        }//end of if
        for (int i = i1; i <= i2; i++) {//pulling selected section i1-i2 out
            displaced.add(genomeList.remove(i1));
        }//end of loop
        if (Math.random() > .5) {
            ArrayList<City> dispCopy = new ArrayList(displaced);
            displaced.clear();
            for (int i = dispCopy.size() - 1; i >= 0; i--) {
                displaced.add(dispCopy.get(i));
            }//end of loop
        }//end of loop
        i3 = getInt.nextInt(genomeList.size() + 1);
        genomeList.addAll(i3, displaced);//putting selected section back in at i3
        for (int i = start; i < genomeList.size(); i++) {
            genomeCopy[i] = genomeList.get(i);
        }//end of loop
        if (!specifiedStart) {
            genomeCopy[genomeCopy.length - 1] = genomeCopy[0];
        }//end of if
        return new Member(genomeCopy);
    }//end of method

    public static Member[] crossover(City[] parent1Genome, City[] parent2Genome, boolean specifiedStart) {
        City[] child1Genome = Arrays.copyOf(parent1Genome, parent1Genome.length), child2Genome = Arrays.copyOf(parent2Genome, parent2Genome.length);
        City curCityParent1, curCityParent2, cityToMove, child1StartCity = parent1Genome[0], child2StartCity = parent2Genome[0];
        double range = (child1Genome.length - 1) - 1;//range will be the index before the last one minus the 2nd index # because the first and last indexes have the starting city
        int crossPoint = (int) (Math.random() * range) + 1;
        //System.out.println("crosspoint: " + crossPoint);
        if (!specifiedStart) {//handling the specifiedStart and end city if the specifiedStart wasnt specified
            cityToMove = child1Genome[0];
            child1Genome[0] = parent2Genome[0];
            child1Genome[child1Genome.length - 1] = parent2Genome[0];
            child1StartCity = child1Genome[0];
            for (int j = 1; j < parent2Genome.length - 1; j++) {
                if (child1Genome[j].getLabel() == child1Genome[0].getLabel()) {
                    child1Genome[j] = cityToMove;
                    break;
                }//end of if
            }//end of loop
            cityToMove = child2Genome[0];
            child2Genome[0] = parent1Genome[0];
            child2Genome[child1Genome.length - 1] = parent1Genome[0];
            child2StartCity = child2Genome[0];
            for (int j = 1; j < parent2Genome.length - 1; j++) {
                if (child2Genome[j].getLabel() == child2Genome[0].getLabel()) {
                    child2Genome[j] = cityToMove;
                    break;
                }//end of if
            }//end of loop
        }//end of if
        for (int i = 1; i <= crossPoint; i++) {
            curCityParent1 = parent1Genome[i];
            curCityParent2 = parent2Genome[i];
            cityToMove = child1Genome[i];
            child1Genome[i] = curCityParent2;//1 recieves city from 2
            if (curCityParent2.getLabel() != child1StartCity.getLabel()) {
                for (int j = 1; j < parent2Genome.length; j++) {//find where city that was received is in the receiving genome and replace it with cityToMove
                    if (j != i && child1Genome[j].getLabel() == curCityParent2.getLabel()) {
                        child1Genome[j] = cityToMove;
                        break;
                    }//end of if
                }//end of loop
            } else if (!specifiedStart) {
                child1Genome[0] = cityToMove;
                child1Genome[child1Genome.length] = cityToMove;
                child1StartCity = child1Genome[0];
            }//end of if and else
            cityToMove = child2Genome[i];
            if (curCityParent1.getLabel() != child2StartCity.getLabel()) {
                cityToMove = child2Genome[i];
                child2Genome[i] = curCityParent1;//2 recieves city from 1
                for (int j = 0; j < parent2Genome.length; j++) {
                    if (j != i && child2Genome[j].getLabel() == curCityParent1.getLabel()) {
                        child2Genome[j] = cityToMove;
                        break;
                    }//end of if
                }//end of loop
            } else if (!specifiedStart) {
                child2Genome[0] = cityToMove;
                child2Genome[child2Genome.length] = cityToMove;
                child2StartCity = child2Genome[0];
            }//end of if and else
        }//end of loop                
        return new Member[]{new Member(child1Genome), new Member(child2Genome)};
    }//end of method

    public static double[] normalizeDistances(Member[] gen) {
        double totalDistance = 0, range = 0, scaledDistance, distanceToTotalDistance, totalDistanceToTotalDistance = 0;
        double[] normDistances = new double[gen.length], distancesToTotalDistance = new double[gen.length];
        for (int i = 0; i < gen.length; i++) {//gets the total distance
            totalDistance += gen[i].getDistance();
        }//end of loop
        for (int i = 0; i < gen.length; i++) {
            distancesToTotalDistance[i] = totalDistance - gen[i].getDistance();
            totalDistanceToTotalDistance += distancesToTotalDistance[i];
        }//end of loop
        for (int i = 0; i < gen.length; i++) {
            //System.out.println("distance " + gen[i].getDistance() + " total distance " + totalDistance);
            scaledDistance = distancesToTotalDistance[i] / totalDistanceToTotalDistance;
            //scales based on how far the sistance is from totalDistance to give lower distances bigger ranges.
            normDistances[i] = range + scaledDistance;
            range += scaledDistance;
        }//end of loop
        return normDistances;
    }//end of method

    public static Object[] changeGAParams(Object[] params) {
        while (true) {
            switch (input.getInteger(true, 1, 1, 8, "Which parameter would you like to change? Enter 1-5."
                    + "\n1.Population size\n2.Crossover rate\n3.Mutation Rate\n4.Specify a starting city"
                    + "\n5.Initialization of starting generation\n(Default = 1)")) {
                case 1:
                    params[0] = input.getInteger(true, 20, 1, 999999999, "Population size? (Default = 20)");
                    break;
                case 2:
                    params[1] = input.getDouble(true, 1, 0, 1, "Crossover rate? (0-1. .8-1 is recommended. Default = 1)");
                    break;
                case 3:
                    params[2] = input.getDouble(true, .02, 0, 1, "Mutation rate? (0-1. .01-.05 is recommended. Default = .02)");
                    break;
                case 4:
                    if (input.getCharacter(true, 'N', "YN", 1, "Specify a starting city? (Y/N. Default = N)") == 'Y') {
                        params[3] = true;
                        startCity = cityList.get(input.getInteger(true, 1, 1, cityList.size(), "City number to start at? (1-" + cityList.size() + ". Default = 1)") - 1);
                    } else {
                        params[3] = false;
                    }//end of if and else
                    break;
                case 5:
                    params[4] = input.getInteger(true, 1, 1, 2, "Create generation 0 (Enter 1 or 2):\n1. Randomly (Default)\n2. Next best neighbor");
                    //targetReached = false;
                    break;
            }//end of switch
            if (input.getCharacter(true, 'N', "YN", 1, "Change another parameter? (Y/N. Default = N)") == 'N') {
                break;
            }//end of if
        }//end of loop
        return params;
    }//end of method

    public static Object[] changeBellCurveParams(Object[] params) {
        int minBinSize, defaultBinSize;
        while (true) {
            switch (input.getInteger(true, 1, 1, 3, "Which parameter would you like to change? Enter 1-3."
                    + "\n1.Number of runs\n2.Generations per run and bin size\n3.Target distance")) {
                case 1:
                    params[0] = input.getInteger(true, (int) params[0], 1, 999999999, "Number of runs? (1-999999999. Default = " + params[0] + ")");

                    break;
                case 2:
                    params[1] = input.getInteger(true, (int) params[1], 1, 999999999, "Max number of generations per run? (1-999999999. Default = " + params[1] + ")");
                    minBinSize = (int) ((int) params[1] / 13.333333333333333333333333333333333333);
                    defaultBinSize = (int) ((int) params[1] / 5);
                    params[2] = input.getInteger(true, minBinSize, minBinSize, (int) params[1], "Histogram bin size? (Minimun size of " + Integer.toString(minBinSize) + " due to window width limitations. Max size is " + params[1] + ". Default = " + minBinSize + ")");
                    break;
                case 3:
                    params[3] = input.getDouble(true, (double) params[3], 1, 999999999, "Target distance? Choose a near optimal distance for good bell curve. (1-999999999. Default = " + params[3] + ")");
            }//end of switch
            if (input.getCharacter(true, 'N', "YN", 1, "Change another parameter? (Y/N. Default = N)") == 'N') {
                break;
            }//end of if
        }//end of loop
        return params;
    }//end of method
}//end of class
