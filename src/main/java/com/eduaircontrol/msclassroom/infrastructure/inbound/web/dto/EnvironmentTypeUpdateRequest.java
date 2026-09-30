package com.eduaircontrol.msclassroom.infrastructure.inbound.web.dto;

public record EnvironmentTypeUpdateRequest(
        String code,
        String name,
        String description) {
}
