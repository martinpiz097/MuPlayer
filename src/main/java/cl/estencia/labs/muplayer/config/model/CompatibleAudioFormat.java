package cl.estencia.labs.muplayer.config.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CompatibleAudioFormat (
        @JsonProperty("format_name") String formatName,
        @JsonProperty("format_class") String formatClass) {
}