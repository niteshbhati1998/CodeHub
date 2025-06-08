package builder.pattern;

class Main {
	public static void main(String[] args) {
		Employee e = new Employee.EmployeeBuilder()
				.setName("Nitesh")
				.setAge(26)
				.build();
		System.out.println(e.toString());
	}
}