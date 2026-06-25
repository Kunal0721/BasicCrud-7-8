package com.example.crud.dto;

import lombok.Data;

@Data
public class IssueRequest {
    private Long studentId;
    private Long bookId;
    private Integer durationDays; // default to 14 days if not specified
}
