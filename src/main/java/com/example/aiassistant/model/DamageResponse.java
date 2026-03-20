package com.example.aiassistant.model;

public class DamageResponse {
    private String damageLocation;
    private String severity;
    private String description;
    private boolean damageDetected;
    private String tokensUsed;

    public DamageResponse(String damageLocation, String severity,
                          String description, boolean damageDetected, String tokensUsed) {
        this.damageLocation = damageLocation;
        this.severity = severity;
        this.description = description;
        this.damageDetected = damageDetected;
        this.tokensUsed = tokensUsed;
    }

    public String getDamageLocation() { return damageLocation; }
    public String getSeverity() { return severity; }
    public String getDescription() { return description; }
    public boolean isDamageDetected() { return damageDetected; }
    public String getTokensUsed() { return tokensUsed; }
}