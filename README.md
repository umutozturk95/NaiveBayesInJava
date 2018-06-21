# Data Structure
* HashMap<String, HashMap<String, HashMap<String, Integer>>> featuresOfTrainData<br/>
  The hashmap is used to store the feature vectors in the train dataset.<br/>
  This hasmap keeps the lexelt names with their sense ids and also has the context words in the sense ids of each lexelt name.
* HashMap<String, String> stopWords <br/>
  The hashmap is used to keep the stopwords in the stopwords file. So the stop words are detected by using this hashmap easily.<br/> 
 
# Important points about this application

* The lexelt name ,instance id and the context of sense id are extracted by using regex for both test data and train data.
* The all files are read  line by line.
* The words are collected according to window size  and if one word is in stopword list then this application removes the words from windows size list.
* The value of N is the total word number in the train dataset.
* The mixed feature vector(F1+F2) has the words and tags with index .
* The each sense id has one hashmap for storing words with tags. In other words, this hashmap keeps the items of mixed vectors(F1+F2).
* C(si) is the total word number of context in the given sense id/si in the training dataset.
* C(fj,si) is the number of occurrences of fj in the given sense id/si . Also fj is the count of a word in the given sense id/si.
* P(fj |w = si) equals to C(si)/N . N is total word number of train dataset.
* If the count of a word in the context of the given sense id/(si) is zero ,then P(fj |w = si) will equal to 1/N.
* Math.log probability is used to detect the maximum possible sense id for test data.
* When detecting the maximum possible sense id for test data, Double.NEGATIVE_INFINITY is used to initialize the max variable.
* The working time of this application is approximately 4 seconds.
