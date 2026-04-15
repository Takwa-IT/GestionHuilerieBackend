package dto;

import lombok.Data;

import java.util.List;

@Data
public class ApiResponseDTO<T> {
    private boolean success;
    private T data;
    private String message;
    private List<String> errors;

    public static <T> ApiResponseDTO<T> ok(T data, String message) {
        ApiResponseDTO<T> response = new ApiResponseDTO<>();
        response.setSuccess(true);
        response.setData(data);
        response.setMessage(message);
        return response;
    }

    public static <T> ApiResponseDTO<T> fail(String message, List<String> errors) {
        ApiResponseDTO<T> response = new ApiResponseDTO<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setErrors(errors);
        return response;
    }
}


