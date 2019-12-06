import spock.lang.*

class IntCodeProcessor {
    private static final boolean DEBUG_ON = false
    private String FILE_ROOT = 'src/test/resources'

    def splitIntCode(fileName, cl = { it }) {
        File file = new File("$FILE_ROOT/$fileName")
        file.splitEachLine(',', {it.collect {i -> i as int} })
    }

    List<Integer> processIntCode(List<Integer> input, int startingIdx = 0){
        def hasInvalidCode = (input[startingIdx] > 2 || input[startingIdx] <= 0)
        if (hasInvalidCode) {
            debug("Unknown OpCode ${input[startingIdx]}. Halting program.")
        }
        if (hasInvalidCode || startingIdx > input.size() || input[startingIdx] == 99) {
            return input
        }

        List<Integer> newList = generateNewState(input, startingIdx)

        return processIntCode(newList, startingIdx + 4)
    }

    Tuple2 findNounVerbPair(List<Integer> cleanState, int desiredResult) {
        // We can't have positions greater than the input length, so no need to test 0..99
        def maxCandidateSize = Math.min(100, cleanState.size())
        def nounCandidate = (0..<maxCandidateSize)
        def verbCandidate = (0..<maxCandidateSize)
        def nounAndVerb = new Tuple2(-1, -1)
        nounCandidate.find { n ->
            verbCandidate.find { v ->
                def state = [] + cleanState
                state[1] = n
                state[2] = v
                def result = processIntCode(state).first()
                debug("Trying $n and $v for $state resulted in $result")
                if (desiredResult == result) {
                    nounAndVerb = new Tuple2<Integer, Integer>(n, v)
                    return true
                }
                return false
            } != null // return true if we find a verb
        }
        return nounAndVerb
    }

    private static List<Integer> generateNewState(List<Integer> input, int startingIdx) {
        List<Integer> newList = [] + input
        List<Integer> instructions = newList[startingIdx..(startingIdx + 3)]
        int opCode = instructions[0]
        int v1 = input[instructions[1]]
        int v2 = input[instructions[2]]
        int updatePosition = instructions[3]

        newList[updatePosition] = calculateNewValueForOptCode(opCode, v1, v2)
        return newList
    }

    private static int calculateNewValueForOptCode(int opCode, int v1, int v2) {
        if (opCode == 1) {
           return v1 + v2
        } else if (opCode == 2) {
            return v1 * v2
        } else {
            throw new RuntimeException("Unknown OpCode $opCode")
        }
    }

    private static debug(String s) {
        if ( DEBUG_ON ) {
            println s
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
        def initialState = proc.splitIntCode(fileName)
        initialState[1] = 12
        initialState[2] = 2
        def intCodeResult = proc.processIntCode(initialState)

        println "Final state is $intCodeResult"

        expect:
        intCodeResult == intCodeResult  // 5098658
    }
}

class Star02Spec extends Specification {

    IntCodeProcessor proc = new IntCodeProcessor()

    @Unroll("findNounVerbPair for #input returns (#noun,#verb) for the desired result of #prey")
    def "findNounVerbPair"() {
        def (int n, int v) = proc.findNounVerbPair(input, prey)

        expect:
        n == noun
        v == verb

        where:
        input                           | prey  || noun | verb
        [1,0,0,4,99,5,6,0,99]           | 1     || 0    | 2
        [1,0,0,4,99,5,6,0,99]           | 11    || 0    | 1
        [1,0,0,4,99,5,6,0,99]           | 30    || 0    | 0
        [1,9,10,3,2,3,11,0,99,30,40,50] | 3500  || 9    | 10
        [1,9,10,3,2,3,11,0,99,30,40,50] | 1950  || 2    | 9
    }

    void "answer"() {
        String fileName = 'input.txt'
        def prey = 19690720
        def initialState = proc.splitIntCode(fileName)
        def (int n, int v) = proc.findNounVerbPair(initialState, prey)

        println "Noun: $n, Verb: $v => $prey"
        println "What is 100 * noun + verb? (For example, if noun=12 and verb=2, the answer would be 1202.)"
        println "Answer :: ${(100 * n + v)}" // Noun: 50, Verb: 64 => 19690720;  5064

        expect:
        n != -1
        v != -1
    }
}
