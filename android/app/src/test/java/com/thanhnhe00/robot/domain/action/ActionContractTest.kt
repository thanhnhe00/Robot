package com.thanhnhe00.robot.domain.action

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ActionContractTest {

    private fun findRepoRoot(): File {
        val sysProp = System.getProperty("repo.root")
        if (!sysProp.isNullOrBlank()) {
            val dir = File(sysProp)
            if (File(dir, "ai/schemas/action_schema.json").exists()) {
                return dir
            }
        }
        var cur = File(System.getProperty("user.dir") ?: ".")
        repeat(5) {
            if (File(cur, "ai/schemas/action_schema.json").exists()) {
                return cur
            }
            cur = cur.parentFile ?: return@repeat
        }
        throw IllegalStateException("Không tìm thấy ai/schemas/action_schema.json")
    }

    @Test
    fun testKotlinContractMatchesJsonSchema() {
        val repoRoot = findRepoRoot()
        val schemaFile = File(repoRoot, "ai/schemas/action_schema.json")
        assertTrue("File action_schema.json phải tồn tại", schemaFile.exists())

        val rootJson = Json.parseToJsonElement(schemaFile.readText(Charsets.UTF_8)).jsonObject
        val definitions = rootJson["definitions"]!!.jsonObject
        val actionDef = definitions["action"]!!.jsonObject
        val properties = actionDef["properties"]!!.jsonObject
        val typeProp = properties["type"]!!.jsonObject
        val schemaActionTypes = typeProp["enum"]!!.jsonArray.map { it.jsonPrimitive.content }.toSet()

        // 1. Kiểm tra tập hợp các action được đăng ký phải khớp 100%
        val kotlinActionTypes = ActionValidator.REGISTRY.keys
        assertEquals("Danh sách action trong ActionValidator phải khớp 100% với action_schema.json", schemaActionTypes, kotlinActionTypes)

        // 2. Kiểm tra package whitelist của open_app từ schema
        val allOf = actionDef["allOf"]!!.jsonArray
        var schemaPackages: Set<String>? = null
        for (item in allOf) {
            val itemObj = item.jsonObject
            val ifObj = itemObj["if"]?.jsonObject
            val ifType = ifObj?.get("properties")?.jsonObject?.get("type")?.jsonObject?.get("const")?.jsonPrimitive?.content
            if (ifType == "open_app") {
                val thenObj = itemObj["then"]!!.jsonObject
                val thenParams = thenObj["properties"]!!.jsonObject["params"]!!.jsonObject
                val packageProp = thenParams["properties"]!!.jsonObject["package"]!!.jsonObject
                schemaPackages = packageProp["enum"]!!.jsonArray.map { it.jsonPrimitive.content }.toSet()
                break
            }
        }

        assertTrue("Phải trích xuất được package whitelist từ schema", schemaPackages != null)
        assertEquals("Package whitelist trong Kotlin phải khớp 100% với action_schema.json", schemaPackages, ActionValidator.ALLOWED_PACKAGES)

        // 3. Kiểm tra timeout các action không vượt ngưỡng an toàn
        assertEquals(1000L, ActionValidator.REGISTRY["get_time"]?.timeoutMs)
        assertEquals(1000L, ActionValidator.REGISTRY["get_battery"]?.timeoutMs)
        assertEquals(3000L, ActionValidator.REGISTRY["set_alarm"]?.timeoutMs)
        assertEquals(5000L, ActionValidator.REGISTRY["open_app"]?.timeoutMs)
        assertEquals(2000L, ActionValidator.REGISTRY["set_volume"]?.timeoutMs)
        assertEquals(5000L, ActionValidator.REGISTRY["move"]?.timeoutMs)
        assertEquals(1000L, ActionValidator.REGISTRY["stop"]?.timeoutMs)

        // 4. Kiểm tra trạng thái phần cứng (move và stop phải là STUB)
        assertEquals(HardwareStatus.STUB, ActionValidator.REGISTRY["move"]?.hardware)
        assertEquals(HardwareStatus.STUB, ActionValidator.REGISTRY["stop"]?.hardware)
        assertEquals(HardwareStatus.READY, ActionValidator.REGISTRY["get_time"]?.hardware)
    }
}
