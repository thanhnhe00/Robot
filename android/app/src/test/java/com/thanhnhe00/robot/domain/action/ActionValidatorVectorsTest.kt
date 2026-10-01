package com.thanhnhe00.robot.domain.action

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ActionValidatorVectorsTest {

    private fun findRepoRoot(): File {
        val sysProp = System.getProperty("repo.root")
        if (!sysProp.isNullOrBlank()) {
            val dir = File(sysProp)
            if (File(dir, "ai/schemas/action_vectors.json").exists()) {
                return dir
            }
        }
        var cur = File(System.getProperty("user.dir") ?: ".")
        repeat(5) {
            if (File(cur, "ai/schemas/action_vectors.json").exists()) {
                return cur
            }
            cur = cur.parentFile ?: return@repeat
        }
        throw IllegalStateException("Không tìm thấy thư mục gốc repo Robot chứa ai/schemas/action_vectors.json")
    }

    @Test
    fun testAllVectorsAgainstKotlinValidator() {
        val repoRoot = findRepoRoot()
        val vectorFile = File(repoRoot, "ai/schemas/action_vectors.json")
        assertTrue("File action_vectors.json phải tồn tại", vectorFile.exists())

        val content = vectorFile.readText(Charsets.UTF_8)
        val jsonArray = Json.parseToJsonElement(content).jsonArray

        var passedCount = 0

        for (item in jsonArray) {
            val obj = item.jsonObject
            val id = obj["id"]!!.jsonPrimitive.content
            val rawAction = obj["action"]
            val expected = obj["expected"]

            val result = ActionValidator.validateJson(rawAction)

            if (expected == null || expected is JsonNull) {
                assertTrue(
                    "[$id] Kỳ vọng bị từ chối nhưng validator lại trả về Valid: $result",
                    result is ValidationResult.Rejected
                )
            } else {
                assertTrue(
                    "[$id] Kỳ vọng Valid nhưng validator lại từ chối: $result",
                    result is ValidationResult.Valid
                )
                val validAction = (result as ValidationResult.Valid).action
                val expectedObj = expected.jsonObject
                val expectedType = expectedObj["type"]!!.jsonPrimitive.content
                val expectedParams = expectedObj["params"]?.jsonObject ?: JsonObject(emptyMap())

                when (validAction) {
                    is ValidatedAction.GetTime -> {
                        assertEquals("[$id] Type phải là get_time", "get_time", expectedType)
                    }
                    is ValidatedAction.GetBattery -> {
                        assertEquals("[$id] Type phải là get_battery", "get_battery", expectedType)
                    }
                    is ValidatedAction.SetAlarm -> {
                        assertEquals("[$id] Type phải là set_alarm", "set_alarm", expectedType)
                        val expectedTime = expectedParams["time"]!!.jsonPrimitive.content
                        val formattedTime = String.format("%02d:%02d", validAction.hour, validAction.minute)
                        assertEquals("[$id] Giờ báo thức không khớp", expectedTime, formattedTime)

                        val expectedLabel = expectedParams["label"]?.jsonPrimitive?.content
                        assertEquals("[$id] Label không khớp", expectedLabel, validAction.label)
                    }
                    is ValidatedAction.OpenApp -> {
                        assertEquals("[$id] Type phải là open_app", "open_app", expectedType)
                        val expectedPkg = expectedParams["package"]!!.jsonPrimitive.content
                        assertEquals("[$id] Package không khớp", expectedPkg, validAction.packageName)
                    }
                    is ValidatedAction.SetVolume -> {
                        assertEquals("[$id] Type phải là set_volume", "set_volume", expectedType)
                        val expectedLevel = expectedParams["level"]!!.jsonPrimitive.intOrNull
                        assertEquals("[$id] Mức âm lượng không khớp", expectedLevel, validAction.percent)
                    }
                }
            }
            passedCount++
        }

        assertTrue("Phải chạy qua ít nhất 40 vectors", passedCount >= 40)
    }

    @Test
    fun testValidatorNeverThrowsOnUnexpectedInputs() {
        val malformedCases = listOf(
            null,
            JsonPrimitive("string"),
            JsonPrimitive(12345),
            JsonPrimitive(true),
            JsonArray(emptyList()),
            JsonObject(mapOf("type" to JsonPrimitive("   "))),
            JsonObject(mapOf("type" to JsonPrimitive("unknown_foo"))),
            JsonObject(mapOf("type" to JsonPrimitive("set_volume"), "params" to JsonPrimitive("1e999"))),
            JsonObject(mapOf("type" to JsonPrimitive("set_volume"), "params" to JsonArray(listOf(JsonPrimitive(50))))),
            JsonObject(mapOf("type" to JsonPrimitive("set_alarm"), "params" to JsonPrimitive(false)))
        )

        for (badInput in malformedCases) {
            val result = ActionValidator.validateJson(badInput)
            assertTrue("Input xấu không được crash và phải trả Rejected: $badInput", result is ValidationResult.Rejected)
        }
    }
}
