package com.example.notificacoes.services;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.notificacoes.config.RabbitMqConfig;
import com.example.notificacoes.payload.TaskPayload;

@Service
public class NotificationListener {

    private static final long NOTIFICATION_TIME_BEFORE_END = 10; // minutos antes de enviar a notificação

    @Autowired
    private  RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMqConfig.TASK_NOTIFICATION_QUEUE)
    public void receiveTaskNotification(TaskPayload payload) {
        // Calcular o tempo de conclusão previsto
        LocalDateTime completionTime = payload.getStartDate().plus(payload.getDuration());

        // Calcular a diferença entre o tempo de conclusão e o tempo atual
        long minutesLeft = Duration.between(LocalDateTime.now(), completionTime).toMinutes();

        System.out.println("Tempo de conclusão previsto: " + completionTime + payload.getTaskId());
        System.out.println("Minutos restantes: " + minutesLeft + payload.getTaskId());

        // Enviar notificação se faltarem 10 minutos ou menos
        if (minutesLeft <= NOTIFICATION_TIME_BEFORE_END && minutesLeft >= 0) {
            sendPushNotification(payload.getUserId(),
                    "Sua tarefa está prestes a ser concluída. Faltam " + minutesLeft + " minutos.", payload.getTaskId());
        } else if (minutesLeft > NOTIFICATION_TIME_BEFORE_END) {
            // Reenviar a mensagem para a fila, aguardando para processar novamente depois
            System.out.println("Reenviando para a fila, ainda faltam " + minutesLeft + " minutos." + payload.getTaskId());
            requeueMessage(payload);
        }
    }

    private void requeueMessage(TaskPayload payload) {
        // Configurar o tempo de espera antes de reenviar (1 minuto, por exemplo)
        try {
            Thread.sleep(60000); // Aguarda 1 minuto antes de reenviar
            rabbitTemplate.convertAndSend(RabbitMqConfig.TASK_NOTIFICATION_QUEUE, payload);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Erro ao aguardar para reenviar mensagem: " + e.getMessage());
        }
    }

    private void sendPushNotification(Long userId, String message, Long taskId) {
        System.out.println("Enviando Push Notification para o usuário " + userId + ": " + message + taskId);
    }
}
