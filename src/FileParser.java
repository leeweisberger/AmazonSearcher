
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;




public class FileParser {

	public static Map<String, Map<String,Double>> readFileToMap(String file){
		Map<String, Map<String,Double>> minPriceMap = new HashMap<String, Map<String,Double>>();

		FileInputStream fstream=null;
		try {
			fstream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

		String line;

		//Read File Line By Line
		try {
			//skip the first line
			line=br.readLine();
			while ((line = br.readLine()) != null)   {
				String[] itemArray = line.split(",");
				if(itemArray.length==0)continue;
				String asin = Isbn13to10(itemArray[0]);
				Map<String,Double> conditionMap = new HashMap<String, Double>();
				for(int i=1; i<itemArray.length; i+=2){
					String formattedCondition = ConditionConstants.getFileFromReadable(itemArray[i]);
					conditionMap.put(formattedCondition, Double.valueOf(itemArray[i+1]));
				}


				minPriceMap.put(asin, conditionMap);
			}
			br.close();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		return minPriceMap;
	}

	private static String Isbn13to10(String isbn13)
	{
		isbn13 = isbn13.replace("-", "").replaceAll(" ", "").trim();
		if(isbn13.length()!=13)
			return isbn13;
		String isbn10 = isbn13.substring(3, 12);
		int checksum = 0;
		int val;
		int l = isbn10.length();
		for (int i = 0; i < l; i++) {
			val = isbn10.charAt(i) - '0';

			checksum += val * (10-i);
		}
		int mod = checksum % 11;
		char check= mod == 0 ? '0' : mod == 1 ? 'X' : (char)((11-mod) + '0');

		return isbn10+check;
	}
}
