package com.eduaircontrol.msclassroom.infrastructure.inbound.web.dto;

import com.eduaircontrol.msclassroom.domain.model.RecordStatus;

public record CampusUpdateRequest(
        String code,
        String name,
        String city,
        RecordStatus status) {
}
