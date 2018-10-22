package productdescription;

public enum CharacteristicType {
	featureType("Boolean Feature"), integerType("Integer Attribute"), doubleType("Double Attribute"), literalType("Literal Attribute");

	private String name;
	
	private CharacteristicType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
}
