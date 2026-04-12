package com.example.backend.config;

import com.example.backend.controller.File2Controller;
import com.example.backend.controller.File3Controller;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import java.util.List;

/**
 * Springdoc가 multipart 파일 배열을 문자열 배열로 문서화하면 Swagger UI(AJV)에서
 * "Value must be a string" 검증 오류가 납니다. 업로드 API의 requestBody를
 * {@code items: { type: string, format: binary }} 형태로 덮어씁니다.
 */
@Configuration
public class MultipartUploadOpenApiCustomizer {

    @Bean
    public OperationCustomizer multipartUploadOperationCustomizer() {
        return (operation, handlerMethod) -> {
            Class<?> declaring = handlerMethod.getMethod().getDeclaringClass();
            String name = handlerMethod.getMethod().getName();
            if (!"upload".equals(name)) {
                return operation;
            }
            if (File2Controller.class.equals(declaring)) {
                return applyFile2UploadSchema(operation);
            }
            if (File3Controller.class.equals(declaring)) {
                return applyFile3UploadSchema(operation);
            }
            return operation;
        };
    }

    private static Operation applyFile2UploadSchema(Operation operation) {
        operation.setParameters(List.of());

        Schema<?> binary = new Schema<>().type("string").format("binary");
        Schema<?> filesArray = new Schema<>().type("array").items(binary);
        Schema<?> root = new Schema<>().type("object");
        root.addProperty("files", filesArray);
        root.setRequired(List.of("files"));

        Content content = new Content();
        MediaType mt = new MediaType();
        mt.setSchema(root);
        content.addMediaType(org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE, mt);

        RequestBody body = new RequestBody();
        body.setRequired(true);
        body.setContent(content);
        operation.setRequestBody(body);
        return operation;
    }

    private static Operation applyFile3UploadSchema(Operation operation) {
        operation.setParameters(List.of());

        Schema<?> binary = new Schema<>().type("string").format("binary");
        Schema<?> filesArray = new Schema<>().type("array").items(binary);

        Schema<?> root = new Schema<>().type("object");
        root.addProperty("files", filesArray);
        root.addProperty("file", binary);
        root.description("files(다건) 또는 file(단건) 중 하나 이상. 실제 검증은 서버에서 수행합니다.");

        Content content = new Content();
        MediaType mt = new MediaType();
        mt.setSchema(root);
        content.addMediaType(org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE, mt);

        RequestBody body = new RequestBody();
        body.setRequired(false);
        body.setContent(content);
        operation.setRequestBody(body);
        return operation;
    }
}
