package builder.pattern;

class Employee {

	private String name;
	private int age;
	
	private Employee(EmployeeBuilder employeeBuilder) {
		this.name = employeeBuilder.name;
		this.age = employeeBuilder.age;
	}

	public String getName() {
		return name;
	}

	public int getAge() {
		return age;
	}
	
	@Override
	public String toString() {
		return "Employee [name=" + name + ", age=" + age + "]";
	}

	static class EmployeeBuilder {
		private String name;
		private int age;
		
		public EmployeeBuilder() {	
			
		}

		public EmployeeBuilder setName(String name) {
			this.name = name;
			return this;
		}

		public EmployeeBuilder setAge(int age) {
			this.age = age;
			return this;
		}
		
		public Employee build() {
			return new Employee(this);
		}	
	}
}
