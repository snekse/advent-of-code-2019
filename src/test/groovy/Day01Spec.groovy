import spock.lang.*

class FuelCalculator {

    private String FILE_ROOT = 'src/test/resources'

    static int fuelForMass(int mass) {
        return (mass / 3) - 2
    }

	static int fuelForMassAndFuel(int mass) {
		int fuelForMassAmt = fuelForMass(mass)
		int fuelForFuelAmt = fuelForFuel(fuelForMassAmt)
		return fuelForMassAmt + fuelForFuelAmt
	}

	static int fuelForFuel(int initialFuel) {
		int additionalFuelNeeded = fuelForMass(initialFuel)
		return additionalFuelNeeded <= 0 ? 0 : additionalFuelNeeded + fuelForFuel(additionalFuelNeeded)
	}

    def collectFile(fileName, cl = { it }) {
        File file = new File("$FILE_ROOT/$fileName")
        file.collect { cl(it as int) } // we only handle numeric values
    }

    def sumFile(fileName, cl = { fuelForMass(it) } ) {
        collectFile(fileName, { cl(it) }).sum()
    }

}

class Star01Spec extends Specification {

    FuelCalculator calc = new FuelCalculator()

    @Unroll("A mass of #mass requires #fuel fuel")
    void "find fuel requirement"() {
        when:
        def result = FuelCalculator.fuelForMass(mass)

        then:
        result == fuel

        where:
        mass   || fuel
        12     || 2
        13     || 2
        14     || 2
        1969   || 654
        100756 || 33583
    }

    void "collectFile can read and process every line of the file"() {
        expect:
        [12, 100756, 1969, 15] == calc.collectFile('test-input.txt')
    }

    void "sumFile adds all of the values in a file"() {
        long expected = (2 + 33583 + 654 + 3)
        long total = calc.sumFile('test-input.txt')

        expect:
        expected == total
    }

    void "answer"() {
        String fileName = 'input.txt'
        long total = calc.sumFile(fileName) //3391707

        print "Total for $fileName is $total"

        expect:
        total == total
    }
}

class Star02Spec extends Specification {

	FuelCalculator calc = new FuelCalculator()

	@Unroll("A mass of #mass requires #fuel fuel for both the mass and extra fuel")
	void "find fuel requirement for mass and fuel"() {
		when:
		def result = FuelCalculator.fuelForMassAndFuel(mass)

		then:
		result == fuel

		where:
		mass   || fuel
		14     || 2
		1969   || 966
		100756 || 50346
	}

    void "answer"() {
        String fileName = 'input.txt'
        long total = calc.sumFile(fileName, {FuelCalculator.fuelForMassAndFuel(it)})

        print "Total for $fileName is $total"

        expect:
        total == total
    }
}
