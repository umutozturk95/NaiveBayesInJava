
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Umut Ozturk
 */
public class WSDNaiveBayes {

    /*
    The following hashmap is used to store the feature vectors in the train dataset.
     */
    private HashMap<String, HashMap<String, HashMap<String, Integer>>> featuresOfTrainData;
    /*
    The following hashmap is used to keep the stopwords in the stopwords file.
     */
    private HashMap<String, String> stopWords;
    private int totalWordNumberInTrainData = 0;//This variable keeps the total word number in train dataset.
    private Stemmer stemmer;//This variable refers to Stemmer class.
    //The following constructor initializes the above variables.

    public WSDNaiveBayes() {
        featuresOfTrainData = new HashMap<String, HashMap<String, HashMap<String, Integer>>>();
        stopWords = new HashMap<String, String>();
        stemmer = new Stemmer();
    }
//The following triggers the main operations such as reading files, naive bayes etc...

    public void startWSD(String trainFileName, String testFileName, String outputFileName) {
        readStopWordFile();
        readTrainFile(trainFileName);
        readTestFile(testFileName, outputFileName);
    }
//The following function reads the stopwords and the inserts these stopwords into hashmap.

    public void readStopWordFile() {
        try {
            File file = new File("stopwords.txt");
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            while (line != null) {

                stopWords.put(line.trim(), "<unk>");
                line = br.readLine();
            }

            br.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    /*
    The following functions reads the test file  line by line.
     */
    public void readTestFile(String testFileName, String outputFileName) {
        try {

            File file = new File(testFileName);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            PrintWriter wt = new PrintWriter(new File(outputFileName));

            String line = br.readLine();
            Pattern pattern = null;
            Matcher matcher = null;
            String lextName = "", instanceID = "";

            while (line != null) {
                line = line.trim();
                if (line.equals("") == false && line.length() > 1) {
                    pattern = Pattern.compile("<lexelt item=\"(.*?)\">");
                    matcher = pattern.matcher(line);

                    if (matcher.find()) {
                        //The value of lexelt is stored into the following variable.
                        lextName = matcher.group(1).trim();

                    }

                    pattern = Pattern.compile("<instance id=\"(.*?)\">");
                    matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        //The value of instanceID is stored into following variable.
                        instanceID = matcher.group(1).trim();
                    }

                    if (Pattern.matches("(.*?)<head>(.*?)</head>(.*?)", line)) {
                        //The following function is called to extract the text between <context> and </context>.
                        extractWordsAndTagsForTestData(line.trim(), instanceID, lextName, wt);
                    }
                }
                line = br.readLine();
            }

            br.close();
            wt.flush();
            wt.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //  The following function extracts the words and tags by using window size(3).
    public void extractWordsAndTagsForTestData(String text, String instanceID, String lextName, PrintWriter wt) {

        int foundFirstHead = 0, foundLastHead = 0;
        String[] words = null;
        Pattern pattern = null;
        Matcher matcher = null;
        int count = 1;
        String tagKey = "";

        pattern = Pattern.compile("<p=\"(.*?)\"/>");
        ArrayList<String> features = new ArrayList<String>();
        words = text.split("\\s+");
        //The following code fragment detects the first index of <head> and </head>.
        for (int i = 0; i < words.length; i++) {
            if (words[i].contains("<head>")) {
                foundFirstHead = i - 1;
            } else if (words[i].contains("</head>")) {
                foundLastHead = i + 1;
                break;
            }

        }
        /*
        The while loop is used to detect the left side words by using the above first index of <head>.
        Also The stop words are detected,then these detected stop words are not included into the set of left side words.
        Also Porter stemmer algorithm is used to detect the stemming of the word.
         */
        while (foundFirstHead > 0 && count <= 3) {

            String word = words[foundFirstHead - 1].trim();
            if (stopWords.containsKey(word) == false) {
                matcher = pattern.matcher(words[foundFirstHead].trim());
                if (matcher.find()) {
                    words[foundFirstHead] = matcher.group(1).trim();
                }
                word = stemmer.findStem(word).trim();
                tagKey = "(" + words[foundFirstHead] + "," + (-1 * count) + ")";
                features.add(tagKey);
                features.add(word);
            }
            foundFirstHead = foundFirstHead - 2;
            count++;

        }
        count = 1;
        /*
        The while loop is used to detect the right side words by using the above first index of </head>.
        Also The stop words are detected,then these detected stop words are not included into the set of right side words.
        Also Porter stemmer algorithm is used to detect the stemming of the word.
         */

        while (foundLastHead < words.length && count <= 3) {
            String word = words[foundLastHead].trim();
            if (stopWords.containsKey(word) == false) {
                matcher = pattern.matcher(words[foundLastHead + 1].trim());
                if (matcher.find()) {
                    words[foundLastHead + 1] = matcher.group(1).trim();
                }
                word = stemmer.findStem(word).trim();
                tagKey = "(" + words[foundLastHead + 1] + "," + count + ")";
                features.add(word);
                features.add(tagKey);
            }
            count++;
            foundLastHead = foundLastHead + 2;
        }
        //The following funtions applies the naive bayes.
        findSenseByUsingNaiveBayes(features, instanceID, lextName, wt);

    }

    //The following function reads the train file line by line.
    public void readTrainFile(String trainFileName) {
        try {

            File file = new File(trainFileName);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            Pattern pattern = null;
            Matcher matcher = null;
            String lextName = "", senseID = "";
            int contextControl = 0;
            while (line != null) {
                line = line.trim();
                if (line.equals("") == false && line.length() > 1) {

                    pattern = Pattern.compile("<lexelt item=\"(.*?)\">");
                    matcher = pattern.matcher(line);

                    if (matcher.find()) {
                        //The lexelt name is extracted by using regex.
                        lextName = matcher.group(1).trim();

                    }

                    pattern = Pattern.compile("senseid=\"(.*?)\"/>");
                    matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        //The sense id is extracted by using regex.
                        senseID = matcher.group(1).trim();
                    }
                    //The <context> must be detected to find the first entry of text in one sense id.
                    if (line.equals("<context>")) {
                        contextControl = 1;

                    }
                    //The </context> must be detected to find the end of text in one sense id.
                    if (line.equals("</context>")) {
                        contextControl = 0;
                    }
                    if (contextControl == 1 && line.equals("<context>") == false) {
                        //The sentences with <head></head> are processed for window size.
                        if (Pattern.matches("(.*?)<head>(.*?)</head>(.*?)", line)) {

                            if (featuresOfTrainData.get(lextName) == null) {
                                featuresOfTrainData.put(lextName, new HashMap<String, HashMap<String, Integer>>());

                            }
                            //The following functions is used to extract the words according to window size.
                            extractWordsAndTagsForTrainData(line.trim(), senseID, lextName);
                        }
                        //The total word number is calculated for each sentences.
                        totalWordNumberInTrainData += line.replaceAll("<p=\"(.*?)\"/>", "").trim().split("\\s+").length;

                    }
                }
                line = br.readLine();
            }
            br.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /*
    The following function extracts the words and tags by using window size(3).
     */
    public void extractWordsAndTagsForTrainData(String text, String senseID, String lextName) {
        int foundFirstHead = 0, foundLastHead = 0;
        String[] words = null;
        Pattern pattern = null;
        Matcher matcher = null;
        int count = 1;
        String tagKey = "";
        pattern = Pattern.compile("<p=\"(.*?)\"/>");
        words = text.split("\\s+");
        //The following code fragment detects the first index of <head> and </head>.
        for (int i = 0; i < words.length; i++) {
            if (words[i].contains("<head>")) {
                foundFirstHead = i - 1;
            } else if (words[i].contains("</head>")) {
                foundLastHead = i + 1;
                break;
            }

        }
        //If the hashmap of sense id does not take place,then it will be created. 
        if (featuresOfTrainData.get(lextName).get(senseID) == null) {
            featuresOfTrainData.get(lextName).put(senseID, new HashMap<String, Integer>());
        }

        HashMap<String, Integer> featureCount = featuresOfTrainData.get(lextName).get(senseID);

        /*
        The while loop is used to detect the left side words by using the above first index of <head>.
        Also The stop words are detected,then these detected stop words are not included into the set of left side words.
        Also Porter stemmer algorithm is used to detect the stemming of the word.
        Finally the left side words are inserted into the hashmap of train dataset.
         */
        while (foundFirstHead > 0 && count <= 3) {

            String word = words[foundFirstHead - 1].trim();
            if (stopWords.containsKey(word) == false) {
                matcher = pattern.matcher(words[foundFirstHead].trim());
                if (matcher.find()) {
                    words[foundFirstHead] = matcher.group(1).trim();
                }

                word = stemmer.findStem(word).trim();
                int countOfFeature = featureCount.containsKey(word) ? featureCount.get(word) : 0;
                featureCount.put(word, countOfFeature + 1);

                tagKey = "(" + words[foundFirstHead] + "," + (-1 * count) + ")";
                countOfFeature = featureCount.containsKey(tagKey) ? featureCount.get(tagKey) : 0;
                featureCount.put(tagKey, countOfFeature + 1);
            }
            foundFirstHead = foundFirstHead - 2;
            count++;

        }
        count = 1;
        /*
        The while loop is used to detect the right side words by using the above first index of </head>.
        Also The stop words are detected,then these detected stop words are not included into the set of right side words.
        Also Porter stemmer algorithm is used to detect the stemming of the word.
        Finally the right side words are inserted into the hashmap of train dataset.
         */
        while (foundLastHead < words.length && count <= 3) {
            String word = words[foundLastHead].trim();
            if (stopWords.containsKey(word) == false) {
                matcher = pattern.matcher(words[foundLastHead + 1].trim());
                if (matcher.find()) {
                    words[foundLastHead + 1] = matcher.group(1).trim();
                }

                word = stemmer.findStem(word).trim();
                int countOfFeature = featureCount.containsKey(word) ? featureCount.get(word) : 0;
                featureCount.put(word, countOfFeature + 1);
                //The tag is inserted into hashmap such as (NN,-3)
                tagKey = "(" + words[foundLastHead + 1] + "," + count + ")";
                countOfFeature = featureCount.containsKey(tagKey) ? featureCount.get(tagKey) : 0;
                featureCount.put(tagKey, countOfFeature + 1);
            }
            count++;
            foundLastHead = foundLastHead + 2;

        }
    }

    /*
    The following function is used to apply the naive bayes theorem.
    The given arraylist as parameter is used to fetch the test words of window size.
    The 
     */
    public void findSenseByUsingNaiveBayes(ArrayList<String> testFeatures, String instanceID, String lextName, PrintWriter wt) {
        //The Double.NEGATIVE_INFINITY is used to detect the sense id  which has maximum probability for all test words. 
        double max = Double.NEGATIVE_INFINITY, numerator = 0.0, divider = 0.0, totalProb = 0.0, senseProb = 0.0;
        int wordNumberInSense = 0;
        String maxSense = "";
        //The following loop is used to scan the all sense ids. 
        for (Map.Entry<String, HashMap<String, Integer>> senseentry : featuresOfTrainData.get(lextName).entrySet()) {
            String senseKey = senseentry.getKey();
            HashMap<String, Integer> counts = featuresOfTrainData.get(lextName).get(senseKey);
            totalProb = 0.0;
            //The following variable keeps the total word number of one sense id.
            wordNumberInSense = findTotalWordNumberInSense(lextName, senseKey);
            //The following loop scans the words of test arraylist.
            for (int i = 0; i < testFeatures.size(); i++) {
                numerator = counts.containsKey(testFeatures.get(i)) ? counts.get(testFeatures.get(i)) : 1;
                divider = counts.containsKey(testFeatures.get(i)) ? wordNumberInSense : totalWordNumberInTrainData;
                //The probablity of test word is calculated by using Math.log function.
                double prob = numerator / divider;
                totalProb += (Math.log(prob) / Math.log(2));
            }
            //P(si) is calculated .In other words, the probability of one sense id is calculated.
            senseProb = (double) wordNumberInSense / (double) totalWordNumberInTrainData;
            totalProb += (Math.log(senseProb) / Math.log(2));

            if (totalProb > max) {
                max = totalProb;
                maxSense = senseKey;
            }

        }
        //The maximum probability sense id with instance id are written into output file.
        wt.println(instanceID + "  " + maxSense);
    }
//The following function is used to find the total word number in one sense id.

    public int findTotalWordNumberInSense(String lextName, String senseID) {
        int count = 0;
        for (Map.Entry<String, Integer> entry : featuresOfTrainData.get(lextName).get(senseID).entrySet()) {
            String wordKey = entry.getKey();
            count += featuresOfTrainData.get(lextName).get(senseID).get(wordKey);
        }
        return count;
    }

}
