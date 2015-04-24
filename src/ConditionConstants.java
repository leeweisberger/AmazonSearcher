


public class ConditionConstants {
	public static final String CHOOSE = "Choose";
	public enum Condition {

		ACCEPTABLE("Acceptable", "Used-Acceptable"),
		GOOD("Good","Used-Good"),
		VERY_GOOD("Very Good","Used-VeryGood"),
		LIKE_NEW("Like New", "Used-LikeNew");

		private String readableName;
		private String fileName;

		Condition(String readableName, String fileName) {
			this.readableName = readableName;
			this.fileName = fileName;
		}

		public String getReadableName() { 
			return readableName; 
		}

		public String getFileName(){
			return fileName;
		}
	}
	public static String[] getReadableConditionsList(){
		String[] conditionsList = new String[Condition.values().length+1];
		for(int i=0; i<Condition.values().length; i++){
			conditionsList[i+1] = Condition.values()[i].readableName;
		}
		conditionsList[0] = CHOOSE;
		return conditionsList;
	}

	public static String getFileFromReadable(String readableName){
		for(Condition condition : Condition.values()){
			if(condition.getReadableName().equals(readableName))
				return condition.getFileName();
		}
		return "";
	}

	public static String getReadableFromFile(String fileName){
		for(Condition condition : Condition.values()){
			if(condition.getFileName().equals(fileName))
				return condition.getReadableName();
		}
		return "";
	}
}
