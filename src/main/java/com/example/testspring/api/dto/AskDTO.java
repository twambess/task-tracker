package com.example.testspring.api.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AskDTO {

    Boolean answer;

    public static AskDTO makeDefault(Boolean answer){
        return builder()
                .answer(answer)
                .build();
    }
}
