package com.earthworm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_daily_budget")
public class AiDailyBudget {
    @Id
    @Column(name = "usage_date")
    private LocalDate usageDate;

    @Column(name = "reserved_output_tokens", nullable = false)
    private Long reservedOutputTokens = 0L;

    @Column(name = "request_count", nullable = false)
    private Long requestCount = 0L;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public LocalDate getUsageDate() { return usageDate; }
    public void setUsageDate(LocalDate usageDate) { this.usageDate = usageDate; }
    public Long getReservedOutputTokens() { return reservedOutputTokens; }
    public void setReservedOutputTokens(Long reservedOutputTokens) { this.reservedOutputTokens = reservedOutputTokens; }
    public Long getRequestCount() { return requestCount; }
    public void setRequestCount(Long requestCount) { this.requestCount = requestCount; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
