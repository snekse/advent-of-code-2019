import spock.lang.*

class IntCodeProcessor {

    private String FILE_ROOT = 'src/test/resources'

    def splitIntCode(fileName, cl = { it }) {
        File file = new File("$FILE_ROOT/$fileName")
        file.splitEachLine(',', {it.collect {i -> i as int} })
    }

    List<Integer> processIntCode(List<Integer> input, int startingIdx = 0){
        if (startingIdx > input.size() || input[startingIdx] == 99) {
            return input
        }

        List<Integer> newList = [] + input
        List<Integer> fragmentList = newList[startingIdx..(startingIdx+3)]
        int optCode = fragmentList[0]
        int v1 = input[fragmentList[1]]
        int v2 = input[fragmentList[2]]
        int updatePosition = fragmentList[3]

        newList[updatePosition] = calculateNewValueForOptCode(optCode, v1, v2, newList[updatePosition])

        return processIntCode(newList, startingIdx + 4)
    }

    private static int calculateNewValueForOptCode(int optCode, int v1, int v2, int currentValue) {
        if (optCode == 1) {
           return v1 + v2
        } else if (optCode == 2) {
            return v1 * v2
        } else {
            throw new RuntimeException("Unknown OptCode $optCode")
        }
    }
}

class Star01Spec extends Specification {

    IntCodeProcessor proc = new IntCodeProcessor()

    def "split int code reads a file and splits on commas"() {
        expect:
        [1,1,1,4,99,5,6,0,99] == proc.splitIntCode('test-input.txt')
    }

    @Unroll("#input -> #finalState")
    def "processing IntCodes works as expected"() {
        expect:
        finalState == proc.processIntCode(input)

        where:
        input                           || finalState
        [1,0,0,0,99]                    || [2,0,0,0,99]
        [2,3,0,3,99]                    || [2,3,0,6,99]
        [2,4,4,5,99,0]                  || [2,4,4,5,99,9801]
        [1,1,1,4,99,5,6,0,99]           || [30,1,1,4,2,5,6,0,99]
        [1,9,10,3,2,3,11,0,99,30,40,50] || [3500,9,10,70,2,3,11,0,99,30,40,50]
    }

    void "answer"() {
        String fileName = 'input.txt'
        def intCodeResult = proc.processIntCode(proc.splitIntCode(fileName))

        print "Final state is $intCodeResult"

        expect:
        intCodeResult == intCodeResult
    }
}
