package com.example.notificacoes.payload;

import java.time.Duration;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class TaskPayload {

    private Long taskId;
    private Long userId;
    private LocalDateTime startDate;
    private Duration duration;
    private LocalDateTime finishDate;
}
