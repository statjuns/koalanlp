package kr.bydelta.koala.etri

import com.beust.klaxon.Klaxon
import kr.bydelta.koala.POS
import kr.bydelta.koala.data.Sentence
import org.amshove.kluent.*
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.util.*

object KlaxonTest : Spek({
    describe("Json") {
        it("should deparse JSON string correctly") {
            val klaxon = Klaxon()
            val payload = klaxon.parse<ResultPayload>(POS::class.java.getResourceAsStream("/etri-sample.json"))

            payload `should not be` null

            if (payload != null) {
                payload.result.sentences.size `should be equal to` 2
                payload.result.sentences[0].morphemes.size `should be equal to` 45
                payload.result.sentences[0].senses.size `should be equal to` 38
                payload.result.sentences[0].words.size `should be equal to` 20
                payload.result.sentences[0].deps.size `should be equal to` 20
                payload.result.sentences[0].entities.size `should be equal to` 4
                payload.result.sentences[0].roles.size `should be equal to` 3

                var nullableSent: List<Sentence>? = null
                { nullableSent = payload.result.sentences.map { it.sentence } } `should not throw` AnyException

                val sentences = nullableSent!!

                sentences[0].getDependencies() `should not be` null
                sentences[0].getRoles() `should not be` null
                sentences[0].getEntities() `should not be` null

                sentences[0][0].getGovernorEdge() `should not be` null
                sentences[0][0].getGovernorEdge()?.governor `should equal` sentences[0][1]

                sentences[0][5].getArgumentRoles() `should not be` null
                sentences[0][5].getArgumentRoles()!!.map { it.argument } shouldContainSame sentences[0].subList(1, 5)

                sentences[0].getEntities()!!.map { it.surface } shouldContainSame listOf("중국", "북한", "군사지원 의무제", "유엔 안보리")
                sentences[0].getRoles()!!.size `should be equal to` 7
                sentences[0].getDependencies()!!.size `should be equal to` 20
            }
        }
    }
})

object ETRICommunicationTest : Spek({
    val API_KEY = System.getenv("ETRI_KEY")
    val testText = "중국 관영매체가 그동안 북한에 자제를 요구한 적은 있지만, 군사지원 의무제공 포기 가능성과 함께 유엔 안보리 제재안을 먼저 제시한 것은 이례적입니다."

    val klaxon = Klaxon()
    val expectedSentence = klaxon.parse<ResultPayload>(POS::class.java.getResourceAsStream("/etri-sample.json"))!!.result.sentences[0].sentence

    describe("Tagger") {
        it("should correctly communicate with ETRI API Service") {
            // 반복 요청을 막기 위해 적절한 시간동안 멈춥니다.
            Thread.sleep(1000 + Random().nextInt(10) * 100L)

            val tagger = Tagger(API_KEY)
            var result: Sentence? = null

            { result = tagger.tag(testText)[0] } `should not throw` AnyException

            val testResult = result!!

            testResult `should equal` expectedSentence
        }

        it("can do other analysis request") {
            // 반복 요청을 막기 위해 적절한 시간동안 멈춥니다.
            Thread.sleep(1000 + Random().nextInt(10) * 100L);

            { Tagger(API_KEY, "wsd_poly").tag(testText)[0] } `should not throw` AnyException
            { Tagger(API_KEY, "morp").tag(testText)[0] } `should not throw` AnyException
            { Tagger(API_KEY, "").tag(testText)[0] } `should throw` AssertionError::class
        }

        it("should correctly answer wrong API KEY") {
            // 반복 요청을 막기 위해 적절한 시간동안 멈춥니다.
            Thread.sleep(1000 + Random().nextInt(10) * 100L)

            val tagger = Tagger("");

            { tagger.tag(testText) } `should throw` APIException::class
        }
    }

    describe("Parser") {
        it("should correctly communicate with ETRI API Service") {
            // 반복 요청을 막기 위해 적절한 시간동안 멈춥니다.
            Thread.sleep(1000 + Random().nextInt(10) * 100L)

            val analyzer = Parser(API_KEY)
            var result: Sentence? = null

            { result = analyzer(testText)[0] } `should not throw` AnyException

            val testResult = result!!

            testResult `should equal` expectedSentence
            testResult.getDependencies() `should not be` null
            testResult.getDependencies()!! shouldContainSame expectedSentence.getDependencies()!!

            testResult.getEntities() `should not be` null
            testResult.getEntities()!! shouldContainSame expectedSentence.getEntities()!!
        }

        it("should correctly answer wrong API KEY") {
            // 반복 요청을 막기 위해 적절한 시간동안 멈춥니다.
            Thread.sleep(1000 + Random().nextInt(10) * 100L)

            val analyzer = Parser("");

            { analyzer(testText) } `should throw` APIException::class
        }
    }

    describe("EntityRecognizer") {
        it("should correctly communicate with ETRI API Service") {
            // 반복 요청을 막기 위해 적절한 시간동안 멈춥니다.
            Thread.sleep(1000 + Random().nextInt(10) * 100L)

            val analyzer = EntityRecognizer(API_KEY)
            var result: Sentence? = null

            { result = analyzer(testText)[0] } `should not throw` AnyException

            val testResult = result!!

            testResult `should equal` expectedSentence
            testResult.getEntities() `should not be` null
            testResult.getEntities()!! shouldContainSame expectedSentence.getEntities()!!
        }

        it("should correctly answer wrong API KEY") {
            // 반복 요청을 막기 위해 적절한 시간동안 멈춥니다.
            Thread.sleep(1000 + Random().nextInt(10) * 100L)

            val analyzer = EntityRecognizer("");

            { analyzer(testText) } `should throw` APIException::class
        }
    }

    describe("RoleLabeler") {
        it("should correctly communicate with ETRI API Service") {
            // 반복 요청을 막기 위해 적절한 시간동안 멈춥니다.
            Thread.sleep(1000 + Random().nextInt(10) * 100L)

            val analyzer = RoleLabeler(API_KEY)
            var result: Sentence? = null

            { result = analyzer(testText)[0] } `should not throw` AnyException

            val testResult = result!!

            testResult `should equal` expectedSentence
            testResult.getDependencies() `should not be` null
            testResult.getDependencies()!! shouldContainSame expectedSentence.getDependencies()!!

            testResult.getEntities() `should not be` null
            testResult.getEntities()!! shouldContainSame expectedSentence.getEntities()!!

            testResult.getRoles() `should not be` null
            testResult.getRoles()!! shouldContainSame expectedSentence.getRoles()!!
        }

        it("should correctly answer wrong API KEY") {
            // 반복 요청을 막기 위해 적절한 시간동안 멈춥니다.
            Thread.sleep(1000 + Random().nextInt(10) * 100L)

            val analyzer = RoleLabeler("");

            { analyzer(testText) } `should throw` APIException::class
        }
    }
})