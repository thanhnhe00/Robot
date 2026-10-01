"""Test đồng bộ action_schema.json ↔ action_registry.py.

ADR-0001 coi action_schema.json là nguồn sự thật. Test này bảo vệ
hai bên không bị lệch khi sửa một bên mà quên cập nhật bên kia.
"""

import json
from pathlib import Path

import pytest

from app.action_registry import (
    ALLOWED_PACKAGES,
    DIRECTION_VALUES,
    REGISTRY,
)

SCHEMA_PATH = Path(__file__).resolve().parents[2] / "ai" / "schemas" / "action_schema.json"


@pytest.fixture(scope="module")
def schema() -> dict:
    return json.loads(SCHEMA_PATH.read_text(encoding="utf-8"))


def _get_schema_action_types(schema: dict) -> set[str]:
    """Trích danh sách action type từ enum trong schema."""
    action_def = schema["definitions"]["action"]
    return set(action_def["properties"]["type"]["enum"])


def _get_schema_conditionals(schema: dict) -> dict[str, dict]:
    """Trích conditional (if/then) cho từng action type."""
    action_def = schema["definitions"]["action"]
    result = {}
    for cond in action_def.get("allOf", []):
        if_clause = cond.get("if", {})
        action_type = if_clause.get("properties", {}).get("type", {}).get("const")
        if action_type:
            then_clause = cond.get("then", {})
            params_schema = then_clause.get("properties", {}).get("params", {})
            result[action_type] = params_schema
    return result


class TestSchemaRegistrySync:
    """action_schema.json và REGISTRY phải đồng bộ."""

    def test_schema_file_exists(self):
        assert SCHEMA_PATH.exists(), f"Schema file không tồn tại: {SCHEMA_PATH}"

    def test_action_types_match(self, schema):
        """Danh sách action type trong schema phải khớp registry."""
        schema_types = _get_schema_action_types(schema)
        registry_types = set(REGISTRY.keys())
        assert schema_types == registry_types, (
            f"Lệch action types!\n"
            f"  Chỉ có trong schema: {schema_types - registry_types}\n"
            f"  Chỉ có trong registry: {registry_types - schema_types}"
        )

    def test_every_action_has_conditional(self, schema):
        """Mỗi action type trong schema phải có if/then conditional."""
        conditionals = _get_schema_conditionals(schema)
        schema_types = _get_schema_action_types(schema)
        for action_type in schema_types:
            assert action_type in conditionals, (
                f"Action '{action_type}' có trong schema enum nhưng thiếu if/then conditional"
            )

    def test_required_params_match(self, schema):
        """Required params trong schema phải khớp registry."""
        conditionals = _get_schema_conditionals(schema)
        for action_type, spec in REGISTRY.items():
            params_schema = conditionals.get(action_type, {})
            schema_required = set(params_schema.get("required", []))
            registry_required = set(spec.required_params)
            assert schema_required == registry_required, (
                f"Lệch required params cho '{action_type}'!\n"
                f"  Schema: {schema_required}\n"
                f"  Registry: {registry_required}"
            )

    def test_optional_params_in_schema(self, schema):
        """Optional params trong registry phải tồn tại trong schema properties."""
        conditionals = _get_schema_conditionals(schema)
        for action_type, spec in REGISTRY.items():
            if not spec.optional_params:
                continue
            params_schema = conditionals.get(action_type, {})
            schema_props = set(params_schema.get("properties", {}).keys())
            for param in spec.optional_params:
                assert param in schema_props, (
                    f"Optional param '{param}' của '{action_type}' "
                    f"có trong registry nhưng không có trong schema"
                )

    def test_set_volume_level_range(self, schema):
        """Schema và registry phải nhất quán về range level."""
        conditionals = _get_schema_conditionals(schema)
        level_schema = conditionals["set_volume"]["properties"]["level"]
        assert level_schema["minimum"] == 0
        assert level_schema["maximum"] == 100
        assert level_schema["type"] == "integer"

    def test_open_app_whitelist_match(self, schema):
        """Whitelist package trong schema phải khớp registry."""
        conditionals = _get_schema_conditionals(schema)
        package_schema = conditionals["open_app"]["properties"]["package"]
        schema_packages = set(package_schema["enum"])
        assert schema_packages == ALLOWED_PACKAGES, (
            f"Lệch whitelist packages!\n"
            f"  Schema: {schema_packages}\n"
            f"  Registry: {ALLOWED_PACKAGES}"
        )

    def test_move_direction_values_match(self, schema):
        """Direction enum trong schema phải khớp registry."""
        conditionals = _get_schema_conditionals(schema)
        direction_schema = conditionals["move"]["properties"]["direction"]
        schema_directions = set(direction_schema["enum"])
        assert schema_directions == DIRECTION_VALUES, (
            f"Lệch direction values!\n"
            f"  Schema: {schema_directions}\n"
            f"  Registry: {DIRECTION_VALUES}"
        )

    def test_move_speed_range(self, schema):
        """Speed range trong schema phải khớp logic validate_move."""
        conditionals = _get_schema_conditionals(schema)
        speed_schema = conditionals["move"]["properties"]["speed"]
        assert speed_schema["minimum"] == 0
        assert speed_schema["maximum"] == 100
        assert speed_schema["type"] == "integer"

    def test_move_duration_range(self, schema):
        """Duration_ms range trong schema phải khớp logic validate_move."""
        conditionals = _get_schema_conditionals(schema)
        duration_schema = conditionals["move"]["properties"]["duration_ms"]
        assert duration_schema["minimum"] == 0
        assert duration_schema["maximum"] == 5000
        assert duration_schema["type"] == "integer"

    def test_set_alarm_time_pattern(self, schema):
        """Time pattern trong schema phải tồn tại."""
        conditionals = _get_schema_conditionals(schema)
        time_schema = conditionals["set_alarm"]["properties"]["time"]
        assert "pattern" in time_schema, "set_alarm time thiếu pattern trong schema"
        assert time_schema["type"] == "string"

    def test_no_params_actions_have_no_additional_properties(self, schema):
        """Action không cần params phải có additionalProperties: false."""
        conditionals = _get_schema_conditionals(schema)
        no_params = [name for name, spec in REGISTRY.items() if not spec.required_params and not spec.optional_params]
        for action_type in no_params:
            params_schema = conditionals.get(action_type, {})
            assert params_schema.get("additionalProperties") is False, (
                f"Action '{action_type}' không cần params nhưng schema "
                f"thiếu additionalProperties: false"
            )
