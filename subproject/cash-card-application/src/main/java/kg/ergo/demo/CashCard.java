package kg.ergo.demo;

import org.springframework.data.annotation.Id;

public record CashCard(@Id Long id, Double amount) {
}