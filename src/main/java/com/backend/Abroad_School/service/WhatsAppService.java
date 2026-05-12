package com.backend.Abroad_School.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class WhatsAppService {

    @Value("${whatsapp.phone-number-id}")
    private String phoneNumberId;

    @Value("${whatsapp.access-token}")
    private String accessToken;

    @Value("${whatsapp.api-version:v22.0}")
    private String apiVersion;

    private final RestTemplate restTemplate = new RestTemplate();

    // ── Result wrapper ────────────────────────────────────────────────────────
    public record WhatsAppResult(boolean success, String response, String errorCode) {
        public static WhatsAppResult ok(String r) {
            return new WhatsAppResult(true, r, null);
        }
        public static WhatsAppResult fail(String r, String code) {
            return new WhatsAppResult(false, r, code);
        }
    }

    private String apiUrl() {
        return "https://graph.facebook.com/" + apiVersion
                + "/" + phoneNumberId + "/messages";
    }

    // ── 1. Approved template: auto_pay_reminder_1 ─────────────────────────────
    // {{1}} = parentName, {{2}} = issueDate, {{3}} = childName
    public WhatsAppResult sendFeeReminderTemplate(String toNumber,
                                                   String parentName,
                                                   String issueDate,
                                                   String childName) {
        String params = buildParams(parentName, issueDate, childName);

        String body = """
                {
                  "messaging_product": "whatsapp",
                  "to": "%s",
                  "type": "template",
                  "template": {
                    "name": "auto_pay_reminder_1",
                    "language": { "code": "en_US" },
                    "components": [{
                      "type": "body",
                      "parameters": [%s]
                    }]
                  }
                }
                """.formatted(toNumber, params);

        return execute(body, toNumber, "TEMPLATE_auto_pay_reminder_1");
    }

    // ── 2. PDF voucher — upload media → send document ─────────────────────────
    public WhatsAppResult sendPdfVoucher(String toNumber, byte[] pdfBytes,
                                          String filename, String caption) {
        String mediaId = uploadMedia(pdfBytes, filename);
        if (mediaId == null) {
            return WhatsAppResult.fail("Media upload failed", "MEDIA_UPLOAD_FAILED");
        }

        String body = """
                {
                  "messaging_product": "whatsapp",
                  "to": "%s",
                  "type": "document",
                  "document": {
                    "id": "%s",
                    "filename": "%s",
                    "caption": "%s"
                  }
                }
                """.formatted(toNumber, mediaId,
                        escapeJson(filename), escapeJson(caption));

        return execute(body, toNumber, "PDF_DOCUMENT");
    }

    // ── Media upload ──────────────────────────────────────────────────────────
    private String uploadMedia(byte[] pdfBytes, String filename) {
        try {
            String uploadUrl = "https://graph.facebook.com/" + apiVersion
                    + "/" + phoneNumberId + "/media";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            ByteArrayResource fileResource = new ByteArrayResource(pdfBytes) {
                @Override
                public String getFilename() { return filename; }
            };

            HttpHeaders filePart = new HttpHeaders();
            filePart.setContentType(MediaType.APPLICATION_PDF);

            MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
            parts.add("file", new HttpEntity<>(fileResource, filePart));
            parts.add("messaging_product", "whatsapp");
            parts.add("type", "application/pdf");

            ResponseEntity<String> resp = restTemplate.postForEntity(
                    uploadUrl,
                    new HttpEntity<>(parts, headers),
                    String.class);

            String rb = resp.getBody();
            log.info("Media upload response: {}", rb);

            if (rb != null && rb.contains("\"id\"")) {
                return rb.replaceAll(".*\"id\"\\s*:\\s*\"([^\"]+)\".*", "$1");
            }
            log.error("No media ID in upload response: {}", rb);
            return null;

        } catch (Exception e) {
            log.error("Media upload failed for '{}': {}", filename, e.getMessage());
            return null;
        }
    }

    // ── Core executor ─────────────────────────────────────────────────────────
    private WhatsAppResult execute(String jsonBody, String toNumber, String type) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    apiUrl(),
                    new HttpEntity<>(jsonBody, headers),
                    String.class);

            log.info("[{}] OK → {}: {}", type, toNumber, response.getBody());
            return WhatsAppResult.ok(response.getBody());

        } catch (HttpClientErrorException e) {
            String rb = e.getResponseBodyAsString();
            String code = extractMetaErrorCode(rb);
            log.error("[{}] 4xx → {} ({}): {}", type, toNumber, code, rb);
            return WhatsAppResult.fail(rb, code);

        } catch (HttpServerErrorException e) {
            String rb = e.getResponseBodyAsString();
            log.error("[{}] 5xx → {}: {}", type, toNumber, rb);
            return WhatsAppResult.fail(rb, "META_SERVER_ERROR");

        } catch (ResourceAccessException e) {
            log.error("[{}] Timeout → {}: {}", type, toNumber, e.getMessage());
            return WhatsAppResult.fail(e.getMessage(), "NETWORK_TIMEOUT");

        } catch (Exception e) {
            log.error("[{}] Unknown → {}: {}", type, toNumber, e.getMessage());
            return WhatsAppResult.fail(e.getMessage(), "UNKNOWN_ERROR");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private String buildParams(String... values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(",");
            sb.append("{\"type\":\"text\",\"text\":\"")
              .append(escapeJson(values[i]))
              .append("\"}");
        }
        return sb.toString();
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String extractMetaErrorCode(String body) {
        if (body == null) return "UNKNOWN";
        try {
            if (body.contains("\"code\"")) {
                String code = body.replaceAll(".*\"code\"\\s*:\\s*(\\d+).*", "$1");
                return code.matches("\\d+") ? code : "UNKNOWN";
            }
        } catch (Exception ignored) {}
        return "UNKNOWN";
    }
}