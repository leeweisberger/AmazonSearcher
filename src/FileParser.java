
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
			while ((line = br.readLine()) != null)   {
				String[] itemArray = line.split(",");
				Map<String,Double> conditionMap = new HashMap<String, Double>();
				for(int i=1; i<itemArray.length; i+=2){
					String formattedCondition = ConditionConstants.getFileFromReadable(itemArray[i]);
					conditionMap.put(formattedCondition, Double.valueOf(itemArray[i+1]));
				}
				minPriceMap.put(itemArray[0], conditionMap);
				line = br.readLine();
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
}
